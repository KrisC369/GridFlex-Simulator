package be.kuleuven.cs.flexsim.domain.energy.tso.contractual;

import java.util.List;
import java.util.Map;

import be.kuleuven.cs.flexsim.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.tso.MechanismHost;
import be.kuleuven.cs.flexsim.domain.energy.tso.simple.CopperplateTSO;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.domain.util.data.PowerCapabilityBand;

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
    public void afterTick(int t) {
        super.afterTick(t);
        pollCapacities();
        calculateAndSignal(t);
    }

    private void pollCapacities() {
        for (ContractualMechanismParticipant p : participants) {
            testValidParticipant(p);
            this.powerLimits.put(p, p.getPowerCapacity());
        }
    }

    private void calculateAndSignal(int timestep) {
        if (getCurrentImbalance() > 0) {
            int sum = CollectionUtils.sum(
                    Lists.newArrayList(powerLimits.values()),
                    new IntNNFunction<PowerCapabilityBand>() {
                        @Override
                        public int apply(PowerCapabilityBand input) {
                            return input.getUp();
                        }
                    });

            if (sum <= Math.abs(getCurrentImbalance())) {
                sendSignal(timestep, 1, true);
            } else {
                sendSignal(timestep, getFactor(sum, getCurrentImbalance()),
                        true);
            }
        } else if (getCurrentImbalance() < 0) {
            int sum = CollectionUtils.sum(
                    Lists.newArrayList(powerLimits.values()),
                    new IntNNFunction<PowerCapabilityBand>() {
                        @Override
                        public int apply(PowerCapabilityBand input) {
                            return input.getDown();
                        }
                    });
            if (sum <= Math.abs(getCurrentImbalance())) {
                sendSignal(timestep, 1, false);
            } else {
                sendSignal(timestep, getFactor(sum, getCurrentImbalance()),
                        false);
            }
        } else {
            sendSignal(timestep, 0, true);
        }
    }

    private void sendSignal(int t, double frac, boolean upflex) {
        for (java.util.Map.Entry<ContractualMechanismParticipant, PowerCapabilityBand> e : powerLimits
                .entrySet()) {
            int value = 0;
            if (upflex) {
                value = e.getValue().getUp();
            } else {
                value = e.getValue().getDown() * -1;
            }
            e.getKey().signalTarget(t, (int) Math.round(value * frac));
        }

    }

    private double getFactor(double sum, double currentImbalance) {
        if (sum == 0 || currentImbalance == 0) {
            return 0;
        }
        return Math.abs(currentImbalance) / sum;
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
