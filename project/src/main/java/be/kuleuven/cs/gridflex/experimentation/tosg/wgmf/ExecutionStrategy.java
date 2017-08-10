package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.runners.jppf.RemoteRunners;
import be.kuleuven.cs.gridflex.experimentation.runners.local.LocalRunners;
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
    REMOTE((o) -> (GameInstanceConfiguration) ((Task<?>) o).getResult()),
    /**
     * Run local with a multithreaded executor service.
     */
    LOCAL((o) -> (GameInstanceConfiguration) ((Future<?>) o).get());

    ExperimentRunner getRunner(WgmfGameParams params, String paramString) {
        return getRunner(params, paramString, "PocJob");
    }

    ExperimentRunner getRunner(WgmfGameParams params, String paramString, String jobName) {
        ExperimentRunner toRet;
        switch (this) {
        case REMOTE:
            Map<String, Object> data = Maps.newLinkedHashMap();
            data.put(paramString, params);
            toRet = RemoteRunners.createDefaultBlockedJPPFRunner(jobName, data);
            break;
        case LOCAL:
            toRet = LocalRunners.createOSTunedMultiThreadedRunner();
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
            for (Task<?> task : (List<Task<?>>) results) {
                if (task.getThrowable() != null) {
                    getLogger(ExecutionStrategy.class)
                            .error("An error occured executing task:", task.getThrowable());
                    throw new IllegalStateException(
                            "An exception occured during task execution. The results are likely "
                                    + "tainted.",
                            task.getThrowable());
                } else {
                    director.notifyVersionHasBeenPlayed((GameInstanceResult) task.getResult());
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
                    throw new IllegalStateException("Exception caught. Results are likely tainted.",
                            e);
                } catch (ExecutionException e) {
                    getLogger(ExecutionStrategy.class)
                            .error("An error occured during execution.", e);
                    throw new IllegalStateException(
                            "An exception occured during task execution. The results are likely "
                                    + "tainted.",
                            e);

                }
            }
            break;
        default:
        }
    }

    <R> void processExecutionResults(List<R> allResults, String varKey,
            Map<Double, ConfigurableGameDirector> directors) {
        switch (this) {
        case REMOTE:
            for (Task<?> task : (List<Task<?>>) allResults) {
                if (task.getThrowable() != null) {
                    getLogger(ExecutionStrategy.class).error(task.getThrowable().toString());
                } else {
                    GameInstanceResult result = (GameInstanceResult) task.getResult();
                    Double price = result.getGameInstanceConfig().getExtraConfigValues()
                            .get(varKey);
                    directors.get(price)
                            .notifyVersionHasBeenPlayed((GameInstanceResult) task.getResult());
                }
            }
            break;
        case LOCAL:
            for (Future<?> future : (List<Future<?>>) allResults) {
                try {
                    GameInstanceResult result = (GameInstanceResult) future.get();
                    Double price = result.getGameInstanceConfig().getExtraConfigValues()
                            .get(varKey);
                    directors.get(price)
                            .notifyVersionHasBeenPlayed((GameInstanceResult) future.get());
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
            final List<GameInstanceConfiguration> playableConfigs,
            WgmfGameParams params, String paramString, WgmfJppfTask.GameInstanceFactory factory) {
        //TODO take List<GameInstanceCOnfig iso director.
        List<WgmfJppfTask> experiments;
        switch (this) {
        case REMOTE:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : playableConfigs) {
                experiments.add(new WgmfJppfTask(p, paramString, factory));
            }
            break;
        case LOCAL:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : playableConfigs) {
                experiments.add(new WgmfJppfTask(p, params, factory));
            }
            break;
        default:
            experiments = Lists.newArrayList();
        }
        return experiments;
    }

    private final ConfigurationExtractor extractor;

    private ExecutionStrategy(ConfigurationExtractor ce) {
        this.extractor = ce;
    }

    @FunctionalInterface
    interface ConfigurationExtractor {
        GameInstanceConfiguration getConfig(Object extractrable)
                throws InterruptedException, ExecutionException;
    }
}
