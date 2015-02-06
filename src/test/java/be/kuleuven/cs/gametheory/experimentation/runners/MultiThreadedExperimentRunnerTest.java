package be.kuleuven.cs.gametheory.experimentation.runners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class MultiThreadedExperimentRunnerTest {

    private RunnableExperiment expmock = mock(RunnableExperiment.class);
    private MultiThreadedExperimentRunner runner = mock(MultiThreadedExperimentRunner.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMultiThreadedExperimentRunnerRunnableExperimentInt() {
        List<Double> list = Lists.newArrayList();
        runner = new MultiThreadedExperimentRunner(new ExpTester(list), 4, 0.05);
        runner.runExperiments();
        blockWait(runner);
        assertEquals(21, list.size());
        int count = 0;
        for (double i = 0; i <= 1.0; i += 0.05) {
            assertTrue(containsAprox(list, i, 0.01));
        }
    }

    private boolean containsAprox(List<Double> list, double elem, double error) {
        for (Double d : list) {
            if (Math.abs(d - elem) < error) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSingleThreadedExperimentRunner() {
        List<Double> list = Lists.newArrayList();
        runner = new SingleThreadedExperimentRunner(new ExpTester(list), 0.2);
        runner.runExperiments();
        blockWait(runner);
        assertEquals(6, list.size());
        int count = 0;
        for (float i = 0; i <= 1.0; i += 0.2) {
            assertEquals(i, list.get(count++), 0.2);
        }
    }

    @Test
    public void testSingleThreadedExperimentRunner2() {
        List<Double> list = Lists.newArrayList();
        runner = new SingleThreadedExperimentRunner(new ExpTester(list));
        runner.runExperiments();
        blockWait(runner);
        assertEquals(11, list.size());
        int count = 0;
        for (float i = 0; i <= 1.0; i += 0.1) {
            assertEquals(i, list.get(count++), 0.1);
        }
    }

    private void blockWait(MultiThreadedExperimentRunner runner) {
        while (runner.hasThreadsRunning()) {
            sleep(600);
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class ExpTester implements RunnableExperiment {
        private List<Double> list;

        public ExpTester(List<Double> list) {
            this.list = list;
        }

        @Override
        public void doExperimentRun(double varParam) {
            list.add(varParam);
        }
    }

}
