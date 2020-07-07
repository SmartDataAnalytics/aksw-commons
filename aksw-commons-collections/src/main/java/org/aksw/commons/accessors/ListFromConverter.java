package org.aksw.commons.accessors;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import com.google.common.base.Converter;

public class ListFromConverter<F, B>
    extends CollectionFromConverter<F, B, List<B>>
    implements List<F>
{
    public static class ListIteratorFromConverter<T, U>
        extends IteratorFromConverter<T, U, ListIterator<U>>
        implements ListIterator<T>
    {
        public ListIteratorFromConverter(ListIterator<U> core, Converter<U, T> converter) {
            super(core, converter);
        }

        @Override
        public boolean hasNext() {
            boolean result = core.hasNext();
            return result;
        }

        @Override
        public T next() {
            U raw = core.next();
            T result = converter.convert(raw);
            return result;
        }

        @Override
        public boolean hasPrevious() {
            boolean result = core.hasPrevious();
            return result;
        }

        @Override
        public T previous() {
            U raw = core.previous();
            T result = converter.convert(raw);
            return result;
        }

        @Override
        public int nextIndex() {
            int result = core.nextIndex();
            return result;
        }

        @Override
        public int previousIndex() {
            int result = core.previousIndex();
            return result;
        }

//		@Override
//		public void remove() {
//			core.remove();
//		}

        @Override
        public void set(T e) {
            U item = converter.reverse().convert(e);
            core.set(item);
        }

        @Override
        public void add(T e) {
            U item = converter.reverse().convert(e);
            core.add(item);
        }
    };



    public ListFromConverter(List<B> backend, Converter<B, F> converter) {
        super(backend, converter);
    }

    @Override
    public boolean addAll(int index, Collection<? extends F> c) {
        Collection<B> transformed = c.stream().map(item -> converter.reverse().convert(item)).collect(Collectors.toList());
        boolean result = backend.addAll(index, transformed);
        return result;
    }

    @Override
    public F get(int index) {
        B raw = backend.get(index);
        F result = converter.convert(raw);
        return result;
    }

    @Override
    public F set(int index, F element) {
        B raw = converter.reverse().convert(element);
        backend.set(index, raw);
        return element;
    }

    @Override
    public void add(int index, F element) {
        addAll(index, Collections.singleton(element));
    }

    @Override
    public F remove(int index) {
        B raw = backend.remove(index);
        F result = converter.convert(raw);
        return result;
    }

    @Override
    public int indexOf(Object o) {
        // TODO Check whether this cast is possible
        int result;
        try {
            F front = (F)o;
            B raw = converter.reverse().convert(front);
            result = backend.indexOf(raw);

        } catch(ClassCastException e) {
            result = -1;
            // Treat class cast exception as 'item does not exist in listt'
        }

        return result;
    }

    @Override
    public int lastIndexOf(Object o) {
        // TODO Check whether this cast is possible
        int result;
        try {
            F front = (F)o;
            B raw = converter.reverse().convert(front);
            result = backend.lastIndexOf(raw);

        } catch(ClassCastException e) {
            result = -1;
            // Treat class cast exception as 'item does not exist in listt'
        }

        return result;
    }

    @Override
    public ListIterator<F> listIterator() {
        ListIterator<B> core = backend.listIterator();

        ListIterator<F> result = new ListIteratorFromConverter<F, B>(core, converter);
        return result;
    }

    @Override
    public ListIterator<F> listIterator(int index) {
        ListIterator<B> core = backend.listIterator();

        ListIterator<F> result = new ListIteratorFromConverter<F, B>(core, converter);
        return result;
    }

    @Override
    public List<F> subList(int fromIndex, int toIndex) {
        List<B> subList = backend.subList(fromIndex, toIndex);
        List<F> result = new ListFromConverter<>(subList, converter);
        return result;
    }

//    public static <F, B, C extends Collection<B>> List<F> createSafe(Collection<B> backend, Converter<B, F> converter) {
//        Collection<B> safeBackend = ConverterUtils.safeCollection(backend, converter);
//        List<F> result = new ListFromConverter<>(safeBackend, converter);
//        return result;
//    }

}
