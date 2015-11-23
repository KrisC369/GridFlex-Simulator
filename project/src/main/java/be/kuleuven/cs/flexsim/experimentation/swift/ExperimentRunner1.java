/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunner1 extends ExperimentRunnerAllRes {

    private static final int N = 1000;
    private static final int ALLOWED_EXCESS = 33;
    private static final boolean ALLOW_LESS_ACTIVATIONS = true;

    private final int n;

    protected ExperimentRunner1(int n, int nagents, int allowed) {
        super(n, nagents, allowed);
        this.n = n;
    }

    /**
     * @param args
     *            Standard in args.
     */
    public static void main(String[] args) {
        ExpGenerator gen = new ExpGenerator() {

            @Override
            public ExecutableExperiment getExperiment(int reps, int agents,
                    int allowed) {
                return new ExperimentRunner1(reps, agents, allowed);
            }
        };
        parseInput(gen, args, N, ALLOWED_EXCESS);
    }

    protected static void startExperiment(int reps, int agents, int allowed) {
        new ExperimentRunner1(reps, agents, allowed).execute();
    }

    @Override
    protected void printResult() {
        System.out.println("BEGINRESULT:");
        System.out.println("Res1=" + getMainRes1());
        System.out.println("Res2=" + getMainRes2());
        System.out.println("Not meeting 40 acts: "
                + String.valueOf(n - getMainRes1().size()));
        System.out.println("Not meeting 40 acts: "
                + String.valueOf(n - getMainRes2().size()));
        System.out.println("ENDRESULT:");
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        @Nullable
        private final double[] real;
        @Nullable
        private ExperimentInstance p;
        @Nullable
        private final CongestionProfile profile;

        ExperimentAtomImplementation(double[] realisation,
                CongestionProfile profile) {
            this.real = Arrays.copyOf(realisation, realisation.length);
            this.profile = profile;
            this.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    addMainResult(getLabel(), checkNotNull(p).getEfficiency());
                    p = null;
                }
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = (new ExperimentInstance(getSolverBuilder(),
                    checkNotNull(real), checkNotNull(profile),
                    ALLOW_LESS_ACTIVATIONS));
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }
}
