package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.io.ResultWriter;

/**
 * Writes game results to this writer.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class GameResultWriter extends ResultWriter {
    private final GameDirector g;

    /**
     * Default constructor.
     *
     * @param target
     *            the game target.
     */
    public GameResultWriter(final GameDirector target) {
        super(target.getFormattedResults());
        this.g = target;
    }

    /**
     * Constructor for filenames.
     *
     * @param target
     *            the game target.
     * @param loggerTag
     *            The result writer tag.
     */
    public GameResultWriter(final GameDirector target, final String loggerTag) {
        super(target.getFormattedResults(), loggerTag);
        this.g = target;
    }

    @Override
    public void write() {
        addResultComponent("Game dynamics results",
                g.getDynamicEquationArguments());
        super.write();
    }
}
