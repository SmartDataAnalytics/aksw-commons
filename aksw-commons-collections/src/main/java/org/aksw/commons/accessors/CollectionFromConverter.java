package org.aksw.commons.accessors;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.aksw.commons.collections.SinglePrefetchIterator;

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

		return new SinglePrefetchIterator<F>() {
			@Override
			protected F prefetch() throws Exception {
				while(baseIt.hasNext()) {
					B b = baseIt.next();
					F f;
					try {
						f = converter.convert(b);
					} catch(Exception e) {
						/* Ignore items that fail to convert */
						continue;
					}
					return f;
				}
				return finish();
			}
			@Override
			public void doRemove(F item) { baseIt.remove(); }
		};
	}
	
	@Override
	public int size() {
		return backend.size();
	}
}