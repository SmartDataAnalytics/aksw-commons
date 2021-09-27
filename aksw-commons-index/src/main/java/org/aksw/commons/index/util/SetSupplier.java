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

package org.aksw.commons.index.util;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Helper interface for creating new set instances with automatically inferred
 * types
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public interface SetSupplier {
    <T> Set<T> get();

//    @SuppressWarnings("unchecked")
//    default <X extends Set<?>> SetSupplier<X> cast() {
//        return (SetSupplier<X>)this;
//    }


    /**
     * Wrap this map supplier such that any supplied map becomes wrapped
     * as a cmap.
     *
     * @return
     */
    default <X> CSetSupplier<X> wrapAsCSet(Supplier<X> valueSupplier) {
        return SetSuppliers.wrapAsCSet(this, valueSupplier);
    }
}
