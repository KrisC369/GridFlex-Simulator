package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.OptiFlexCsvResultWriter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.stream.Collectors;

import static java.lang.StrictMath.sqrt;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfMultiJobGameRunnerVariableFlexParams
        extends WgmfGameRunnerVariableDistributionCosts {
    public static final String PARAM_KEY = "DISTRIBUTION_E_S_PRICE";
    protected static final int FLEX_BASE = 40 * 2;
    private static final Logger logger = getLogger(
            WgmfMultiJobGameRunnerVariableFlexParams.class);
    protected static final int BASE_SEED = 1234;
    private final List<OptiFlexCsvResultWriter.OptiFlexResults> writableResults;

    private final String resultFileName;
    private final int dataProfileIdx;
    private final int windErrorFileIdx;

    private final double iastart;
    private final double iastep;
    private final double iastop;

    private final HourlyFlexConstraints constraints;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    WgmfMultiJobGameRunnerVariableFlexParams(ExperimentParams expP) {
        super(expP);
        writableResults = Lists.newArrayList();
        resultFileName =
                RES_OUTPUT_FILE + String.valueOf(getnAgents()) + "R" + String.valueOf(getnReps())
                        + "_" + String.valueOf(System.currentTimeMillis() / 100) + RES_EXTENSION;
        this.windErrorFileIdx = expP.getWindErrorProfileIndex();
        this.dataProfileIdx = expP.getCurrentDataProfileIndex();
        this.iastart = expP.getP1Start();
        this.iastep = expP.getP1Step();
        this.iastop = expP.getP1End();
        this.constraints = expP.getActivationConstraints();
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args,
                WgmfMultiJobGameRunnerVariableFlexParams::new);
    }

    @Override
    protected void execute(WgmfGameParams params) {
        String[] splitted = DATAPROFILE_TEMPLATE.split("/");

        OptiFlexCsvResultWriter.writeCsvFile(resultFileName, Collections.emptyList(), false);
        final int agents = getnAgents();

        logger.info("Creating tasks objects.");
        //Create tasks
        List<GenericTask<OptaExperimentResults>> executables = Lists.newArrayList();
        ListMultimap<HourlyFlexConstraints, GenericTask<OptaExperimentResults>> experiments =
                LinkedListMultimap.create();
        configureExperiments(params, agents, executables, experiments);

        logger.info("Starting experiment execution");
        //Execution
        ListMultimap<HourlyFlexConstraints, BigDecimal> experimentResults = LinkedListMultimap
                .create();
        ListMultimap<HourlyFlexConstraints, Double> allocEffResults = LinkedListMultimap
                .create();
        runExperiments(params, executables, experimentResults, allocEffResults);

        logger.info("Parsing experiment results.");
        //Parse results
        parseResults(splitted[splitted.length - 1]
                        .replace("*", String.valueOf("[" + dataProfileIdx + "]")), experiments,
                experimentResults,
                allocEffResults);
    }

    protected void parseResults(String dataFile,
            ListMultimap<HourlyFlexConstraints, GenericTask<OptaExperimentResults>> experiments,
            ListMultimap<HourlyFlexConstraints, BigDecimal> experimentResults,
            ListMultimap<HourlyFlexConstraints, Double> allocEffResults) {
        for (HourlyFlexConstraints f : experiments.keySet()) {
            List<Double> resCongestionVals = experimentResults.get(f).stream()
                    .mapToDouble(BigDecimal::doubleValue).boxed()
                    .collect(Collectors.toList());
            List<Double> allocEffVals = allocEffResults.get(f).stream()
                    .collect(Collectors.toList());
            StatAccumulator resolvedCongestionAcc = new StatAccumulator();
            StatAccumulator accumulatedEfficiencyAcc = new StatAccumulator();
            resCongestionVals.forEach(resolvedCongestionAcc::accept);
            allocEffVals.forEach(accumulatedEfficiencyAcc::accept);
            ConfidenceInterval resolvedCongestionCI = resolvedCongestionAcc
                    .getCI(ConfidenceLevel._95pc);
            ConfidenceInterval allocEffCI = accumulatedEfficiencyAcc.getCI(ConfidenceLevel._95pc);

            OptiFlexCsvResultWriter.OptiFlexResults optiFlexResults = OptiFlexCsvResultWriter
                    .OptiFlexResults
                    .create(getnAgents(), getnReps(),
                            dataFile, f.getActivationDuration(), f.getInterActivationTime(),
                            (int) f.getMaximumActivations(),
                            CI_LEVEL.getConfidenceLevel(), resolvedCongestionCI, allocEffCI,
                            windErrorFileIdx);
            writableResults.add(optiFlexResults);
        }
        OptiFlexCsvResultWriter.writeCsvFile(resultFileName, writableResults, true);
    }

    protected void runExperiments(WgmfGameParams params,
            List<GenericTask<OptaExperimentResults>> executables,
            ListMultimap<HourlyFlexConstraints, BigDecimal> experimentResults,
            ListMultimap<HourlyFlexConstraints, Double> allocEffResults) {
        ExperimentRunner runner = getStrategy()
                .getRunner(params, this.PARAM_KEY, "OptiFlex job.");
        List<GenericTask<OptaExperimentResults>> adaptedExecutables = getStrategy()
                .adapt(executables, this.PARAMS_KEY);
        runner.runExperiments(adaptedExecutables);
        List<?> resultObjects = runner.waitAndGetResults();
        getStrategy()
                .processExecutionResultsLogErrorsOnly(resultObjects,
                        (obj) -> processResults(experimentResults, allocEffResults, obj));
    }

    protected void configureExperiments(WgmfGameParams params, int agents,
            List<GenericTask<OptaExperimentResults>> executables,
            ListMultimap<HourlyFlexConstraints, GenericTask<OptaExperimentResults>> experiments) {
        //Defaults:
        double ia = getConstraints().getInterActivationTime();
        double dur = getConstraints().getActivationDuration();
        double count = getConstraints().getMaximumActivations();
        //params:
        for (ia = getIastart(); ia < getIastop(); ia += getIastep()) {
            for (dur = 1; dur <= 10; dur += 1) {
                if (FLEX_BASE % dur == 0) {
                    HourlyFlexConstraints constraints = HourlyFlexConstraints
                            .builder()
                            .activationDuration(dur).interActivationTime(ia)
                            .maximumActivations(FLEX_BASE / dur).build();
                    long seed = BASE_SEED;
                    for (int rep = 0; rep < getnReps(); rep++) {
                        GenericTask<OptaExperimentResults> optaJppfTaskDSO = new OptaJppfTaskDSO(

                                params, seed + rep, agents, constraints);
                        executables.add(optaJppfTaskDSO);
                        experiments.put(constraints, optaJppfTaskDSO);
                    }
                }
            }
        }
    }

    private static void processResults(
            ListMultimap<HourlyFlexConstraints, BigDecimal> congestionRes,
            ListMultimap<HourlyFlexConstraints, Double> allocEffRes, Object obj) {
        congestionRes
                .put(((OptaExperimentResults) obj).getFlexConstraints(),
                        ((OptaExperimentResults) obj).getResolvedCongestionValue());
        allocEffRes
                .put(((OptaExperimentResults) obj).getFlexConstraints(),
                        ((OptaExperimentResults) obj).getAllocEfficiencyValue());
    }

    @Override
    protected void processResults() {
        OptiFlexCsvResultWriter.writeCsvFile(resultFileName + ".whole", writableResults, false);
    }

    class StatAccumulator implements DoubleConsumer {
        private final Mean mean = new Mean();
        private final Variance variance = new Variance();
        private int count = 0;

        ConfidenceInterval getCI(ConfidenceLevel level) {
            double meanResult = this.mean.getResult();
            double std = Math.sqrt(variance.getResult());
            //hack to allow creating CI's
            if (std == 0) {
                std += 0.00001;
            }
            double error = level.getConfideneCoeff() * std / sqrt((double) count);
            return new ConfidenceInterval(meanResult - error, meanResult + error,
                    level.getConfidenceLevel());
        }

        @Override
        public void accept(double value) {
            mean.increment(value);
            variance.increment(value);
            count++;
        }
    }

    public double getIastart() {
        return iastart;
    }

    public double getIastep() {
        return iastep;
    }

    public double getIastop() {
        return iastop;
    }

    public HourlyFlexConstraints getConstraints() {
        return constraints;
    }
}