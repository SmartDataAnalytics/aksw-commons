package org.aksw.commons.util.obj;

/** This interface is useful for builders of the form below, in order to hide
 * the uncheck cast warning of {@code return (X)this;}.
 *
 * <pre>{@code
 * interface MyBuilderMixin<X extends MyBuilderMixin<X>> extends HasSelf<X> {
 *   default X someMethod() {
 *      // Do something
 *      return self();
 *   }
 * }
 * }</pre>
 */
public interface HasSelf<T> {
    @SuppressWarnings("unchecked")
    default T self() {
        return (T)this;
    }
}
