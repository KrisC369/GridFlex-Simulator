package be.kuleuven.cs.gametheory;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * A representation of a full game specification for all configurations of
 * agents over the action space.
 *
 * @param <T> The type instances for games returned.
 * @param <I> The type of results expected to be handled.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AbstractGame<T, I> {
    private static final String CONSOLE = "CONSOLE";
    private static final String CONFIGURING = "Configuring instance: ";
    private static final String EXECUTING = "Executing instance: ";
    private final int agents;
    private final int actions;
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final int reps;
    private final Logger logger;
    private final List<T> instanceList;

    /**
     * Default constructor.
     *
     * @param agents The number of agents.
     * @param config The configurator instance.
     * @param reps   The number of repetitions.
     */
    public AbstractGame(final int agents, final int actions, final int reps) {
        this.agents = agents;
        this.actions = actions;
        this.payoffs = new HeuristicSymmetricPayoffMatrix(this.agents, this.actions);
        this.reps = reps;
        this.logger = LoggerFactory.getLogger(CONSOLE);
        this.instanceList = Lists.newArrayList();
    }

    /**
     * Configure and set-up the game specifics for different iterations of games
     * over the strategy space.
     */
    protected abstract void configureInstances();

    /**
     * Run experiments.
     */
    protected abstract void runExperiments();

    public List<T> getGameInstances() {
        return Collections.unmodifiableList(this.instanceList);
    }

    protected List<T> getInternalInstanceList() {
        return this.instanceList;
    }

    public abstract void gatherResults(List<I> results);

    protected final void addPayoffEntry(final Double[] values, final int[] entry) {
        this.payoffs.addEntry(values, entry);
    }

    protected void printConfigProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, CONFIGURING);

    }

    protected void printExecutionProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, EXECUTING);
    }

    protected void printProgress(final int progressCounter, final long l, final String message) {
        final StringBuilder b = new StringBuilder();
        b.append(message).append(progressCounter).append("/").append(l);
        logger.info(b.toString());
    }

    protected void logResults() {
        final StringBuilder b = new StringBuilder(30);
        b.append(getResultString()).append("\n")
                .append("Dynamics equation params:");
        for (final Double d : payoffs.getDynamicEquationFactors()) {
            b.append(d).append("\n");
        }
        logger.info(b.toString());
    }

    protected String getResultString() {
        return payoffs.toString();
    }

    /**
     * Returns the parameters of the dynamics in a formatted string.
     *
     * @return the params in a MATLAB style formatted string.
     */
    protected String getDynamicsParametersString() {
        final StringBuilder b = new StringBuilder(30);
        char character = 'a';
        b.append("\n");
        for (final Double d : payoffs.getDynamicEquationFactors()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     * for this game.
     */
    protected GameResult getResults() {
        final GameResult result = GameResult
                .create(payoffs.getDynamicEquationFactors())
                .withDescription("Reps", String.valueOf(reps))
                .withDescription("agents", String.valueOf(agents))
                .withDescription("actions", String.valueOf(actions));
        return result;
    }
}
