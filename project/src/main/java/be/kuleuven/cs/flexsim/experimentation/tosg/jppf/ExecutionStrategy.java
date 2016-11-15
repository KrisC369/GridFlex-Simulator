package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.jppf.RemoteRunners;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jppf.node.protocol.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The execution strategy to use with specific case handling methods for different strategies.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
enum ExecutionStrategy {
    /**
     * Run remote on JPPF cluster.
     */
    REMOTE,
    /**
     * Run local with a multithreaded executor service.
     */
    LOCAL;

    ExperimentRunner getRunner(WgmfGameParams params, String paramString) {
        ExperimentRunner toRet;
        switch (this) {
        case REMOTE:
            Map<String, Object> data = Maps.newLinkedHashMap();
            data.put(paramString, params);
            toRet = RemoteRunners.createDefaultBlockedJPPFRunner("PocJob", data);
            break;
        case LOCAL:
            toRet = LocalRunners.createDefaultMultiThreadedRunner();
            break;
        default:
            toRet = LocalRunners.createDefaultSingleThreadedRunner();
        }
        return toRet;
    }

    <R> void processExecutionResults(List<R> results,
            ConfigurableGameDirector director) {
        switch (this) {
        case REMOTE:
            for (Task<?> result : (List<Task<?>>) results) {
                if (result.getThrowable() != null) {
                    getLogger(ExecutionStrategy.class).error(result.getThrowable().toString());
                } else {
                    director.notifyVersionHasBeenPlayed(
                            (GameInstanceResult) result.getResult());
                }
            }
            break;
        case LOCAL:
            for (Future<?> result : (List<Future<?>>) results) {
                try {
                    director.notifyVersionHasBeenPlayed((GameInstanceResult) result.get());
                } catch (InterruptedException e) {
                    getLogger(ExecutionStrategy.class)
                            .error("Experimentation got interrupted.", e);
                } catch (ExecutionException e) {
                    getLogger(ExecutionStrategy.class)
                            .error("An error occured during execution.", e);
                }
            }
            break;
        default:
        }
    }

    List<WgmfJppfTask> adapt(
            final ConfigurableGameDirector dir,
            WgmfGameParams params, String paramString, WgmfJppfTask.GameInstanceFactory factory) {
        List<WgmfJppfTask> experiments;
        switch (this) {
        case REMOTE:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : dir.getPlayableVersions()) {
                experiments.add(new WgmfJppfTask(p, paramString, factory));
            }
            break;
        case LOCAL:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : dir.getPlayableVersions()) {
                experiments.add(new WgmfJppfTask(p, params, factory));
            }
            break;
        default:
            experiments = Lists.newArrayList();
        }
        return experiments;
    }
}
