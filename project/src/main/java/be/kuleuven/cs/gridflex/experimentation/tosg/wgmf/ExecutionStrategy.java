package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
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

    <R> void processExecutionResultsFailFast(List<R> results,
            ProcessingCallback callback) {
        handleProcessing(results, callback, true);
    }

    <R> void processExecutionResultsLogErrorsOnly(List<R> results, ProcessingCallback callback) {
        handleProcessing(results, callback, false);
    }

    private <R> void handleProcessing(List<R> allResults, ProcessingCallback callback,
            boolean failfast) {
        switch (this) {
        case REMOTE:
            for (Task<?> task : (List<Task<?>>) allResults) {
                if (task.getThrowable() != null) {
                    getLogger(ExecutionStrategy.class)
                            .error("An error occured executing task:", task.getThrowable());
                    if (failfast) {
                        throw new IllegalStateException(
                                "An exception occured during task execution. The results are "
                                        + "likely "

                                        + "tainted.",
                                task.getThrowable());
                    }
                } else {
                    if (!(task instanceof RemoteTaskDecorator)) {
                        throw new IllegalStateException(
                                "Tasks should be decorated for remote use at this point.");
                    }
                    GenericTask target = ((RemoteTaskDecorator) task).getTarget();
                    callback.processResults(target.getResult());
                }
            }
            break;
        case LOCAL:
            for (Future<?> result : (List<Future<?>>) allResults) {
                try {
                    callback.processResults(result.get());
                } catch (InterruptedException e) {
                    getLogger(ExecutionStrategy.class)
                            .error("Experimentation got interrupted.", e);
                    if (failfast) {
                        throw new IllegalStateException(
                                "Exception caught. Results are likely tainted.",
                                e);
                    }
                } catch (ExecutionException e) {
                    getLogger(ExecutionStrategy.class)
                            .error("An error occured during execution.", e);
                    if (failfast) {
                        throw new IllegalStateException(
                                "An exception occured during task execution. The results are "
                                        + "likely "

                                        + "tainted.",
                                e);
                    }
                }
            }
            break;
        default:
        }
    }

    <T> List<GenericTask<T>> adapt(final List<GenericTask<T>> tasks, String paramString) {
        List<GenericTask<T>> experiments;
        switch (this) {
        case REMOTE:
            experiments = Lists.newArrayList();
            for (final GenericTask<T> task : tasks) {
                experiments.add(new RemoteTaskDecorator<T>(paramString, task));
            }
            break;
        case LOCAL:
            experiments = Lists.newArrayList();
            experiments.addAll(tasks);
            break;
        default:
            experiments = Lists.newArrayList();
        }
        return experiments;
    }

    private final ConfigurationExtractor extractor;

    ExecutionStrategy(ConfigurationExtractor ce) {
        this.extractor = ce;
    }

    @FunctionalInterface
    interface ConfigurationExtractor {
        GameInstanceConfiguration getConfig(Object extractrable)
                throws InterruptedException, ExecutionException;
    }

    @FunctionalInterface
    interface ProcessingCallback {
        void processResults(Object arg);
    }
}
