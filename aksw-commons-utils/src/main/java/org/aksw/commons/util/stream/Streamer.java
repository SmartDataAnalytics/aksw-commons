/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.aksw.commons.util.stream;

import java.util.function.Function;
import java.util.stream.Stream;


/**
 * A streamer returns a stream of items from a collection-like object (referred to as a 'store')
 * given as the argument. For example, in the case of a map data structure a
 * streamer may return any of its key, value or entry set. If the actual
 * type of the collection is not known the {{@link #streamRaw(Object)} method
 * should be used.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <V> The type of the store from which to stream items
 * @param <T> The item type to stream
 */
@FunctionalInterface
public interface Streamer<V, T> {

    Stream<T> stream(V store);

    /**
     * Convenience method that yields a new Streamer that wraps this streamer such
     * that its streams are transformed by the given streamTransform.
     *
     * @param <U>
     * @param streamTransform
     * @return
     */
    default <U> Streamer<V, U> mapStream(StreamFunction<T, U> streamTransform) {
        return store -> streamTransform.apply(stream(store));
    }

    /**
     * Convenience method to derive a new streamer that transforms the items
     * emitted by the original streamer.
     *
     * @param <U>
     * @param itemTransform
     * @return
     */
    default <U> Streamer<V, U> mapItems(Function<T, U> itemTransform) {
        return store -> stream(store).map(itemTransform);
    }

    /**
     * Delegate to {@link #stream(Object)}.
     *
     * @param store
     * @return A stream from given store. Arguments of incorrect type will cause a {@link ClassCastException}.
     *
     */
    @SuppressWarnings("unchecked")
    default Stream<T> streamRaw(Object store) {
        return stream((V)store);
    }
}

