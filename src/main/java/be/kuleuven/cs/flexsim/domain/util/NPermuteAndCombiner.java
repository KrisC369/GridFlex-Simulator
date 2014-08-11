package be.kuleuven.cs.flexsim.domain.util;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Utility class for generation of Permutation and combinations of items.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The elements that will be permuted or recombined.
 *
 */
public class NPermuteAndCombiner<T> {

    /**
     * Process a list and partition it in all unique k-size combinations.
     * 
     * @param set
     *            the source set of items.
     * @param k
     *            the size of the combinations
     * @return A list of combinations of k.
     */
    public List<List<T>> processSubsets(List<T> set, int k) {
        int n;
        if (k > set.size()) {
            n = set.size();
        } else {
            n = k;
        }
        List<List<T>> result = Lists.newArrayList();
        List<T> subset = Lists.newArrayListWithCapacity(n);
        for (int i = 0; i < n; i++) {
            subset.add(null);
        }
        return processLargerSubsets(result, set, subset, 0, 0);
    }

    private List<List<T>> processLargerSubsets(List<List<T>> result,
            List<T> set, List<T> subset, int subsetSize, int nextIndex) {
        if (subsetSize == subset.size()) {
            result.add(ImmutableList.copyOf(subset));
        } else {
            for (int j = nextIndex; j < set.size(); j++) {
                subset.set(subsetSize, set.get(j));
                processLargerSubsets(result, set, subset, subsetSize + 1, j + 1);
            }
        }
        return result;
    }

    /**
     * Creates all k-size permutations of the input list.
     * 
     * @param list
     *            The input list.
     * @param n
     *            the size of the permutations
     * @return a collection of permutations.
     */
    public Collection<List<T>> permutations(List<T> list, int n) {
        Collection<List<T>> all = Lists.newArrayList();
        int size = n;
        if (list.size() < size) {
            size = list.size();
        }
        if (list.size() == size) {
            all.addAll(Collections2.permutations(list));
        } else {
            for (List<T> p : processSubsets(list, size)) {
                all.addAll(Collections2.permutations(p));
            }
        }
        return all;
    }
}