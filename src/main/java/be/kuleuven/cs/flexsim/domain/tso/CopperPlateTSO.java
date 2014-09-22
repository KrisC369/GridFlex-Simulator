package be.kuleuven.cs.flexsim.domain.tso;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.site.Site;
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
public class CopperPlateTSO implements SimulationComponent, SteeringSignal {

    private List<Site> prosumers;
    private SteeringSignal signal;
    private final int initialImbalance;
    private int currentImbalance;
    private Listener<? super Integer> newBalanceValueListener;
    private int currentSignalOfset;

    /**
     * Default constructor.
     * 
     * @param signal
     *            The steeringSignal to use as offset.
     * @param sites
     *            The sites connected to this TSO
     */
    public CopperPlateTSO(SteeringSignal signal, Site... sites) {
        this(0, signal, sites);
    }

    /**
     * Constructor including initial balance.
     * 
     * @param initialbal
     *            the initial balance ofset representing other prosumers'
     *            balance
     * @param signal
     *            The steeringSignal to use as offset.
     * @param sites
     *            The sites connected to this TSO
     */
    public CopperPlateTSO(int initialbal, SteeringSignal signal, Site... sites) {
        this.prosumers = Lists.newArrayList(sites);
        this.signal = signal;
        this.initialImbalance = initialbal;
        this.currentImbalance = 0;
        this.newBalanceValueListener = NoopListener.INSTANCE;
        this.currentSignalOfset = 0;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
        int bal = 0;
        for (Site s : prosumers) {
            bal += s.getAggregatedLastStepConsumptions();
        }
        currentImbalance = initialImbalance - bal;
        newBalanceValueListener.eventOccurred(currentSignalOfset
                + this.currentImbalance);
    }

    @Override
    public void tick(int t) {
        this.currentSignalOfset = this.signal.getCurrentValue(t);
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Lists.newArrayList(prosumers);
    }

    @Override
    public int getCurrentValue(int timeMark) {
        return currentSignalOfset + this.currentImbalance;
    }

    /**
     * Add a new listener for new steervalue requests to this tso.
     * 
     * @param listener
     *            The listener to add.
     */
    public void addNewBalanceValueListener(Listener<? super Integer> listener) {
        this.newBalanceValueListener = MultiplexListener.plus(
                this.newBalanceValueListener, listener);
    }
}
