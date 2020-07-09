package org.aksw.commons.collections;

import java.util.ListIterator;

import com.google.common.base.Converter;

public class ConvertingListIterator<T, U, I extends ListIterator<U>>
        extends ConvertingIterator<T, U, ListIterator<U>>
        implements ListIterator<T>
    {
        public ConvertingListIterator(I core, Converter<U, T> converter) {
            super(core, converter);
        }

//        @Override
//        public boolean hasNext() {
//            boolean result = core.hasNext();
//            return result;
//        }
//
//        @Override
//        public T next() {
//            U raw = core.next();
//            T result = converter.convert(raw);
//            return result;
//        }

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
    }