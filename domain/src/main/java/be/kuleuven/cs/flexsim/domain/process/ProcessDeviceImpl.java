package be.kuleuven.cs.flexsim.domain.process;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationVisitor;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Implements the process device interface.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class ProcessDeviceImpl implements ProcessDevice {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDeviceImpl.class);
    private boolean fresh;
    private List<FlexTuple> flexibility;
    private LinkedListMultimap<Long, Workstation> profileMap;
    private final Set<FlexAspect> aspects;
    private final UpFlexVisitor upFlexVisitor;
    private final DownFlexVisitor downFlexVisitor;

    /**
     * Default constructor
     */
    ProcessDeviceImpl() {
        this.flexibility = Lists.newArrayList();
        this.profileMap = LinkedListMultimap.create();
        this.aspects = Sets.newLinkedHashSet();
        this.upFlexVisitor = new UpFlexVisitor();
        this.downFlexVisitor = new DownFlexVisitor();
    }

    @Override
    public List<FlexTuple> getCurrentFlexbility(
            final List<CurtailableWorkstation> curtailableWorkstations,
            final List<TradeofSteerableWorkstation> tradeofSteerableWorkstations,
            final List<DualModeWorkstation> dualModeWorkstations) {

        if (!fresh) {
            this.flexibility = recalculateFlex(curtailableWorkstations,
                    tradeofSteerableWorkstations, dualModeWorkstations);
            this.fresh = true;
        }
        return this.flexibility;
    }

    private List<FlexTuple> recalculateFlex(
            final List<CurtailableWorkstation> curtailableWorkstations,
            final List<TradeofSteerableWorkstation> tradeofSteerableWorkstations,
            final List<DualModeWorkstation> dualModeWorkstations) {
        this.profileMap = LinkedListMultimap.create();
        return calcInstantaneousFlex(curtailableWorkstations,
                tradeofSteerableWorkstations, dualModeWorkstations);
    }

    private List<FlexTuple> calcInstantaneousFlex(
            final List<CurtailableWorkstation> curtailableWorkstations,
            final List<TradeofSteerableWorkstation> tradeofSteerableWorkstations,
            final List<DualModeWorkstation> dualModeWorkstations) {
        if (curtailableWorkstations.isEmpty()
                && tradeofSteerableWorkstations.isEmpty()) {
            return Lists.newArrayList(FlexTuple.createNONE());
        }
        List<FlexTuple> flexRet = Lists.newArrayList();
        final List<CurtailableWorkstation> effectivelyCurtailableStations =
                getEffectivelyCurtailableStations(
                        curtailableWorkstations);
        final List<CurtailableWorkstation> curtailedStations = getCurtailedStations(
                curtailableWorkstations);
        for (final FlexAspect aspect : aspects) {
            flexRet.addAll(aspect.getFlexibility(effectivelyCurtailableStations,
                    curtailedStations, dualModeWorkstations, profileMap));
        }
        flexRet = filterOutDuplicates(flexRet);
        return someOrNone(flexRet);
    }

    private static List<FlexTuple> filterOutDuplicates(final List<FlexTuple> flex) {
        return Lists.newArrayList(Sets.newLinkedHashSet(flex));
    }

    private static List<FlexTuple> someOrNone(final List<FlexTuple> flex) {
        final List<FlexTuple> fr = Lists.newArrayList();
        for (final FlexTuple f : flex) {
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
            final List<CurtailableWorkstation> curtailableWorkstations) {
        return testAndFilterCurtailedStation(curtailableWorkstations, false);
    }

    private List<CurtailableWorkstation> getCurtailedStations(
            final List<CurtailableWorkstation> curtailableWorkstations) {
        return testAndFilterCurtailedStation(curtailableWorkstations, true);
    }

    private static List<CurtailableWorkstation> testAndFilterCurtailedStation(
            final List<CurtailableWorkstation> curtailableWorkstations,
            final boolean isCurt) {
        final List<CurtailableWorkstation> toret = Lists.newArrayList();
        for (final CurtailableWorkstation w : curtailableWorkstations) {
            if (w.isCurtailed() == isCurt) {
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
    public void executeDownFlexProfile(final long id) {
        final List<Workstation> stations = profileMap.get(id);
        for (final Workstation t : stations) {
            t.acceptVisitor(downFlexVisitor);
        }
    }

    @Override
    public void executeUpFlexProfile(final long id) {
        final List<Workstation> stations = profileMap.get(id);
        for (final Workstation t : stations) {
            t.acceptVisitor(upFlexVisitor);
        }

    }

    @Override
    public ProcessDevice addFlexAspect(final FlexAspect aspect) {
        this.aspects.add(aspect);
        return this;
    }

    private final class UpFlexVisitor implements WorkstationVisitor {

        private void logDualModeHigh(final DualModeWorkstation ws) {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing singal High on {}", ws);
            }
        }

        private void logCancelCurtailment(final Workstation c) {
            if (logger.isDebugEnabled()) {
                logger.debug("Restoring curtailment on {}", c);
            }
        }

        @Override
        public void register(final DualModeWorkstation ws) {
            ws.signalHighConsumption();
            logDualModeHigh(ws);
        }

        @Override
        public void register(final TradeofSteerableWorkstation ws) {
        }

        @Override
        public void register(final CurtailableWorkstation c) {
            c.restore();
            logCancelCurtailment(c);
        }

        @Override
        public void register(final Workstation workstation) {
        }
    }

    private final class DownFlexVisitor implements WorkstationVisitor {

        private void logDualModeLow(final DualModeWorkstation ws) {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing singal Low on {}", ws);
            }
        }

        private void logFullCurtailment(final Workstation c) {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing curtailment on {}", c);
            }
        }

        @Override
        public void register(final DualModeWorkstation ws) {
            ws.signalLowConsumption();
            logDualModeLow(ws);
        }

        @Override
        public void register(final TradeofSteerableWorkstation ws) {
        }

        @Override
        public void register(final CurtailableWorkstation c) {
            c.doFullCurtailment();
            logFullCurtailment(c);
        }

        @Override
        public void register(final Workstation workstation) {
        }
    }
}
