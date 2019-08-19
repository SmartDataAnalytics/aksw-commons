package org.aksw.commons.collections.generator;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Generator which allows giving ids back for re-use.
 * 
 * @author raven
 *
 * @param <T>
 */
public class GeneratorLendingImpl<T>
	extends GeneratorForwarding<T>
	implements GeneratorLending<T>
{
	protected Supplier<? extends Collection<T>> collectionSupplier;
	protected Collection<T> freeIds;
	
	protected T current;
	
//	public GeneratorLendingImpl(Generator<T> delegate) {
//		this(delegate, null);
//	}

	public GeneratorLendingImpl(Generator<T> delegate, Supplier<? extends Collection<T>> collectionSupplier) {
		this(delegate, collectionSupplier.get(), collectionSupplier);
	}

	public GeneratorLendingImpl(Generator<T> delegate, Collection<T> freeIds, Supplier<? extends Collection<T>> collectionSupplier) {
		super(delegate);
		this.freeIds = freeIds;
		this.collectionSupplier = collectionSupplier;
	}

	@Override
	public boolean giveBack(T item) {
		freeIds.add(item);
		return true;
	}

	@Override
	public T next() {
		Iterator<T> it = freeIds.iterator();
		if(it.hasNext()) {
			current = it.next();
			it.remove();
		} else {
			current = delegate.next();
		}
		
		return current;
	}
	
	@Override
	public T current() {
		return current;
	}
	
	@Override
	public GeneratorForwarding<T> clone() {
		Collection<T> copy = collectionSupplier.get();
		copy.addAll(freeIds);

		return new GeneratorLendingImpl<>(delegate.clone(), copy, collectionSupplier);
	}

	public static <T extends Comparable<T>> GeneratorLendingImpl<T> createSorted(Generator<T> delegate) {
		return new GeneratorLendingImpl<T>(delegate, TreeSet::new);
	}

	public static GeneratorLendingImpl<Integer> createInt() {
		return createInt(0);
	}

	public static GeneratorLendingImpl<Integer> createInt(int initialNext) {
		return new GeneratorLendingImpl<>(GeneratorFromFunction.createInt(initialNext), TreeSet::new);
	}
	
	public static GeneratorLending<String> createPrefixedInt(String prefix, int offset) {
		return new GeneratorLendingFromConverter<>(createInt(offset), Converters.prefixIntToStr(prefix));
	}
	
}
