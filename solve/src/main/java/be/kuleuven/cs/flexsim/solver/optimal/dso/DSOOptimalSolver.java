package be.kuleuven.cs.flexsim.solver.optimal.dso;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.ConstraintConversion;
import be.kuleuven.cs.flexsim.solver.optimal.QuarterHourlyFlexConstraints;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static net.sf.jmpi.main.expression.MpExpr.prod;

/**
 * Solver for using flexibility to avoid wind curtailment.
 * This class generates MIP problem formulation to solve for optimal allocation.
 * Where optimality is defined as the maximum recution of congestion.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolver extends AbstractOptimalSolver {
    private static final String CONG = "Cong:";
    private static final String SOLVED = "Solved";
    private static final String ALLOC = "alloc:";
    private static final String FLEX = "Flex";
    private static final Logger logger = LoggerFactory.getLogger(DSOOptimalSolver.class);
    private final CongestionProfile profile;
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
     * @param s The solver to use.
     */
    public DSOOptimalSolver(final FlexAllocProblemContext context, final Solver s) {
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
            logger.info(concreteResult.toString());
            final List<Boolean> t = Lists.newArrayList();
            final ListMultimap<FlexibilityProvider, Boolean> allocResults = ArrayListMultimap.create();

            for (final FlexibilityProvider p : getProviders()) {
                for (final String s : allocDvarID.get(p)) {
                    t.add(concreteResult.getBoolean(s));
                    allocResults.put(p, concreteResult.getBoolean(s));
                }
            }
            this.results = AllocResults
                    .create(allocResults, concreteResult.getObjective().doubleValue());
        } else {
            this.results = AllocResults.INFEASIBLE;
        }
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
                rhs.add(prod(prod(allocDvarID.get(p).get(i), p.getFlexibilityActivationRate().getUp()),
                        1 / STEPS_PER_HOUR));
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
        final MpDsoAdapter adapt = new MpDsoAdapter(adapted, allocDvarID.get(pv));
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
