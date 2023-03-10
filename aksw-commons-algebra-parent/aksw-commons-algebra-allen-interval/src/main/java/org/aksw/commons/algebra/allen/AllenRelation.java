package org.aksw.commons.algebra.allen;

import java.util.Arrays;
import java.util.Objects;

/** An immutable wrapper for a bit-pattern (of primitive type 'short') that represents the Allen relations */
public class AllenRelation {

    /** Dummy relation with a 0 bit pattern. Useful for reduce/fold operations. */
    public static final AllenRelation EMPTY       = AllenRelation.of(AllenConstants.EMPTY);

    public static final AllenRelation BEFORE       = AllenRelation.of(AllenConstants.BEFORE);
    public static final AllenRelation AFTER        = AllenRelation.of(AllenConstants.AFTER);
    public static final AllenRelation DURING       = AllenRelation.of(AllenConstants.DURING);
    public static final AllenRelation CONTAINS     = AllenRelation.of(AllenConstants.CONTAINS);
    public static final AllenRelation OVERLAPS     = AllenRelation.of(AllenConstants.OVERLAPS);
    public static final AllenRelation OVERLAPPEDBY = AllenRelation.of(AllenConstants.OVERLAPPEDBY);
    public static final AllenRelation MEETS        = AllenRelation.of(AllenConstants.MEETS);
    public static final AllenRelation METBY        = AllenRelation.of(AllenConstants.METBY);
    public static final AllenRelation STARTS       = AllenRelation.of(AllenConstants.STARTS);
    public static final AllenRelation STARTEDBY    = AllenRelation.of(AllenConstants.STARTEDBY);
    public static final AllenRelation FINISHES     = AllenRelation.of(AllenConstants.FINISHES);
    public static final AllenRelation FINISHEDBY   = AllenRelation.of(AllenConstants.FINISHEDBY);
    public static final AllenRelation EQUALS       = AllenRelation.of(AllenConstants.EQUALS);

    protected final short pattern;

    /** The public API to create instances is {@link #create(short)}. */
    protected AllenRelation(short pattern) {
        super();
        this.pattern = pattern;
    }

    public short getPattern() {
        return pattern;
    }

    /** Create a new relation with the inverse */
    public AllenRelation invert() {
        return of(AllenConstants.invert(pattern));
    }

    public static AllenRelation of(short pattern) {
        return new AllenRelation(pattern);
    }

    public boolean isEmpty() {
        return pattern == 0;
    }

    /** Combine all given patterns with bitwise OR */
    public static AllenRelation of(short pattern, short ... additionalPatterns) {
        short effectivePattern = pattern;
        for (short contrib : additionalPatterns) {
            effectivePattern |= contrib;
        }
        return of(effectivePattern);
    }

    public AllenRelation union(AllenRelation other) {
        return new AllenRelation((short)(this.pattern | other.pattern));
    }

    public static AllenRelation union(AllenRelation... others) {
        short unionPattern = (short)Arrays.asList(others).stream().mapToInt(AllenRelation::getPattern).reduce(0, (x, y) -> x | y);
        return of(unionPattern);
    }

//
//    boolean isBefore() {
//        return (pattern & AllenConstants.BEFORE) != 0;
//    }
//
//    boolean isAfter() {
//        return (pattern & AllenConstants.AFTER) != 0;
//    }
//
//    boolean isDuring() {
//        return (pattern & AllenConstants.DURING) != 0;
//    }
//
//    boolean isContaining() {
//        return (pattern & AllenConstants.CONTAINS) != 0;
//    }
//
//    boolean isOverlapping() {
//        return (pattern & AllenConstants.OVERLAPS) != 0;
//    }
//
//    boolean isOverlappedBy() {
//        return (pattern & AllenConstants.OVERLAPPEDBY) != 0;
//    }
//
//    boolean isMeeting() {
//        return (pattern & AllenConstants.MEETS) != 0;
//    }
//
//    boolean isMetBy() {
//        return (pattern & AllenConstants.METBY) != 0;
//    }
//
//    boolean isStarting() {
//        return (pattern & AllenConstants.STARTS) != 0;
//    }
//
//    boolean isStartedBy() {
//        return (pattern & AllenConstants.STARTEDBY) != 0;
//    }
//
//    boolean isFinishing() {
//        return (pattern & AllenConstants.FINISHES) != 0;
//    }
//
//    boolean isFinishedBy() {
//        return (pattern & AllenConstants.FINISHEDBY) != 0;
//    }
//
//    boolean isEqual() {
//        return (pattern & AllenConstants.EQUALS) != 0;
//    }

    @Override
    public String toString() {
        return AllenConstants.toString(pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AllenRelation other = (AllenRelation) obj;
        return pattern == other.pattern;
    }
}
