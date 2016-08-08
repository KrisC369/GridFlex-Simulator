package be.kuleuven.cs.flexsim.event;

/**
 * EventFactory for simplified events.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface EventFactory {

    /**
     * Builds an event of a certain type.
     *
     * @param eventType the type of event.
     * @return an event of the given type without other properties.
     */
    Event build(String eventType);

}
