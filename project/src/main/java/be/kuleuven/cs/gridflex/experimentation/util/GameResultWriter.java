package be.kuleuven.cs.gridflex.experimentation.util;

import be.kuleuven.cs.gametheory.AbstractGameDirector;
import be.kuleuven.cs.gridflex.io.ResultWriter;

/**
 * Writes game results to this writer.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class GameResultWriter extends ResultWriter {
    private final AbstractGameDirector g;

    /**
     * Default constructor.
     *
     * @param target the game target.
     */
    public GameResultWriter(final AbstractGameDirector target) {
        super(target::getFormattedResults);
        this.g = target;
    }

    /**
     * Constructor for filenames.
     *
     * @param target    the game target.
     * @param loggerTag The result writer tag.
     */
    public GameResultWriter(final AbstractGameDirector target, final String loggerTag) {
        super(target::getFormattedResults, loggerTag);
        this.g = target;
    }

    @Override
    public void write() {
        addResultComponent("Game dynamics results",
                g.getDynamicEquationArguments());
        super.write();
    }
}
