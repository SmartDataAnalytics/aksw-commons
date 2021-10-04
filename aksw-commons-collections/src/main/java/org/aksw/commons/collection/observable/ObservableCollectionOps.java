package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

public class ObservableCollectionOps {


    /**
     * Updates the 'backend' collection by first performing removals followed by additions.
     *
     * @param <T>
     * @param self
     * @param backend
     * @param vcs
     * @param pcs
     * @param clearIntersection Whether to adept additions and removals for an empty intersection
     * @param rawAdditions
     * @param rawRemovals
     * @return
     */
    public static <T> boolean applyDeltaSet(
            Collection<T> self,
            Set<T> backend,
            VetoableChangeSupport vcs,
            PropertyChangeSupport pcs,
            boolean clearIntersection,
            Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {

        // Set up the physical removals / additions that will be sent to the backend
        // This may include overlapping items
        @SuppressWarnings("unchecked")
        Set<T> physRemovals = rawRemovals == self
            ? rawRemovals.stream().map(x -> (T)x).collect(Collectors.toCollection(LinkedHashSet::new))
            : rawRemovals.stream().filter(backend::contains).map(x -> (T)x).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<T> physAdditions = rawAdditions.stream()
                .filter(x -> !backend.contains(x) || physRemovals.contains(x))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<T> intersection = new LinkedHashSet<>(Sets.intersection(physAdditions, physRemovals));

        Set<T> as;
        Set<T> rs;

        if (clearIntersection || intersection.isEmpty()) {
            physRemovals.removeAll(intersection);
            physAdditions.removeAll(intersection);
            as = physAdditions;
            rs = physRemovals;
        } else {
            // Set up the change sets
            as = new LinkedHashSet<>(physAdditions);
            rs = new LinkedHashSet<>(physRemovals);

            as.removeAll(intersection);
            rs.removeAll(intersection);
        }


        // FIXME additions and removals may have common items! those should be removed in
        // the event's additions / removals sets

        boolean result = false;

        {
            Collection<T> oldValue = self;
            Collection<T> newValue = rawRemovals == self
                    ? physAdditions
                    : Sets.union(Sets.difference(backend, rs), as);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                        self, oldValue, newValue,
                        as, rs, Collections.emptySet()));
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
            changeByRemoval = backend.removeAll(physRemovals);
        }

        boolean changeByAddition = backend.addAll(physAdditions);
        result = changeByRemoval || changeByAddition;

        {
            Collection<T> oldValue = rawRemovals == self
                    ? physRemovals
                    : Sets.union(Sets.difference(backend, as), rs);
            Collection<T> newValue = self;

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    self, oldValue, newValue,
                    as, rs, Collections.emptySet()));
        }

        return result;
    }

//
//    public static <T> boolean applyDelta(
//            Collection<T> self,
//            Collection<T> backend,
//            VetoableChangeSupport vcs,
//            PropertyChangeSupport pcs,
//            boolean duplicateAware,
//            Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {
//
//        // Set up the physical removals / additions that will be sent to the backend
//        // This may include overlapping items
//        @SuppressWarnings("unchecked")
//        Collection<T> physRemovals = rawRemovals == self
//            ? StreamOps.collect(duplicateAware, rawRemovals.stream().map(x -> (T)x))
//            : StreamOps.collect(duplicateAware, rawRemovals.stream().filter(backend::contains).map(x -> (T)x));
//        Collection<T> physAdditions = StreamOps.collect(duplicateAware, rawAdditions.stream().filter(x -> !backend.contains(x) || physRemovals.contains(x)));
//
//
//        // Set up the change sets -
//        Set<T> as = new LinkedHashSet<>(physAdditions);
//        Set<T> rs = new LinkedHashSet<>(physRemovals);
//        Set<T> intersection = new LinkedHashSet<>(Sets.intersection(as, rs));
//
//        as.remove(intersection);
//        rs.remove(intersection);
//
//
//
//        // FIXME additions and removals may have common items! those should be removed in
//        // the event's additions / removals sets
//
//        boolean result = false;
//
//        {
//            Collection<T> oldValue = self;
//            Collection<T> newValue = rawRemovals == self
//                    ? physAdditions
//                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, physRemovals), physAdditions);
//
//            try {
//                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                        self, oldValue, newValue,
//                        as, rs, Collections.emptySet()));
//            } catch (PropertyVetoException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        boolean changeByRemoval;
//        if (rawRemovals == self) {
//            changeByRemoval = !backend.isEmpty();
//            if (changeByRemoval) {
//                // Only invoke clear if we have to; prevent triggering anything
//                backend.clear();
//            }
//        } else {
//            changeByRemoval = backend.removeAll(physRemovals);
//        }
//
//        boolean changeByAddition = backend.addAll(physAdditions);
//        result = changeByRemoval || changeByAddition;
//
//        {
//            Collection<T> oldValue = rawRemovals == self
//                    ? physRemovals
//                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, physAdditions), physRemovals);
//            Collection<T> newValue = self;
//
//            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                    self, oldValue, newValue,
//                    as, rs, Collections.emptySet()));
//        }
//
//        return result;
//    }
}
