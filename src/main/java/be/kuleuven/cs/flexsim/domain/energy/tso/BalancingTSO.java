package be.kuleuven.cs.flexsim.domain.energy.tso;

import java.util.List;
import java.util.Map;

import be.kuleuven.cs.flexsim.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A TSO implementation that can accept bids for balancing actions and clears
 * the bids, optimally selecting the best choices.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class BalancingTSO extends CopperplateTSO implements
        MechanismHost<ContractualMechanismParticipant> {
    private List<ContractualMechanismParticipant> participants;
    private Map<ContractualMechanismParticipant, PowerCapabilityBand> powerLimits;

    /**
     * Constructor with consumption instances as parameter.
     * 
     * @param sites
     *            The consumption sites connected to this TSO
     */
    public BalancingTSO(EnergyConsumptionTrackable... sites) {
        this(new EnergyProductionTrackable[0], sites);
    }

    /**
     * Constructor with production instances as parameter.
     * 
     * @param sites
     *            The production sites connected to this TSO
     */
    public BalancingTSO(EnergyProductionTrackable... sites) {
        this(sites, new EnergyConsumptionTrackable[0]);
    }

    /**
     * Constructor with no initial partakers.
     * 
     */
    public BalancingTSO() {
        this(new EnergyProductionTrackable[0],
                new EnergyConsumptionTrackable[0]);
    }

    /**
     * Actual initializing constructor.
     * 
     * @param prod
     *            the producers.
     * @param cons
     *            the consumers.
     */
    private BalancingTSO(EnergyProductionTrackable[] prod,
            EnergyConsumptionTrackable[] cons) {
        super(prod, cons);
        this.participants = Lists.newArrayList();
        this.powerLimits = Maps.newLinkedHashMap();
    }

    /**
     * @return the participants
     */
    public List<ContractualMechanismParticipant> getParticipants() {
        return participants;
    }

    @Override
    public void tick(int t) {
        super.tick(t);
    }

    @Override
    public void afterTick(int t) {
        super.afterTick(t);
    }

    @Override
    public void registerParticipant(ContractualMechanismParticipant participant) {
        this.participants.add(participant);
        this.powerLimits.put(participant, PowerCapabilityBand.createZero());

    }

    /**
     * Returns the contractual limits registered to a participant.
     * 
     * @param agg
     *            The client to check.
     * @return The limits.
     */
    public PowerCapabilityBand getContractualLimit(
            ContractualMechanismParticipant agg) {
        testValidParticipant(agg);
        return this.powerLimits.get(agg);
    }

    private void testValidParticipant(ContractualMechanismParticipant agg) {
        if (!hasParticipant(agg)) {
            throw new IllegalStateException(
                    "Should have this aggregator registered before calling this method.");
        }
    }

    private boolean hasParticipant(ContractualMechanismParticipant agg) {
        return this.participants.contains(agg);
    }

    /**
     * Signal that this participant has a new margin of power capabilities.
     * 
     * @param agg
     *            The client
     * @param cap
     *            The new capabilities.
     */
    public void signalNewLimits(ContractualMechanismParticipant agg,
            PowerCapabilityBand cap) {
        testValidParticipant(agg);
        this.powerLimits.put(agg, cap);
    }
}
