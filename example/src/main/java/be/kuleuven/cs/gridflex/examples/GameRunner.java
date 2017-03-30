package be.kuleuven.cs.gridflex.examples;

import be.kuleuven.cs.gridflex.experimentation.DefaultGameConfigurator;
import be.kuleuven.cs.gametheory.standalone.Game;
import be.kuleuven.cs.gametheory.standalone.GameDirector;
import be.kuleuven.cs.gametheory.GameResultWriter;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class GameRunner {
    private static final int AGENTS = 3;
    private static final int REPITITIONS = 20;

    private GameRunner() {
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(final String[] args) {
        final DefaultGameConfigurator ex = new DefaultGameConfigurator(1);
        final GameDirector g = new GameDirector(new Game<>(AGENTS, ex, REPITITIONS));
        g.playAutonomously();
        new GameResultWriter(g).write();
    }
}
