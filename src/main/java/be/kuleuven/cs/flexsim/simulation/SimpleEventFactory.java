package be.kuleuven.cs.flexsim.simulation;

import be.kuleuven.cs.gridlock.simulation.events.Event;

/**
 * EventFactory for simplified events not using an event manager.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public interface SimpleEventFactory {

    /**
     * Builds an event of a certain type.
     * 
     * @param eventType
     *            the type of event.
     * @return an event of the given type without other properties.
     */
    Event build(String eventType);

}
