package be.kuleuven.cs.flexsim.domain.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;

import com.google.common.collect.HashMultimap;
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
    private boolean fresh;
    private List<FlexTuple> flex;
    private HashMultimap<Long, Workstation> profileMap;

    /**
     * Default constructor
     * 
     * @param subject
     *            the subject PLine.
     */
    public ProcessDeviceImpl(ProductionLine subject) {
        this.layout = subject.getLayout();
        this.flex = Lists.newArrayList();
        this.profileMap = HashMultimap.create();
    }

    List<FlexTuple> getCurrentFlexbility(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations) {

        if (!fresh) {
            this.flex = recalculateFlex(curtailableWorkstations,
                    tradeofSteerableWorkstations);
            this.fresh = true;
        }
        return this.flex;
    }

    private List<FlexTuple> recalculateFlex(
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

    private FlexTuple calculateFirstOrderCurtFlex(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        if (presentInSamePhase(a, cs)) {
            return samePhaseFirstOrderFlex(a, cs);
        }
        return twoPhasesFirstOrderFlex(a, cs);
    }

    private FlexTuple samePhaseFirstOrderFlex(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        double totalCurrentPhaseRate = calculateCurrentPhaseRate(a);
        double previousPhaseRate = calculatePreviousPhaseRate(a);
        double currentPR = aggregateProcessingRate(a, Lists.newArrayList(cs));
        if (canCurtail(totalCurrentPhaseRate, previousPhaseRate, currentPR)) {
            return makeCurtFlexTuple(a, cs);
        }
        return FlexTuple.createNONE();
    }

    private FlexTuple twoPhasesFirstOrderFlex(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        List<CurtailableWorkstation> firstPhase = Lists.newArrayList();
        List<CurtailableWorkstation> secondPhase = Lists.newArrayList();
        splitLists(a, firstPhase, secondPhase, Lists.newArrayList(cs));
        double firstPhaseTotal = calculateCurrentPhaseRate(a);
        double preFirstPhase = calculatePreviousPhaseRate(a);
        double secondPhaseTotal = secondPhase.isEmpty() ? 0
                : calculatePreviousPhaseRate(secondPhase.get(0));
        double curtEstFirstPhase = aggregateProcessingRate(a, firstPhase);
        double curtEstSecondPhase = aggregateProcessingRate(secondPhase);
        if (canCurtail(firstPhaseTotal, preFirstPhase, curtEstFirstPhase)
                && canCurtail(secondPhaseTotal, curtEstSecondPhase,
                        firstPhaseTotal - curtEstFirstPhase)) {
            return makeCurtFlexTuple(a, cs);
        }
        return FlexTuple.createNONE();
    }

    private List<FlexTuple> calculateOrder2CurtFlex(
            List<CurtailableWorkstation> curtailableStations) {
        List<FlexTuple> flex = Lists.newArrayList();
        int size = curtailableStations.size();
        for (int i = 0; i <= size - 2; i++) {
            for (int j = i + 1; j <= size - 1; j++) {
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

    private <T extends Workstation> double aggregateProcessingRate(T a,
            List<T> firstPhase) {
        List<T> list = Lists.newArrayList();
        list.add(a);
        list.addAll(firstPhase);
        return aggregateProcessingRate(list);
    }

    private double aggregateProcessingRate(
            Iterable<? extends Workstation> stations) {
        double result = 0;
        for (Workstation c : stations) {
            result += c.getProcessingRate();
        }
        return result;
    }

    private double calculateCurrentPhaseRate(CurtailableWorkstation c) {
        return aggregateProcessingRate(layout.getAllEdges(
                layout.getEdgeSource(c), layout.getEdgeTarget(c)));
    }

    private double calculatePreviousPhaseRate(CurtailableWorkstation c) {
        return aggregateProcessingRate(filterNotSource(layout.getEdgeSource(c)));
    }

    private void splitLists(CurtailableWorkstation firstPhaseExample,
            List<CurtailableWorkstation> firstPhase,
            List<CurtailableWorkstation> secondPhase,
            List<CurtailableWorkstation> stations) {
        CurtailableWorkstation mark;
        for (int i = 0; i < stations.size(); i++) {
            mark = stations.get(i);
            if (presentInSamePhase(firstPhaseExample, mark)) {
                firstPhase.add(mark);
            } else {
                secondPhase.add(mark);
            }
        }
    }

    private boolean canCurtail(double totalCurrentPhaseRate,
            double previousPhaseRate, double currentCurtEst) {
        return totalCurrentPhaseRate - currentCurtEst >= previousPhaseRate;
    }

    private FlexTuple makeCurtFlexTuple(CurtailableWorkstation a,
            CurtailableWorkstation... cs) {
        double sump = a.getAverageConsumption();
        for (CurtailableWorkstation c : cs) {
            sump += c.getAverageConsumption();
        }
        sump *= -1;
        long id = newId();
        HashSet<Workstation> set = Sets.newHashSet();
        set.add(a);
        set.addAll(Lists.newArrayList(cs));
        profileMap.putAll(id, set);
        return FlexTuple.create(id, (int) sump, false, 1, 0, 0);
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

    private ArrayList<FlexTuple> filterOutDuplicates(List<FlexTuple> flex) {
        return Lists.newArrayList(com.google.common.collect.Sets
                .newHashSet(flex));
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

    private FlexTuple calculateSteerFlex(TradeofSteerableWorkstation c) {
        // TODO implement
        return FlexTuple.createNONE();
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

    private synchronized long newId() {
        final int prime = this.hashCode();
        int result = 1;
        result = prime * result + (int) (idcount ^ (idcount >>> 32));
        idcount++;
        return result;
    }

    void invalidate() {
        this.fresh = false;
    }

    void executeCurtailment(Long id, List<CurtailableWorkstation> list) {
        Set<Workstation> stations = profileMap.get(id);
        for (CurtailableWorkstation c : list) {
            for (Workstation s : stations) {
                if (c.equals(s)) {
                    c.doFullCurtailment();
                }
            }
        }
    }
}
