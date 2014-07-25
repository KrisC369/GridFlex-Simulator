package be.kuleuven.cs.flexsim.domain.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Implements the process device interface.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
class ProcessDeviceImpl {

    private final Graph<Buffer<Resource>, Workstation> layout;
    private volatile long idcount;

    /**
     * Default constructor
     * 
     * @param subject
     *            the subject PLine.
     */
    public ProcessDeviceImpl(ProductionLine subject) {
        this.layout = subject.getLayout();
    }

    private synchronized long newId() {
        return idcount++;
    }

    public List<FlexTuple> getCurrentFlexbility(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations) {
        // downflex only
        if (curtailableWorkstations.isEmpty()
                && tradeofSteerableWorkstations.isEmpty()) {
            return Lists.newArrayList(FlexTuple.createNONE());
        }
        List<FlexTuple> flex = Lists.newArrayList();
        for (CurtailableWorkstation c : getEffectivelyCurtailableStations(curtailableWorkstations)) {
            flex.add(calculateFirstOrderCurtFlex(c));
        }
        flex.addAll(calculateOrder2CurtFlex(getEffectivelyCurtailableStations(curtailableWorkstations)));
        flex.addAll(calculateOrder3CurtFlex(getEffectivelyCurtailableStations(curtailableWorkstations)));
        for (TradeofSteerableWorkstation c : tradeofSteerableWorkstations) {
            flex.add(calculateSteerFlex(c));
        }
        flex = filterOutDuplicates(flex);
        flex = someOrNone(flex);
        return flex;
    }

    private List<CurtailableWorkstation> getEffectivelyCurtailableStations(
            List<CurtailableWorkstation> curtailableWorkstations) {
        List<CurtailableWorkstation> toret = Lists.newArrayList();
        for (CurtailableWorkstation w : curtailableWorkstations) {
            if (!w.isCurtailed()) {
                toret.add(w);
            }
        }
        return toret;
    }

    private List<FlexTuple> someOrNone(List<FlexTuple> flex) {
        List<FlexTuple> fr = Lists.newArrayList();
        for (FlexTuple f : flex) {
            if (!f.equals(FlexTuple.NONE)) {
                fr.add(f);
            }
        }
        if (!fr.isEmpty())
            return fr;
        return Lists.newArrayList(FlexTuple.NONE);
    }

    private List<FlexTuple> calculateOrder2CurtFlex(
            List<CurtailableWorkstation> curtailableStations) {
        List<FlexTuple> flex = Lists.newArrayList();
        int size = curtailableStations.size();
        for (int i = 0; i < size - 2; i++) {
            for (int j = i + 1; j < size - 1; j++) {
                flex.add(calculateFirstOrderCurtFlex(
                        curtailableStations.get(i), curtailableStations.get(j)));
            }
        }
        if (!flex.isEmpty()) {
            return flex;
        }
        return Lists.newArrayList(FlexTuple.NONE);
    }

    private List<FlexTuple> calculateOrder3CurtFlex(
            List<CurtailableWorkstation> curtailableStations) {
        List<FlexTuple> flex = Lists.newArrayList();
        int size = curtailableStations.size();
        for (int i = 0; i < size - 2; i++) {
            for (int j = i + 1; j < size - 1; j++) {
                for (int k = j + 1; k < size - 1; k++) {
                    flex.add(calculateFirstOrderCurtFlex(
                            curtailableStations.get(i),
                            curtailableStations.get(j),
                            curtailableStations.get(k)));
                }
            }
        }
        if (!flex.isEmpty()) {
            return flex;
        }
        return Lists.newArrayList(FlexTuple.NONE);
    }

    private boolean presentInSamePhase(CurtailableWorkstation a,
            CurtailableWorkstation... b) {
        if (b.length == 0) {
            return true;
        }
        if (b.length == 1) {
            return (layout.getEdgeSource(a).equals(layout.getEdgeSource(b[0])) && layout
                    .getEdgeTarget(a).equals(layout.getEdgeTarget(b[0])));
        }
        for (CurtailableWorkstation cb : b) {
            if (layout.getEdgeSource(a).equals(layout.getEdgeSource(cb))
                    && layout.getEdgeTarget(a).equals(layout.getEdgeTarget(cb))) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<FlexTuple> filterOutDuplicates(List<FlexTuple> flex) {
        return Lists.newArrayList(com.google.common.collect.Sets
                .newHashSet(flex));
    }

    private FlexTuple calculateSteerFlex(TradeofSteerableWorkstation c) {
        // TODO implement
        return FlexTuple.createNONE();
    }

    private FlexTuple calculateFirstOrderCurtFlex(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        // TODO implement
        if (presentInSamePhase(a, cs)) {
            double totalCurrentPhaseRate = calculateCurrentPhaseRate(a);
            double previousPhaseRate = calculatePreviousPhaseRate(a);
            double currentPR = a.getProcessingRate();
            for (CurtailableWorkstation c : cs) {
                currentPR += c.getProcessingRate();
            }
            if (totalCurrentPhaseRate - currentPR >= previousPhaseRate) {
                return makeCurtFlexTuple(a, cs);
            }
        }
        return FlexTuple.createNONE();
    }

    private FlexTuple makeCurtFlexTuple(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        double sump = a.getAverageConsumption();
        for (CurtailableWorkstation c : cs) {
            sump += c.getAverageConsumption();
        }
        return FlexTuple.create(newId(), (int) sump, false, 1, 0, 0);
    }

    private double calculatePreviousPhaseRate(CurtailableWorkstation c) {
        double sum = 0;
        for (Workstation w : filterNotSource(layout.getEdgeSource(c))) {
            sum += w.getProcessingRate();
        }
        return sum;
    }

    private Set<Workstation> filterNotSource(Buffer<Resource> c) {
        Set<Workstation> t = Sets.newHashSet();
        for (Workstation w : layout.edgesOf(c)) {
            if (layout.getEdgeTarget(w).equals(c)) {
                t.add(w);
            }
        }
        return t;
    }

    private double calculateCurrentPhaseRate(CurtailableWorkstation c) {
        double sum = 0;
        for (Workstation w : layout.getAllEdges(layout.getEdgeSource(c),
                layout.getEdgeTarget(c))) {
            sum += w.getProcessingRate();
        }
        return sum;
    }
}
