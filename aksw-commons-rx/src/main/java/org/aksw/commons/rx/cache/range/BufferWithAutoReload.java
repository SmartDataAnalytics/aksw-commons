package org.aksw.commons.rx.cache.range;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.IntSupplier;

import org.aksw.commons.store.object.key.api.ObjectResource;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.api.ObjectStoreConnection;
import org.aksw.commons.txn.api.TxnApi;
import org.aksw.commons.util.array.ArrayBuffer;
import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.Buffer;


public class BufferWithAutoReload<A>
	implements BufferDelegate<A>
{
	protected ObjectStore objectStore;
	protected String fileName;
	
	protected ArrayOps<A> arrayOps;

	// If there is no data to load then create an empty array of size 'freshSize'
	protected int freshSize;
	
	protected int generationHere;
	protected IntSupplier generationNowSupplier;

	protected CompletableFuture<Buffer<A>> future;

	public BufferWithAutoReload(ObjectStore objectStore, String fileName, ArrayOps<A> arrayOps, int freshSize, int generationHere,
			IntSupplier generationNowSupplier) {
		super();
		this.objectStore = objectStore;
		this.fileName = fileName;
		this.arrayOps = arrayOps;
		this.freshSize = freshSize;
		this.generationHere = generationHere;
		this.generationNowSupplier = generationNowSupplier;
	}

	public void reload() {
		int generationNow = generationNowSupplier.getAsInt();
		if (generationHere != generationNow || future == null) {
			future = CompletableFuture.supplyAsync(() -> {
				Buffer<A> r;
				try (ObjectStoreConnection conn = objectStore.getConnection()) {
					r = TxnApi.execRead(conn, () -> {
						ObjectResource res = conn.access(fileName);
						A array = res.reload();
						if (array == null) {
							array = arrayOps.create(freshSize);
						}
						
						Buffer<A> arrayBuffer = ArrayBuffer.create(arrayOps, array);
						// RangeBufferImpl<A> rangeBuffer = new RangeBufferImpl<A>(globalRanges, offsetInRanges, arrayBuffer);
						return arrayBuffer;
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				return r;
			});
		}		
	}
	
	@Override
	public Buffer<A> getDelegate() {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
}
