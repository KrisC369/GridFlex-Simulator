package be.kuleuven.cs.flexsim.domain.energy.generation;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OutputGeneratorTest {
    private EnergyProductionTrackable gen = mock(ConstantOutputGenerator.class);
    private final int BASE = 200;
    private final double RAND = 0.42;
    private final int MAX = 50;
    private final int MIN = 0;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        gen = new ConstantOutputGenerator(BASE);
    }

    @Test
    public void testConstant() {
        assertEquals(BASE, gen.getLastStepProduction(), 0);
        assertEquals(0, gen.getTotalProduction(), 0);
        gen.tick(0);
        gen.afterTick(0);
        assertEquals(BASE, gen.getTotalProduction(), 0);
    }

    @Test
    public void testNormal() {
        RandomGenerator r = mock(RandomGenerator.class);
        Mockito.when(r.nextDouble()).thenReturn(RAND);
        gen = new NormalRandomOutputGenerator(0, MAX, r);
        assertEquals(0, gen.getLastStepProduction(), 0);
        assertEquals(0, gen.getTotalProduction(), 0);
        gen.tick(0);
        gen.afterTick(0);
        assertEquals(MIN, gen.getLastStepProduction(), 0);
        assertEquals(0, gen.getTotalProduction(), 0);
        Mockito.when(r.nextDouble()).thenReturn(RAND * 2);
        gen.tick(0);
        gen.afterTick(0);
        assertEquals(MAX, gen.getLastStepProduction(), MAX / 2);
        assertEquals(gen.getLastStepProduction(), gen.getTotalProduction(), 0);
    }

    @Test
    public void testWeighedRandomWeightIs1() {
        int expected = 29;
        RandomGenerator r = mock(RandomGenerator.class);
        Mockito.when(r.nextDouble()).thenReturn(RAND);
        gen = new NormalRandomOutputGenerator(0, MAX, r);
        EnergyProductionTrackable gen2 = new WeighedNormalRandomOutputGenerator(
                0, MAX, r, 1);
        gen.tick(0);
        gen.afterTick(0);
        gen2.tick(0);
        gen.afterTick(0);
        Mockito.when(r.nextDouble()).thenReturn(RAND * 2);
        gen.tick(0);
        gen.afterTick(0);
        gen2.tick(0);
        gen2.afterTick(0);
        assertEquals(expected, gen.getLastStepProduction(), 0);
        assertEquals(expected, gen2.getLastStepProduction(), 0);
        assertEquals(gen2.getLastStepProduction(), gen.getLastStepProduction(),
                0);
    }

    @Test
    public void testWeighedRandomWeightIsNot1() {
        RandomGenerator r = mock(RandomGenerator.class);
        Mockito.when(r.nextDouble()).thenReturn(RAND);
        gen = new NormalRandomOutputGenerator(0, MAX, r);
        EnergyProductionTrackable gen2 = new WeighedNormalRandomOutputGenerator(
                0, MAX, r, 0.5);
        gen.tick(0);
        gen.afterTick(0);
        gen2.tick(0);
        gen.afterTick(0);
        Mockito.when(r.nextDouble()).thenReturn(RAND * 2);
        gen.tick(0);
        gen.afterTick(0);
        gen2.tick(0);
        gen.afterTick(0);
        assertNotEquals(gen2.getLastStepProduction(),
                gen.getLastStepProduction());
        assertTrue(gen2.getLastStepProduction() < RAND * 2);
    }

    @Test
    public void testInit1() {
        RandomGenerator r = mock(RandomGenerator.class);
        gen = new NormalRandomOutputGenerator(0, MAX, r);
        gen = new WeighedNormalRandomOutputGenerator(0, 15, r);
        thrown.expect(IllegalArgumentException.class);
        gen = new WeighedNormalRandomOutputGenerator(0, 15, r, 2);
    }

    @Test
    public void testInit2() {
        RandomGenerator r = mock(RandomGenerator.class);
        gen = new NormalRandomOutputGenerator(0, MAX, r);
        gen = new WeighedNormalRandomOutputGenerator(0, 15, r);
        thrown.expect(IllegalArgumentException.class);
        gen = new WeighedNormalRandomOutputGenerator(0, 15, r, -2);
    }

    @Test
    public void testInit3() {
        RandomGenerator r = mock(RandomGenerator.class);
        thrown.expect(IllegalArgumentException.class);
        gen = new NormalRandomOutputGenerator(5, MAX, r);
    }

    @Test
    public void testInit4() {
        RandomGenerator r = mock(RandomGenerator.class);
        thrown.expect(IllegalArgumentException.class);
        gen = new NormalRandomOutputGenerator(-5, -4, r);
    }
}
