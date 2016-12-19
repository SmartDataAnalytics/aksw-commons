package org.aksw.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Fold<T> {
    private Set<T> train;
    private Set<T> validate;

    public Fold(Set<T> train, Set<T> validate) {
        super();
        this.train = train;
        this.validate = validate;
    }

    public Set<T> getTrain() {
        return train;
    }

    public Set<T> getValidate() {
        return validate;
    }

    public static <T> List<Fold<T>> createFolds(Collection<T> items, int n) {
    	int size = items.size() / n;

    	// TODO Avoid copy if items is already a list
    	List<Set<T>> parts = Lists.partition(new ArrayList<T>(items), size).stream()
    			.map(p -> new LinkedHashSet<T>(p))
    			.collect(Collectors.toList());

    	// TODO Switch to a lazy list implementation
    	List<Fold<T>> result = IntStream.range(0, parts.size()).boxed()
    			.map(i -> new Fold<>(createFold(i, parts), parts.get(i)))
    			.collect(Collectors.toList());

    	return result;
    }

    public static <T> Set<T> createFold(int i, List<Set<T>> parts) {
    	Set<T> result = StreamUtils.zipWithIndex(parts.stream())
    		.filter(indexed -> indexed.getIndex() != i)
    		.map(indexed -> indexed.getValue())
    		.reduce(Sets::union).orElse(Collections.emptySet());

    	return result;
    }

    /**
     * Utility function for casting the items back to their appropriate type.
     * Useful in cases where a fold is used in APIs which work with classes:
     *
     * <pre>
     * {@code
     * Fold<?> fold = someMap.get(Fold.class);
     * Fold<Integer> fold.itemsAs(Integer.class)
     *
     * // As a one-liner:
     * Fold<Integer> someMap.get(Fold.class).itemsAs(Integer.class)
     * @}
     * </pre>
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
	public <X> Fold<X> itemsAs(Class<X> clazz) {
    	return (Fold<X>)this;
    }

	@Override
	public String toString() {
		return "Fold [train=" + train + ", validate=" + validate + "]";
	}
}
