package org.aksw.commons.util.direction;

/**
 * An enumeration for representing forward and backward directions.
 * Booleans quickly become confusing.
 */
public enum Direction {
    FORWARD(true), BACKWARD(false);

    boolean isForward;

    Direction(boolean isForward) {
        this.isForward = isForward;
    }

    public boolean isForward() {
        return isForward;
    }

    public boolean isBackward() {
        return !isForward;
    }

    public static Direction ofFwd(boolean isFwd) {
        Direction result = isFwd ? FORWARD : BACKWARD;
        return result;
    }

    /** Return the opposite direction - forward becomes backward and vice versa. */
    public Direction opposite() {
        return Direction.ofFwd(!isForward);
    }
}
