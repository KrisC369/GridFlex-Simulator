package be.kuleuven.cs.flexsim.domain.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Utility class for generation of Permutation and combinations of items.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The elements that will be permuted or recombined.
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
        List<@Nullable T> subset = Lists.newArrayListWithCapacity(n);
        for (int i = 0; i < n; i++) {
            subset.add(null);
        }
        return processLargerSubsets(result, set, subset, 0, 0);
    }

    private List<List<T>> processLargerSubsets(List<List<T>> result,
            List<T> set, List<@Nullable T> subset, int subsetSize,
            int nextIndex) {
        if (subsetSize == subset.size()) {
            result.add(ImmutableList.copyOf(subset));
        } else {
            for (int j = nextIndex; j < set.size(); j++) {
                subset.set(subsetSize, set.get(j));
                processLargerSubsets(result, set, subset, subsetSize + 1,
                        j + 1);
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

    /**
     * Combines several collections of elements and create permutations of all
     * of them, taking one element from each collection, and keeping the same
     * order in resultant lists as the one in original list of collections.
     * Example: Input = { {a,b,c} , {1,2,3,4} } Output = { {a,1} , {a,2} , {a,3}
     * , {a,4} , {b,1} , {b,2} , {b,3} , {b,4} , {c,1} , {c,2} , {c,3} , {c,4} }
     *
     * @param collections
     *            Original list of collections which elements have to be
     *            combined.
     * @return Resulting collection of lists with all permutations of original
     *         list.
     * @deprecated Use Guava's Sets.carthesianProduct instead
     */
    public Collection<List<T>> permutations(List<Collection<T>> collections) {
        if (collections.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<List<T>> res = Lists.newLinkedList();
        permutationsImpl(collections, res, 0, new LinkedList<>());
        return res;
    }

    /** Recursive implementation for {@link #permutations(List, Collection)} */
    private void permutationsImpl(List<Collection<T>> ori,
            Collection<List<T>> res, int d, List<T> current) {
        // if depth equals number of original collections, final reached, add
        // and return
        if (d == ori.size()) {
            res.add(current);
            return;
        }

        // iterate from current collection and copy 'current' element N times,
        // one for each element
        @SuppressWarnings("null")
        Collection<T> currentCollection = ori.get(d);
        for (T element : currentCollection) {
            List<T> copy = Lists.newLinkedList(current);
            copy.add(element);
            permutationsImpl(ori, res, d + 1, copy);
        }
    }
}