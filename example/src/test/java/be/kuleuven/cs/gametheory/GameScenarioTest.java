package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import be.kuleuven.cs.gametheory.standalone.Game;
import be.kuleuven.cs.gametheory.standalone.GameDirector;
import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.experimentation.DefaultGameConfigurator;

public class GameScenarioTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGameScenarioExample1() {
        DefaultGameConfigurator ex = new DefaultGameConfigurator(0);
        Game<Site, Aggregator> game = new Game<>(3, ex, 20);
        GameDirector g = new GameDirector(game);
        g.playAutonomously();
        new GameResultWriter(g, "CONSOLE").write();
        System.out.println(game.getResultString());
        assertTrue(game.getResultString()
                .contains("C:PayoffEntry{entries=[0, 3]}->20"));
        assertTrue(game.getResultString().contains(
                "V:PayoffEntry{entries=[0, 3]}->[628830.0, 633277.5, 581825.0]"));
        assertFalse(game.getDynamicsParametersString().isEmpty());
    }

}
