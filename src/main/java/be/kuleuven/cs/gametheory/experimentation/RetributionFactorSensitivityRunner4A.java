package be.kuleuven.cs.gametheory.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.gametheory.Game;

/**
 * An example class running some experiments.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner4A {

    private static final int SEED = 3722;
    private MersenneTwister twister;
    private final int nAgents = 5;
    private final int repititions = 15;
    private static final int THREADS = 8;
    private volatile int threadCount;

    private RetributionFactorSensitivityRunner4A() {
        this.twister = new MersenneTwister(SEED);
        this.threadCount = 0;
    }

    /**
     * Run the experiments.
     */
    public void runExperiments() {
        for (double d = 1; d < 500; d += 25) {
            while (threadCount >= THREADS) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final double dd = d;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    doExperimentRun(dd);
                    decreaseThreadCount();
                }
            }).start();
            threadCount++;

        }
    }

    private synchronized void decreaseThreadCount() {
        this.threadCount--;
    }

    /**
     * @param retributionFactor
     */
    private void doExperimentRun(double retributionFactor) {
        GameConfiguratorEx ex = new GameConfiguratorEx(retributionFactor,
                twister);
        Game<Site, Aggregator> g = new Game<>(nAgents, ex, repititions);
        g.runExperiment();
        ResultWriter rw = new GameResultWriter<>(g);
        rw.addResultComponent("RetributionFactor",
                String.valueOf(retributionFactor));
        rw.addResultComponent("NumberOfAgents", String.valueOf(nAgents));
        rw.addResultComponent("Reps", String.valueOf(repititions));
        rw.write();
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner4A().runExperiments();
    }
}
