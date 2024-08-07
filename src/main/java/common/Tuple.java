package common;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.Serializable;

/**
 * A common.Tuple class that holds two values of any type.
 * This class is serializable, which means it can be converted into a byte stream.
 *
 * @param <A> the type of the first value
 * @param <B> the type of the second value
 */
public class Tuple<A, B> implements Serializable {
    private final A first;
    private final B second;

    /**
     * Constructs a new common.Tuple with the given values.
     *
     * @param first  the first value
     * @param second the second value
     */
    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value of this common.Tuple.
     *
     * @return the first value
     */
    public A getFirst() {
        return first;
    }

    /**
     * Returns the second value of this common.Tuple.
     *
     * @return the second value
     */
    public B getSecond() {
        return second;
    }

    /**
     * Returns a string representation of this common.Tuple.
     * The string representation will be in the format "(first, second)".
     *
     * @return a string representation of this common.Tuple
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    /**
     * Checks if this common.Tuple is equal to the specified object.
     * The result is true if and only if the argument is not null and is a common.Tuple object that has the same first and second values as this object.
     *
     * @param obj the object to compare this common.Tuple against
     * @return true if the given object represents a common.Tuple equivalent to this common.Tuple, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
        return first.equals(tuple.first) && second.equals(tuple.second);
    }

    /**
     * Returns a hash code value for this common.Tuple.
     * This method is supported for the benefit of hash tables such as those provided by java.util.HashMap.
     *
     * @return a hash code value for this common.Tuple
     */
    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}