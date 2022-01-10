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
    protected List<ConvertFunctionRaw> converters = new ArrayList<>();

    protected MemoizedBiFunction<Class<?>, Class<?>, ConvertFunctionRaw> cachedFind = MemoizedBiFunctionImpl.create(this::findUncached);

    public synchronized void register(ConvertFunctionRaw converter) {
        cachedFind.clearCache();
        converters.add(converter);
    }

    protected ConvertFunctionRaw findUncached(Class<?> src, Class<?> tgt) {
        List<ConvertFunctionRaw> converters = findMatches(src, tgt);

        ConvertFunctionRaw result;
        if (converters.size() > 1) {
            throw new RuntimeException(String.format("Multiple converters found for (%s -> %s): %s", src, tgt, converters));
        } else if (converters.isEmpty()) {
            result = null;
        } else {
            result = converters.iterator().next();
        }
        return result;
    }

    protected List<ConvertFunctionRaw> findMatches(Class<?> src, Class<?> tgt) {

        List<ConvertFunctionRaw> cands = converters.stream()
            .map(c -> new SimpleEntry<>(c, ClassUtils.getDistance(src, c.getFrom())))
            .filter(e -> e.getValue() != null)
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList());

        List<ConvertFunctionRaw> matches = cands.stream()
            .map(c -> new SimpleEntry<>(c, ClassUtils.getDistance(c.getTo(), tgt)))
            .filter(e -> e.getValue() != null)
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList());

        return matches;
    }

    @Override
    public ConvertFunctionRaw getConverter(Class<?> from, Class<?> to) {
        return cachedFind.apply(from, to);
    }
}
