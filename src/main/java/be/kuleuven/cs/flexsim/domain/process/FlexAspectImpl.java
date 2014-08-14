/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.simulation.UIDGenerator;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.Graph;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
abstract class FlexAspectImpl<T extends Workstation> implements FlexAspect<T> {
    @Nullable
    private UIDGenerator generator;
    @Nullable
    private Graph<Buffer<Resource>, Workstation> layout;
    private LinkedListMultimap<Long, Workstation> profileMap;

    public FlexAspectImpl() {
        profileMap = LinkedListMultimap.create();
    }

    protected void resetProfile() {
        profileMap = LinkedListMultimap.create();
    }

    private FlexTuple findFlex(CurtailableWorkstation a,
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
            return makeCurtFlexTuple(false, a, cs);
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
            return makeCurtFlexTuple(false, a, cs);
        }
        return FlexTuple.createNONE();
    }

    private double calculateCurrentPhaseRate(CurtailableWorkstation c) {
        checkNotNull(this.layout,
                "Initialise this aspect first. No layout present.");
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

    private double calculatePreviousPhaseRate(CurtailableWorkstation c) {
        checkNotNull(this.layout,
                "Initialise this aspect first. No layout present.");
        Graph<Buffer<Resource>, Workstation> layout2 = layout;
        return aggregateProcessingRate(layout2.getInEdges(layout2.getSource(c)));
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
        checkNotNull(this.layout,
                "Initialise this aspect first. No layout present.");
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

    private FlexTuple makeCurtFlexTuple(boolean upflex,
            List<CurtailableWorkstation> cs) {
        if (cs.isEmpty()) {
            throw new IllegalArgumentException("No stations to create curt.");
        }
        if (cs.size() == 1) {
            return makeCurtFlexTuple(upflex, cs.get(0),
                    new CurtailableWorkstation[0]);
        }
        return makeCurtFlexTuple(upflex, cs.get(0), cs.subList(1, cs.size())
                .toArray(new CurtailableWorkstation[cs.size() - 1]));
    }

    private FlexTuple makeCurtFlexTuple(boolean upflex,
            CurtailableWorkstation a, CurtailableWorkstation... cs) {
        double sump = a.getAverageConsumption();
        checkNotNull(this.generator,
                "Initialize this aspect first. No generator present.");
        UIDGenerator gen = generator;
        for (CurtailableWorkstation c : cs) {
            sump += c.getAverageConsumption();
        }
        long id = gen.getNextUID();
        Set<Workstation> set = Sets.newLinkedHashSet();
        set.add(a);
        set.addAll(Lists.newArrayList(cs));
        return makeTuple(id, (int) Math.round(sump), set, upflex);
    }

    private FlexTuple makeTuple(long id, int deltaP,
            Iterable<Workstation> target, boolean upflex) {
        profileMap.putAll(id, target);
        return FlexTuple.create(id, deltaP, upflex, 1, 0, 0);
    }

    @Override
    public void initialize(UIDGenerator generator,
            Graph<Buffer<Resource>, Workstation> layout) {
        this.generator = generator;
        this.layout = layout;
    }

    class SingleStationDownFlex extends FlexAspectImpl<CurtailableWorkstation> {

        @Override
        public FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>> getFlexibility(
                List<CurtailableWorkstation> effectivelyCurtableStations) {
            resetProfile();
            final List<FlexTuple> flexRet = Lists.newArrayList();
            for (CurtailableWorkstation c : effectivelyCurtableStations) {
                flexRet.add(findFlex(c));
            }
            final LinkedListMultimap<Long, Workstation> profiles = profileMap;
            return new FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>>() {

                @Override
                public List<FlexTuple> getFirst() {
                    return flexRet;
                }

                @Override
                public LinkedListMultimap<Long, Workstation> getSecond() {
                    return profiles;
                }
            };
        }
    }

    class TwoStationsDownFlex extends FlexAspectImpl<CurtailableWorkstation> {

        @Override
        public FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>> getFlexibility(
                List<CurtailableWorkstation> effectivelyCurtableStations) {
            resetProfile();
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findTwoStationsFlex(effectivelyCurtableStations));
            final LinkedListMultimap<Long, Workstation> profiles = profileMap;
            return new FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>>() {

                @Override
                public List<FlexTuple> getFirst() {
                    return flexRet;
                }

                @Override
                public LinkedListMultimap<Long, Workstation> getSecond() {
                    return profiles;
                }
            };
        }

        private List<FlexTuple> findTwoStationsFlex(
                List<CurtailableWorkstation> curtailableStations) {
            List<FlexTuple> flexRet = Lists.newArrayList();
            int size = curtailableStations.size();
            for (int i = 0; i <= size - 2; i++) {
                for (int j = i + 1; j <= size - 1; j++) {
                    flexRet.add(findFlex(curtailableStations.get(i),
                            curtailableStations.get(j)));
                }
            }
            if (!flexRet.isEmpty()) {
                return flexRet;
            }
            return Lists.newArrayList(FlexTuple.NONE);
        }
    }

    class ThreeStationsDownFlex extends FlexAspectImpl<CurtailableWorkstation> {

        @Override
        public FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>> getFlexibility(
                List<CurtailableWorkstation> effectivelyCurtableStations) {
            resetProfile();
            final List<FlexTuple> flexRet = Lists.newArrayList();
            flexRet.addAll(findThreeStationFlex(effectivelyCurtableStations));
            final LinkedListMultimap<Long, Workstation> profiles = profileMap;
            return new FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>>() {

                @Override
                public List<FlexTuple> getFirst() {
                    return flexRet;
                }

                @Override
                public LinkedListMultimap<Long, Workstation> getSecond() {
                    return profiles;
                }
            };
        }

        private List<FlexTuple> findThreeStationFlex(
                List<CurtailableWorkstation> curtailableStations) {
            List<FlexTuple> flexRet = Lists.newArrayList();
            int size = curtailableStations.size();
            for (int i = 0; i < size - 2; i++) {
                for (int j = i + 1; j < size - 1; j++) {
                    for (int k = j + 1; k < size - 1; k++) {
                        flexRet.add(findFlex(curtailableStations.get(i),
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

    class UpFlex extends FlexAspectImpl<CurtailableWorkstation> {

        @Override
        public FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>> getFlexibility(
                List<CurtailableWorkstation> stations) {
            final List<FlexTuple> flexRet = Lists.newArrayList();
            List<Set<CurtailableWorkstation>> sets2 = Lists.newArrayList(Sets
                    .powerSet(Sets.newLinkedHashSet(stations)));
            sets2.remove(Sets.newLinkedHashSet());

            for (Set<CurtailableWorkstation> lc : sets2) {
                flexRet.add(makeCurtFlexTuple(true, Lists.newArrayList(lc)));
            }

            final LinkedListMultimap<Long, Workstation> profiles = profileMap;
            return new FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>>() {

                @Override
                public List<FlexTuple> getFirst() {
                    return flexRet;
                }

                @Override
                public LinkedListMultimap<Long, Workstation> getSecond() {
                    return profiles;
                }
            };
        }

    }

}
