package org.aksw.commons.algebra.allen;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/** Guava adapter for computing allen relations from guava ranges */
public class AllenRelations {

    public static <T extends Comparable<T>> AllenRelation compute(Range<T> x, Range<T> y) {
        short pattern = 0;

        if (isBefore(x, y))       pattern |= AllenConstants.BEFORE;
        if (isMeeting(x, y))      pattern |= AllenConstants.MEETS;
        if (isOverlapping(x, y))  pattern |= AllenConstants.OVERLAPS;
        if (isStarting(x, y))     pattern |= AllenConstants.STARTS;
        if (isDuring(x, y))       pattern |= AllenConstants.DURING;
        if (isFinishing(x, y))    pattern |= AllenConstants.FINISHES;

        if (x.equals(y))          pattern |= AllenConstants.EQUALS;

        if (isAfter(x, y))        pattern |= AllenConstants.AFTER;
        if (isMetBy(x, y))        pattern |= AllenConstants.METBY;
        if (isOverlappedBy(x, y)) pattern |= AllenConstants.OVERLAPPEDBY;
        if (isStartedBy(x, y))    pattern |= AllenConstants.STARTEDBY;
        if (isContaining(x, y))   pattern |= AllenConstants.CONTAINS;
        if (isFinishedBy(x, y))   pattern |= AllenConstants.FINISHEDBY;

        return AllenRelation.of(pattern);
    }

    /** is strictly before (not meeting) */
    public static <T extends Comparable<T>> boolean isBefore(Range<T> x, Range<T> y) {
        boolean result =
                // Bounds must exist
                x.hasUpperBound() && y.hasLowerBound() &&
                // Either: If both are closed then the endpoints must be strictly less, otherwise less-than-or equal
                isUpperEndpointOfXBeforeLowerEndpointOfY(x.upperEndpoint(), x.upperBoundType(), y.lowerEndpoint(), y.lowerBoundType());
        return result;
    }

    /** The intersection must be empty */
    public static <T extends Comparable<T>> boolean isMeeting(Range<T> x, Range<T> y) {
        boolean result =
                (x.upperBoundType().equals(BoundType.OPEN) && y.lowerBoundType().equals(BoundType.CLOSED) ||
                 x.upperBoundType().equals(BoundType.CLOSED) && y.lowerBoundType().equals(BoundType.OPEN))
                && x.upperEndpoint().equals(y.lowerEndpoint());
        return result;
    }

    public static <T extends Comparable<T>> boolean isOverlapping(Range<T> x, Range<T> y) {
        boolean xStartsBeforeYStarts = y.hasLowerBound() && (
                !x.hasLowerBound() ||
                isLowerEndpointBefore(x.lowerEndpoint(), x.lowerBoundType(), y.lowerEndpoint(), y.lowerBoundType()));

        boolean xEndsBeforeYEnds = x.hasUpperBound() && (
                !y.hasUpperBound() ||
                isUpperEndpointBefore(x.upperEndpoint(), x.upperBoundType(), y.upperEndpoint(), y.upperBoundType()));

        boolean xEndsAfterYStarts = x.hasUpperBound() && y.hasLowerBound()
                && y.lowerEndpoint().compareTo(x.upperEndpoint()) < 0; // isLowerEndpointOfYBeforeUpperEndpointOfX

        boolean result = xStartsBeforeYStarts && xEndsBeforeYEnds && xEndsAfterYStarts;
        return result;
    }

    public static <T extends Comparable<T>> boolean isStarting(Range<T> x, Range<T> y) {
        boolean hasSameStart =
                // If neither range has a lower bound is qualifies as starting
                (!x.hasLowerBound() && !y.hasLowerBound()) ||
                (x.hasLowerBound() && y.hasLowerBound() &&
                        x.lowerBoundType().equals(y.lowerBoundType()) &&
                        x.lowerEndpoint().equals(y.lowerEndpoint()));

        boolean xFinishesBeforeY = x.hasUpperBound() && (
                !y.hasUpperBound() || isUpperEndpointBefore(x.upperEndpoint(), x.upperBoundType(), y.upperEndpoint(), y.upperBoundType()));

        boolean result = hasSameStart && xFinishesBeforeY;
        return result;
    }

