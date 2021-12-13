package org.aksw.commons.util.convert;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.commons.util.memoize.MemoizedBiFunction;
import org.aksw.commons.util.memoize.MemoizedBiFunctionImpl;
import org.aksw.commons.util.reflect.ClassUtils;

public class ConverterRegistryImpl
    implements ConverterRegistry
{
    protected List<Converter> converters = new ArrayList<>();

    protected MemoizedBiFunction<Class<?>, Class<?>, Converter> cachedFind = MemoizedBiFunctionImpl.create(this::findUncached);

    public synchronized void register(Converter converter) {
        cachedFind.clearCache();
        converters.add(converter);
    }

    protected Converter findUncached(Class<?> src, Class<?> tgt) {
        List<Converter> converters = findMatches(src, tgt);

        Converter result;
        if (converters.size() > 1) {
            throw new RuntimeException(String.format("Multiple converters found for (%s -> %s): %s", src, tgt, converters));
        } else if (converters.isEmpty()) {
            result = null;
        } else {
            result = converters.iterator().next();
        }
        return result;
    }

    protected List<Converter> findMatches(Class<?> src, Class<?> tgt) {

        List<Converter> cands = converters.stream()
            .map(c -> new SimpleEntry<>(c, ClassUtils.getDistance(src, c.getFrom())))
            .filter(e -> e.getValue() != null)
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList());

        List<Converter> matches = cands.stream()
            .map(c -> new SimpleEntry<>(c, ClassUtils.getDistance(c.getTo(), tgt)))
            .filter(e -> e.getValue() != null)
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList());

        return matches;
    }

    @Override
    public Converter getConverter(Class<?> from, Class<?> to) {
        return cachedFind.apply(from, to);
    }
}
