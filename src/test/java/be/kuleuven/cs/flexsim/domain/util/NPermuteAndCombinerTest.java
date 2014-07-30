package be.kuleuven.cs.flexsim.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

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
}
