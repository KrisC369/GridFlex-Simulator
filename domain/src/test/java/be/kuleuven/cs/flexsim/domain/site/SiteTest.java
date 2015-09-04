package be.kuleuven.cs.flexsim.domain.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SiteTest {

    Site s = new SiteImpl();
    private static final int SIMSTEPS = 10;

    @Before
    public void setUp() throws Exception {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
    }

    @Test
    public void testCreationAndAdd() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        assertTrue(s.containsLine(line1));
        assertTrue(s.containsLine(line2));

    }

    @Test
    public void testNoFlex() {
        s = new SiteImpl();
        assertTrue(s.getFlexTuples().isEmpty());
    }

    @Test
    public void testAggregatedFlex() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        List<FlexTuple> flex1 = line1.getCurrentFlexbility();
        List<FlexTuple> flex2 = line2.getCurrentFlexbility();
        List<FlexTuple> siteflex = s.getFlexTuples();
        assertTrue(siteflex.containsAll(flex1));
        assertTrue(siteflex.containsAll(flex2));
        assertEquals(siteflex.size(), flex1.size() + flex2.size(), 0);
    }

    @Test
    public void testDeterministicGatherFlex() {
        List<List<FlexTuple>> res = Lists.newArrayList();
        for (int i = 0; i < 750; i++) {
            ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                    .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                    .addShifted(7).build();
            ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                    .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                    .addShifted(4).build();
            ProductionLine line3 = new ProductionLine.ProductionLineBuilder()
                    .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                    .addShifted(7).build();
            ProductionLine line4 = new ProductionLine.ProductionLineBuilder()
                    .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                    .addShifted(4).build();
            s = new SiteImpl(line1, line2, line3, line4);
            List<FlexTuple> siteflex = s.getFlexTuples();
            res.add(siteflex);
        }

        assertEquals(1, Sets.newLinkedHashSet(res).size(), 0);
    }

    @Test
    public void testAggregatedInfo() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        double cons1 = line1.getLastStepConsumption();
        double cons2 = line2.getLastStepConsumption();
        double aggregate = s.getLastStepConsumption();
        assertEquals(aggregate, cons1 + cons2, 0);

        cons1 = line1.getTotalConsumption();
        cons2 = line2.getTotalConsumption();
        aggregate = s.getTotalConsumption();
        assertEquals(aggregate, cons1 + cons2, 0);

        List<Integer> flex1 = line1.getBufferOccupancyLevels();
        List<Integer> flex2 = line2.getBufferOccupancyLevels();
        List<Integer> siteflex = s.getBufferOccupancyLevels();
        assertTrue(siteflex.containsAll(flex1));
        assertTrue(siteflex.containsAll(flex2));
        assertEquals(siteflex.size(), Math.max(flex1.size(), flex2.size()), 0);
    }

    @Test
    public void testGatherAndDeliverResources() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        s.deliverResources(
                ResourceFactory.createBulkMPResource(50, 3, 3, 3, 3, 3, 3));
        assertTrue(line1.getBufferOccupancyLevels().get(0) > 0);
        assertTrue(line2.getBufferOccupancyLevels().get(0) > 0);
        Simulator sim = Simulator.createSimulator(1000);
        sim.register(s);
        sim.start();
        assertFalse(s.takeResources().isEmpty());
    }

    @Test
    public void testToString() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        String res = s.toString();
        assertTrue(res.contains(((Integer) s.hashCode()).toString()));
    }

    @Test
    public void testChangeConsumptionToLow() {
        s = genHighSite();
        Simulator sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();

        double before = s.getTotalConsumption();

        s = genHighSite();

        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        final FlexTuple t = s.getFlexTuples().get(1);
        s.activateFlex(new ActivateFlexCommand() {

            @Override
            public long getReferenceID() {
                return t.getId();
            }
        });
        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        double after = s.getTotalConsumption();
        assertTrue(before > after);
    }

    @Test
    public void testChangeConsumptionToHigh() {
        s = genLowSite();
        Simulator sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();

        double before = s.getTotalConsumption();

        s = genLowSite();

        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        final FlexTuple t = s.getFlexTuples().get(1);
        s.activateFlex(new ActivateFlexCommand() {
            @Override
            public long getReferenceID() {
                return t.getId();
            }
        });
        sim = Simulator.createSimulator(SIMSTEPS);
        sim.register(s);
        sim.start();
        double after = s.getTotalConsumption();
        assertTrue(before < after);
    }

    private Site genHighSite() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addRFSteerableStation(2, 20).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addRFSteerableStation(2, 20).build();
        for (DualModeWorkstation w : line1.getDualModeStations()) {
            w.signalHighConsumption();
        }
        for (DualModeWorkstation w : line2.getDualModeStations()) {
            w.signalHighConsumption();
        }
        line1.deliverResources(ResourceFactory.createBulkMPResource(1000, 5));
        line2.deliverResources(ResourceFactory.createBulkMPResource(1000, 5));
        return new SiteImpl(line1, line2);
    }

    private Site genLowSite() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addRFSteerableStation(2, 20).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addRFSteerableStation(2, 20).build();
        line1.deliverResources(ResourceFactory.createBulkMPResource(1000, 5));
        line2.deliverResources(ResourceFactory.createBulkMPResource(1000, 5));
        return new SiteImpl(line1, line2);
    }
}
