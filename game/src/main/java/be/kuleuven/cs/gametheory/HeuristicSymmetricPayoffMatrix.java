package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class HeuristicSymmetricPayoffMatrix {
    private final int agents;
    private final int actions;
    private final Map<PayoffEntry, Double[]> table;
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
        this.table = Maps.newLinkedHashMap();
        this.tableCount = Maps.newLinkedHashMap();
        this.numberOfCombinations = MathUtils.multiCombinationSize(actions,
                agents);
    }

    /**
     * Returns true if every space in the table is filled in with a value.
     *
     * @return true if every entry has a value.
     */
    public boolean isComplete() {
        final long possibilities = getNumberOfPossibilities();
        if (this.table.size() != possibilities) {
            return false;
        }
        return true;
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
            plusEntry(entry, value);
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
        if (count != agents) {
            return false;
        }
        return true;
    }

    private boolean testValues(final Double[] value) {
        if (value.length != agents) {
            return false;
        }
        return true;
    }

    private void plusEntry(final PayoffEntry entry, final Double[] value) {
        this.table.put(entry, arrayAdd(table.get(entry), value));
        this.tableCount.put(entry, tableCount.get(entry) + 1);
    }

    private Double[] arrayAdd(final Double[] first, final Double[] second) {
        final Double[] toret = new Double[first.length];
        for (int i = 0; i < first.length; i++) {
            toret[i] = first[i] + second[i];
        }
        return toret;
    }

    private void newEntry(final PayoffEntry entry, final Double[] value) {
        final Double[] toret = new Double[value.length];
        for (int i = 0; i < value.length; i++) {
            toret[i] = value[i];
        }
        this.table.put(entry, toret);
        this.tableCount.put(entry, 1);
    }

    private int getEntryCount(final PayoffEntry entry) {
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
        final Double[] sums = table.get(entry);
        final Double[] toret = new Double[sums.length];
        for (int i = 0; i < sums.length; i++) {
            toret[i] = sums[i] / (double) tableCount.get(entry);
        }
        return toret;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (final Entry<PayoffEntry, Double[]> e : table.entrySet()) {
            b.append("V:").append(e.getKey()).append("->")
                    .append(Arrays
                            .toString(this.getEntry(e.getKey().getEntries())))
                    .append("\n");
            b.append("C:").append(e.getKey()).append("->")
                    .append(tableCount.get(e.getKey())).append("\n");
        }
        return b.toString();
    }

    /**
     * Generate all unique coefficients that are used for specifying dynamics
     * equations.
     *
     * @return A list of coefficients.
     */
    // TODO Refactor out this analysis specific data computation + calculate
    // other specs in the refactored out module.
    public List<Double> getDynamicEquationFactors() {
        final List<Double> toReturn = Lists.newArrayList();
        for (final Entry<PayoffEntry, Double[]> e : table.entrySet()) {
            final PayoffEntry entry = e.getKey();
            final Double[] values = getEntry(e.getKey().getEntries());
            int coeffDone = 0;
            for (int i = 0; i < entry.getEntries().length; i++) {
                final int currCoeff = entry.getEntries()[i];
                long sum = 0;
                for (int j = coeffDone; j < coeffDone + currCoeff; j++) {
                    sum += values[j];
                }
                if (currCoeff > 0) {
                    final double avg = sum / (double) currCoeff;
                    toReturn.add(avg);
                }
                coeffDone += currCoeff;
            }
        }
        return toReturn;
    }
}
