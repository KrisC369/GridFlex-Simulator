package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Implements the process device interface.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
class ProcessDeviceImpl implements ProcessDevice {

    private boolean fresh;
    private List<FlexTuple> flexibility;
    private LinkedListMultimap<Long, Workstation> profileMap;
    private final Set<FlexAspect> aspects;

    /**
     * Default constructor
     * 
     * @param subject
     *            the subject PLine.
     */
    ProcessDeviceImpl() {
        this.flexibility = Lists.newArrayList();
        this.profileMap = LinkedListMultimap.create();
        this.aspects = Sets.newLinkedHashSet();
    }

    @Override
    public List<FlexTuple> getCurrentFlexbility(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations) {

        if (!fresh) {
            this.flexibility = recalculateFlex(curtailableWorkstations,
                    tradeofSteerableWorkstations);
            this.fresh = true;
        }
        return this.flexibility;
    }

    private List<FlexTuple> recalculateFlex(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations) {
        this.profileMap = LinkedListMultimap.create();
        return gratuitousFlex(curtailableWorkstations,
                tradeofSteerableWorkstations);
    }

    private List<FlexTuple> gratuitousFlex(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations) {
        if (curtailableWorkstations.isEmpty()
                && tradeofSteerableWorkstations.isEmpty()) {
            return Lists.newArrayList(FlexTuple.createNONE());
        }
        List<FlexTuple> flexRet = Lists.newArrayList();
        List<CurtailableWorkstation> effectivelyCurtailableStations = getEffectivelyCurtailableStations(curtailableWorkstations);
        List<CurtailableWorkstation> curtailedStations = getCurtailedStations(curtailableWorkstations);
        for (FlexAspect aspect : aspects) {
            flexRet.addAll(aspect.getFlexibility(
                    effectivelyCurtailableStations, curtailedStations,
                    profileMap));
        }
        flexRet = filterOutDuplicates(flexRet);
        flexRet = someOrNone(flexRet);
        return flexRet;
    }

    private List<FlexTuple> filterOutDuplicates(List<FlexTuple> flex) {
        return Lists.newArrayList(Sets.newLinkedHashSet(flex));
    }

    private List<FlexTuple> someOrNone(List<FlexTuple> flex) {
        List<FlexTuple> fr = Lists.newArrayList();
        for (FlexTuple f : flex) {
            if (!f.equals(FlexTuple.NONE)) {
                fr.add(f);
            }
        }
        if (!fr.isEmpty()) {
            return fr;
        }
        return Lists.newArrayList(FlexTuple.NONE);
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

    private List<CurtailableWorkstation> getCurtailedStations(
            List<CurtailableWorkstation> curtailableWorkstations) {
        List<CurtailableWorkstation> toret = Lists.newArrayList();
        for (CurtailableWorkstation w : curtailableWorkstations) {
            if (w.isCurtailed()) {
                toret.add(w);
            }
        }
        return toret;
    }

    @Override
    public void invalidate() {
        this.fresh = false;
    }

    @Override
    public void executeCurtailment(long id, List<CurtailableWorkstation> list) {
        List<Workstation> stations = profileMap.get(id);
        for (CurtailableWorkstation c : getEffectivelyCurtailableStations(list)) {
            for (Workstation s : stations) {
                if (c.equals(s)) {
                    c.doFullCurtailment();
                    logFullCurtailment(c);
                }
            }
        }
    }

    @Override
    public void executeCancelCurtailment(long id,
            List<CurtailableWorkstation> curtailableStations) {
        List<Workstation> stations = profileMap.get(id);
        for (CurtailableWorkstation c : curtailableStations) {
            for (Workstation s : stations) {
                if (c.equals(s)) {
                    c.restore();
                    logCancelCurtailment(c);
                }
            }
        }
    }

    private void logFullCurtailment(Workstation c) {
        LoggerFactory.getLogger(ProductionLine.class).debug(
                "Executing curtailment on {}", c);
    }

    private void logCancelCurtailment(Workstation c) {
        LoggerFactory.getLogger(ProductionLine.class).debug(
                "Restoring curtailment on {}", c);
    }

    @Override
    public ProcessDevice addFlexAspect(FlexAspect aspect) {
        this.aspects.add(aspect);
        return this;
    }
}
