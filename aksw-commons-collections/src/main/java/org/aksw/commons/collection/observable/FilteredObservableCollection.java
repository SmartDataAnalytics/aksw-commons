package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.FilteringCollection;

import com.google.common.collect.Collections2;


public class FilteredObservableCollection<T>
    extends FilteringCollection<T, ObservableCollection<T>>
    implements ObservableCollection<T>
    //extends ObservableCollectionBase<T, Collection<T>>
{
    public FilteredObservableCollection(ObservableCollection<T> backend, Predicate<? super T> predicate) {
        super(backend, predicate);
    }


    @Override
    public boolean delta(Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {
        Collection<T> filteredRemovals = StreamOps.<T>filter(rawRemovals.stream(), predicate).collect(Collectors.toSet());
        Collection<T> filteredAdditions = rawAdditions.stream().filter(predicate).collect(Collectors.toSet());

        return delta(filteredAdditions, filteredRemovals);
    }

//	@Override
//	public Collection<T> getBackend() {
//		return (ObservableCollection<T>)super.getBackend();
//	}

//    @Override
//    public boolean replace(Collection<? extends T> newValues) {
//        Set<T> replacement = Stream.concat(
//                // Retain all prior values NOT matching the predicate
//                backend.stream().filter(x -> !predicate.test(x)),
//
//                // Retail only new values MATCHING the predicate
//                newValues.stream().filter(predicate)
//        ).collect(Collectors.toSet());
//
//        return backend.replace(replacement);
////        Collection<T> newSet = newValues.stream().filter(predicate).collect(Collectors.toSet());
////        backend.replace(newSet);
//    }

    @SuppressWarnings("unchecked")
    public static <T> CollectionChangedEvent<T> filter(Object self, CollectionChangedEvent<T> event, Predicate<? super T> predicate) {
        return new CollectionChangedEventImpl<>(self,
                Collections2.filter((Collection<T>)event.getOldValue(), predicate::test),
                Collections2.filter((Collection<T>)event.getNewValue(), predicate::test),
                Collections2.filter((Collection<T>)event.getAdditions(), predicate::test),
                Collections2.filter((Collection<T>)event.getDeletions(), predicate::test),
                Collections2.filter((Collection<T>)event.getRefreshes(), predicate::test)
        );
    }


    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        return getBackend().addVetoableChangeListener(event -> {
            CollectionChangedEvent<T> newEv = filter(this, (CollectionChangedEvent<T>)event, predicate);
            if (newEv.hasChanges()) {
                listener.vetoableChange(newEv);
            }
        });
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return getBackend().addPropertyChangeListener(event -> {
            CollectionChangedEvent<T> newEv = filter(this, (CollectionChangedEvent<T>)event, predicate);
            if (newEv.hasChanges()) {
                listener.propertyChange(newEv);
            }
        });
    }
}

