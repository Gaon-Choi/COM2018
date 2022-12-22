package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private static final boolean ADD_MODE = true;
    private static final boolean DEL_MODE = false;

    private int size;
    private T[] items;
    private int capacity;

    // index
    int front;
    int rear;

    public ArrayDeque() {
        this.size = 0;
        this.capacity = 8;
        this.items = (T[]) new Object[this.capacity];

        // this can be anything! e.g. (3, 4) or (4, 5) or (1, 2) and so on...
        this.front = 3;
        this.rear = 4;
    }

    private int updateFront(boolean mode) {
        // add an element
        if (mode) {
            return (this.front - 1) % this.capacity;
        }
        // delete an element
        else {
            return (this.front + 1) % this.capacity;
        }
    }

    private int updateRear(boolean mode) {
        // add an element
        if (mode) {
            return (this.rear + 1) % this.capacity;
        }
        // delete an element
        else {
            return (this.rear - 1) % this.capacity;
        }
    }

    public int getFront() {
        return this.front;
    }

    public int getRear() {
        return this.rear;
    }

    // inspired by AList.java in lab3
    private void resize(int size) {
        T[] newArray = (T[]) new Object[size];
        // System.arraycopy(items, 0, newArray, 0, size);
        for (int i = 0; i < this.size(); i++) {
            newArray[i] = this.items[(updateFront(DEL_MODE) + i) % this.capacity];
        }
        this.capacity = size;
        this.items = newArray;
        this.front = this.capacity - 1;
        this.rear = this.size();
    }

    @Override
    public void addFirst(T item) {
        if (this.isFull()) {
            resize(this.capacity * 2);
        }
        this.items[this.getFront()] = item;
        this.front = updateFront(ADD_MODE);
        this.size++;
    }

    @Override
    public void addLast(T item) {
        if (this.isFull()) {
            resize(this.capacity * 2);
        }
        this.items[this.getRear()] = item;
        this.rear = updateRear(ADD_MODE);
        this.size++;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean isFull() {
        return this.size() == this.capacity;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < this.size() - 1; i++) {
            System.out.print(this.items[(updateFront(DEL_MODE) + i) % this.capacity] + " ");
        }
        System.out.print(this.items[(updateFront(DEL_MODE) + this.size() - 1) % this.capacity]);
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (this.isEmpty()) {
            return null;
        }
        if ((this.size < this.capacity / 4) && (this.size > 4)) {
            resize(this.capacity / 2);
        }
        this.front = updateFront(DEL_MODE);
        this.size -= 1;

        T delItem = this.items[this.front];
        this.items[this.front] = null;

        return delItem;
    }

    @Override
    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }
        if ((this.size < this.capacity / 4) && (this.size > 4)) {
            resize(this.capacity / 4);
        }
        this.rear = updateRear(DEL_MODE);
        this.size -= 1;

        T delItem = this.items[this.rear];
        this.items[this.rear] = null;

        return delItem;
    }

    @Override
    public T get(int index) {
        return this.items[(updateFront(DEL_MODE) + index) % this.capacity];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if ((obj instanceof ArrayDeque) == false)
            return false;
        else {
            ArrayDeque<T> tmp = (ArrayDeque<T>) obj;
            if (tmp.size() != this.size())
                return false;
            for (int i = 0; i < this.size(); i++) {
                if ((tmp.get(i) != this.get(i)))
                    return false;
            }
            return true;
        }
    }

    // Iterator
    // idea: https://www.youtube.com/watch?v=Gv6LjusNBU0&ab_channel=JoshHug

    // The TA Younsang Cho gave some advices!
        // before(my implementation): define Iterator-implements class in the ArrayDeque
        // after(TA's advice)       : make them in the Deque.java and implement the method!

    public Iterator<T> iterator() {
        return new ArrayDequeIterator<T>(this);
    }

    private class ArrayDequeIterator<T> implements Iterator<T> {
        private int wizPos;

        public ArrayDequeIterator(ArrayDeque<T> arrdeque) {
            this.wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size();
        }

        public T next() {
            // why .. cast to T?
            T returnItem = (T) get(wizPos);
            wizPos++;   // increment index by 1
            return returnItem;
        }
    }

}
