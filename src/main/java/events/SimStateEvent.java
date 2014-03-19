package events;

import java.util.HashMap;
import java.util.Map;

public class SimStateEvent implements IEvent{
    SimState state;
    
    public SimStateEvent(SimState state){
        this.state = state;
    }
    @Override
    public Map<String, Object> getEventInfo() {
        Map<String, Object> res =  new HashMap<>();
        res.put("State", state);
        return res;
    }
}
