package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.AbstractGameInstanceConfigurator;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;

import java.io.Serializable;

/**
 * A runnable task for executing wgmf simulations.
 * Represents one single game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTask extends GenericTask<GameInstanceResult> {

    private static final long serialVersionUID = -5970733781444570465L;
    private final GameInstanceConfiguration instanceConfig;
    private final GameInstanceFactory instanceFactory;

    WgmfJppfTask(GameInstanceConfiguration instanceConfig, WgmfGameParams params,
            GameInstanceFactory factory) {
        super(params);
        this.instanceConfig = instanceConfig;
        this.instanceFactory = factory;
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
