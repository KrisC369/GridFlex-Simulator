package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.gametheory.experimentation.GameConfiguratorEx;
import be.kuleuven.cs.gametheory.experimentation.GameResultWriter;

public class GameScenarioTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGameScenarioExample1() {
        GameConfiguratorEx ex = new GameConfiguratorEx(0);
        Game<Site, Aggregator> game = new Game<>(3, ex, 20);
        GameDirector g = new GameDirector(game);
        g.playAutonomously();
        new GameResultWriter(g, "CONSOLE").write();
        assertTrue(game.getResultString().contains("C:PayoffEntry [0, 3]->20"));
        assertTrue(game.getResultString().contains(
                "V:PayoffEntry [0, 3]->[729632.5, 763297.5, 678570.0]"));

    }
}
