package org.aksw.commons.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.Sets;

public class Fold<T> {
    private List<T> train;
    private List<T> validate;

    public Fold(List<T> train, List<T> validate) {
        super();
        this.train = train;
        this.validate = validate;
    }

    public List<T> getTrain() {
        return train;
    }

    public List<T> getValidate() {
        return validate;
    }


    public static <T> List<T> concatExclude(List<T> partitions, int j) {
    	List<T> result = new ListLazy<>((i) -> partitions.get(i >= j ? i + 1 : i), Math.max(partitions.size() - 1, 0));
    	return result;
    }

    public static <T> List<Fold<T>> createFolds(List<T> items, int n) {
    	List<List<T>> partitions = Lists.distribute(items, n);


    	List<Fold<T>> result = new ListLazy<>(
    			(i) -> new Fold<T>(new ListConcat<T>(concatExclude(partitions, i)), partitions.get(i)),
    			n);

    	return result;
    }

    public static <T> List<Fold<T>> createFolds(List<T> pos, List<T> neg, int n) {
    	List<List<T>> posParts = Lists.distribute(pos, n);

    	System.out.println(posParts);

    	List<List<T>> negParts = Lists.distribute(neg, n);
    	System.out.println(negParts);


    	List<Fold<T>> result = new ListLazy<>(
    			(i) -> new Fold<T>(
    					new ListConcat<T>(Arrays.asList(
    							posParts.get(i),
    							negParts.get(i)
    					)),
						new ListConcat<T>(Arrays.asList(
								new ListConcat<T>(concatExclude(posParts, i)),
								new ListConcat<T>(concatExclude(negParts, i))
						))),
    			n);

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
