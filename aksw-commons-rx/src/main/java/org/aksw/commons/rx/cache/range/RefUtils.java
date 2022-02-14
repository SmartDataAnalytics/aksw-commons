package org.aksw.commons.rx.cache.range;

import java.util.concurrent.CompletableFuture;

import org.aksw.commons.util.ref.Ref;

public class RefUtils {

	/**
	 * Create a proxy over a reference to an object whose class implements AutoCloseable.
	 * All methods (except close) delegate to the referent.
	 * The proxy's close() method closes the reference itself.
	 * 
	 * @param <T>
	 * @param clz
	 * @param ref
	 */
	public static <T extends AutoCloseable> T proxyRefFuture(Class<T> clz, Ref<CompletableFuture<? extends T>> ref) {
		
//        ByteBuddy bb = new ByteBuddy();
//        Builder<?> builder;
//        if(clazz.isInterface()) {
//            builder = bb
//                .subclass(ResourceProxyBase.class)
//                .implement(clazz);
//
//        for(Entry<Method, BiFunction<Object, Object[], Object>> e : methodImplMap.entrySet()) {
//            builder = builder.method(ElementMatchers.anyOf(e.getKey()))
//                    .intercept(InvocationHandlerAdapter.of((obj, method, args) -> {
//                        Object r = e.getValue().apply(obj, args);
//                        return r;
//                    }));
//        }

		return null;
	}
}
