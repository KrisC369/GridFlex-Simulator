/**
 *
 */
package be.kuleuven.cs.gridflex.domain.process;

import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.util.Buffer;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import be.kuleuven.cs.gridflex.domain.util.NPermuteAndCombiner;
import be.kuleuven.cs.gridflex.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.gridflex.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.gridflex.domain.workstation.Workstation;
import be.kuleuven.cs.gridflex.simulation.UIDGenerator;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.Graph;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract helper super class for flex aspects.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
abstract class FlexAspectImpl implements FlexAspect {
    private static final String INITIALISE_ERR = "Initialise this aspect first. No layout present.";
    private final UIDGenerator generator;
    private final Graph<Buffer<Resource>, Workstation> layout;
    private static final double EPSILON = 0.0001d;

    /**
     * Constructor
     *
     * @param gen    The generator to use for unique ids.
     * @param layout the layout of the process to calculate flex for.
     */
    FlexAspectImpl(final UIDGenerator gen,
            final Graph<Buffer<Resource>, Workstation> layout) {
        this.layout = layout;
        this.generator = gen;
    }

    protected final FlexTuple findFlex(
            final LinkedListMultimap<Long, Workstation> profileMap, final Workstation a,
            final Workstation... cs) {
        if (presentInSamePhase(a, cs)) {
            return samePhaseFirstOrderFlex(profileMap, a, cs);
        }
        return twoPhasesFirstOrderFlex(profileMap, a, cs);
    }

    private FlexTuple samePhaseFirstOrderFlex(
            final LinkedListMultimap<Long, Workstation> profileMap, final Workstation a,
            final Workstation... cs) {
        final double totalCurrentPhaseRate = calculateCurrentPhaseRate(a);
        final double previousPhaseRate = calculatePreviousPhaseRate(a);
        final double currentPR = aggregateProcessingRate(a, Lists.newArrayList(cs));
        if (canCurtail(totalCurrentPhaseRate, previousPhaseRate, currentPR)) {
            return makeCurtFlexTuple(profileMap, false, a, cs);
        }
        return FlexTuple.createNONE();
    }

    private FlexTuple twoPhasesFirstOrderFlex(
            final LinkedListMultimap<Long, Workstation> profileMap, final Workstation a,
            final Workstation... cs) {
        final List<Workstation> firstPhase = Lists.newArrayList();
        final List<Workstation> secondPhase = Lists.newArrayList();
        splitLists(a, firstPhase, secondPhase, Lists.newArrayList(cs));
        final double firstPhaseTotal = calculateCurrentPhaseRate(a);
        final double preFirstPhase = calculatePreviousPhaseRate(a);
        final double secondPhaseTotal = secondPhase.isEmpty() ? 0
                : calculatePreviousPhaseRate(secondPhase.get(0));
        final double curtEstFirstPhase = aggregateProcessingRate(a, firstPhase);
        final double curtEstSecondPhase = aggregateProcessingRate(secondPhase);
        if (canCurtail(firstPhaseTotal, preFirstPhase, curtEstFirstPhase)
                && canCurtail(secondPhaseTotal, curtEstSecondPhase,
                firstPhaseTotal - curtEstFirstPhase)) {
            return makeCurtFlexTuple(profileMap, false, a, cs);
        }
        return FlexTuple.createNONE();
    }

    private double calculateCurrentPhaseRate(final Workstation c) {
        checkNotNull(this.layout, INITIALISE_ERR);
        final Graph<Buffer<Resource>, Workstation> layout2 = layout;
        final List<Workstation> edges = Lists.newArrayList();
        for (final Workstation s : layout2.getEdges()) {
            if (layout2.getSource(s).equals(layout2.getSource(c))
                    && layout2.getDest(s).equals(layout2.getDest(c))) {
                edges.add(s);
            }
        }
        return aggregateProcessingRate(edges);
    }

    private double calculatePreviousPhaseRate(final Workstation c) {
        checkNotNull(this.layout, INITIALISE_ERR);
        final Graph<Buffer<Resource>, Workstation> layout2 = layout;
        return aggregateProcessingRate(
                layout2.getInEdges(layout2.getSource(c)));
    }

    private void splitLists(final Workstation firstPhaseExample,
            final List<Workstation> firstPhase, final List<Workstation> secondPhase,
            final List<Workstation> stations) {
        Workstation mark;
        for (final Workstation station : stations) {
            mark = station;
            if (presentInSamePhase(firstPhaseExample, mark)) {
                firstPhase.add(mark);
            } else {
                secondPhase.add(mark);
            }
        }
    }

