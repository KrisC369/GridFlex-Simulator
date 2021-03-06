package be.kuleuven.cs.gridflex.event;

/**
 * Default implementation for the factory interface.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class EventFactoryImplementation implements EventFactory {
    /**
     * Default empty constructor.
     */
    public EventFactoryImplementation() {
    }

    @Override
    public Event build(final String eventType) {
        return new Event(eventType);
    }
}
