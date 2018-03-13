package be.kuleuven.cs.gridflex.solvers.optimal.mip;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.solvers.common.data.AllocResults;
import be.kuleuven.cs.gridflex.solvers.common.data.QuarterHourlyFlexConstraints;
import be.kuleuven.cs.gridflex.solvers.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.gridflex.solvers.common.ConstraintConversion;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpOperator;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.expression.MpExpr;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.sf.jmpi.main.expression.MpExpr.prod;
import static org.apache.commons.math3.util.FastMath.min;

/**
 * Solver for using flexibility to avoid wind curtailment.
 * This class generates MIP problem formulation to solve for optimal allocation.
 * Where optimality is defined as the maximum recution of congestion.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MIPOptimalSolver extends AbstractOptimalSolver {
    private static final String CONG = "Cong:";
    private static final String SOLVED = "Solved";
    private static final String ALLOC = "alloc:";
    private static final String FLEX = "Flex";
    private static final Logger logger = LoggerFactory.getLogger(MIPOptimalSolver.class);
    private final TimeSeries profile;
    private final List<String> congID;
    private final List<String> solvedID;
    private final Map<FlexibilityProvider, String> flexID;
    private final ListMultimap<FlexibilityProvider, String> allocDvarID;
    @Nullable
    private AllocResults results;

    /**
     * Public constructor
     *
     * @param c The congestion profile to use.
     * @param s The solvers to use.
     */
    public MIPOptimalSolver(final FlexAllocProblemContext context, final Solver s) {
        super(context, s);
        profile = context.getEnergyProfileToMinimizeWithFlex();
        congID = Lists.newArrayList();
        solvedID = Lists.newArrayList();
        flexID = Maps.newLinkedHashMap();
        allocDvarID = ArrayListMultimap.create();
        for (int i = 0; i < profile.length(); i++) {
            congID.add(CONG + i);
        }
        for (int i = 0; i < profile.length(); i++) {
            solvedID.add(SOLVED + ":" + i);
        }
    }

    @Override
    protected void processResults(final Optional<MpResult> result) {
        if (result.isPresent()) {
            final MpResult concreteResult = result.get();
            logger.info("Processing MpResult: {}", concreteResult.toString());
            final ListMultimap<FlexibilityProvider, Boolean> allocResults = ArrayListMultimap
                    .create();

            for (final FlexibilityProvider p : getProviders()) {
                for (final String s : allocDvarID.get(p)) {
                    allocResults.put(p, concreteResult.getBoolean(s));
                }
            }
            double idealSum = getIdealActivationVolume();
            double activatedSum = getActualActivatedVolume(allocResults);
            double relativeObj = activatedSum / idealSum;
            this.results = AllocResults
                    .create(allocResults, activatedSum, relativeObj);
        } else {
            this.results = AllocResults.INFEASIBLE;
        }
    }

    private double getIdealActivationVolume() {
        return getProviders().stream().mapToDouble(
                p -> p.getFlexibilityActivationRate().getUp() * ConstraintConversion
                        .fromHourlyToQuarterHourly(p.getFlexibilityActivationConstraints())
                        .getActivationDuration() * ConstraintConversion.fromHourlyToQuarterHourly(p
                        .getFlexibilityActivationConstraints()).getMaximumActivations()).sum();
    }

    private double getActualActivatedVolume(
            ListMultimap<FlexibilityProvider, Boolean> allocResults) {
        return IntStream.range(0, profile.length())
                .filter(i -> activated(allocResults, i))
                .mapToDouble(i -> min(profile.value(i), getSum(allocResults, i))).sum();
    }

    private double getSum(ListMultimap<FlexibilityProvider, Boolean> acts, int idx) {
        return acts.keySet().stream().mapToDouble(
                p -> p.getFlexibilityActivationRate().getUp() * (acts.get(p).get(idx) ? 1 : 0))
                .sum();
    }

    private boolean activated(ListMultimap<FlexibilityProvider, Boolean> acts, int idx) {
        int prod = 1;
        for (FlexibilityProvider p : acts.keySet()) {

            prod *= (1 - (acts.get(p).get(idx) ? 1 : 0));
        }
        return prod == 1 ? false : true;
    }

    @Override
    public MpProblem getProblem() {
        final MpProblem prob = new MpProblem();
        addDataToProblem(prob);

        for (final FlexibilityProvider f : getProviders()) {
            addDVarsForAllocationToProb(prob, f);
        }

        addSolvedClauseToProb(prob);
        addGoalToProb(prob);

        for (final FlexibilityProvider f : getProviders()) {
            addConstraintsForFlexToProb(prob, f);
        }
        return prob;
    }

    private void addSolvedClauseToProb(final MpProblem prob) {
        //solvedID
        for (final String s : solvedID) {
            prob.addVar(s, Double.class);
        }
        //solvedIDConstraints1
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(solvedID.get(i));
            final MpExpr rhs = new MpExpr().add(profile.value(i));
            prob.addConstraint(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
        //solvedIDConstraints2 = isMin0andActive
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(solvedID.get(i));
            final MpExpr rhs = new MpExpr();
            for (final FlexibilityProvider p : getProviders()) {
                //removed scaling from hour to quarter hourly because handled by decorator of
                // providers at init.
                rhs.add(prod(allocDvarID.get(p).get(i), p.getFlexibilityActivationRate().getUp()));
            }
            prob.addConstraint(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
    }

    private void addDataToProblem(final MpProblem prob) {
        //cong
        for (final String s : congID) {
            prob.addVar(s, Double.class);
        }
        //congConstraints
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(congID.get(i));
            final MpExpr rhs = new MpExpr().add(profile.value(i));
            prob.addConstraint(new MpConstraint(lhs, MpOperator.EQ, rhs));
        }
        //flexRateConstraints
        for (final FlexibilityProvider f : getProviders()) {
            final String flexVar = getFlexID(f);
            flexID.put(f, flexVar);
            prob.addVar(flexVar, Double.class);
            final MpExpr lhs = new MpExpr().add(flexVar);
            final MpExpr rhs = new MpExpr().add(f.getFlexibilityActivationRate().getUp());
            prob.addConstraint(new MpConstraint(lhs, MpOperator.EQ, rhs));
        }
    }

    private void addGoalToProb(final MpProblem tempProb) {
        final MpExpr goalExpr = new MpExpr();
        for (int i = 0; i < profile.length(); i++) {
            goalExpr.add(solvedID.get(i));
        }
        tempProb.setObjective(goalExpr, MpDirection.MAX);
    }

    private void addDVarsForAllocationToProb(final MpProblem p, final FlexibilityProvider pv) {
        for (int c = 0; c < profile.length(); c++) {
            final String alloc = ALLOC + pv.hashCode() + ":" + c;
            allocDvarID.put(pv, alloc);
            p.addVar(alloc, Boolean.class);
        }
    }

    private String getFlexID(final FlexibilityProvider pv) {
        return FLEX + ":" + pv.hashCode();
    }

    private void addConstraintsForFlexToProb(final MpProblem p, final FlexibilityProvider pv) {
        //flexConstraints
        final QuarterHourlyFlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                pv.getFlexibilityActivationConstraints());
        final MPFlexProviderAdapter adapt = new MPFlexProviderAdapter(adapted, allocDvarID.get(pv));
        adapt.getConstraints().forEach(p::addConstraint);

    }

    /**
     * @return The results.
     */
    @Override
    public AllocResults getSolution() {
        checkNotNull(results, "Results has not been set yet.");
        return this.results;
    }

}
