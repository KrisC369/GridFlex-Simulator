package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfInputParser;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableDistributionCostsTest {
    private static final String DISTRIBUTIONFILE = "windspeedDistributionsNormalized.csv";
    private static final String DATAFILE = "test.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices_short.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final int HORIZON = 7;

    private ExperimentParams experimentParams;
    private WgmfGameRunnerVariableDistributionCosts runner;

    @Before
    public void setUp() throws Exception {
        experimentParams = WgmfInputParser.parseInputAndExec(new String[] {
                "-n", "2", "-r", "1", "-s", "DUMMY", "-m", "LOCAL", "-p1start", "35.4", "-p1step",
                "10", "-p1end", "61.5" });
        runner = new WgmfGameRunnerVariableDistributionCosts(experimentParams);
    }

    @Test
    public void main() throws Exception {
        runner.execute(loadResources(experimentParams));
    }

    public static WgmfGameParams loadResources(ExperimentParams expP) {
        try {
            WindBasedInputData dataIn = WindBasedInputData
                    .loadFromResource(DATAFILE, "test", "test");
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, HORIZON);
            return WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(expP.getSolver()), specs, distribution,
                            imbalIn, dayAheadPriceProfile);
        } catch (IOException e) {
            throw new IllegalStateException("One of the resources could not be loaded.", e);
        }
    }
}