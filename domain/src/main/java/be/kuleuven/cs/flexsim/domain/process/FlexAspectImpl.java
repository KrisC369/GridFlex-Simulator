/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.NPermuteAndCombiner;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.simulation.UIDGenerator;
import edu.uci.ics.jung.graph.Graph;

/**
 * Abstract helper super class for flex aspects.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
abstract class FlexAspectImpl implements FlexAspect {
    private static final String INITIALISE_ERR = "Initialise this aspect first. No layout present.";
    private final UIDGenerator generator;
    private final Graph<Buffer<Resource>, Workstation> layout;

    /**
     * Constructor
     * 
     * @param gen
     *            The generator to use for unique ids.
     * @param layout
     *            the layout of the process to calculate flex for.
     */
    FlexAspectImpl(UIDGenerator gen,
            Graph<Buffer<Resource>, Workstation> layout) {
        this.layout = layout;
        this.generator = gen;
    }

    protected final FlexTuple findFlex(
            LinkedListMultimap<Long, Workstation> profileMap, Workstation a,
            Workstation... cs) {
        if (presentInSamePhase(a, cs)) {
            return samePhaseFirstOrderFlex(profileMap, a, cs);
        }
        return twoPhasesFirstOrderFlex(profileMap, a, cs);
    }

    private FlexTuple samePhaseFirstOrderFlex(
            LinkedListMultimap<Long, Workstation> profileMap, Workstation a,
            Workstation... cs) {
        double totalCurrentPhaseRate = calculateCurrentPhaseRate(a);
        double previousPhaseRate = calculatePreviousPhaseRate(a);
        double currentPR = aggregateProcessingRate(a, Lists.newArrayList(cs));
        if (canCurtail(totalCurrentPhaseRate, previousPhaseRate, currentPR)) {
            return makeCurtFlexTuple(profileMap, false, a, cs);
        }
        return FlexTuple.createNONE();
    }

    private FlexTuple twoPhasesFirstOrderFlex(
            LinkedListMultimap<Long, Workstation> profileMap, Workstation a,
            Workstation... cs) {
        List<Workstation> firstPhase = Lists.newArrayList();
        List<Workstation> secondPhase = Lists.newArrayList();
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
            return makeCurtFlexTuple(profileMap, false, a, cs);
        }
        return FlexTuple.createNONE();
    }

    private double calculateCurrentPhaseRate(Workstation c) {
        checkNotNull(this.layout, INITIALISE_ERR);
        Graph<Buffer<Resource>, Workstation> layout2 = layout;
        List<Workstation> edges = Lists.newArrayList();
        for (Workstation s : layout2.getEdges()) {
            if (layout2.getSource(s).equals(layout2.getSource(c))
                    && layout2.getDest(s).equals(layout2.getDest(c))) {
                edges.add(s);
            }
        }
        return aggregateProcessingRate(edges);
    }

    private double calculatePreviousPhaseRate(Workstation c) {
        checkNotNull(this.layout, INITIALISE_ERR);
        Graph<Buffer<Resource>, Workstation> layout2 = layout;
        return aggregateProcessingRate(
                layout2.getInEdges(layout2.getSource(c)));
    }

    private void splitLists(Workstation firstPhaseExample,
            List<Workstation> firstPhase, List<Workstation> secondPhase,
            List<Workstation> stations) {
        Workstation mark;
        for (Workstation station : stations) {
            mark = station;
            if (presentInSamePhase(firstPhaseExample, mark)) {
                firstPhase.add(mark);
            } else {
                secondPhase.add(mark);
            }
        }
    }

    private <Y extends Workstation> double aggregateProcessingRate(Y a,
            List<Y> firstPhase) {
        List<Y> list = Lists.newArrayList();
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

    private boolean canCurtail(double totalCurrentPhaseRate,
            double previousPhaseRate, double currentCurtEst) {
        return totalCurrentPhaseRate - currentCurtEst >= previousPhaseRate;
    }

    private boolean presentInSamePhase(Workstation a, Workstation... b) {
        checkNotNull(this.layout, INITIALISE_ERR);
        Graph<Buffer<Resource>, Workstation> layout2 = layout;
        if (b.length == 0) {
            return true;
        }
        if (b.length == 1) {
            return layout2.getSource(a).equals(layout2.getSource(b[0]))
                    && layout2.getDest(a).equals(layout2.getDest(b[0]));
        }
        for (Workstation cb : b) {
            if (layout2.getSource(a).equals(layout2.getSource(cb))
                    && layout2.getDest(a).equals(layout2.getDest(cb))) {
                return false;
            }
        }
        return true;
    }

    protected final FlexTuple makeCurtFlexTuple(
            LinkedListMultimap<Long, Workstation> profileMap, boolean upflex,
            List<Workstation> cs) {
        if (cs.isEmpty()) {
            throw new IllegalArgumentException("No stations to create curt.");
        }
        if (cs.size() == 1) {
            return makeCurtFlexTuple(profileMap, upflex, cs.get(0),
                    new CurtailableWorkstation[0]);
        }
        return makeCurtFlexTuple(profileMap, upflex, cs.get(0),
                cs.subList(1, cs.size())
                        .toArray(new CurtailableWorkstation[cs.size() - 1]));
    }

    private FlexTuple makeCurtFlexTuple(
            LinkedListMultimap<Long, Workstation> profileMap, boolean upflex,
            Workstation a, Workstation... cs) {
        double sump = a.getAverageConsumption();
        checkNotNull(this.generator,
                "Initialize this aspect first. No generator present.");
        UIDGenerator gen = generator;
        for (Workstation c : cs) {
            sump += c.getAverageConsumption();
        }
        long id = gen.getNextUID();
        Set<Workstation> set = Sets.newLinkedHashSet();
        set.add(a);
        set.addAll(Lists.newArrayList(cs));
        return makeTuple(profileMap, id, (int) Math.round(sump), 1, set,
                upflex);
    }

    protected final FlexTuple makeDualModeFlexTuple(
            LinkedListMultimap<Long, Workstation> profileMap, int dP, double dT,
            boolean upflex, List<? extends Workstation> cs) {
        checkNotNull(this.generator,
                "Initialize this aspect first. No generator present.");
        UIDGenerator gen = generator;
        long id = gen.getNextUID();
        List<Workstation> set = Lists.newArrayList(cs);
        return makeTuple(profileMap, id, dP, (int) Math.ceil(dT), set, upflex);
    }

    private FlexTuple makeTuple(
            LinkedListMultimap<Long, Workstation> profileMap, long id,
            int deltaP, int deltaT, Iterable<Workstation> target,
            boolean upflex) {
        profileMap.putAll(id, target);
        return FlexTuple.create(id, deltaP, upflex, deltaT, 0, 0);
    }

    static class SingleStationDownFlex extends FlexAspectImpl {

        SingleStationDownFlex(UIDGenerator gen,
                Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> effectivelyCurtableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            for (Workstation c : effectivelyCurtableStations) {
                flexRet.add(findFlex(profileMap, c));
            }
            return flexRet;
        }
    }

    static class TwoStationsDownFlex extends FlexAspectImpl {

        TwoStationsDownFlex(UIDGenerator gen,
                Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> effectivelyCurtableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findTwoStationsFlex(profileMap,
                    effectivelyCurtableStations));
            return flexRet;

        }

        private List<FlexTuple> findTwoStationsFlex(
                LinkedListMultimap<Long, Workstation> profileMap,
                List<? extends Workstation> curtailableStations) {
            List<FlexTuple> flexRet = Lists.newArrayList();
            int size = curtailableStations.size();
            for (int i = 0; i <= size - 2; i++) {
                for (int j = i + 1; j <= size - 1; j++) {
                    flexRet.add(findFlex(profileMap, curtailableStations.get(i),
                            curtailableStations.get(j)));
                }
            }
            if (!flexRet.isEmpty()) {
                return flexRet;
            }
            return Lists.newArrayList(FlexTuple.NONE);
        }
    }

    static class ThreeStationsDownFlex extends FlexAspectImpl {

        ThreeStationsDownFlex(UIDGenerator gen,
                Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> effectivelyCurtableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findThreeStationFlex(profileMap,
                    effectivelyCurtableStations));
            return flexRet;

        }

        private List<FlexTuple> findThreeStationFlex(
                LinkedListMultimap<Long, Workstation> profileMap,
                List<? extends Workstation> curtailableStations) {
            List<FlexTuple> flexRet = Lists.newArrayList();
            int size = curtailableStations.size();
            for (int i = 0; i < size - 2; i++) {
                for (int j = i + 1; j < size - 1; j++) {
                    for (int k = j + 1; k < size - 1; k++) {
                        flexRet.add(
                                findFlex(profileMap, curtailableStations.get(i),
                                        curtailableStations.get(j),
                                        curtailableStations.get(k)));
                    }
                }
            }
            if (!flexRet.isEmpty()) {
                return flexRet;
            }
            return Lists.newArrayList(FlexTuple.NONE);
        }
    }

    static class UpFlex extends FlexAspectImpl {

        UpFlex(UIDGenerator gen, Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> curtailableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            List<Set<Workstation>> sets2 = Lists.newArrayList(
                    Sets.powerSet(Sets.newLinkedHashSet(curtailedStations)));
            sets2.remove(Sets.newLinkedHashSet());

            for (Set<Workstation> lc : sets2) {
                flexRet.add(makeCurtFlexTuple(profileMap, true,
                        Lists.newArrayList(lc)));
            }

            return flexRet;

        }
    }

    static class SteerFlex extends FlexAspectImpl {
        SteerFlex(UIDGenerator gen,
                Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> curtailableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            // TODO Implement!
            return flexRet;
        }
    }

    static class DualModeFlex extends FlexAspectImpl {
        DualModeFlex(UIDGenerator gen,
                Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                List<? extends Workstation> curtailableStations,
                List<? extends Workstation> curtailedStations,
                List<DualModeWorkstation> dualModeWorkstations,
                LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();

            List<DualModeWorkstation> highs = getOnlyHighs(
                    dualModeWorkstations);
            List<DualModeWorkstation> lows = getOnlyLows(dualModeWorkstations);
            flexRet.addAll(getUpFlex(profileMap, lows));
            flexRet.addAll(getDownFlex(profileMap, highs));

            return flexRet;
        }

        private Collection<? extends FlexTuple> getDownFlex(
                LinkedListMultimap<Long, Workstation> profileMap,
                List<DualModeWorkstation> highs) {
            return getFlex(profileMap, highs, false);
        }

        private Collection<? extends FlexTuple> getUpFlex(
                LinkedListMultimap<Long, Workstation> profileMap,
                List<DualModeWorkstation> lows) {
            return getFlex(profileMap, lows, true);
        }

        private Collection<? extends FlexTuple> getFlex(
                LinkedListMultimap<Long, Workstation> profileMap,
                List<DualModeWorkstation> lows, boolean upFlex) {
            List<FlexTuple> flexRet = Lists.newArrayList();
            NPermuteAndCombiner<DualModeWorkstation> g = new NPermuteAndCombiner<>();
            List<List<DualModeWorkstation>> combos = Lists.newArrayList();
            for (int i = 1; i <= lows.size(); i++) {
                combos.addAll(g.processSubsets(Lists.newArrayList(lows), i));
            }

            for (List<DualModeWorkstation> options : combos) {
                flexRet.add(findFlexForCombo(options, profileMap, upFlex));
            }
            return flexRet;
        }

        FlexTuple findFlexForCombo(List<DualModeWorkstation> options,
                LinkedListMultimap<Long, Workstation> profileMap,
                boolean upFlex) {
            int sumP = 0;
            double maxT = 0;
            for (DualModeWorkstation w : options) {
                sumP += w.getHighConsumptionRate() - w.getLowConsumptionRate();
                double currentT = w.getProcessingRate() != 0
                        ? w.getRatedCapacity() / w.getProcessingRate() : 1;
                if (currentT > maxT) {
                    maxT = currentT;
                }
            }
            return makeDualModeFlexTuple(profileMap, sumP, maxT, upFlex,
                    options);
        }

        private List<DualModeWorkstation> getOnlyLows(
                List<DualModeWorkstation> dualModeWorkstations) {
            return filterByMode(dualModeWorkstations, false);
        }

        private List<DualModeWorkstation> getOnlyHighs(
                List<DualModeWorkstation> dualModeWorkstations) {
            return filterByMode(dualModeWorkstations, true);
        }

        private List<DualModeWorkstation> filterByMode(
                List<DualModeWorkstation> stations, boolean isHigh) {
            List<DualModeWorkstation> toRet = Lists.newArrayList();
            for (DualModeWorkstation s : stations) {
                if (s.isHigh() == isHigh) {
                    toRet.add(s);
                }
            }
            return toRet;
        }
    }
}
