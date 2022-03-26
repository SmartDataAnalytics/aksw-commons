package org.aksw.commons.collections;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * A set of values with positive or negative polarity
 * This set can thus act as a white or blacklist.
 *
 * @author Claus Stadler, Aug 1, 2018
 *
 * @param <T>
 */
public class PolaritySet<T>
    implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Set<T> values;
    protected boolean isPositive;

    public PolaritySet(Set<T> values) {
        this.values = values;
        this.isPositive = true;
    }

    public PolaritySet(boolean isPositive, Set<T> values) {
        this.values = values;
        this.isPositive = isPositive;
    }


    @Override
    protected PolaritySet<T> clone() {
        return new PolaritySet<>(isPositive, new LinkedHashSet<T>(values));
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


    /** Immutable union; returns a view */
    public PolaritySet<T> union(PolaritySet<T> that) {
        return createUnionView(this, that);
    }

    /** Immutable union; returns a view */
    public PolaritySet<T> intersect(PolaritySet<T> that) {
        return createIntersectionView(this, that);
    }

    /** Immutable union; returns a view */
    public PolaritySet<T> difference(PolaritySet<T> that) {
        return createDifferenceView(this, that);
    }

    /** Immutable negate; returns a view */
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

    /**
     * Set the polarity
     * @param isPositive the new value for the polarity; true = positive
     * @return The old polarity befor setting the new one.
     */
    public boolean setPolarity(boolean isPositive) {
        boolean result = isPositive;
        this.isPositive = isPositive;
        return result;
    }

    public Set<T> getValue() {
        return values;
    }


    public void clear() {
        this.values.clear();
        this.isPositive = true;
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


    /** Mutating intersection */
    public PolaritySet<T> stateIntersect(PolaritySet<T> that) {
        if(isPositive) {
            if(that.isPositive) {
                values.retainAll(that.values);
            } else {
                values.removeAll(that.values);
            }
        } else {
            if(that.isPositive) {
                values = new HashSet<>(Sets.difference(that.values, values));
                isPositive = true;
            } else {
                values.addAll(that.values);
            }
        }

        return this;
    }


    /** Mutating union */
    public PolaritySet<T> stateUnion(PolaritySet<T> that) {
        if(isPositive) {
            if(that.isPositive) {
                values.addAll(that.values);
            } else {
                values = new HashSet<>(Sets.difference(that.values, values));
                isPositive = false;
            }
        } else {
            if(that.isPositive) {
                values.removeAll(that.values);
                isPositive = false;
            } else {
                values.retainAll(that.values);
            }
        }

        return this;
    }

    /** Mutates this by flipping the polarity */
    public PolaritySet<T> stateNegate() {
        isPositive = !isPositive;
        return this;
    }



    /**
     * case: positive - positive
     *     Simply take the difference
     *
     * case: positive - negative
     *     {1, 2, 3} difference {not {3}}: -> { 3 } (intersection)
     *
     * case: negative - positive
     *     {not {3} } difference {1, 2, 3}:
     *
     * case: negative - negative
     *     Simply take the intersection
     *
     *
     * @param other
     * @return
     */
    public static <T> PolaritySet<T> createDifferenceView(PolaritySet<T> self, PolaritySet<T> that) {
        if (true) {
            throw new RuntimeException("not implemented yet");
        }
        Set<T> set;
        boolean isPos = true;

        if(self.isPositive) {
            if(that.isPositive) {
                set = Sets.difference(self.values, that.values);
            } else {
                set = Sets.intersection(self.values, that.values);
            }
        } else {
            // TODO This part is probably wrong
            if(that.isPositive) {
                set = Sets.union(that.values, self.values);
            } else {
                set = Sets.union(self.values, that.values);
                isPos = false;
            }
        }

        PolaritySet<T> result = new PolaritySet<T>(isPos, set);
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
    public static <T> PolaritySet<T> createIntersectionView(PolaritySet<T> self, PolaritySet<T> that) {
        Set<T> set;
        boolean isPos = true;

        if(self.isPositive) {
            if(that.isPositive) {
                set = Sets.intersection(self.values, that.values);
            } else {
                set = Sets.difference(self.values, that.values);
            }
        } else {
            if(that.isPositive) {
                set = Sets.difference(that.values, self.values);
            } else {
                set = Sets.union(self.values, that.values);
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
    public static <T> PolaritySet<T> createUnionView(PolaritySet<T> self, PolaritySet<T> that) {
        Set<T> set;
        boolean isPos = true;

        if(self.isPositive) {
            if(that.isPositive) {
                set = Sets.union(self.values, that.values);
            } else {
                set = Sets.difference(that.values, self.values);
                isPos = false;
            }
        } else {
            if(that.isPositive) {
                set = Sets.difference(self.values, that.values);
                isPos = false;
            } else {
                set = Sets.intersection(self.values, that.values);
            }
        }

        PolaritySet<T> result = new PolaritySet<T>(isPos, set);
        return result;
    }
}

