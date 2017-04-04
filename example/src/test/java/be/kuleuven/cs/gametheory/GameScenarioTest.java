package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.experimentation.DefaultGameConfigurator;
import be.kuleuven.cs.gametheory.standalone.Game;
import be.kuleuven.cs.gametheory.standalone.GameDirector;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
                "V:PayoffEntry{entries=[0, 3]}->[628830.0, 633277.5,"));
        assertFalse(game.getDynamicsParametersString().isEmpty());
    }

}
