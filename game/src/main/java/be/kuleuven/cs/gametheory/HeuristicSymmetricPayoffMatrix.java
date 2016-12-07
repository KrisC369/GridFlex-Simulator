package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;

import static be.kuleuven.cs.flexsim.domain.util.ArrayUtils.arrayAdd;
import static be.kuleuven.cs.flexsim.domain.util.ArrayUtils.arrayDot;
import static be.kuleuven.cs.flexsim.domain.util.ArrayUtils.arrayScale;
import static be.kuleuven.cs.flexsim.domain.util.ArrayUtils.arraySubtract;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.fill;

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
    private final Map<PayoffEntry, Double[]> tableMean;
    private final Map<PayoffEntry, Double[]> tableVariance;
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
        int nplus1 = getEntryCount(entry) + 1;
        Double[] deltas = arraySubtract(value, tableMean.get(entry));
        this.tableMean.put(entry,
                arrayAdd(tableMean.get(entry), arrayScale(deltas, 1 / (double) nplus1)));
        Double[] deltas2 = arraySubtract(value, tableMean.get(entry));
        Double[] m2s = arrayDot(deltas, deltas2);
        this.tableVariance.put(entry, arrayAdd(tableVariance.get(entry), m2s));
        this.tableCount.put(entry, nplus1);
    }

    private void newEntry(final PayoffEntry entry, final Double[] value) {
        final Double[] toret = Arrays.copyOf(value, value.length);
        this.tableMean.put(entry, toret);
        this.tableCount.put(entry, 1);
        Double[] zeros = new Double[value.length];
        fill(zeros, 0d);
        this.tableVariance.put(entry, zeros);
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
        return tableMean.get(entry);
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
        final Double[] variance = tableVariance.get(entry);
        final Double[] toret = new Double[variance.length];
        for (int i = 0; i < variance.length; i++) {
            if (tableCount.get(entry) > 1) {
                toret[i] = variance[i] / ((double) tableCount.get(entry) - 1);
            } else {
                toret[i] = 0d;
            }
        }
        return toret;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (final Entry<PayoffEntry, Double[]> e : tableMean.entrySet()) {
            b.append("V:").append(e.getKey()).append("->")
                    .append(Arrays
                            .toString(this.getEntry(e.getKey().getEntries())))
                    .append("\n");
            b.append("C:").append(e.getKey()).append("->")
                    .append(tableCount.get(e.getKey())).append("\n");
        }
        return b.toString();
    }

    @Override
    public Spliterator<Entry<PayoffEntry, Double[]>> spliterator() {
        return tableMean.entrySet().spliterator();
    }

    @Override
    public Iterator<Entry<PayoffEntry, Double[]>> iterator() {
        return tableMean.entrySet().iterator();
    }
}