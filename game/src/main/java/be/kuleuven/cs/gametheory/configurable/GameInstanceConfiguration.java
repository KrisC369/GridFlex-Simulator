package be.kuleuven.cs.gametheory.configurable;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class GameInstanceConfiguration implements Serializable {
    private final int agentSize;
    private final int actionSize;

    private final Map<Integer, Integer> agentActionMap;

    public GameInstanceConfiguration(int agentSize, int actionSize) {
        this.agentSize = agentSize;
        this.actionSize = actionSize;
        this.agentActionMap = Maps.newLinkedHashMap();
    }

    public void fixAgentToAction(int tokenAgent, int tokenAction) {
        agentActionMap.put(tokenAgent, tokenAction);
    }

    public Map<Integer, Integer> getAgentActionMap() {
        return Collections.unmodifiableMap(agentActionMap);
    }
}
