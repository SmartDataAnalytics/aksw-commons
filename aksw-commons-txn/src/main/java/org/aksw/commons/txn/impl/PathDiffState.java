package org.aksw.commons.txn.impl;

/**
 * A class that holds the original and current state of path metadata.
 *
 * @author raven
 */
public class PathDiffState {
    protected PathState originalState;
    protected PathState currentState;

    public PathDiffState(PathState originalState, PathState currentState) {
        super();
        this.originalState = originalState;
        this.currentState = currentState;
    }

    public PathState getOriginalState() {
        return originalState;
    }

    public void setOriginalState(PathState originalState) {
        this.originalState = originalState;
    }

    public PathState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(PathState currentState) {
        this.currentState = currentState;
    }
    
    /** If the original state differs from the current something is dirty */
    public boolean isDirty() {
    	return !originalState.equals(currentState);
    }

    @Override
    public String toString() {
        return "State [originalState=" + originalState + ", currentState=" + currentState + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentState == null) ? 0 : currentState.hashCode());
        result = prime * result + ((originalState == null) ? 0 : originalState.hashCode());
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
        PathDiffState other = (PathDiffState) obj;
        if (currentState == null) {
            if (other.currentState != null)
                return false;
        } else if (!currentState.equals(other.currentState))
            return false;
        if (originalState == null) {
            if (other.originalState != null)
                return false;
        } else if (!originalState.equals(other.originalState))
            return false;
        return true;
    }
}