    public static <T extends Comparable<T>> boolean isDuring(Range<T> x, Range<T> y) {
        boolean result =
                (x.hasLowerBound() && (!y.hasLowerBound() || isLowerEndpointBefore(y.lowerEndpoint(), y.lowerBoundType(), x.lowerEndpoint(), x.lowerBoundType()))) &&
                (x.hasUpperBound() && (!y.hasUpperBound() || isUpperEndpointBefore(x.upperEndpoint(), x.upperBoundType(), y.upperEndpoint(), y.upperBoundType())));
        return result;
    }

    public static <T extends Comparable<T>> boolean isFinishing(Range<T> x, Range<T> y) {
        boolean hasSameFinish =
                // If neither range has an upper bound is qualifies as finishing
                (!x.hasUpperBound() && !y.hasUpperBound()) ||
                (x.hasUpperBound() && y.hasUpperBound() &&
                        x.upperBoundType().equals(y.upperBoundType()) &&
                        x.upperEndpoint().equals(y.upperEndpoint()));

        boolean yStartsBeforeX = x.hasLowerBound() && (
                !y.hasLowerBound() || isLowerEndpointBefore(y.lowerEndpoint(), y.lowerBoundType(), x.lowerEndpoint(), x.lowerBoundType()));

        boolean result = hasSameFinish && yStartsBeforeX;
        return result;
    }

    public static <T extends Comparable<T>> boolean isAfter(Range<T> x, Range<T> y) {
        return isBefore(y, x);
    }

    public static <T extends Comparable<T>> boolean isMetBy(Range<T> x, Range<T> y) {
        return isMeeting(y, x);
    }

    public static <T extends Comparable<T>> boolean isOverlappedBy(Range<T> x, Range<T> y) {
        return isOverlapping(y, x);
    }

    public static <T extends Comparable<T>> boolean isStartedBy(Range<T> x, Range<T> y) {
        return isStarting(y, x);
    }

    public static <T extends Comparable<T>> boolean isContaining(Range<T> x, Range<T> y) {
        return isDuring(y, x);
    }

    public static <T extends Comparable<T>> boolean isFinishedBy(Range<T> x, Range<T> y) {
        return isFinishing(y, x);
    }


    /**
     * Is lower endpoint of x before upper endpoint of Y
     *       v
     *       |--x---|
     * |---y--|
     *        ^
     * This is just comparison of the values - the bounds don't matter:
     * x >= 5, y <= 5 -&gt; false
     * x >= 5, y <  5 -&gt; false
     * x >  5, y <= 5 -&gt; false
     * x >  5, y <  5 -&gt; false
     *
     */
    private static <T extends Comparable<T>> boolean isLowerEndpointOfXBeforeUpperEndpointOfY(T x, BoundType xbt, T y, BoundType ybt) {
        boolean result = x.compareTo(y) < 0;
        return result;
    }

    /**
     * Is upper endpoint of x before lower endpoint of Y
     *          v
     *   |--x---|
     *           |---y--|
     *           ^
     */
    public static <T extends Comparable<T>> boolean isUpperEndpointOfXBeforeLowerEndpointOfY(T x, BoundType xbt, T y, BoundType ybt) {
        boolean result = xbt.equals(BoundType.OPEN) && ybt.equals(BoundType.OPEN)
                ? x.compareTo(y) <= 0
                : x.compareTo(y) < 0;
        return result;
    }

    /**
     * cmp((_, 5) (_, 5]) yields true
     * cmp(( , 5] (_, 5)) yields false
     */
    public static <T extends Comparable<T>> boolean isUpperEndpointBefore(T x, BoundType xbt, T y, BoundType ybt) {
        boolean result = xbt.equals(ybt)
                  || ybt.equals(BoundType.CLOSED) // implies xbt = OPEN
                ? x.compareTo(y) < 0
                : x.compareTo(y) <= 0;
        return result;
    }


    /**
     * cmp([5, _) (5, _)) yields true
     * cmp((5, _) [5, _)) yields false
     */
    public static <T extends Comparable<T>> boolean isLowerEndpointBefore(T x, BoundType xbt, T y, BoundType ybt) {
        boolean result = xbt.equals(ybt)
                    || ybt.equals(BoundType.OPEN) // implies xbt = CLOSED
                ? x.compareTo(y) < 0
                : x.compareTo(y) <= 0;
        return result;
    }
}
