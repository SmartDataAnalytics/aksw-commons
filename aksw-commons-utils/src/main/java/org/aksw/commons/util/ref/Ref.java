package org.aksw.commons.util.ref;

import java.util.function.Function;

/**
 * Interface for nested references.
 * References allow for sharing an entity across several clients and
 * deferring the release of that entity's resources immediately to the point
 * in time when the last reference is released. The main use case is for memory paging
 * such that if several threads request the same page only one physical buffer is handed out
 * from a cache - conversely, as long as a page is still in used by a client, cache eviction
 * and synchronization may be suppressed.
 *
 *
 * Terminology:
 * A reference is <b>closed</b> when {@link #close()} was called; <b>open</> otherwise.
 * A reference is <b>alive</b> when it is <b>open</b> and/or any of the child refs acquired from it are still <b>alive</b>.
 * A reference is <b>released</b> (dead) as soon it is no longer alive. This immediately triggers its release action.
 *   Implementation note: At present the alive-check and release action are assumed to run synchronously. As such there
 *   is no transition phase ('dying' or 'releasing'). This could be added in the future.
 * 
 *
 * @author raven
 *
 * @param <T>
 */
public interface Ref<T>
    extends AutoCloseable
{
    /**
     * Get the root reference
     */
    Ref<T> getRootRef();

    /**
     * Get the referent only iff this ref instance has not yet been closed.
     * This method fails for closed alive refs.
     * A closed reference is alive if it has unclosed child references.
     *
     * For most use cases the referent should be accessed using this method.
     *
     * @return The referent
     */
    T get();


    /**
     * Return the object on which reference acquisition, release and the close action
     * are synchronized on.
     */
    Object getSynchronizer();



    /**
     * Acquire a new reference with a given comment object
     * Acquiration fails if isAlive() returns false
     *
     * @return
     */
    Ref<T> acquire(Object purpose);

    default Ref<T> acquire() {
        return acquire(null);
    }

//    default Ref<T> acquireAlive() {
//        return acquireAlive(null);
//    }



    /**
     * A reference may itself be closed, but references to it may keep it alive
     *
     * @return true iff either this reference is not closed or there exists any acquired reference.
     */
    boolean isAlive();

    /**
     * Check whether this reference is closed
     */
    boolean isClosed();


    // TODO The throws declaration of Autoclose can be a pain to work with - override it?
    @Override
    void close();


    /**
     * Optional operation.
     *
     * References may expose where they were acquired
     *
     * @return
     */
    StackTraceElement[] getAcquisitionStackTrace();


    StackTraceElement[] getCloseStackTrace();

    StackTraceElement[] getCloseTriggerStackTrace();

    /**
     * Return a ref with a new referent obtained by mapping this ref's value with mapper.
     * Closing the returned ref closes the original one. Synchronizes on the same object as this ref.
     */
    default <X> Ref<X> acquireMapped(Function<? super T, ? extends X> mapper) {
    	Ref<T> base = acquire();
    	X mapped = mapper.apply(base.get());
    	return RefImpl.create(mapped, base.getSynchronizer(), base::close);
    }
}
