package be.kuleuven.cs.gametheory.configurable;

import be.kuleuven.cs.gametheory.util.Tuple;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ConfigurableGameDirectorTest {

    private ConfigurableGameDirector director;
    private ConfigurableGameDirector director2;
    private static final int SECOND_AGENT_SIZE = 3;

    @Before
    public void setUp() {
        ConfigurableGame g = new ConfigurableGame(2, 2, 1);
        this.director = new ConfigurableGameDirector(g);
        ConfigurableGame g2 = new ConfigurableGame(SECOND_AGENT_SIZE, 2, 1);
        this.director2 = new ConfigurableGameDirector(g2);
    }

    @Test
    public void testSizes() {
        List<GameInstanceConfiguration> playableVersions = director2.getPlayableVersions();
        for (GameInstanceConfiguration config : playableVersions) {
            assertEquals(SECOND_AGENT_SIZE, config.getAgentSize(), 0);
            assertEquals(2, config.getActionSize(), 0);
        }
    }

    @Test
    public void testConfigs() {
        List<GameInstanceConfiguration> playableVersions = director.getPlayableVersions();
        assertEquals(3, playableVersions.size(), 0);
        List<Tuple<Integer, Integer>> expected =
                Lists.newArrayList(Tuple.create(2, 0), Tuple.create(1, 1), Tuple.create(0, 2));
        for (GameInstanceConfiguration config : playableVersions) {
            assertEquals(2, config.getAgentSize(), 0);
            assertEquals(2, config.getActionSize(), 0);
        }

        for (GameInstanceConfiguration config : playableVersions) {
            int countA1 = 0;
            int countA2 = 0;
            if (config.getAgentActionMap().get(0) == 0) {
                countA1++;
            } else {
                countA2++;
            }
            if (config.getAgentActionMap().get(1) == 0) {
                countA1++;
            } else {
                countA2++;
            }
            expected.remove(Tuple.create(countA1, countA2));
        }
        assertEquals(0, expected.size(), 0);
    }

    @Test
    public void notifyVersionHasBeenPlayed() throws Exception {
        List<GameInstanceConfiguration> playableVersions = director.getPlayableVersions();
        List<GameInstanceResult> results = Lists.newArrayList();
        Map<Integer, Double> po = Maps.newLinkedHashMap();
        po.put(0, 12345d);
        po.put(1, 12345d);
        playableVersions
                .forEach(p -> results.add(GameInstanceResult.create(p, Maps.newLinkedHashMap(po))));
        results.forEach(r -> director.notifyVersionHasBeenPlayed(r));

        String resultStr = director.getFormattedResults().getFormattedResultString();
        System.out.println(resultStr);
        assertTrue(resultStr.contains("V:PayoffEntry{entries=[2, 0]}->[12345.0, 12345.0]"));
        assertTrue(resultStr.contains("V:PayoffEntry{entries=[1, 1]}->[12345.0, 12345.0]"));
        assertTrue(resultStr.contains("V:PayoffEntry{entries=[0, 2]}->[12345.0, 12345.0]"));
        assertTrue(resultStr.contains("C:PayoffEntry{entries=[2, 0]}->1"));
        assertTrue(resultStr.contains("C:PayoffEntry{entries=[1, 1]}->1"));
        assertTrue(resultStr.contains("C:PayoffEntry{entries=[0, 2]}->1"));
    }

}