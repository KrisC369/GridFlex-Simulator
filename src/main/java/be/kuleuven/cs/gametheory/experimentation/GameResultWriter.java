package be.kuleuven.cs.gametheory.experimentation;

import be.kuleuven.cs.gametheory.Game;

/**
 * Writes game results to this writer.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <N>
 *            Param N.
 * @param <K>
 *            Param K.
 *
 */
public class GameResultWriter<N, K> extends ResultWriter {
    private final Game<N, K> g;

    /**
     * Default constructor.
     * 
     * @param target
     *            the game target.
     */
    public GameResultWriter(Game<N, K> target) {
        super(target);
        this.g = target;
    }

    @Override
    public void write() {
        addResultComponent("Game dynamics results",
                g.getDynamicsParametersString());
        super.write();
    }
}
