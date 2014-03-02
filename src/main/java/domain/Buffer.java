package domain;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * The Class Buffer represents a simple FIFI queue buffer implementation.
 *
 * @param <T> the generic type of contents in this buffer.
 */
public class Buffer<T> {

    /** The internal queue representation. */
    private final Queue<T> data;

    /**
     * Instantiates a new buffer.
     */
    public Buffer() {
        this.data = new LinkedList<T>();
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
     * Push a resource into this buffer.
     *
     * @param res the content item to push.
     */
    public void push(T res) {
        data.add(res);
    }

    /**
     * Gets the current capacity.
     *
     * @return the current capacity
     */
    public int getCurrentCapacity() {
        return data.size();
    }

    /**
     * Pull a content item out of this buffer.
     *
     * @return the content up for grabs.
     */
    public T pull() {
        T t = data.poll();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }
}
