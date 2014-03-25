package be.kuleuven.cs.flexsim.domain.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 * The Class Buffer represents a simple FIFI queue buffer implementation.
 * 
 * @param <T>
 *            the generic type of contents in this buffer.
 */
public class Buffer<T extends IBufferable> {

    /** The internal queue representation. */
    private final Queue<T> data;

    /**
     * Instantiates a new buffer.
     */
    public Buffer() {
        this.data = new LinkedList<T>();
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
     * Checks if this buffer is empty.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
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

    /**
     * Pull all items from this buffer.
     * 
     * @return all the present items.
     */
    public Collection<T> pullAll() {
        Set<T> returnset = new HashSet<>();
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
}
