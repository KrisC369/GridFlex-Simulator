package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.domain.util.listener.MultiplexListener;
import be.kuleuven.cs.flexsim.domain.util.listener.NoopListener;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * An implementation for the Site interface.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SiteImpl implements Site {

    private final List<FlexProcess> processes;
    private Multimap<FlexProcess, FlexTuple> flex;
    private Listener<? super FlexTuple> activationListener;

    /**
     * Default constructor based on lines.
     *
     * @param lines
     *            The lines present in this site.
     */
    public SiteImpl(final FlexProcess... lines) {
        this.processes = Lists.newArrayList(lines);
        this.flex = LinkedListMultimap.create();
        this.activationListener = NoopListener.INSTANCE;
    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        gatherFlex();
        return Lists.newArrayList(flex.values());
    }

    @Override
    public void activateFlex(final ActivateFlexCommand schedule) {
        for (final FlexProcess p : flex.keySet()) {
            for (final FlexTuple t : flex.get(p)) {
                if (t.getId() == schedule.getReferenceID()) {
                    if (!t.getDirection().booleanRepresentation()) {
                        p.executeDownFlexProfile(t.getId());
                    } else {
                        p.executeUpFlexProfile(t.getId());
                    }
                    this.activationListener.eventOccurred(t);
                }
            }
        }
    }

    @Override
    public boolean containsLine(final FlexProcess process) {
        return getProcesses().contains(process);
    }

    /**
     * @return the processes
     */
    final List<FlexProcess> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    @Override
    public void initialize(final SimulationContext context) {
    }

    @Override
    public void afterTick(final int t) {

    }

    @Override
    public void tick(final int t) {
        gatherFlex();
    }

    private void gatherFlex() {
        this.flex = LinkedListMultimap.create();
        for (final FlexProcess proc : processes) {
            flex.putAll(proc, Lists.newArrayList(proc.getCurrentFlexbility()));
        }
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        final List<SimulationComponent> toret = Lists.newArrayList();
        toret.addAll(processes);
        return toret;
    }

    @Override
    public double getTotalConsumption() {
        double sum = 0;
        for (final FlexProcess fp : processes) {
            sum += fp.getTotalConsumption();
        }
        return sum;
    }

    @Override
    public double getLastStepConsumption() {
        double sum = 0;
        for (final FlexProcess fp : processes) {
            sum += fp.getLastStepConsumption();
        }
        return sum;
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        final List<List<Integer>> occupancies = Lists.newArrayList();
        final List<Integer> toret = Lists.newArrayList();
        for (final FlexProcess fp : processes) {
            occupancies.add(fp.getBufferOccupancyLevels());
        }
        int max = 0;
        for (final List<Integer> p : occupancies) {
            if (p.size() > max) {
                max = p.size();
            }
        }
        for (int i = 0; i < max; i++) {
            int sum = 0;
            int count = 0;
            for (final List<Integer> p : occupancies) {
                if (i <= p.size() - 1) {
                    sum += p.get(i);
                    count++;
                }
            }
            toret.add(count == 0 ? sum : (sum / count));
        }
        return toret;
    }

    @Override
    public Collection<Resource> takeResources() {
        final List<Resource> toret = Lists.newArrayList();
        for (final FlexProcess fp : processes) {
            toret.addAll(fp.takeResources());
        }
        return toret;
    }

    @Override
    public void deliverResources(final List<Resource> res) {
        final Deque<Resource> q = Lists.newLinkedList(res);
        for (int i = 0; i < res.size(); i++) {
            processes.get(i % processes.size())
                    .deliverResources(Lists.newArrayList(q.pop()));
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Site [hC=").append(hashCode()).append("]");
        return builder.toString();
    }

    @Override
    public double getAverageConsumption() {
        return getLastStepConsumption();
    }

    @Override
    public void addActivationListener(final Listener<? super FlexTuple> listener) {
        this.activationListener = MultiplexListener
                .plus(this.activationListener, listener);
    }
}
