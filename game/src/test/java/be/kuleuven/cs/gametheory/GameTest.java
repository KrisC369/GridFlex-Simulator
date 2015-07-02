package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteBuilder;

public class GameTest {
    private GameConfigurator config = mock(GameConfigurator.class);
    private Game<Site, Aggregator> g = mock(Game.class);
    private GameDirector director = mock(GameDirector.class);
    private List<Site> players = Lists.newArrayList();
    private int agents = 3;
    private int reps = 20;
    private Long value = 34L;

    @Before
    public void setUp() throws Exception {
        players = Lists.newArrayList();
        for (int i = 0; i < agents; i++) {
            players.add(SiteBuilder.newEquidistantSiteSimulation().withBaseConsumption(200).withMinConsumption(50)
                    .withMaxConsumption(400).withTuples(6).create());
        }
        config = new GameConfigurator<Site, Aggregator>() {
            private int i = 0;

            @Override
            public Site getAgent() {
                Site s = players.get(i);
                i = (i + 1) % agents;
                return s;
            }

            @Override
            public GameInstance<Site, Aggregator> generateInstance() {

                return new GameInstance<Site, Aggregator>() {
                    private int i = 0;
                    private Aggregator agg1 = mock(Aggregator.class);
                    private Aggregator agg2 = mock(Aggregator.class);

                    @Override
                    public void play() {
                    }

                    @Override
                    public void init() {
                    }

                    @Override
                    public Map<Site, Long> getPayOffs() {
                        Map<Site, Long> m = Maps.newLinkedHashMap();
                        for (int i = 0; i < agents; i++) {
                            m.put(players.get(i), value);
                        }
                        i = (i + 1) % agents;
                        return m;
                    }

                    @Override
                    public Map<Site, Aggregator> getAgentToActionMapping() {
                        Map<Site, Aggregator> m = Maps.newLinkedHashMap();
                        for (int j = 0; j < agents; j++) {
                            if (j % 2 == 0) {
                                m.put(players.get(j), agg1);
                            } else {
                                m.put(players.get(j), agg2);
                            }
                        }
                        return m;
                    }

                    @Override
                    public List<Aggregator> getActionSet() {
                        return Lists.newArrayList(agg1, agg2);
                    }

                    @Override
                    public void fixActionToAgent(Site agent, Aggregator action) {

                    }

                    @Override
                    public long getExternalityValue() {
                        return 0;
                    }
                };
            }

            @Override
            public int getActionSpaceSize() {
                return 2;
            }
        };
        g = new Game<>(agents, config, reps);
        this.director = new GameDirector(g);
    }

    @Test
    public void testGame() {
        director.playAutonomously();
        // System.out.println(g.getResultString());
        // System.out.println(g.getDynamicsParametersString());
        assertTrue(g.getDynamicsParametersString().contains("34.0"));
        assertTrue(g.getResultString().contains("[34.0, 34.0, 34.0]"));
        assertTrue(director.getResults().getDescription().get("Reps").contains(String.valueOf(20)));
        assertTrue(director.getResults().getResults().contains(Double.valueOf(34.0)));
    }
}
