package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ObservableCollection<T>
    extends Collection<T>
{
    /** Whether to notify listeners */
//    void setEnableEvents(boolean onOrOff);

//    boolean isEnableEvents();

//    Runnable addListener(Consumer<CollectionChangedEvent<? super T>> listener);

    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);


    default ObservableCollection<T> filter(Predicate<? super T> predicate) {
        return new FilteredObservableCollection<>(this, predicate);
    }

    default <U> ObservableCollection<T> map(Function<? super T, ? extends U> predicate) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** 
     * Return a view of this collection as a scalar value:
     * If the collection contains a single item then this item becomes the view's value.
     * Otherwise the view's value is null.
     * 
     * @return
     */
    default ObservableValue<T> mapToValue() {
    	return ObservableValueFromObservableCollection.decorate(this);
    }

    default <O> ObservableValue<O> mapToValue(
    		Function<? super Collection<? extends T>, O> xform,
    		Function<? super O, ? extends T> valueToItem) {
    	return new ObservableValueFromObservableCollection<>(this, xform, valueToItem);
    }
    
//    default <U> ObservableValue<U> mapToValue(Function<? super Collection<? super T>, ? extends U> fn) {
//    	return new ObservableValueFromObservableCollection<>(this);
//    }
    
//    default ObservableCollection<T> mapToSet(Predicate<T> predicate) {
//    	return null;
//    }

}