package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ObservableSetImpl<T>
    extends ObservableCollectionBase<T, Set<T>>
    implements ObservableSet<T>
{
    public ObservableSetImpl(Set<T> decoratee) {
        super(decoratee);
    }

    public static <T> ObservableSet<T> decorate(Set<T> decoratee) {
        return new ObservableSetImpl<T>(decoratee);
    }

    /**
     *
     * Removals are carried out as given in order for linked collections to yield items in the right order.
     * Hence, even if an item to be removed is contained in the additions it will be removed first.
     *
     */
   @Override
   public boolean delta(Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {
       return ObservableCollectionOps.applyDeltaSet(
               this, backend,
               vcs, pcs,
               true,
               rawAdditions, rawRemovals);
   }

    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        // return () -> pcs.removePropertyChangeListener(listener);

        return Registration.from(
            () ->  { listener.propertyChange(new CollectionChangedEventImpl<T>(
                    this, this, this,
                    Collections.emptySet(), Collections.emptySet(), Collections.emptySet())); },
            () -> pcs.removePropertyChangeListener(listener));
    }

}
