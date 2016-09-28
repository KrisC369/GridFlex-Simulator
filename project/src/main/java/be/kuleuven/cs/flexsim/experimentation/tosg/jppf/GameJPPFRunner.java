package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WindBasedInputData;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.gametheory.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.GameInstanceParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import com.google.common.collect.Lists;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.AbstractTask;
import org.jppf.node.protocol.DataProvider;
import org.jppf.node.protocol.MemoryMapDataProvider;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class GameJPPFRunner {
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    public static final String PARAMS = "PARAMS";
    private final ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> director;
    private final Logger logger = LoggerFactory.getLogger(GameJPPFRunner.class);
    private static final long SEED = 19486454L;

    public GameJPPFRunner(
            ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> director) {
        this.director = director;
    }

    public static void main(String[] args) {
        JppFWgmfGameRunner runner = JppFWgmfGameRunner.parseInputAndExec(args);
        try (JPPFClient jppfClient = new JPPFClient()) {
            ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser>
                    gameDirectorInstance = runner
                    .getGameDirectorInstance();
            GameJPPFRunner gameJPPFRunner = new GameJPPFRunner(gameDirectorInstance);
            WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);
            WgmfGameParams params = WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(AbstractOptimalSolver.Solver.DUMMY),
                            specs, distribution, imbalIn);
            gameJPPFRunner.execute(jppfClient, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(JPPFClient jppfClient, WgmfGameParams params) {
        List<AbstractTask<GameInstanceResult>> adapted = adapt(director, params);
        DataProvider dP = new MemoryMapDataProvider();
        dP.setParameter(PARAMS, params);
        JPPFJob job = new JPPFJob();
        job.setDataProvider(dP);
        adapted.forEach(p -> {
            try {
                job.add(p);
            } catch (JPPFException e) {
                e.printStackTrace();
            }
        });
        job.setName("PoC Job");
        job.setBlocking(true);
        List<Task<?>> results = jppfClient.submitJob(job);
        processExecutionResults(job.getName(), results, director);
        logger.warn(director.getFormattedResults().getFormattedResultString());
    }

    private void processExecutionResults(String name, List<Task<?>> results,
            ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> director) {
        for (Task<?> result : results) {
            if (result.getThrowable() != null) {
                System.err.println(result.getThrowable().toString());
            } else {
                director.notifyVersionHasBeenPlayed((GameInstanceResult) result.getResult());
            }
        }
    }

    private List<AbstractTask<GameInstanceResult>> adapt(
            final ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> dir,
            WgmfGameParams params) {
        final List<AbstractTask<GameInstanceResult>> experiments = Lists.newArrayList();
        for (final GameInstanceConfiguration p : dir.getPlayableVersions()) {
            experiments.add(new WgmfJppfTask(GameInstanceParams.create(p, SEED), PARAMS));
        }
        return experiments;
    }

}
