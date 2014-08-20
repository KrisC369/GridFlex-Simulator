package be.kuleuven.cs.flexsim.domain.site;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * An implementation for the Site interface.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class SiteImpl implements Site {

    private final List<FlexProcess> processes;
    private Multimap<FlexProcess, FlexTuple> flex;

    /**
     * Default constructor based on lines.
     * 
     * @param lines
     *            The lines present in this site.
     */
    public SiteImpl(FlexProcess... lines) {
        processes = Lists.newArrayList(lines);
        this.flex = LinkedListMultimap.create();
    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        gatherFlex();
        return Lists.newArrayList(flex.values());
    }

    @Override
    public void activateFlex(ActivateFlexCommand schedule) {
        for (FlexProcess p : flex.keySet()) {
            for (FlexTuple t : flex.get(p)) {
                if (t.getId() == schedule.getReferenceID()) {
                    if (schedule.isDownFlexCommand()) {
                        p.executeDownFlexProfile(t.getId());
                    } else {
                        p.executeUpFlexProfile(t.getId());
                    }
                }
            }
        }
    }

    @Override
    public boolean containsLine(FlexProcess process) {
        return getProcesses().contains(process);
    }

    /**
     * @return the processes
     */
    final List<FlexProcess> getProcesses() {
        return processes;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {

    }

    @Override
    public void tick(int t) {
        gatherFlex();
    }

    private void gatherFlex() {
        this.flex = LinkedListMultimap.create();
        for (FlexProcess proc : processes) {
            flex.putAll(proc, Lists.newArrayList(proc.getCurrentFlexbility()));
        }
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> toret = Lists.newArrayList();
        toret.addAll(processes);
        return toret;
    }

    @Override
    public int getAggregatedTotalConsumptions() {
        int sum = 0;
        for (FlexProcess fp : processes) {
            sum += fp.getAggregatedTotalConsumptions();
        }
        return sum;
    }

    @Override
    public int getAggregatedLastStepConsumptions() {
        int sum = 0;
        for (FlexProcess fp : processes) {
            sum += fp.getAggregatedLastStepConsumptions();
        }
        return sum;
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        List<List<Integer>> occupancies = Lists.newArrayList();
        List<Integer> toret = Lists.newArrayList();
        for (FlexProcess fp : processes) {
            occupancies.add(fp.getBufferOccupancyLevels());
        }
        int max = 0;
        for (List<Integer> p : occupancies) {
            if (p.size() > max) {
                max = p.size();
            }
        }
        for (int i = 0; i < max; i++) {
            int sum = 0;
            int count = 0;
            for (List<Integer> p : occupancies) {
                if (i <= p.size() - 1) {
                    sum += p.get(i);
                    count++;
                }
            }
            toret.add(count == 0 ? sum : sum / count);
        }
        return toret;
    }

    @Override
    public Collection<Resource> takeResources() {
        List<Resource> toret = Lists.newArrayList();
        for (FlexProcess fp : processes) {
            toret.addAll(fp.takeResources());
        }
        return toret;
    }

    @Override
    public void deliverResources(List<Resource> res) {
        Deque<Resource> q = Lists.newLinkedList(res);
        for (int i = 0; i < res.size(); i++) {
            processes.get(i % processes.size()).deliverResources(
                    Lists.newArrayList(q.pop()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Site [hC=").append(hashCode()).append("]");
        return builder.toString();
    }
}
