package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.OptiFlexCsvResultWriter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static final int FLEX_BASE = 40 * 2;
    private static final Logger logger = getLogger(
            WgmfMultiJobGameRunnerVariableFlexParams.class);
    private final LinkedListMultimap<ConfigurableGameDirector, WgmfJppfTask> directorToTasks;
    private final List<OptiFlexCsvResultWriter.OptiFlexResults> writableResults;

    private final String resultFileName;
    private final int dataProfileIdx;
    private final int windErrorFileIdx;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    private WgmfMultiJobGameRunnerVariableFlexParams(ExperimentParams expP) {
        super(expP);
        directorToTasks = LinkedListMultimap.create();
        writableResults = Lists.newArrayList();
        resultFileName =
                RES_OUTPUT_FILE + String.valueOf(getnAgents()) + "R" + String.valueOf(getnReps())
                        + "_" + String.valueOf(System.currentTimeMillis() / 100) + RES_EXTENSION;
        this.windErrorFileIdx = expP.getWindErrorProfileIndex();
        this.dataProfileIdx = expP.getCurrentDataProfileIndex();
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

        //Create tasks
        List<OptaJppfTask> executables = Lists.newArrayList();
        ListMultimap<HourlyFlexConstraints, OptaJppfTask> experiments = LinkedListMultimap.create();
        for (int ia = 0; ia < 12; ia++) {
            for (int dur = 1; dur <= 8; dur *= 2) {
                HourlyFlexConstraints constraints = HourlyFlexConstraints.builder()
                        .activationDuration(dur).interActivationTime(ia)
                        .maximumActivations(FLEX_BASE / dur).build();
                    long seed = 1234;
                    WgmfAgentGenerator gen = new WgmfAgentGenerator(seed, constraints);
                for (int rep = 0; rep < getnReps(); rep++) {
                    OptaJppfTask optaJppfTask = new OptaJppfTask(params, seed, agents,
                            constraints);
                    executables.add(optaJppfTask);
                    experiments.put(constraints, optaJppfTask);
                }
            }
        }

        executables.get(0).run();
        //Execution
        ListMultimap<HourlyFlexConstraints, BigDecimal> experimentResults = LinkedListMultimap
                .create();
        ExperimentRunner runner = getStrategy()
                .getRunner(params, PARAMS_KEY, "OptiFlex job.");
        runner.runExperiments(executables);
        List<Object> resultObjects = runner.waitAndGetResults();
        getStrategy()
                .processExecutionResultsLogErrorsOnly(resultObjects,
                        (obj) -> experimentResults
                                .put(((OptaExperimentResults) obj).getFlexConstraints(),
                                        ((OptaExperimentResults) obj).getResultValue()));

        //Parse results
        Map<HourlyFlexConstraints, ConfidenceInterval> results = Maps.newLinkedHashMap();
        for (HourlyFlexConstraints f : experiments.keySet()) {
            List<Double> dres = experimentResults.get(f).stream()
                    .mapToDouble(exp -> exp.doubleValue()).boxed()
                    .collect(Collectors.toList());
            StatAccumulator sa = new StatAccumulator();
            dres.forEach((d) -> sa.accept(d));
            ConfidenceInterval ci = sa.getCI(ConfidenceLevel._95pc);
            results.put(f, ci);

            OptiFlexCsvResultWriter.OptiFlexResults optiFlexResults = OptiFlexCsvResultWriter
                    .OptiFlexResults
                    .create(getnAgents(), getnReps(),
                            splitted[splitted.length - 1]
                                    .replace("*", String.valueOf("[" + dataProfileIdx + "]")),
                            f.getActivationDuration(), f.getInterActivationTime(),
                            (int) f.getMaximumActivations(),
                            CI_LEVEL.getConfidenceLevel(), ci, windErrorFileIdx);
            writableResults.add(optiFlexResults);
        }
        OptiFlexCsvResultWriter.writeCsvFile(resultFileName, writableResults, true);
    }

    @Override
    protected void processResults() {
        OptiFlexCsvResultWriter.writeCsvFile(resultFileName + ".whole", writableResults, false);
    }

    class StatAccumulator implements DoubleConsumer {
        private final Mean mean = new Mean();
        private final Variance variance = new Variance();
        private int count = 0;

        private int getCount() {
            return count;
        }

        private Variance getVariance() {
            return variance;
        }

        private Mean getMean() {
            return mean;
        }

        public ConfidenceInterval getCI(ConfidenceLevel level) {
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
}