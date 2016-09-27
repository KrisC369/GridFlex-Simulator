package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.gametheory.GameInstanceParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.JPPFGameDirector;
import com.google.common.collect.Lists;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.AbstractTask;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class GameJPPFRunner {
    private final JPPFGameDirector<FlexibilityProvider, FlexibilityUtiliser> director;
    private final Logger logger = LoggerFactory.getLogger(GameJPPFRunner.class);

    public GameJPPFRunner(JPPFGameDirector<FlexibilityProvider, FlexibilityUtiliser> director) {
        this.director = director;
    }

    public static void main(String[] args) {
        JppFWgmfGameRunner runner = JppFWgmfGameRunner.parseInputAndExec(args);
        try (JPPFClient jppfClient = new JPPFClient()) {
            JPPFGameDirector<FlexibilityProvider, FlexibilityUtiliser> gameDirectorInstance = runner
                    .getGameDirectorInstance();
            GameJPPFRunner gameJPPFRunner = new GameJPPFRunner(gameDirectorInstance);
            gameJPPFRunner.execute(jppfClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(JPPFClient jppfClient) {
        List<AbstractTask<GameInstanceResult>> adapted = adapt(director, );
        JPPFJob job = new JPPFJob();
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
    }

    private void processExecutionResults(String name, List<Task<?>> results,
            JPPFGameDirector<FlexibilityProvider, FlexibilityUtiliser> director) {
        for (Task<?> result : results) {
            if (result.getThrowable() != null) {
                System.err.println(result.getThrowable().toString());
            } else {
                director.notifyVersionHasBeenPlayed((GameInstanceResult) result.getResult());
            }
        }
    }

    private List<AbstractTask<GameInstanceResult>> adapt(
            final JPPFGameDirector<FlexibilityProvider, FlexibilityUtiliser> dir,
            WgmfGameParams params) {
        final List<AbstractTask<GameInstanceResult>> experiments = Lists.newArrayList();
        for (final GameInstanceParams p : dir.getPlayableAbstractInstanceVersions()) {
            experiments.add(new WgmfJppfTask(params, p));
        }
        return experiments;
    }

}
