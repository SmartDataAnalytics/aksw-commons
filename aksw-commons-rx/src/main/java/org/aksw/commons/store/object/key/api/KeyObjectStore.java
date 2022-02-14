package org.aksw.commons.store.object.key.api;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.txn.api.TxnApi;

public interface KeyObjectStore
{
    void put(Iterable<String> keySegments, Object obj) throws IOException;
    <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException;
    <T> T computeIfAbsent(Iterable<String> keySegments, Callable<T> initializer) throws IOException, ClassNotFoundException, ExecutionException;
}
