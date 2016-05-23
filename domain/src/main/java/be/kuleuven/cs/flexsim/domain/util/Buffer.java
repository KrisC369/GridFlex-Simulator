package be.kuleuven.cs.flexsim.domain.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Sets;

/**
 * The Class Buffer represents a simple FIFO queue buffer implementation.
 * 
 * @param <T>
 *            the generic type of contents in this buffer.
 */
public final class Buffer<T extends Bufferable> implements Serializable {

    private static final long serialVersionUID = 9044791947885042068L;

    /** The internal queue representation. */
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
        Set<T> returnset = Sets.newLinkedHashSet();
        while (!isEmpty()) {
            returnset.add(pull());
        }
        return returnset;
    }

    /**
     * Push a resource into this buffer. Notifies IBufferable resource it has
     * been buffered.
     * 
     * @param res
     *            the content item to push.
     */
    public void push(T res) {
        data.add(res);
        res.notifyOfHasBeenBuffered();
    }

    /**
     * Push a Ordered list into this buffer.
     * 
     * @param reslist
     *            the list of items to buffer.
     */
    public void pushAll(List<T> reslist) {
        for (T t : reslist) {
            push(t);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Buffer [OccupancyLevel()=")
                .append(getCurrentOccupancyLevel()).append(", hc=")
                .append(this.hashCode()).append("]");
        return builder.toString();
    }
}
