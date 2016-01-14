package be.kuleuven.cs.flexsim.domain.energy.tso.simple;

import java.util.List;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.domain.util.listener.MultiplexListener;
import be.kuleuven.cs.flexsim.domain.util.listener.NoopListener;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * A TSO that serves as a copper plate connection to all sites present.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CopperplateTSO implements SimulationComponent, BalancingSignal {

    private final List<EnergyConsumptionTrackable> consumers;
    private final List<EnergyProductionTrackable> producers;
    private int currentImbalance;
    private Listener<? super Integer> newBalanceValueListener;

    /**
     * Constructor with consumption instances as parameter.
     * 
     * @param sites
     *            The consumption sites connected to this TSO
     */
    public CopperplateTSO(EnergyConsumptionTrackable... sites) {
        this(new EnergyProductionTrackable[0], sites);
    }

    /**
     * Constructor with production instances as parameter.
     * 
     * @param sites
     *            The production sites connected to this TSO
     */
    public CopperplateTSO(EnergyProductionTrackable... sites) {
        this(sites, new EnergyConsumptionTrackable[0]);
    }

    /**
     * Constructor with no initial partakers.
     */
    public CopperplateTSO() {
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
    protected CopperplateTSO(EnergyProductionTrackable[] prod,
            EnergyConsumptionTrackable[] cons) {
        this.consumers = Lists.newArrayList(cons);
        this.producers = Lists.newArrayList(prod);
        this.currentImbalance = 0;
        this.newBalanceValueListener = NoopListener.INSTANCE;
    }

    /**
     * Add a producer to this tso.
     * 
     * @param producer
     *            the producer to add.
     */
    public void registerProducer(EnergyProductionTrackable producer) {
        this.producers.add(producer);
    }

    /**
     * Add a consumer to this tso.
     * 
     * @param consumer
     *            the consumer to add.
     */
    public void registerConsumer(EnergyConsumptionTrackable consumer) {
        this.consumers.add(consumer);
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
        int cons = 0;
        for (EnergyConsumptionTrackable s : consumers) {
            cons += s.getLastStepConsumption();
        }
        int prod = 0;
        for (EnergyProductionTrackable s : producers) {
            prod += s.getLastStepProduction();
        }
        currentImbalance = prod - cons;
        newBalanceValueListener.eventOccurred(this.currentImbalance);
    }

    @Override
    public void tick(int t) {
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> toret = Lists.newArrayList();
        for (SimulationComponent c : consumers) {
            toret.add(c);
        }
        for (SimulationComponent c : producers) {
            toret.add(c);
        }
        return toret;
    }

    @Override
    public int getCurrentImbalance() {
        return this.currentImbalance;
    }

    @Override
    public void addNewBalanceValueListener(Listener<? super Integer> listener) {
        this.newBalanceValueListener = MultiplexListener
                .plus(this.newBalanceValueListener, listener);
    }
}
