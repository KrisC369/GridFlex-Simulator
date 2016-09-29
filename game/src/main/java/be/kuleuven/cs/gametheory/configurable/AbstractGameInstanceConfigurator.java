package be.kuleuven.cs.gametheory.configurable;

import be.kuleuven.cs.gametheory.AgentGenerator;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Configure an abstract game instance with agents and mapping to their actions.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AbstractGameInstanceConfigurator<N, K> {
    /**
     * @return The abstract game.
     */
    abstract AbstractGameInstance<N, K> getAbstractGame();

    /**
     * Configure the game instance by generating agents and fixing them to their actions.
     *
     * @param gameConfig The configurator to use for agent generation.
     * @param config     The mapping configuration.
     */
    public void configureGameInstance(AgentGenerator<N> gameConfig,
            GameInstanceConfiguration config) {
        List<N> agents = Lists.newArrayList();
        List<K> actions = getAbstractGame().getActionSet();
        IntStream.range(0, config.getAgentActionMap().keySet().size())
                .forEach(p -> agents.add(gameConfig.getAgent()));
        for (Map.Entry<Integer, Integer> e : config.getAgentActionMap().entrySet()) {
            getAbstractGame().fixActionToAgent(agents.get(e.getKey()), actions.get(e.getValue()));
        }
    }

    /**
     * Static factory method.
     *
     * @param instance The instance to configure.
     * @return a configurator instance.
     */
    public static AbstractGameInstanceConfigurator create(AbstractGameInstance instance) {
        return new AutoValue_AbstractGameInstanceConfigurator(instance);
    }
}
