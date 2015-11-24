package be.kuleuven.cs.flexsim.experimentation.runners.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;

public class MultiThreadedExperimentRunnerTest {

    private ExperimentRunner runner = mock(MultiThreadedExperimentRunner.class);
    private List<ExperimentAtom> experiments = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        experiments = Lists.newArrayList();
    }

    @Test
    public void testMultiThreadedExperimentRunnerRunnableExperimentInt() {
        List<Double> list = Lists.newCopyOnWriteArrayList();
        runner = LocalRunners.createDefaultMultiThreadedRunner();
        // Damn float and double inaccuracies.
        for (int i = 0; i <= 20; i += 1) {
            ExpTester tester = new ExpTester(list);
            tester.doExperimentRun(i);
            experiments.add(tester);
        }
        runner.runExperiments(experiments);
        blockWait(runner);
        assertEquals(21, list.size());
        for (int i = 0; i <= 20; i += 1) {
            assertTrue(containsAprox(list, i, 0.01));
        }
    }

    private boolean containsAprox(List<Double> list, double elem,
            double error) {
        for (Double d : list) {
            if (Math.abs(d - elem) < error) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSingleThreadedExperimentRunner() {
        List<Double> list = Lists.newCopyOnWriteArrayList();
        runner = LocalRunners.createDefaultSingleThreadedRunner();
        for (double i = 0; i <= 1.0; i += 0.2) {
            ExpTester tester = new ExpTester(list);
            tester.doExperimentRun(i);
            experiments.add(tester);

        }
        runner.runExperiments(experiments);
        blockWait(runner);
        assertEquals(6, list.size());
        for (float i = 0; i <= 1.0; i += 0.2) {
            assertTrue(containsAprox(list, i, 0.01));
        }
    }

    @Test
    public void testSingleThreadedExperimentRunner2() {
        List<Double> list = Lists.newCopyOnWriteArrayList();
        runner = LocalRunners.createCustomMultiThreadedRunner(2);
        for (double i = 0; i <= 1.0; i += 0.1) {
            ExpTester tester = new ExpTester(list);
            tester.doExperimentRun(i);
            experiments.add(tester);

        }
        runner.runExperiments(experiments);
        blockWait(runner);
        assertEquals(11, list.size());
        for (float i = 0; i <= 1.0; i += 0.1) {
            assertTrue(containsAprox(list, i, 0.01));
        }
    }

    private void blockWait(ExperimentRunner runner) {
        while (runner.isRunning()) {
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

    class ExpTester extends ExperimentAtomImpl {
        private List<Double> list;
        private double target;

        public ExpTester(List<Double> list) {
            super();
            this.list = list;
        }

        public void doExperimentRun(double varParam) {
            this.target = varParam;
        }

        @Override
        protected void execute() {
            list.add(target);
        }
    }

}
