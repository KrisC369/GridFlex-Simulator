package be.kuleuven.cs.flexsim.domain.energy.tso;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.IndependentAggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.ReactiveMechanismAggregator;
import be.kuleuven.cs.flexsim.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import com.google.common.collect.Lists;

public class TSOBackwardsCompatibilityTest {

    private BalancingTSO tso1 = mock(BalancingTSO.class);
    private CopperplateTSO tso2 = mock(CopperplateTSO.class);
    private ReactiveMechanismAggregator agg1 = mock(ReactiveMechanismAggregator.class);
    private IndependentAggregator agg2 = mock(IndependentAggregator.class);
    private Simulator sim1 = Simulator.createSimulator(1);
    private Simulator sim2 = Simulator.createSimulator(1);
    private SiteSimulation site11 = mock(SiteSimulation.class);
    private SiteSimulation site12 = mock(SiteSimulation.class);
    private SiteSimulation site21 = mock(SiteSimulation.class);
    private SiteSimulation site22 = mock(SiteSimulation.class);
    private int simsteps = 20;

    @Before
    public void setUp() throws Exception {
        tso1 = new BalancingTSO();
        tso2 = new CopperplateTSO();
        tso1.registerProducer(new ConstantOutputGenerator(500));
        tso1.registerProducer(new WeighedNormalRandomOutputGenerator(-200, 200));
        tso2.registerProducer(new ConstantOutputGenerator(500));
        tso2.registerProducer(new WeighedNormalRandomOutputGenerator(-200, 200));
        agg1 = new ReactiveMechanismAggregator(tso1);
        agg2 = new IndependentAggregator(tso2, 1);
        sim1 = Simulator.createSimulator(simsteps);
        sim2 = Simulator.createSimulator(simsteps);
        site11 = new SiteSimulation(300, 100, 450, 4);
        site12 = new SiteSimulation(200, 150, 500, 4);
        site21 = new SiteSimulation(300, 100, 450, 4);
        site22 = new SiteSimulation(200, 150, 500, 4);
        tso1.registerConsumer(site11);
        tso1.registerConsumer(site12);
        tso2.registerConsumer(site21);
        tso2.registerConsumer(site22);
        agg1.registerClient(site11);
        agg1.registerClient(site12);
        agg2.registerClient(site21);
        agg2.registerClient(site22);
        sim1.register(tso1);
        sim1.register(agg1);
    }

    @Test
    public void testCompareBothStep1() {
        ComparingModule c = new ComparingModule(tso1, tso2);
        sim1.start();
        sim2.start();
        assertTrue(c.eval());
    }

    private class ComparingModule {
        List<Integer> values1;
        List<Integer> values2;

        ComparingModule(BalancingTSO tso1, CopperplateTSO tso2) {
            values1 = Lists.newArrayList();
            values2 = Lists.newArrayList();
            tso1.addNewBalanceValueListener(new Listener<Integer>() {
                @Override
                public void eventOccurred(Integer arg) {
                    values1.add(arg);

                }
            });
            tso2.addNewBalanceValueListener(new Listener<Integer>() {

                public void eventOccurred(Integer arg) {
                    values2.add(arg);
                }
            });
        }

        public boolean eval() {
            if (values1.size() != values2.size()) {
                return false;
            }
            for (int i = 0; i < values1.size(); i++) {
                if (!values1.get(i).equals(values2.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
