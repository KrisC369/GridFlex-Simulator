package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.WindErrorGenerator;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfConfigurator;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.gametheory.GameInstanceParams;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import com.google.common.collect.Lists;
import org.jppf.node.protocol.AbstractTask;

import java.util.List;
import java.util.Map;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTask extends AbstractTask<GameInstanceResult> {

    private final WgmfGameParams params;
    private GameInstanceParams instanceConfig;

    public WgmfJppfTask(WgmfGameParams params, GameInstanceParams instanceConfig) {
        this.params = params;
        this.instanceConfig = instanceConfig;
    }

    public void run() {

        WgmfConfigurator configurator = new WgmfConfigurator(
                WgmfGameParams
                        .create(params.getInputData(), params.getFactory(), params.getSpecs(),
                                params.getDistribution(), params.getImbalIn()));
        WhoGetsMyFlexGame gameInstance = new WhoGetsMyFlexGame(params.getInputData(),
                params.getSpecs(), params.getImbalIn(),
                new WindErrorGenerator(instanceConfig.getSeed(), params.getDistribution()),
                params.getFactory());

        List<FlexibilityProvider> agents = Lists.newArrayList();
        List<FlexibilityUtiliser> actions = gameInstance.getActionSet();
        for (Map.Entry<Integer, Integer> e : instanceConfig.getGameInstanceConfiguration()
                .getAgentActionMap().entrySet()) {
            gameInstance.fixActionToAgent(agents.get(e.getKey()), actions.get(e.getValue()));
        }
        gameInstance.init();
        gameInstance.play();
        setResult(gameInstance.getGameInstanceResult());
        //        getDataProvider();
    }

}
