package be.kuleuven.cs.flexsim.domain.site;

import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * An implementation for the Site interface.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
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
                    if (schedule.isCurtailmentCommand()) {
                        p.executeCurtailmentProfile(t.getId());
                    } else {
                        p.executeCancelCurtailmentProfile(t.getId());
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
        this.flex = ArrayListMultimap.create();
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
            sum += fp.getAggregatedTotalConsumptions();
        }
        return sum;
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Resource> takeResources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deliverResources(List<Resource> res) {
        // TODO Auto-generated method stub

    }

}
