package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gametheory.standalone.Game;
import be.kuleuven.cs.gametheory.standalone.GameDirector;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameDirectorTest {
    private Game<Site, Aggregator> g = mock(Game.class);
    private GameDirector<Site, BRPAggregator> director = mock(GameDirector.class);
    private GameInstance<Site, Aggregator> inst = mock(GameInstance.class);

    @Before
    public void setUp() throws Exception {
        GameInstance<Site, Aggregator> inst2 = mock(GameInstance.class);
        List<GameInstance<Site, Aggregator>> list = Lists.newArrayList(inst,
                inst2);
        when(g.getGameInstances()).thenReturn(list);
        director = new GameDirector(g);
    }

    @Test
    public void testNotifyHasBeenPlayed() {

        for (Playable p : director.getPlayableVersions()) {
            director.notifyVersionHasBeenPlayed(p);
        }
        verify(g, times(1)).gatherResults(null);
        verify(g, times(1)).logResults();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotifyFail() {
        GameInstance<Site, Aggregator> inst3 = mock(GameInstance.class);
        director.notifyVersionHasBeenPlayed(inst3);
    }

}
