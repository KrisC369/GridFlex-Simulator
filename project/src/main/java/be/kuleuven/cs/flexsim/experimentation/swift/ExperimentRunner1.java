package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import it.unimi.dsi.fastutil.doubles.DoubleList;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunner1 extends ExperimentRunnerAllRes {

    private static final int N = 1000;
    private static final int ALLOWED_EXCESS = 33;

    private final int n;

    protected ExperimentRunner1(final int n, final int nagents, final int allowed) {
        super(n, nagents, allowed);
        this.n = n;
    }

    /**
     * @param args
     *            Standard in args.
     */
    public static void main(final String[] args) {
        final ExpGenerator gen = (reps, agents,
                allowed) -> new ExperimentRunner1(reps, agents, allowed);
        parseInput(gen, args, N, ALLOWED_EXCESS);
    }

    protected static void startExperiment(final int reps, final int agents, final int allowed) {
        new ExperimentRunner1(reps, agents, allowed).execute();
    }

    @Override
    protected void logResults() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BEGINRESULT:\n").append("Res1=").append(getMainRes1())
                .append("\n");
        builder.append("Res2=").append(getMainRes2()).append("\n");
        builder.append("Not meeting 40 acts: ")
                .append(String.valueOf(n - getMainRes1().size())).append("\n");
        builder.append("Not meeting 40 acts: ")
                .append(String.valueOf(n - getMainRes2().size())).append("\n");
        builder.append("ENDRESULT:\n");
        LoggerFactory.getLogger("CONSOLERESULT").info(builder.toString());
    }

    class ExperimentAtomImplementationSingleEff
            extends ExperimentAtomImplementation {
        @Nullable
        private ExperimentInstance p;

        ExperimentAtomImplementationSingleEff(final DoubleList realisation,
                final CongestionProfile profile) {
            super(realisation, profile);

        }

        @Override
        protected void doRegistration() {
            this.registerCallbackOnFinish(instance -> {
                addMainResult(getLabel(), checkNotNull(p).getEfficiency());
                p = null;
            });
        }
    }
}
