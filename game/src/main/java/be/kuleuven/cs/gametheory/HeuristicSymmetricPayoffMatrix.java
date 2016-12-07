package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import com.google.common.collect.Maps;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class represents heuristic payoff tables or matrices. The heuristic part
 * stems from the experimentally gathered values in this table. This table is
 * meant to be filled in with experimentation results. A table consists out of
 * entries for every combination of agents over the action space. Adding entries
 * with already-present keys will sum the values. Retrieving a value pertaining
 * to a certain key will divide it by the amount of entries, therefore
 * effectively returning an average of the values entered with the corresponding
 * key.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class HeuristicSymmetricPayoffMatrix implements Iterable<Entry<PayoffEntry, Double[]>> {
    private final int agents;
    private final int actions;
    private final Map<PayoffEntry, Mean[]> tableMean;
    private final Map<PayoffEntry, Variance[]> tableVariance;
    private final Map<PayoffEntry, Integer> tableCount;
    private final long numberOfCombinations;

    /**
     * Default constructor using the dimensions of the table. and having only
     * all multicombinations as entries.
     *
     * @param agents  the amount of agents.
     * @param actions the amount of actions.
     */
    public HeuristicSymmetricPayoffMatrix(final int agents, final int actions) {
        this.agents = agents;
        this.actions = actions;
        this.tableMean = Maps.newLinkedHashMap();
        this.tableVariance = Maps.newLinkedHashMap();
        this.tableCount = Maps.newLinkedHashMap();
        this.numberOfCombinations = MathUtils.multiCombinationSize(actions,
                agents);
    }

    /**
     * Returns true if every space in the tableMean is filled in with a value.
     *
     * @return true if every entry has a value.
     */
    public boolean isComplete() {
        return this.tableMean.size() == getNumberOfPossibilities();
    }

    private long getNumberOfPossibilities() {
        return numberOfCombinations;
    }

    /**
     * Adds a new entry to this payoff matrix.
     *
     * @param value [] The payoff values.
     * @param key   The population shares as indices for the value
     */
    public void addEntry(final Double[] value, final int... key) {
        checkArgument(testKey(key));
        checkArgument(testValues(value));
        final PayoffEntry entry = PayoffEntry.from(key);
        if (getEntryCount(entry) == 0) {
            newEntry(entry, value);
        } else {
            updateEntry(entry, value);
        }
    }

    private boolean testKey(final int[] key) {
        if (key.length != actions) {
            return false;
        }
        int count = 0;
        for (final int i : key) {
            count += i;
        }
        return count == agents;
    }

    private boolean testValues(final Double[] value) {
        return value.length == agents;
    }

    private void updateEntry(final PayoffEntry entry, final Double[] value) {
        for (int i = 0; i < value.length; i++) {
            tableMean.get(entry)[i].increment(value[i]);
            tableVariance.get(entry)[i].increment(value[i]);
        }
        int nplus1 = getEntryCount(entry) + 1;
        this.tableCount.put(entry, nplus1);
    }

    private void newEntry(final PayoffEntry entry, final Double[] value) {
        final Mean[] means = new Mean[value.length];
        final Variance[] vars = new Variance[value.length];
        for (int i = 0; i < value.length; i++) {
            means[i] = new Mean();
            means[i].increment(value[i]);
            vars[i] = new Variance();
            vars[i].increment(value[i]);
        }
        this.tableMean.put(entry, means);
        this.tableVariance.put(entry, vars);
        this.tableCount.put(entry, 1);
    }

    public int getEntryCount(final PayoffEntry entry) {
        if (this.tableCount.containsKey(entry)) {
            return this.tableCount.get(entry);
        }
        return 0;
    }

    /**
     * Returns an entry in the payoff matrix.
     *
     * @param key the index keys.
     * @return the value recorded in the matrix.
     */
    public Double[] getEntry(final int... key) {
        checkArgument(testKey(key));
        final PayoffEntry entry = PayoffEntry.from(key);
        checkArgument(tableCount.containsKey(entry));
        Mean[] meen = tableMean.get(entry);
        Double[] toRet = new Double[meen.length];
        for (int i = 0; i < meen.length; i++) {
            toRet[i] = meen[i].getResult();
        }
        return toRet;
    }

    /**
     * Returns an entry in the payoff matrix.
     *
     * @param key the index keys.
     * @return the value recorded in the matrix.
     */
    public Double[] getVariance(final int... key) {
        checkArgument(testKey(key));
        final PayoffEntry entry = PayoffEntry.from(key);
        checkArgument(tableCount.containsKey(entry));
        Variance[] vars = tableVariance.get(entry);
        Double[] toRet = new Double[vars.length];
        for (int i = 0; i < vars.length; i++) {
            toRet[i] = vars[i].getResult();
        }
        return toRet;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (final Entry<PayoffEntry, Double[]> e : this) {
            b.append("V:").append(e.getKey()).append("->")
                    .append(Arrays.toString(this.getEntry(e.getKey().getEntries()))).append("\n");
            b.append("C:").append(e.getKey()).append("->").append(tableCount.get(e.getKey()))
                    .append("\n");
        }
        return b.toString();
    }

    @Override
    public Spliterator<Entry<PayoffEntry, Double[]>> spliterator() {
        Map<PayoffEntry, Double[]> toRet = Maps.newLinkedHashMap();
        tableMean.entrySet()
                .forEach((e) -> toRet.put(e.getKey(), getEntry(e.getKey().getEntries())));
        return toRet.entrySet().spliterator();
    }

    @Override
    public Iterator<Entry<PayoffEntry, Double[]>> iterator() {
        Map<PayoffEntry, Double[]> toRet = Maps.newLinkedHashMap();
        tableMean.entrySet()
                .forEach((e) -> toRet.put(e.getKey(), getEntry(e.getKey().getEntries())));
        return toRet.entrySet().iterator();
    }
}