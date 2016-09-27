package be.kuleuven.cs.gametheory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of the GameInstance interface.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractGameInstance<N, K> implements GameInstance<N, K> {

    private final List<N> agents;
    private final List<K> actions;
    private final Map<N, K> agentActionMap;
    private final GameInstanceConfiguration config;

    protected AbstractGameInstance(
            List<K> actions) {
        agents = Lists.newArrayList();
        this.actions = actions;
        this.agentActionMap = Maps.newLinkedHashMap();
        this.config = new GameInstanceConfiguration(agents.size(), actions.size());
    }

    @Override
    public void fixActionToAgent(N agent, K action) {
        agents.add(agent);
        agentActionMap.put(agent, action);
        config.fixAgentToAction(agents.indexOf(agent), actions.indexOf(action));
    }

    @Override
    public List<K> getActionSet() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public Map<N, K> getAgentToActionMapping() {
        return Collections.unmodifiableMap(agentActionMap);
    }

    public GameInstanceResult getGameInstanceResult() {
        Map<Integer, Long> results = Maps.newLinkedHashMap();
        for (Map.Entry<N, Long> e : this.getPayOffs().entrySet()) {
            results.put(agents.indexOf(e.getKey()), e.getValue());
        }
        return GameInstanceResult.create(this.config, results);
    }

    GameInstanceConfiguration getConfig() {
        return config;
    }

}
