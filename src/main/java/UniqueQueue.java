import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * UniqueQueue is a generic class that represents a queue with unique elements.
 * It uses a Queue and a Set to ensure uniqueness and order of elements.
 * It also has a maximum size, and elements cannot be added once this size is reached.
 *
 * @param <E> the type of elements held in this collection
 */
public class UniqueQueue<E> {
    private final Queue<E> queue = new ArrayDeque<>();
    private final Set<E> set = new HashSet<>();
    private final int maxSize;

    /**
     * Constructor for UniqueQueue.
     *
     * @param maxSize the maximum size of the queue
     * @throws IllegalArgumentException if maxSize is less than or equal to 0
     */
    public UniqueQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be greater than 0");
        }
        this.maxSize = maxSize;
    }

    /**
     * Adds the specified element to this queue if it is not already present and the queue has not reached its maximum size.
     *
     * @param e element to be added to this queue
     * @return true if this queue changed as a result of the call
     */
    public boolean offer(E e) {
        if (set.contains(e)) {
            return false;
        } else if (queue.size() < maxSize) {
            set.add(e);
            queue.offer(e);
            return true;
        }
        return false;
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public E poll() {
        E e = queue.poll();
        if (e != null) {
            set.remove(e);
        }
        return e;
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public E peek() {
        return queue.peek();
    }

    /**
     * Returns true if this queue contains the specified element.
     *
     * @param e element whose presence in this queue is to be tested
     * @return true if this queue contains the specified element
     */
    public boolean contains(E e) {
        return set.contains(e);
    }

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns true if this queue contains no elements.
     *
     * @return true if this queue contains no elements
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns the maximum size of this queue.
     *
     * @return the maximum size of this queue
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Returns a string representation of this queue.
     *
     * @return a string representation of this queue
     */
    @Override
    public String toString() {
        return queue.toString();
    }
}