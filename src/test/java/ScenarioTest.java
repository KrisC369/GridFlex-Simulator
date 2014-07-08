import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class ScenarioTest {

    private Simulator s;
    private ProductionLine p;
    private FinanceTracker ft;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(5000);
        p = new ProductionLine.ProductionLineBuilder().addShifted(14)
                .addShifted(14).addShifted(4)
                .addMultiCapExponentialConsuming(4, 125).addShifted(14)
                .addShifted(14).addShifted(10).build();
        ft = FinanceTracker.createDefault(p);
        s.register(p);
        s.register(ft);
    }

    @Test
    public void testSimulationRun() {
        p.deliverResources(ResourceFactory.createBulkMPResource(500000, 0, 2,
                2, 2, 2000));
        s.start();
        assertNotEquals(ft.getTotalReward(), 0);
        assertNotEquals(ft.getTotalProfit(), 0);

    }

    @Test
    public void testDoubleConfig() {
        Set<SimulationComponent> set = new HashSet<SimulationComponent>();
        set.addAll(s.getSimulationComponents());
        assertEquals(s.getSimulationComponents().size(), set.size(), 0);
    }
}
