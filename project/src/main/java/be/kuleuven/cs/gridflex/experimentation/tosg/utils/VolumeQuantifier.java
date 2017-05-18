package be.kuleuven.cs.gridflex.experimentation.tosg.utils;

import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.MultiHorizonNormalErrorGenerator;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.ExperimentParams;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfGameParams;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfGameRunnerVariableDistributionCosts;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;

import java.util.Arrays;

import static java.lang.StrictMath.sqrt;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class VolumeQuantifier extends WgmfGameRunnerVariableDistributionCosts {
    private static final Logger logger = getLogger(
            VolumeQuantifier.class);
    private long baseSeed = 50;
    private int count;
    private Mean mean;
    private StandardDeviation std;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    public VolumeQuantifier(
            ExperimentParams expP) {
        super(expP);
        count = 0;
        mean = new Mean();
        std = new StandardDeviation();
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        String[] args2 = new String[4];
        Lists.newArrayList("-pIdx", "0", "-dIdx", "0").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);
        Lists.newArrayList("-pIdx", "0", "-dIdx", "2").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);
        Lists.newArrayList("-pIdx", "0", "-dIdx", "3").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);

        Lists.newArrayList("-pIdx", "2", "-dIdx", "0").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);
        Lists.newArrayList("-pIdx", "2", "-dIdx", "2").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);
        Lists.newArrayList("-pIdx", "2", "-dIdx", "3").toArray(args2);
        logger.warn("Computing relative error volumes for args: {}", Arrays.toString(args2));
        startExecution(args2,
                VolumeQuantifier::new);

    }

    @Override
    protected void execute(WgmfGameParams params) {
        for (int i = 0; i < 400; i++) {
            MultiHorizonNormalErrorGenerator multiHorizonNormalErrorGenerator = new MultiHorizonNormalErrorGenerator(
                    baseSeed + i, params.getWindSpeedErrorDistributions());

            PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                    params.getFactory(),
                    params.toSolverInputData(0));

            CongestionProfile diff = (CongestionProfile) portfolioBalanceSolver
                    .getCongestionVolumeToResolve();
            double total = params.getInputData().getCongestionProfile().sum();
            double perc = diff.sum() / total;
            this.mean.increment(perc);
            this.std.increment(perc);
            this.count++;

            //            System.out.println(diff.values());

        }
        double corr = 0;
        if (std.getResult() == 0) {
            corr += 0.00001;
            logger.warn("No variance in results... something might be wrong.");
        }
        double error =
                ConfidenceLevel._95pc.getConfideneCoeff() * (std.getResult() + corr) / sqrt(
                        (double) count);

        ConfidenceInterval ci = new ConfidenceInterval(mean.getResult() - error,
                mean.getResult() + error, ConfidenceLevel._95pc.getConfidenceLevel());
        System.out.println(ci);

    }

    @Override
    protected void processResults() {

    }
}
