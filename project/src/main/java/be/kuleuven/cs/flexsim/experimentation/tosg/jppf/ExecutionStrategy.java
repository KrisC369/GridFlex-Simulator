package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.jppf.RemoteRunners;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
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
        ExperimentRunner toRet = LocalRunners.createDefaultSingleThreadedRunner();
        switch (this) {
        case REMOTE:
            Map<String, Object> data = Maps.newLinkedHashMap();
            data.put(paramString, params);
            toRet = RemoteRunners.createDefaultBlockedJPPFRunner("PocJob", data);
            break;
        case LOCAL:
            toRet = LocalRunners.createDefaultMultiThreadedRunner();
            break;
        }
        return toRet;
    }

    <R> void processExecutionResults(List<R> results,
            ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> director) {
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
        }
    }

    List<WgmfJppfTask> adapt(
            final ConfigurableGameDirector<FlexibilityProvider, FlexibilityUtiliser> dir,
            WgmfGameParams params, long seed, String paramString) {
        List<WgmfJppfTask> experiments = Lists.newArrayList();
        switch (this) {
        case REMOTE:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : dir.getPlayableVersions()) {
                experiments.add(new WgmfJppfTask(p, paramString));
            }
            break;
        case LOCAL:
            experiments = Lists.newArrayList();
            for (final GameInstanceConfiguration p : dir.getPlayableVersions()) {
                experiments.add(new WgmfJppfTask(p, params));
            }
            break;
        }
        return experiments;
    }
}
