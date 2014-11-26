package be.kuleuven.cs.flexsim.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NPermuteAndCombinerTest {

    NPermuteAndCombiner<String> g = new NPermuteAndCombiner<String>();
    List<String> f = Lists.newArrayList();;

    @Before
    public void setUp() throws Exception {
        f = Lists.newArrayList("A", "B", "C", "D");
    }

    @Test
    public void testCombination1() {
        List<List<String>> result = g.processSubsets(f, 1);
        assertTrue(result.contains(Lists.newArrayList("A")));
        assertTrue(result.contains(Lists.newArrayList("C")));
        assertTrue(result.contains(Lists.newArrayList("D")));
        assertTrue(result.contains(Lists.newArrayList("B")));
        assertEquals(4, result.size(), 0);
    }

    @Test
    public void testCombination2() {
        List<List<String>> result = g.processSubsets(f, 2);
        assertTrue(result.contains(Lists.newArrayList("A", "B")));
        assertTrue(result.contains(Lists.newArrayList("A", "C")));
        assertTrue(result.contains(Lists.newArrayList("A", "D")));
        assertTrue(result.contains(Lists.newArrayList("B", "C")));
        assertTrue(result.contains(Lists.newArrayList("B", "D")));
        assertTrue(result.contains(Lists.newArrayList("C", "D")));

        assertEquals(6, result.size(), 0);
    }

    @Test
    public void testLargerSizeInput() {
        f = Lists.newArrayList("A", "B");
        List<List<String>> result = g.processSubsets(f, 4);
        List<List<String>> result2 = g.processSubsets(f, 2);
        assertEquals(result, result2);
    }

    @Test
    public void testPermutation1() {
        Collection<List<String>> result = g.permutations(f, 1);
        assertTrue(result.contains(Lists.newArrayList("A")));
        assertTrue(result.contains(Lists.newArrayList("C")));
        assertTrue(result.contains(Lists.newArrayList("D")));
        assertTrue(result.contains(Lists.newArrayList("B")));
        assertEquals(4, result.size(), 0);
    }

    @Test
    public void testPermutation2() {
        Collection<List<String>> result = g.permutations(f, 2);
        assertTrue(result.contains(Lists.newArrayList("A", "B")));
        assertTrue(result.contains(Lists.newArrayList("A", "C")));
        assertTrue(result.contains(Lists.newArrayList("A", "D")));
        assertTrue(result.contains(Lists.newArrayList("B", "C")));
        assertTrue(result.contains(Lists.newArrayList("B", "D")));
        assertTrue(result.contains(Lists.newArrayList("C", "D")));
        assertTrue(result.contains(Lists.newArrayList("B", "A")));
        assertTrue(result.contains(Lists.newArrayList("C", "A")));
        assertTrue(result.contains(Lists.newArrayList("D", "A")));
        assertTrue(result.contains(Lists.newArrayList("C", "B")));
        assertTrue(result.contains(Lists.newArrayList("D", "B")));
        assertTrue(result.contains(Lists.newArrayList("D", "C")));
        assertEquals(12, result.size(), 0);
    }

    @Test
    public void testPermutationN() {
        Collection<List<String>> result = g.permutations(f, 4);
        assertTrue(Collections2.permutations(f).containsAll(result));
    }

    @Test
    public void testPermutationLargerThanN() {
        Collection<List<String>> result = g.permutations(f, 6);
        Collection<List<String>> result2 = g.permutations(f, 4);
        assertEquals(result, result2);
    }

    @Test
    public void testCombiner() {
        Collection<String> k = Lists.newArrayList("X", "Y", "Z");
        Collection<String> ff = f;
        Collection<List<String>> result = g.permutations(Lists.newArrayList(ff,
                k));
        assertTrue(result.contains(Lists.newArrayList("A", "X")));
        assertTrue(result.contains(Lists.newArrayList("A", "Y")));
        assertTrue(result.contains(Lists.newArrayList("A", "Z")));
        assertTrue(result.contains(Lists.newArrayList("B", "X")));
        assertTrue(result.contains(Lists.newArrayList("B", "Y")));
        assertTrue(result.contains(Lists.newArrayList("B", "Z")));
        assertTrue(result.contains(Lists.newArrayList("C", "X")));
        assertTrue(result.contains(Lists.newArrayList("C", "Y")));
        assertTrue(result.contains(Lists.newArrayList("C", "Z")));
        assertTrue(result.contains(Lists.newArrayList("D", "X")));
        assertTrue(result.contains(Lists.newArrayList("D", "Y")));
        assertTrue(result.contains(Lists.newArrayList("D", "Z")));
    }

    @Test
    public void testGuavaEquivalence() {
        Collection<String> k = Lists.newArrayList("X", "Y", "Z");
        Set<String> f1 = Sets.newLinkedHashSet(k);
        Set<String> ff = Sets.newLinkedHashSet(f);
        Collection<List<String>> result = g.permutations(Lists.newArrayList(ff,
                k));
        Set<List<String>> result2 = Sets.cartesianProduct(Lists.newArrayList(
                ff, f1));
        // System.out.println(result);
        // System.out.println(result2);
        assertTrue(result.containsAll(result2));
        assertTrue(result2.containsAll(result));
    }

    @Test
    public void testGuavaPowerset() {

        List<List<String>> result7 = Lists.newArrayList();
        for (int i = 1; i <= f.size(); i++) {
            result7.addAll(g.processSubsets(f, i));
        }
        List<Set<String>> result8 = Lists.newArrayList(Sets.powerSet(Sets
                .newLinkedHashSet(f)));
        result8.remove(Sets.newLinkedHashSet());
        // System.out.println(result7);
        // System.out.println(result8);
        List<List<String>> result9 = Lists.newArrayList();
        for (Set<String> s : result8) {
            result9.add(Lists.newArrayList(s));
        }
        assertTrue(result7.containsAll(result9));
        assertTrue(result9.containsAll(result7));
    }

    @Test
    public void testCombinationSize() {
        int k = 3, n = 20;
        int result = 231;
        assertEquals(MathUtils.multiCombinationSize(k, n), result, 0);
        k = 3;
        n = 6;
        result = 28;
        assertEquals(MathUtils.multiCombinationSize(k, n), result, 0);
        k = 3;
        n = 8;
        result = 45;
        assertEquals(MathUtils.multiCombinationSize(k, n), result, 0);
        k = 3;
        n = 10;
        result = 66;
        assertEquals(MathUtils.multiCombinationSize(k, n), result, 0);
        k = 6;
        n = 4;
        result = 126;
        assertEquals(MathUtils.multiCombinationSize(k, n), result, 0);

        ICombinatoricsVector<String> initialVector = Factory
                .createVector(new String[] { "a", "b", "c", "d", "e", "f", });
        Generator<String> gen = Factory.createMultiCombinationGenerator(
                initialVector, n);

        assertEquals(MathUtils.multiCombinationSize(k, n),
                gen.getNumberOfGeneratedObjects(), 0);
    }
}
