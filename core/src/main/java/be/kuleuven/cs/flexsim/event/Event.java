package be.kuleuven.cs.flexsim.event;

import javax.naming.directory.NoSuchAttributeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The basic event class. Ported, inspired from, and distilled from the Gridlock
 * project.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @author Rutger Claes (rutger.claes AT cs.kuleuven.be)
 */
public class Event {

    private final String type;

    private final Map<String, Object> attributes;

    /**
     * Create a new event.
     *
     * @param type the event type
     */
    protected Event(final String type) {
        this.type = type;
        this.attributes = new HashMap<>();
    }

    /**
     * @return The type of this event
     */
    public final String getType() {
        return this.type;
    }

    /**
     * Remove all attributes from the event.
     */
    void clearAttributes() {
        synchronized (this.attributes) {
            this.attributes.clear();
        }
    }

    /**
     * Get an event attribute.
     *
     * @param <T>  expected type of the attribute.
     * @param key  key of the attribute.
     * @param type class the attribute should be cast to.
     * @return the attribute registered under key cast as type.
     * @throws NoSuchAttributeException when this event does not have the specified attribute.
     */
    public <T> T getAttribute(final String key, final Class<T> type)
            throws NoSuchAttributeException {
        synchronized (this.attributes) {
            if (!this.hasAttribute(key, type)) {
                throw new NoSuchAttributeException();
            }

            return type.cast(this.attributes.get(key));
        }
    }

    /**
     * Returns all attributes on this event in a map. The map will include the
     * type pseudo attribute.
     *
     * @return all attributes including the event type.
     */
    public Map<String, Object> getAttributes() {
        final Map<String, Object> copy;

        synchronized (this.attributes) {
            copy = new HashMap<>(this.attributes);
            copy.put("type", this.type);
        }

        return Collections.unmodifiableMap(copy);
    }

    /**
     * Add an attribute to this event.
     *
     * @param key   the attribute key.
     * @param value the attribute value.
     */
    public void setAttribute(final String key, final Object value) {

        if ("type".equals(key)) {
            throw new IllegalArgumentException(
                    "Attribute with key 'type' not allowed");
        }

        synchronized (this.attributes) {
            this.attributes.put(key, value);
        }
    }

    /**
     * Sets the arguments for this event. <strong>Warning: <em>all</em> previous
     * arguments will be cleared!</strong>.
     *
     * @param args The arguments to set
     */
    public void setAttributes(final Map<String, Object> args) {

        for (final String key : args.keySet()) {
            if (!this.canAcceptKey(key)) {
                throw new IllegalArgumentException("Illegal key in map");
            }
        }
        this.clearAttributes();
        this.attributes.putAll(args);
    }

    private boolean canAcceptKey(final String key) {
        return key != null && !"type".equals(key);
    }

    /**
     * Check whether there is an attribute associated with the key.
     *
     * @param key the key to check for.
     * @return true if there is an attribute for the key.
     */
    public boolean hasAttribute(final String key) {
        synchronized (this.attributes) {
            return this.attributes.containsKey(key);
        }
    }

    /**
     * Check whether there is an attribute associated with the key and that that
     * attribute is assignable to type.
     *
     * @param key  the key to check for.
     * @param type the type to check for.
     * @return true if there is an attribute for key of class type.
     */
    public boolean hasAttribute(final String key, final Class<?> type) {
        synchronized (this.attributes) {
            return this.attributes.containsKey(key) && type
                    .isAssignableFrom(this.attributes.get(key).getClass());
        }
    }
}
