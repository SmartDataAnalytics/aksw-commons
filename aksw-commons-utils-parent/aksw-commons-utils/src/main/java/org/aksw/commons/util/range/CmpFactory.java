package org.aksw.commons.util.range;

import java.util.Comparator;
import java.util.Objects;


/** Factory for the creation of wrappers that make values comparable w.r.t. to the factory's configured comparator. */
public class CmpFactory<T>
{
    private static final Cmp<?> MIN = new CmpMin<>();
    private static final Cmp<?> MAX = new CmpMax<>();

    protected final Comparator<T> comparator;

    public CmpFactory(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public static <T> CmpFactory<T> of(Comparator<T> comparator) {
        return new CmpFactory<>(comparator);
    }

    public Cmp<T> wrap(T value) {
        return new CmpValue<>(comparator, value);
    }

    @SuppressWarnings("unchecked")
    public Cmp<T> minValue() {
        return (Cmp<T>)MIN;
    }

    @SuppressWarnings("unchecked")
    public Cmp<T> maxValue() {
        return (Cmp<T>)MAX;
    }

    public static class CmpMin<T>
        implements Cmp<T>
    {
        @Override public boolean isMin() { return true; }
        @Override public boolean isMax() { return false; }
        @Override public boolean hasValue() { return false; }
        @Override public T getValue() { throw new UnsupportedOperationException(); }

        @Override
        public int compareTo(Cmp<T> o) {
            int result = o instanceof CmpMin
                ? 0
                : -1;
            return result;
        }
    }

    public static class CmpMax<T>
        implements Cmp<T>
    {
        @Override public boolean isMin() { return false; }
        @Override public boolean isMax() { return true; }
        @Override public boolean hasValue() { return false; }
        @Override public T getValue() { throw new UnsupportedOperationException(); }

        @Override
        public int compareTo(Cmp<T> o) {
            int result = o instanceof CmpMax
                ? 0
                : 1;
            return result;
        }
    }


    public static class CmpValue<T>
        implements Cmp<T>
    {
        @Override public boolean isMin() { return false; }
        @Override public boolean isMax() { return false; }
        @Override public boolean hasValue() { return true; }

        protected final T value;
        protected final Comparator<T> comparator;

        public CmpValue(Comparator<T> comparator, T value) {
            super();
            this.comparator = comparator;
            this.value = value;
        }

        @Override public T getValue() { return value; }

        @Override
        public int compareTo(Cmp<T> o) {
            int result = o instanceof CmpMin
                    ? 1
                    : o instanceof CmpMax
                        ? -1
                        : comparator.compare(value, ((CmpValue<T>)o).value);
            return result;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CmpValue<?> other = (CmpValue<?>) obj;
            return Objects.equals(value, other.value);
        }

        @Override
        public String toString() {
            return "CmpValue [value=" + value + "]";
        }
    }
}
