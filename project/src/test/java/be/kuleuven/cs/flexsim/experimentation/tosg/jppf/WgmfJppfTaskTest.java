package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WindBasedInputData;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.gametheory.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.GameInstanceParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTaskTest {
    private static final int SEED = 3722;
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private WgmfJppfTask task;
    private final String PARAMS = "test";

    @Before
    public void setUp() {
        WindBasedInputData dataIn = null;
        try {
            dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);

            WgmfGameParams params = WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(
                            AbstractOptimalSolver.Solver.DUMMY), specs, distribution, imbalIn);
            GameInstanceConfiguration config = new GameInstanceConfiguration(3, 2);
            config.fixAgentToAction(0, 0);
            config.fixAgentToAction(1, 0);
            config.fixAgentToAction(2, 1);

            this.task = new WgmfJppfTask(GameInstanceParams.create(
                    config, 231L), params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRun() {
        task.run();
        GameInstanceResult result = task.getResult();
        assertEquals(0, result.getPayoffs().get(0), 0);

    }
}