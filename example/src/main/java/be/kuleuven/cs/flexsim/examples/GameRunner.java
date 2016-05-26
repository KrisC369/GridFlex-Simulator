package be.kuleuven.cs.flexsim.examples;

import be.kuleuven.cs.flexsim.experimentation.DefaultGameConfigurator;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.GameDirector;
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
    public static void main(String[] args) {
        DefaultGameConfigurator ex = new DefaultGameConfigurator(1);
        GameDirector g = new GameDirector(new Game<>(AGENTS, ex, REPITITIONS));
        g.playAutonomously();
        new GameResultWriter(g).write();
    }
}
