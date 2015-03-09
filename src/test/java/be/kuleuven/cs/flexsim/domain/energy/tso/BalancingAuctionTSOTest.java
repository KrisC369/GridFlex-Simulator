package be.kuleuven.cs.flexsim.domain.energy.tso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.tso.auctioning.BalancingAuctionTSO;
import be.kuleuven.cs.flexsim.domain.util.FlexBid;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class BalancingAuctionTSOTest {

    private BalancingAuctionTSO tso = mock(BalancingAuctionTSO.class);
    private Simulator sim = Simulator.createSimulator(1);
    private double imbalance = 1000d;
    private int steps = 4;

    @Before
    public void setUp() throws Exception {
        EnergyProductionTrackable prod = mock(EnergyProductionTrackable.class);
        when(prod.getLastStepProduction()).thenReturn(imbalance);
        tso = new BalancingAuctionTSO(prod);
        sim = Simulator.createSimulator(steps);
        sim.register(tso);
    }

    @Test
    public void testImbalanceValueInit() {
        sim.start();
        assertEquals(imbalance, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testPlaceBids() {
        FlexBid bid = new FlexBid(FlexTuple.create(1, 500, false, 0, 0, 0), 125);
        tso.placeBid(bid);
        assertTrue(tso.getCurrentOutstandingBids().contains(bid));
    }

    @Test
    public void testRegisterClients() {
        MechanismParticipant agg = mock(MechanismParticipant.class);
        tso.registerParticipant(agg);
        assertTrue(tso.getParticipants().contains(agg));
    }
}