    private <Y extends Workstation> double aggregateProcessingRate(final Y a,
            final List<Y> firstPhase) {
        final List<Y> list = Lists.newArrayList();
        list.add(a);
        list.addAll(firstPhase);
        return aggregateProcessingRate(list);
    }

    private static double aggregateProcessingRate(
            final Iterable<? extends Workstation> stations) {
        double result = 0;
        for (final Workstation c : stations) {
            result += c.getProcessingRate();
        }
        return result;
    }

    private static boolean canCurtail(final double totalCurrentPhaseRate,
            final double previousPhaseRate, final double currentCurtEst) {
        return totalCurrentPhaseRate - currentCurtEst >= previousPhaseRate;
    }

    private boolean presentInSamePhase(final Workstation a, final Workstation... b) {
        if (b.length == 0) {
            return true;
        }
        checkNotNull(this.layout, INITIALISE_ERR);
        final Graph<Buffer<Resource>, Workstation> layout2 = layout;
        if (b.length == 1) {
            return layout2.getSource(a).equals(layout2.getSource(b[0]))
                    && layout2.getDest(a).equals(layout2.getDest(b[0]));
        }
        for (final Workstation cb : b) {
            if (layout2.getSource(a).equals(layout2.getSource(cb))
                    && layout2.getDest(a).equals(layout2.getDest(cb))) {
                return false;
            }
        }
        return true;
    }

    protected final FlexTuple makeCurtFlexTuple(
            final LinkedListMultimap<Long, Workstation> profileMap, final boolean upflex,
            final List<Workstation> cs) {
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
            final LinkedListMultimap<Long, Workstation> profileMap, final boolean upflex,
            final Workstation a, final Workstation... cs) {
        double sump = a.getAverageConsumption();
        checkNotNull(this.generator,
                "Initialize this aspect first. No generator present.");
        final UIDGenerator gen = generator;
        for (final Workstation c : cs) {
            sump += c.getAverageConsumption();
        }
        final long id = gen.getNextUID();
        final Set<Workstation> set = Sets.newLinkedHashSet();
        set.add(a);
        set.addAll(Lists.newArrayList(cs));
        return makeTuple(profileMap, id, (int) Math.round(sump), 1, set,
                upflex);
    }

    protected final FlexTuple makeDualModeFlexTuple(
            final LinkedListMultimap<Long, Workstation> profileMap, final int dP, final double dT,
            final boolean upflex, final List<? extends Workstation> cs) {
        checkNotNull(this.generator,
                "Initialize this aspect first. No generator present.");
        final UIDGenerator gen = generator;
        final long id = gen.getNextUID();
        final List<Workstation> newlist = Lists.newArrayList(cs);
        return makeTuple(profileMap, id, dP, (int) Math.ceil(dT), newlist, upflex);
    }

    private static FlexTuple makeTuple(
            final Multimap<Long, Workstation> profileMap, final long id,
            final int deltaP, final int deltaT, final Iterable<Workstation> target,
            final boolean upflex) {
        profileMap.putAll(id, target);
        return FlexTuple
                .create(id, deltaP, FlexTuple.Direction.fromRepresentation(upflex), deltaT, 0, 0);
    }

    static class SingleStationDownFlex extends FlexAspectImpl {

        SingleStationDownFlex(final UIDGenerator gen,
                final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> effectivelyCurtableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            for (final Workstation c : effectivelyCurtableStations) {
                flexRet.add(findFlex(profileMap, c));
            }
            return flexRet;
        }
    }

    static class TwoStationsDownFlex extends FlexAspectImpl {

        TwoStationsDownFlex(final UIDGenerator gen,
                final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> effectivelyCurtableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findTwoStationsFlex(profileMap,
                    effectivelyCurtableStations));
            return flexRet;

        }

        private List<FlexTuple> findTwoStationsFlex(
                final LinkedListMultimap<Long, Workstation> profileMap,
                final List<? extends Workstation> curtailableStations) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            final int size = curtailableStations.size();
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

