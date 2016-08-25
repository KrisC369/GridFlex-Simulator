package be.kuleuven.cs.flexsim.domain.util;

import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 * The Class Buffer represents a simple FIFO queue buffer implementation.
 *
 * @param <T> the generic type of contents in this buffer.
 */
public final class Buffer<T extends Bufferable> implements Serializable {

    private static final long serialVersionUID = 9044791947885042068L;

    /**
     * The internal queue representation.
     */
    private final Queue<T> data;

    /**
     * Instantiates a new buffer.
     */
    public Buffer() {
        this.data = new LinkedList<>();
    }

    /**
     * Gets the current capacity.
     *
     * @return the current capacity
     */
    public int getCurrentOccupancyLevel() {
        return data.size();
    }

    /**
     * Checks if this buffer is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Pull a content item out of this buffer or NPE if no element present.
     *
     * @return the content up for grabs.
     */
    @SuppressWarnings("unused")
    public T pull() {
        @Nullable
        final
        T t = data.poll();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }

    /**
     * Pull all items from this buffer.
     *
     * @return all the present items.
     */
    public Collection<T> pullAll() {
        final Set<T> returnset = Sets.newLinkedHashSet();
        while (!isEmpty()) {
            returnset.add(pull());
        }
        return returnset;
    }

    /**
     * Push a resource into this buffer. Notifies IBufferable resource it has
     * been buffered.
     *
     * @param res the content item to push.
     */
    public void push(final T res) {
        data.add(res);
        res.notifyOfHasBeenBuffered();
    }

    /**
     * Push a Ordered list into this buffer.
     *
     * @param reslist the list of items to buffer.
     */
    public void pushAll(final List<T> reslist) {
        reslist.forEach(this::push);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(35);
        builder.append("Buffer [OccupancyLevel()=")
                .append(getCurrentOccupancyLevel()).append(", hc=")
                .append(this.hashCode()).append("]");
        return builder.toString();
    }
}
