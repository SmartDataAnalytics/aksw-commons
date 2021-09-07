package org.aksw.commons.collections;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A set of values with positive or negative polarity
 * This set can thus act as a white or blacklist.
 *
 * @author Claus Stadler, Aug 1, 2018
 *
 * @param <T>
 */
public class PolaritySet<T>
{
    private Set<T> values;
    private boolean isPositive;

    public PolaritySet(Set<T> values) {
        this.values = values;
        this.isPositive = true;
    }

    public PolaritySet(boolean isPositive, Set<T> values) {
        this.values = values;
        this.isPositive = isPositive;
    }

    /*
    public ValueSet(boolean isPositive, T ...values) {
        this.isPositive = isPositive;
        this.values = new HashSet<T>(Arrays.asList(values));
    }*/


    public static <T> PolaritySet<T> create(boolean isPositive, T ... values) {
        //this.isPositive = isPositive;
        Set<T> v = new LinkedHashSet<T>(Arrays.asList(values));
        PolaritySet<T> result = new PolaritySet<T>(isPositive, v);

        return result;
    }

    /**
     * case: positive - positive
     *     Simply take the intersection
     *
     * case: positive - negative
     *     {1, 2, 3} intersect {not {2}}: -> {1, 3} (positive.removeAll(negative))
     *
     * case: negative - positive
     *     Same as above
     *
     * case: negative - negative
     *     Simply take the union
     *
     *
     * @param other
     * @return
     */
    public PolaritySet<T> intersect(PolaritySet<T> that) {
        Set<T> set = new LinkedHashSet<T>();
        boolean isPos = true;

        if(isPositive) {
            if(that.isPositive) {
                set.addAll(this.values);
                set.retainAll(that.values);

            } else {

                set.addAll(this.values);
                set.removeAll(that.values);
            }
        } else {
            if(that.isPositive) {

                set.addAll(that.values);
                set.removeAll(this.values);
            } else {

                set.addAll(this.values);
                set.addAll(that.values);
                isPos = false;
            }
        }

        PolaritySet<T> result = new PolaritySet<T>(isPos, set);
        return result;
    }


    /**
     * case: positive - positive
     *     Simply take the union
     *
     * case: positive - negative
     *     {1, 2, 3} intersect {not {1, 4}}: -> {4} (negative.removeAll(positive))
     *
     * case: negative - positive
     *     Same as above
     *
     * case: negative - negative
     *     Simply take the intersection
     *
     * @param that
     * @return
     */
    public PolaritySet<T> union(PolaritySet<T> that) {
        Set<T> set = new LinkedHashSet<T>();
        boolean isPos = true;

        if(isPositive) {
            if(that.isPositive) {
                set.addAll(this.values);
                set.addAll(that.values);

            } else {

                set.addAll(that.values);
                set.removeAll(this.values);
                isPos = false;
            }
        } else {
            if(that.isPositive) {

                set.addAll(this.values);
                set.removeAll(that.values);
                isPos = false;
            } else {

                set.addAll(this.values);
                set.retainAll(that.values);
            }
        }

        PolaritySet<T> result = new PolaritySet<T>(isPos, set);
        return result;
    }

    public PolaritySet<T> negate() {
        PolaritySet<T> result = new PolaritySet<T>(!isPositive, values);
        return result;
    }


    public boolean isEmpty() {
        return isPositive && values.isEmpty();
    }


    public boolean contains(Object item) {
        boolean isContained = values.contains(item);

        boolean result = isPositive
                ? isContained
                : !isContained;

        return result;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public Set<T> getValue() {
        return values;
    }


    @Override
    public String toString() {
        String polarity = (isPositive) ? "+" : "-";

        return polarity + values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isPositive ? 1231 : 1237);
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PolaritySet<?> other = (PolaritySet<?>) obj;
        if (isPositive != other.isPositive)
            return false;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
}