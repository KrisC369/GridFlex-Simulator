package be.kuleuven.cs.flexsim.domain.energy.tso;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.domain.util.listener.MultiplexListener;
import be.kuleuven.cs.flexsim.domain.util.listener.NoopListener;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.Lists;

/**
 * A TSO that serves as a copper plate connection to all sites present.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class CopperplateTSO implements SimulationComponent, BalancingSignal {

    private final List<EnergyConsumptionTrackable> consumers;
    private final List<EnergyProductionTrackable> producers;
    private int currentImbalance;
    private Listener<? super Integer> newBalanceValueListener;

    /**
     * Default constructor.
     * 
     * @param signal
     *            The steeringSignal to use as offset.
     * @param sites
     *            The sites connected to this TSO
     */
    public CopperplateTSO(BalancingSignal signal,
            EnergyConsumptionTrackable... sites) {
        this(0, signal, sites);
    }

    /**
     * Default constructor.
     * 
     * @param sites
     *            The sites connected to this TSO
     */
    public CopperplateTSO(EnergyConsumptionTrackable... sites) {
        this(0, new RandomTSO(0, 1, new MersenneTwister()), sites);
    }

    /**
     * Constructor including initial balance.
     * 
     * @param initialbal
     *            the initial balance offset representing other prosumers'
     *            balance
     * @param signal
     *            The steeringSignal to use as offset.
     * @param sites
     *            The sites connected to this TSO
     */
    public CopperplateTSO(int initialbal, BalancingSignal signal,
            EnergyConsumptionTrackable... sites) {
        this.consumers = Lists.newArrayList(sites);
        this.producers = Lists.newArrayList();
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
        return Lists.newArrayList(consumers);
    }

    @Override
    public int getCurrentImbalance() {
        return this.currentImbalance;
    }

    /**
     * Add a new listener for new steer value requests to this tso.
     * 
     * @param listener
     *            The listener to add.
     */
    @Override
    public void addNewBalanceValueListener(Listener<? super Integer> listener) {
        this.newBalanceValueListener = MultiplexListener.plus(
                this.newBalanceValueListener, listener);
    }
}
