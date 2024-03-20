import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class UniqueQueue<E> {
    private final Queue<E> queue = new ArrayDeque<>();
    private final Set<E> set = new HashSet<>();
    private final int maxSize;

    public UniqueQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be greater than 0");
        }
        this.maxSize = maxSize;
    }

    public boolean offer(E e) {
        if (set.contains(e)) {
            // Element is a duplicate, do not add
            return false;
        } else if (queue.size() < maxSize) {
            // Space is available, and element is unique
            set.add(e);
            queue.offer(e);
            return true;
        }
        // Queue is at max size
        return false;
    }

    public E poll() {
        E e = queue.poll();
        if (e != null) {
            set.remove(e);
        }
        return e;
    }

    public E peek() {
        return queue.peek();
    }

    public boolean contains(E e) {
        return set.contains(e);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}