package org.aksw.commons.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.base.Converter;

public class ConvertingCollection<F, B, C extends Collection<B>>
    extends AbstractCollection<F>
{
    protected C backend;
    protected Converter<B, F> converter;

    public ConvertingCollection(C backend, Converter<B, F> converter) {
//		Objects.requireNonNull(backend);
//		Objects.requireNonNull(converter);

        this.backend = backend;
        this.converter = converter;
    }

    public C getBackend() {
        return backend;
    }

    @Override
    public boolean add(F value) {
        B item = converter.reverse().convert(value);
        boolean result = backend.add(item);

        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result;
        try {
            @SuppressWarnings("unchecked")
            B item = converter.reverse().convert((F)o);
            result = backend.remove(item);
        } catch(ClassCastException e) {
            result = false;
        }

        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result;
        try {
            @SuppressWarnings("unchecked")
            B item = converter.reverse().convert((F)o);
            result = backend.contains(item);
        } catch(ClassCastException e) {
            result = false;
        }

        return result;

    }

    @Override
    public Iterator<F> iterator() {
        Iterator<B> baseIt = backend.iterator();

        Iterator<F> result = new ConvertingIterator<>(baseIt, converter);
        return result;
    }

    @Override
    public int size() {
        return backend.size();
    }

    /**
     * Wraps the backend with a guava filteredCollection that filters out
     * items for which the converter raises an exception
     *
     * @param <F>
     * @param <B>
     * @param <C>
     * @param backend
     * @param converter
     * @return
     */
    public static <F, B, C extends Collection<B>> Collection<F> createSafe(Collection<B> backend, Converter<B, F> converter) {
        Collection<B> safeBackend = MutableCollectionViews.filteringCollection(backend, converter);
        Collection<F> result = new ConvertingCollection<>(safeBackend, converter);
        return result;
    }

    public static <O> Stream<O> convertRaw(Collection<?> c, Converter<?, O> converter) {
        @SuppressWarnings("rawtypes")
        Converter raw = converter;

        Stream<O> itemStream = c.stream().flatMap(item -> {
            Stream<O> r;
            try {
                @SuppressWarnings("unchecked")
                O b = (O)raw.convert(item);
                r = Stream.of(b);
            } catch (ClassCastException e) {
                r = Stream.empty();
            }
            return r;
        })
        .map(x -> (O)x);

        return itemStream;
    }
}

