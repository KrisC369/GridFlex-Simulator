package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfConfigurator;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.gametheory.GameInstanceParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.configurable.AbstractGameInstanceConfigurator;
import com.google.common.annotations.VisibleForTesting;
import org.jppf.node.protocol.AbstractTask;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTask extends AbstractTask<GameInstanceResult> {

    private WgmfGameParams params;
    private final GameInstanceParams instanceConfig;
    private final String paramsDataKey;

    public WgmfJppfTask(GameInstanceParams instanceConfig, String s) {
        this.instanceConfig = instanceConfig;
        paramsDataKey = s;
    }

    @VisibleForTesting
    WgmfJppfTask(GameInstanceParams instanceConfig, WgmfGameParams s) {
        this.instanceConfig = instanceConfig;
        paramsDataKey = "";
        this.params = s;
    }

    public void run() {
        if (getDataProvider() != null) {
            params = (WgmfGameParams) getDataProvider().getParameter(paramsDataKey);
        }
        WgmfConfigurator configurator = new WgmfConfigurator(params);
        WhoGetsMyFlexGame gameInstance = new WhoGetsMyFlexGame(params, instanceConfig.getSeed());
        AbstractGameInstanceConfigurator.create(gameInstance)
                .configureGameInstance(configurator, instanceConfig.getGameInstanceConfiguration());
        gameInstance.init();
        gameInstance.play();
        setResult(gameInstance.getGameInstanceResult());
    }
}
