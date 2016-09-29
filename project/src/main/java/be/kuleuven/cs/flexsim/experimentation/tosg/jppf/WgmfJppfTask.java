package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfAgentGenerator;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.configurable.AbstractGameInstanceConfigurator;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import com.google.common.annotations.VisibleForTesting;
import org.jppf.node.protocol.AbstractTask;

import java.util.concurrent.Callable;

/**
 * A runnable task for executing wgmf simulations.
 * Represents one single game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTask extends AbstractTask<GameInstanceResult>
        implements Callable<Object> {

    private WgmfGameParams params;
    private final GameInstanceConfiguration instanceConfig;
    private final String paramsDataKey;

    /**
     * Default constructor.
     *
     * @param instanceConfig
     * @param s              The key for which to query the data provider for the instance
     *                       parameter data.
     */
    public WgmfJppfTask(GameInstanceConfiguration instanceConfig, String s) {
        this.instanceConfig = instanceConfig;
        paramsDataKey = s;
    }

    @VisibleForTesting
    WgmfJppfTask(GameInstanceConfiguration instanceConfig, WgmfGameParams params) {
        this.instanceConfig = instanceConfig;
        this.paramsDataKey = "";
        this.params = params;
    }

    @Override
    public void run() {
        if (getDataProvider() != null) {
            params = (WgmfGameParams) getDataProvider().getParameter(paramsDataKey);
        }
        WgmfAgentGenerator configurator = new WgmfAgentGenerator(instanceConfig.getSeed());
        WhoGetsMyFlexGame gameInstance = new WhoGetsMyFlexGame(params, instanceConfig.getSeed());
        AbstractGameInstanceConfigurator.create(gameInstance)
                .configureGameInstance(configurator, instanceConfig);
        gameInstance.init();
        gameInstance.play();
        setResult(gameInstance.getGameInstanceResult());
    }

    @Override
    public GameInstanceResult call() throws Exception {
        run();
        return getResult();
    }
}