        ThreeStationsDownFlex(final UIDGenerator gen,
                final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> effectivelyCurtableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findThreeStationFlex(profileMap,
                    effectivelyCurtableStations));
            return flexRet;

        }

        private List<FlexTuple> findThreeStationFlex(
                final LinkedListMultimap<Long, Workstation> profileMap,
                final List<? extends Workstation> curtailableStations) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            final int size = curtailableStations.size();
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

        UpFlex(final UIDGenerator gen, final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> curtailableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            final List<Set<Workstation>> sets2 = Lists.newArrayList(
                    Sets.powerSet(Sets.newLinkedHashSet(curtailedStations)));
            sets2.remove(Sets.newLinkedHashSet());

            for (final Set<Workstation> lc : sets2) {
                flexRet.add(makeCurtFlexTuple(profileMap, true,
                        Lists.newArrayList(lc)));
            }

            return flexRet;

        }
    }

    static class SteerFlex extends FlexAspectImpl {
        SteerFlex(final UIDGenerator gen,
                final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> curtailableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            // TODO Implement!
            return Lists.newArrayList();
        }
    }

    static class DualModeFlex extends FlexAspectImpl {
        DualModeFlex(final UIDGenerator gen,
                final Graph<Buffer<Resource>, Workstation> layout) {
            super(gen, layout);
        }

        @Override
        public List<FlexTuple> getFlexibility(
                final List<? extends Workstation> curtailableStations,
                final List<? extends Workstation> curtailedStations,
                final List<DualModeWorkstation> dualModeWorkstations,
                final LinkedListMultimap<Long, Workstation> profileMap) {
            final List<FlexTuple> flexRet = Lists.newArrayList();

            final List<DualModeWorkstation> highs = getOnlyHighs(
                    dualModeWorkstations);
            final List<DualModeWorkstation> lows = getOnlyLows(dualModeWorkstations);
            flexRet.addAll(getUpFlex(profileMap, lows));
            flexRet.addAll(getDownFlex(profileMap, highs));

            return flexRet;
        }

        private Collection<? extends FlexTuple> getDownFlex(
                final LinkedListMultimap<Long, Workstation> profileMap,
                final List<DualModeWorkstation> highs) {
            return getFlex(profileMap, highs, false);
        }

        private Collection<? extends FlexTuple> getUpFlex(
                final LinkedListMultimap<Long, Workstation> profileMap,
                final List<DualModeWorkstation> lows) {
            return getFlex(profileMap, lows, true);
        }

        private Collection<? extends FlexTuple> getFlex(
                final LinkedListMultimap<Long, Workstation> profileMap,
                final List<DualModeWorkstation> lows, final boolean upFlex) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            final NPermuteAndCombiner<DualModeWorkstation> g = new NPermuteAndCombiner<>();
            final List<List<DualModeWorkstation>> combos = Lists.newArrayList();
            for (int i = 1; i <= lows.size(); i++) {
                combos.addAll(g.processSubsets(Lists.newArrayList(lows), i));
            }

            for (final List<DualModeWorkstation> options : combos) {
                flexRet.add(findFlexForCombo(options, profileMap, upFlex));
            }
            return flexRet;
        }

        FlexTuple findFlexForCombo(final List<DualModeWorkstation> options,
                final LinkedListMultimap<Long, Workstation> profileMap,
                final boolean upFlex) {
            int sumP = 0;
            double maxT = 0;
            for (final DualModeWorkstation w : options) {
                sumP += w.getHighConsumptionRate() - w.getLowConsumptionRate();
                final double currentT = w.getProcessingRate() < EPSILON
                        ? (w.getRatedCapacity() / w.getProcessingRate()) : 1;
                if (currentT > maxT) {
                    maxT = currentT;
                }
            }
            return makeDualModeFlexTuple(profileMap, sumP, maxT, upFlex,
                    options);
        }

        private static List<DualModeWorkstation> getOnlyLows(
                final List<DualModeWorkstation> dualModeWorkstations) {
            return filterByMode(dualModeWorkstations, false);
        }

        private static List<DualModeWorkstation> getOnlyHighs(
                final List<DualModeWorkstation> dualModeWorkstations) {
            return filterByMode(dualModeWorkstations, true);
        }

        private static List<DualModeWorkstation> filterByMode(
                final List<DualModeWorkstation> stations, final boolean isHigh) {
            final List<DualModeWorkstation> toRet = Lists.newArrayList();
            for (final DualModeWorkstation s : stations) {
                if (s.isHigh() == isHigh) {
                    toRet.add(s);
                }
            }
            return toRet;
        }
    }
}
