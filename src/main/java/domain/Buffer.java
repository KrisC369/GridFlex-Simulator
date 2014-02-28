package domain;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Buffer<T> {

	private Queue<T> data;

	public Buffer() {
		this.data = new LinkedList<T>();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public void push(T res) {
		data.add(res);
	}

	public int getCurrentCapacity() {
		return data.size();
	}

	public T pull() {
		T t = data.poll();
		if (t == null)
			throw new NoSuchElementException();
		return t;
	}
}
