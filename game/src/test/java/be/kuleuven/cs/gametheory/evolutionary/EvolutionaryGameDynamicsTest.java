package be.kuleuven.cs.gametheory.evolutionary;

import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrix;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EvolutionaryGameDynamicsTest {

    private HeuristicSymmetricPayoffMatrix table;
    private int agents = 3;
    private int actions = 2;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.table = new HeuristicSymmetricPayoffMatrix(agents, actions);
    }

    @Test
    public void testGetDynamicsArgs() {
        double reward = 39;
        Double[] value = new Double[] { reward, reward, reward };
        for (int i = 0; i <= agents; i++) {
            reward -= 5;
            value = new Double[] { reward, reward, reward };
            table.addEntry(value, agents - i, i);
        }
        List<Double> result = EvolutionaryGameDynamics.from(table).getDynamicEquationFactors();

        // Test values:
        assertEquals(34, result.get(0), 0);
        assertEquals(29, result.get(1), 0);
        assertEquals(29, result.get(2), 0);
        assertEquals(24, result.get(3), 0);
        assertEquals(24, result.get(4), 0);
        assertEquals(19, result.get(5), 0);
    }

    @Test
    public void testGetDynamicsArgs3S() {
        agents = 2;
        actions = 3;
        this.table = new HeuristicSymmetricPayoffMatrix(agents, actions);
        double reward = 39;
        Double[] value = new Double[] { reward, reward };
        for (int i = 0; i <= agents; i++) {
            reward -= 5;
            value = new Double[] { reward, reward };
            table.addEntry(value, agents - i, i, 0);
        }
        reward -= 5;
        value = new Double[] { reward, reward };
        table.addEntry(value, 1, 0, 1);
        reward -= 5;
        value = new Double[] { reward, reward };
        table.addEntry(value, 0, 1, 1);
        reward -= 5;
        value = new Double[] { reward, reward };
        table.addEntry(value, 0, 0, 2);

        List<Double> result = EvolutionaryGameDynamics.from(table).getDynamicEquationFactors();

        assertEquals(9, result.size(), 0);
        // Test values:
        assertEquals(34, result.get(0), 0);
        assertEquals(29, result.get(1), 0);
        assertEquals(29, result.get(2), 0);
        assertEquals(24, result.get(3), 0);
        assertEquals(19, result.get(5), 0);
        assertEquals(14, result.get(6), 0);
        assertEquals(14, result.get(7), 0);
        assertEquals(9, result.get(8), 0);
    }

    @Test
    public void testGetDynamicsArgs2by2() {
        this.table = new HeuristicSymmetricPayoffMatrix(2, 2);
        table.addEntry(new Double[] { 5d, 15d }, 2, 0);
        table.addEntry(new Double[] { 20d, 25d }, 1, 1);
        table.addEntry(new Double[] { 20d, 40d }, 0, 2);

        List<Double> result = EvolutionaryGameDynamics.from(table).getDynamicEquationFactors();
        List<Double> vars = EvolutionaryGameDynamics.from(table).getDynamicEquationStds();
        System.out.println(vars);
        // Test values:
        assertEquals(10, result.get(0), 0);
        assertEquals(20, result.get(1), 0);
        assertEquals(25, result.get(2), 0);
        assertEquals(30, result.get(3), 0);
        assertEquals(0, vars.get(0), 0);
        assertEquals(0, vars.get(1), 0);
        assertEquals(0, vars.get(2), 0);
        assertEquals(0, vars.get(3), 0);
    }

}