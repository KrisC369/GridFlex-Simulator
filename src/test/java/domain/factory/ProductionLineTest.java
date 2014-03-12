package domain.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;


import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulation.ISimulationComponent;
import simulation.ISimulationContext;
import simulation.Simulator;
import domain.IResource;
import domain.ResourceFactory;

public class ProductionLineTest {

    private ProductionLine lineSimple;
    private ProductionLine lineExtended;
    private int simSteps;
    private ISimulationContext sim;
    @Before
    public void setUp() throws Exception {
        lineSimple = ProductionLine.createSimpleLayout();
        lineExtended = ProductionLine.createExtendedLayout();
        simSteps = 20;
        sim =Simulator.createSimulator(simSteps);
        sim.register(lineSimple);
        sim.register(lineExtended);
    }

    @Test
    public void testInitialExtendedSetup() {
        assertEquals(4,lineExtended.getNumberOfWorkstations());
        assertEquals(0,lineExtended.takeResources().size());
    }
    @Test
    public void testInitialSimpleSetup() {
        assertEquals(1,lineSimple.getNumberOfWorkstations());
        assertEquals(0,lineSimple.takeResources().size());
    }
    
    @Test
    public void testDeliverAndProcessResources() {
        int n = 3;
        deliverResources(n);
        ISimulationComponent tester = mock(ISimulationComponent.class);
        sim.register(tester);
        ((Simulator)sim).start();
        verify(tester, times(simSteps)).tick();
        assertEquals(n,lineExtended.takeResources().size());
        
    }

    private void deliverResources(int n) {
        List<IResource> res = ResourceFactory.createBulkMPResource(n, 3,1);
        lineExtended.deliverResources(res);
    }
    
    

}
