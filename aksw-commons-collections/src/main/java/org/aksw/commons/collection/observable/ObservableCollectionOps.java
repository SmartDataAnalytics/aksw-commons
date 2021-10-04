package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.util.Collection;
import java.util.Collections;

public class ObservableCollectionOps {


    public static <T> boolean delta(
            Collection<T> self,
            Collection<T> backend,
            VetoableChangeSupport vcs,
            PropertyChangeSupport pcs,
            boolean duplicateAware,
            Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {

        @SuppressWarnings("unchecked")
        Collection<T> removals = rawRemovals == self
            ? StreamOps.collect(duplicateAware, rawRemovals.stream().map(x -> (T)x))
            : StreamOps.collect(duplicateAware, rawRemovals.stream().filter(backend::contains).map(x -> (T)x));
        Collection<T> additions = StreamOps.collect(duplicateAware, rawAdditions.stream().filter(x -> !backend.contains(x) || removals.contains(x)));

        // FIXME additions and removals may have common items! those should be removed in
        // the event's additions / removals sets

        boolean result = false;

        {
            Collection<T> oldValue = self;
            Collection<T> newValue = rawRemovals == self
                    ? additions
                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, removals), additions);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                        self, oldValue, newValue,
                        additions, removals, Collections.emptySet()));
            } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
            }
        }

        boolean changeByRemoval;
        if (rawRemovals == self) {
            changeByRemoval = !backend.isEmpty();
            if (changeByRemoval) {
                // Only invoke clear if we have to; prevent triggering anything
                backend.clear();
            }
        } else {
            changeByRemoval = backend.removeAll(removals);
        }

        boolean changeByAddition = backend.addAll(additions);
        result = changeByRemoval || changeByAddition;

        {
            Collection<T> oldValue = rawRemovals == self
                    ? removals
                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, additions), removals);
            Collection<T> newValue = self;

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    self, oldValue, newValue,
                    additions, removals, Collections.emptySet()));
        }

        return result;
    }
}
