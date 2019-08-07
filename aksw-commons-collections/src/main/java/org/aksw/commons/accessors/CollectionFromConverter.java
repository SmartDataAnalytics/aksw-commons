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
		protected Converter<T, U> converter;

		public IteratorFromConverter(I core, Converter<T, U> converter) {
			super();
			this.core = core;
			this.converter = converter;
		}

		@Override
		public T next() {
			U raw = core.next();
			T result = converter.reverse().convert(raw);
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
	protected Converter<F, B> converter;
	
	public CollectionFromConverter(C backend, Converter<F, B> converter) {
//		Objects.requireNonNull(backend);
//		Objects.requireNonNull(converter);
		
		this.backend = backend;
		this.converter = converter;
	}

	@Override
	public boolean add(F value) {
		B item = converter.convert(value);
		boolean result = backend.add(item);
		
		return result;
	}
	
	@Override
	public boolean contains(Object o) {
		boolean result = false;
		try {
			B item = converter.convert((F)o);
			result = backend.contains(item);
		} catch(ClassCastException e) {
			
		}
		
		return result;

	}
	
	@Override
	public boolean remove(Object o) {
		boolean result = false;
		try {
			B item = converter.convert((F)o);
			result = backend.remove(item);
		} catch(ClassCastException e) {
			
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
					F f = converter.reverse().convert(b);
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