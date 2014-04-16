package be.kuleuven.cs.flexsim.domain.finances;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.base.Optional;

/**
 * Tracks and finalizes the finances of productionlines.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class FinanceTracker implements SimulationComponent {

    private final InOutTrackableSimulationComponent target;
    private Optional<SimulationContext> context;

    public FinanceTracker(InOutTrackableSimulationComponent target) {
        this.target = target;
        this.context = Optional.absent();
    }

    @Override
    public void initialize(SimulationContext context) {
        this.context = Optional.of(context);
    }

    /**
     * This method refines the following documentation by generating a report
     * event when there is simulation context present for this line instance.
     * {@inheritDoc}
     */
    @Override
    public void afterTick() {
        report();
    }

    private void report() {
        notifyReport(getTarget().getAggregatedLastStepConsumptions(),
                getTarget().getAggregatedTotalConsumptions(), getTarget()
                        .getBufferOccupancyLevels());
    }

    private void notifyReport(int totalLaststep, int totalTotal,
            List<Integer> buffSizes) {
        if (this.context.isPresent()) {
            Event e = getContext().getEventFactory().build("report");
            e.setAttribute("pLinehash", this.hashCode());
            e.setAttribute("time", getContext().getSimulationClock()
                    .getTimeCount());
            e.setAttribute("totalLaststepE", totalLaststep);
            e.setAttribute("totalTotalE", totalTotal);
            int idx = 0;
            for (long i : buffSizes) {
                e.setAttribute("buffer_" + idx++, i);
            }
            getContext().getEventbus().post(e);
        }
    }

    @Override
    public void tick() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> toret = new ArrayList<SimulationComponent>();
        toret.add(getTarget());
        return toret;
    }

    /**
     * @return the target
     */
    private final InOutTrackableSimulationComponent getTarget() {
        return target;
    }

    /**
     * @return the context
     */
    private final SimulationContext getContext() {
        return context.get();
    }

}
