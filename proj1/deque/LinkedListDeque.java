package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private Node<T> header;

    public LinkedListDeque() {
        this.size = 0;

        // as the linked list is empty in the initial state
        // circular deque
        this.header = new Node<T>(null, null, null);
        this.header.prev = header;
        this.header.next = header;
        this.header.data = null;
    }

    public T getRecursive(int index) {
        // check index validity
        if (index < 0 || index >= this.size)
            return null;
        return getRecursive_(index, this.header);
    }

    public T getRecursive_(int index, Node<T> header) {
        if (index == 0) {
            return header.data;
        } else
            return getRecursive_(--index, header.next);
    }

    private static class Node<P> {
        // doubly-linked list
        // https://docs.google.com/presentation/d/1suIeJ1SIGxoNDT8enLwsSrMxcw4JTvJBsMcdARpqQCk/pub?start=false&loop=false&delayms=3000&slide=id.g829fe3f43_0_291

        /*
            // STRUCTURE OF A NODE
            prev    <-  | data |    ->      next
         */
        private P data;
        private Node<P> prev;
        private Node<P> next;

        Node(P data_, Node<P> prev_, Node<P> next_) {
            this.data = data_;
            this.prev = prev_;
            this.next = next_;
        }
    }

    @Override
    public void addFirst(T item) {
        Node<T> data = new Node<T>(item, null, null);
        Node<T> nextNode = this.header.next;

        data.prev = this.header;
        data.next = nextNode;

        this.header.next = data;
        nextNode.prev = data;

        // increment the deque size
        this.size++;
    }

    @Override
    public void addLast(T item) {
        Node<T> data = new Node<T>(item, null, null);
        Node<T> prevNode = this.header.prev;

        data.prev = prevNode;
        data.next = this.header;

        this.header.prev = data;
        prevNode.next = data;

        // increment the deque size
        this.size++;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void printDeque() {
        Node<T> tmp = this.header.next;
        while (tmp.next.data != null) {
            System.out.print(tmp.data + " ");
            tmp = tmp.next;
        }
        System.out.print(tmp.data);
        System.out.println();
    }

    @Override
    public T removeFirst() {
        // check if empty
        if (this.size() == 0) {
            return null;
        }

        Node<T> delNode = this.header.next;
        Node<T> nextNode = this.header.next.next;

        this.header.next = nextNode;
        nextNode.prev = this.header;

        // store the data of the node
        T delData = delNode.data;

        // isolate the original first node
        delNode.next = null;
        delNode.prev = null;

        // decrement the size
        this.size--;

        return delData;
    }

    @Override
    public T removeLast() {
        // check if empty
        if (this.size() == 0) {
            return null;
        }

        Node<T> delNode = this.header.prev;
        Node<T> prevNode = this.header.prev.prev;

        this.header.prev = prevNode;
        prevNode.next = this.header;

        // store the data of the node
        T delData = delNode.data;

        // isolate the original first node
        delNode.next = null;
        delNode.prev = null;

        // decrement the size
        this.size--;

        return delData;
    }

    @Override
    public T get(int index) {
        // check index validity
        if (index < 0 || index >= this.size() || this.isEmpty())
            return null;

        Node<T> tmp = this.header;
        for (int i = 0; i <= index; i++) {
            tmp = tmp.next;
        }
        return tmp.data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if ((obj instanceof LinkedListDeque) == false)
            return false;
        else {
            LinkedListDeque<T> tmp = (LinkedListDeque<T>) obj;
            if (tmp.size() != this.size())
                return false;
            for (int i = 0; i < this.size(); i++) {
                if ((tmp.get(i) != this.get(i)))
                    return false;
            }
            return true;
        }
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator<T>(this);
    }

    // Iterator
    // idea: https://www.youtube.com/watch?v=Gv6LjusNBU0&ab_channel=JoshHug

    private class LinkedListDequeIterator<T> implements Iterator<T> {
        private Node<T> headerNode;

        public LinkedListDequeIterator(LinkedListDeque<T> lldeque){
            headerNode = (Node<T>)header.next;
        }

        public boolean hasNext() {
            return headerNode != header;
        }

        public T next() {
            T returnItem = headerNode.data;
            headerNode = headerNode.next;   // increment index by 1
            return returnItem;
        }
    }
}
