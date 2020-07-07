package org.aksw.commons.accessors;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.base.Converter;

public class CollectionFromConverter<F, B, C extends Collection<B>>
    extends AbstractCollection<F>
{
    public static class IteratorFromConverter<T, U, I extends Iterator<U>>
        implements Iterator<T>
    {
        protected I core;
        protected Converter<U, T> converter;

        public IteratorFromConverter(I core, Converter<U, T> converter) {
            super();
            this.core = core;
            this.converter = converter;
        }

        @Override
        public T next() {
            U raw = core.next();
            T result = converter.convert(raw);
            return result;
        }

        @Override
        public boolean hasNext() {
            boolean result = core.hasNext();
            return result;
        }

        @Override
        public void remove() {
            core.remove();
        }
    }

    protected C backend;
    protected Converter<B, F> converter;

    public CollectionFromConverter(C backend, Converter<B, F> converter) {
//		Objects.requireNonNull(backend);
//		Objects.requireNonNull(converter);

        this.backend = backend;
        this.converter = converter;
    }

    @Override
    public boolean add(F value) {
        B item = converter.reverse().convert(value);
        boolean result = backend.add(item);

        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result;
        try {
            B item = converter.reverse().convert((F)o);
            result = backend.contains(item);
        } catch(ClassCastException e) {
            result = false;
        }

        return result;

    }

    @Override
    public boolean remove(Object o) {
        boolean result;
        try {
            B item = converter.reverse().convert((F)o);
            result = backend.remove(item);
        } catch(ClassCastException e) {
            result = false;
        }

        return result;
    }

    @Override
    public Iterator<F> iterator() {
        Iterator<B> baseIt = backend.iterator();

        Iterator<F> result = new IteratorFromConverter<>(baseIt, converter);
        return result;
    }

    @Override
    public int size() {
        return backend.size();
    }

    /**
     * Wraps the backend with a guava Collection2.filteredCollection that filters out
     * items for whith the converter raises an exception
     *
     * @param <F>
     * @param <B>
     * @param <C>
     * @param backend
     * @param converter
     * @return
     */
    public static <F, B, C extends Collection<B>> Collection<F> createSafe(Collection<B> backend, Converter<B, F> converter) {
        Collection<B> safeBackend = ConverterUtils.safeCollection(backend, converter);
        Collection<F> result = new CollectionFromConverter<>(safeBackend, converter);
        return result;
    }
}

