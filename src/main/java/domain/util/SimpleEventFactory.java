package domain.util;

import be.kuleuven.cs.gridlock.simulation.events.Event;
import be.kuleuven.cs.gridlock.simulation.events.EventFactory;
import be.kuleuven.cs.gridlock.simulation.events.EventFactoryImplementation;

/**
 * EventFactory implementation for simplified events not using an event manager.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class SimpleEventFactory {

    private EventFactory ef;

    /**
     * Default constructor.
     */
    public SimpleEventFactory() {
        this.ef = new EventFactoryImplementation();
    }

    /**
     * Builds an event of a certain type.
     * 
     * @param eventType
     *            the type of event.
     * @return an event of the given type without other properties.
     */
    public Event build(String eventType) {
        return ef.build(eventType, null);
    }
}
