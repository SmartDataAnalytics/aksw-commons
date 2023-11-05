package org.aksw.commons.util.history;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A simple history object that is backed by a list of {@link Callable} instances.
 * Invoking a callable is expected to update the state appropriately.
 *
 * Going back to memento x and then adding a new memento y first clears all mementos past x.
 */
public class History {
    // List of mementos
    protected List<Callable<?>> mementos = new ArrayList<>();
    protected int currentPosition = -1;

    public void addMemento(Runnable memento) {
        addMemento(() -> { memento.run(); return null; });
    }

    public void addMemento(Callable<?> memento) {
        currentPosition++;
        mementos.subList(currentPosition, mementos.size()).clear();
        mementos.add(memento);
        try {
            memento.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        mementos.clear();
        currentPosition = -1;
    }

    public boolean canDoBackward() {
        return currentPosition > 0;
    }

    public boolean canDoForward() {
        int n = mementos.size();
        return currentPosition < n - 1;
    }

    protected Callable<?> backward() {
        currentPosition--;
        return mementos.get(currentPosition);
    }

    public boolean doBackward() {
        boolean result;
        if ((result = canDoBackward()) == true) {
            Callable<?> callable = backward();
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public boolean doFoward() {
        boolean result;
        if ((result = canDoForward()) == true) {
            Callable<?> callable = forward();
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    protected Callable<?> forward() {
        currentPosition++;
        return mementos.get(currentPosition);
    }

    public boolean isEmpty() {
        return mementos.isEmpty();
    }

    public int size() {
        return mementos.size();
    }
}
