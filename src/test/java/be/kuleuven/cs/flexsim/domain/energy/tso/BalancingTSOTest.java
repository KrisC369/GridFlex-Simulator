package be.kuleuven.cs.flexsim.domain.energy.tso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class BalancingTSOTest {

    private BalancingTSO tso = mock(BalancingTSO.class);
    private Simulator sim = Simulator.createSimulator(1);
    private double imbalance = 1000d;
    private int steps = 1;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        EnergyProductionTrackable prod = mock(EnergyProductionTrackable.class);
        when(prod.getLastStepProduction()).thenReturn(imbalance);
        tso = new BalancingTSO(prod);
        sim = Simulator.createSimulator(steps);
        sim.register(tso);
    }

    @Test
    public void testImbalanceValueInit() {
        sim.start();
        assertEquals(imbalance, tso.getCurrentImbalance(), 0);
    }

    // @Test
    // public void testPlaceBids() {
    // FlexBid bid = new FlexBid(FlexTuple.create(1, 500, false, 0, 0, 0), 125);
    // tso.placeBid(bid);
    // assertTrue(tso.getCurrentOutstandingBids().contains(bid));
    // }

    @Test
    public void testRegisterClients() {
        ContractualMechanismParticipant agg = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg);
        assertTrue(tso.getParticipants().contains(agg));
        assertTrue(tso.getContractualLimit(agg).isZero());
    }

    @Test
    public void testWrongRegisterClients() {
        ContractualMechanismParticipant agg = mock(ContractualMechanismParticipant.class);
        exception.expect(IllegalStateException.class);
        assertFalse(tso.getParticipants().contains(agg));
        assertTrue(tso.getContractualLimit(agg).isZero());
    }

    @Test
    public void testSignalPowerFlex() {
        ContractualMechanismParticipant agg = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg);
        PowerCapabilityBand cap = PowerCapabilityBand.create(50, 100);
        tso.signalNewLimits(agg, cap);
        assertEquals(cap, tso.getContractualLimit(agg));
    }

    @Test
    public void testActivation1() {
        int capS1 = 40;
        int capS2 = 400;
        ContractualMechanismParticipant agg1 = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg1);
        ContractualMechanismParticipant agg2 = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg2);
        PowerCapabilityBand cap1 = PowerCapabilityBand.create(capS1, capS1 * 2);
        PowerCapabilityBand cap2 = PowerCapabilityBand.create(capS2, capS2 * 2);
        tso.signalNewLimits(agg1, cap1);
        tso.signalNewLimits(agg2, cap2);
        sim.start();
        verify(agg1, times(1)).signalTarget(capS1 * 2);
        verify(agg2, times(1)).signalTarget(capS2 * 2);
    }

    @Test
    public void testActivation2() {
        int capS1 = 500;
        int capS2 = 5000;
        double factor = 0.0909090909;
        ContractualMechanismParticipant agg1 = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg1);
        ContractualMechanismParticipant agg2 = mock(ContractualMechanismParticipant.class);
        tso.registerParticipant(agg2);
        PowerCapabilityBand cap1 = PowerCapabilityBand.create(capS1, capS1 * 2);
        PowerCapabilityBand cap2 = PowerCapabilityBand.create(capS2, capS2 * 2);
        tso.signalNewLimits(agg1, cap1);
        tso.signalNewLimits(agg2, cap2);
        sim.start();
        verify(agg1, times(1)).signalTarget(
                (int) Math.round(capS1 * 2 * factor));
        verify(agg2, times(1)).signalTarget(
                (int) Math.round(capS2 * 2 * factor));
    }
}
