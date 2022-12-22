package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c) {
        this.comparator = c;
    }

    /*
        returns the maximum element in the deque as governed by the previously given Comparator
    */
    public T max() {
        if (this.isEmpty())
            return null;
        return max(this.comparator);
    }

    public T max(Comparator<T> c) {
        int maxIdx = 0;

        // iterate all elements in the deque and find the maximum index
        for(int i = 0; i < this.size(); i++) {
            if (c.compare(this.get(i), this.get(maxIdx)) > 0) {
                maxIdx = i;
            }
        }

        // return the item with its index
        return this.get(maxIdx);
    }
}
