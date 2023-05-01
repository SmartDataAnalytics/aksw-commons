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

package org.aksw.commons.tuple.accessor;

import org.aksw.commons.tuple.bridge.TupleBridge;

/**
 * A forwarding tuple accessor that can remap indices
 * to shuffle the components
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class TupleAccessorRemap<TupleType, ComponentType>
    implements TupleBridge<TupleType, ComponentType>
{
    protected TupleBridge<TupleType, ComponentType> delegate;
    protected int[] remap;

    public TupleAccessorRemap(TupleBridge<TupleType, ComponentType> delegate, int[] remap) {
        super();
        this.delegate = delegate;
        this.remap = remap;
    }

    @Override
    public int getDimension() {
        return remap.length;
    }

    @Override
    public ComponentType get(TupleType domainObject, int idx) {
        int remappedIdx = remap[idx];
        ComponentType result = delegate.get(domainObject, remappedIdx);
        return result;
    }

    @Override
    public <T> TupleType build(T obj, TupleAccessor<? super T, ? extends ComponentType> accessor) {
        //return delegate.restore(obj, accessor);
        throw new RuntimeException("implement me");

//        if (this.getRank() != delegate.getRank()) {
//            throw new IllegalStateException("Cannot delegate restoration of a domain object from a remapped tuple if the ranks differ");
//        }

//        validateRestoreArg(components);

//        int l = accessor.getRank();
//        @SuppressWarnings("unchecked")
//        ComponentType[] remppadComponents = (ComponentType[])new Object[l];
//
//        for (int i = 0; i < l; ++i) {
//            remppadComponents[remap[i]] = components[i];
//        }
//
//        TupleType result = delegate.restore(remppadComponents);
//        return result;
    }
}
