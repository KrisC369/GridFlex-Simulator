package be.kuleuven.cs.gametheory.configurable;

import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstanceConfiguration;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AbstractGameInstanceConfigurator<N, K> {
    abstract AbstractGameInstance<N, K> getAbstractGame();

    public void configureGameInstance(GameConfigurator<N, K> gameConfig,
            GameInstanceConfiguration config) {
        List<N> agents = Lists.newArrayList();
        List<K> actions = getAbstractGame().getActionSet();
        IntStream.range(0, config.getAgentActionMap().keySet().size())
                .forEach(p -> agents.add(gameConfig.getAgent()));
        for (Map.Entry<Integer, Integer> e : config.getAgentActionMap().entrySet()) {
            getAbstractGame().fixActionToAgent(agents.get(e.getKey()), actions.get(e.getValue()));
        }
    }

    public static AbstractGameInstanceConfigurator create(AbstractGameInstance instance) {
        return new AutoValue_AbstractGameInstanceConfigurator(instance);
    }
}
