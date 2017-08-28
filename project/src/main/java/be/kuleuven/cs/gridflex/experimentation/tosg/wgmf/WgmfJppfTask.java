package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.AbstractGameInstanceConfigurator;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;
import com.google.common.annotations.VisibleForTesting;

import java.io.Serializable;

/**
 * A runnable task for executing wgmf simulations.
 * Represents one single game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTask extends GenericTask<GameInstanceResult> {

    private static final long serialVersionUID = -5970733781444570465L;
    //    private WgmfGameParams params;
    private final GameInstanceConfiguration instanceConfig;
    //    private final String paramsDataKey;
    private final GameInstanceFactory instanceFactory;

    //    /**
    //     * Default constructor.
    //     *
    //     * @param instanceConfig The instance config value for this task.
    //     * @param s              The key for which to query the data provider for the instance
    //     *                       parameter data.
    //     */
    //    public WgmfJppfTask(GameInstanceConfiguration instanceConfig, String s,
    //            GameInstanceFactory factory) {
    //        this.instanceConfig = instanceConfig;
    //        paramsDataKey = s;
    //        instanceFactory = factory;
    //    }

    @VisibleForTesting
    WgmfJppfTask(GameInstanceConfiguration instanceConfig, WgmfGameParams params,
            GameInstanceFactory factory) {
        super(params);
        this.instanceConfig = instanceConfig;
        this.instanceFactory = factory;
        //        this.paramsDataKey = "";
        //        this.params = params;
    }

    @Override
    public void run() {
        WgmfAgentGenerator configurator = new WgmfAgentGenerator(instanceConfig.getSeed(),
                getParams().getActivationConstraints());
        WhoGetsMyFlexGame gameInstance = instanceFactory
                .createGameInstance(getParams(), instanceConfig);
        AbstractGameInstanceConfigurator.create(gameInstance)
                .configureGameInstance(configurator, instanceConfig);
        gameInstance.init();
        gameInstance.play();
        setResult(GameInstanceResult
                .create(instanceConfig, gameInstance.getGameInstanceResult().getPayoffs(),
                        gameInstance.getExternalityValue()));
    }

    @Override
    public GameInstanceResult call() throws Exception {
        run();
        return getResult();
    }

    @FunctionalInterface
    interface GameInstanceFactory extends Serializable {
        WhoGetsMyFlexGame createGameInstance(WgmfGameParams params,
                GameInstanceConfiguration instanceConfig);
    }
}
