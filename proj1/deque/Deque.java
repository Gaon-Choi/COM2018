package deque;

import java.util.Iterator;

// Create an interface in a new file named Deque.java that contains all of the methods above.
public interface Deque<T> {
    public void addFirst(T item);

    public void addLast(T item);

    public boolean isEmpty();

    public int size();

    public void printDeque();

    public T removeFirst();

    public T removeLast();

    public T get(int index);

    public boolean equals(Object obj);

    public Iterator<T> iterator();
}
