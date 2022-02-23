package org.aksw.commons.store.object.key.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.aksw.commons.cache.async.AsyncClaimingCache;
import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.store.object.key.api.ObjectResource;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.api.ObjectStoreConnection;
import org.aksw.commons.store.object.path.api.ObjectSerializer;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.commons.txn.impl.FileSync;
import org.aksw.commons.txn.impl.FileSyncImpl;
import org.aksw.commons.txn.impl.PathDiffState;
import org.aksw.commons.txn.impl.TxnHandler;
import org.aksw.commons.txn.impl.TxnMgrImpl;
import org.aksw.commons.util.array.Array;
import org.aksw.commons.util.ref.RefFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;

public class ObjectStoreImpl
	implements ObjectStore
{
	protected TxnMgr txnMgr;
	protected TxnHandler txnHandler;
	protected ObjectSerializer objectSerializer;
	
	// The content cache can save object state to disk
	// The content cache can only be accessed via transactions
	// and a write transaction can own specific entries in the content cache
	// It is write transaction's responsibility to ensure the cache is in sync with the disk upon commit / rollback!
	// Read transactions must not access entries which may be modified by a write transaction (MRSW locking)
	protected AsyncClaimingCache<org.aksw.commons.path.core.Path<String>, ObjectInfo> contentCache;
	
	
	// Accessors provide a thread-safe api to read metadata about a resource and load the content
	// Accessors should be generally cheap to create but they can hold certain state that should only exist once
	// to avoid redundant checks or inconsistent data (e.g. when content was loaded) so it makes sense to manage
	// them in a claiming cache
	protected AsyncClaimingCache<org.aksw.commons.path.core.Path<String>, ObjectResource> accessorCache; // accessorCache
	

	
	

	public ObjectStoreImpl(TxnMgr txnMgr, TxnHandler txnHandler, ObjectSerializer objectSerializer,
			AsyncClaimingCache<org.aksw.commons.path.core.Path<String>, ObjectInfo> contentCache,
			AsyncClaimingCache<org.aksw.commons.path.core.Path<String>, ObjectResource> accessorCache) {
		super();
		this.txnMgr = txnMgr;
		this.txnHandler = txnHandler;
		this.objectSerializer = objectSerializer;
		this.contentCache = contentCache;
		this.accessorCache = accessorCache;
	}

	
	@Override
	public PathDiffState fetchRecencyStatus(org.aksw.commons.path.core.Path<String> key) {
		Path path = PathUtils.resolve(txnMgr.getRootPath().resolve(txnMgr.getResRepo().getRootPath()), key.getSegments());
		FileSync fileSync = FileSyncImpl.create(path, false);
		PathDiffState result = FileSyncImpl.getState(fileSync);
		
		return result;
	}

	public static ObjectStore create(Path rootPath, ObjectSerializer objectSerializer) {
		TxnMgr txnMgr = TxnMgrImpl.createSimple(rootPath);
		TxnHandler txnHandler = new TxnHandler(txnMgr);
		
		try {
			txnHandler.cleanupStaleTxns();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

//		AsyncClaimingCache<Array<String>, ObjectResource> accessorCache = AsyncClaimingCacheImpl
//				.<Array<String>, ObjectResource>newBuilder(Caffeine.newBuilder())
//				.setCacheLoader(new CacheLoader<Array<String>, ObjectResource>() {
//					
//					@Override
//					public @Nullable ObjectResource load(Array<String> key) throws Exception {
//
//						// Lock the file while we read from it
//						Txn txn = txnMgr.newTxn(true, false);
//						TxnResourceApi api = txn.getResourceApi(key.getArray());
//						api.declareAccess();
//						api.lock(false);
//						Path path = api.getFileSync().getCurrentPath();
//
//						PathDiffState status = FileSyncImpl.getState(api.getFileSync());
//
//						Object obj;
//						try (InputStream in = Files.newInputStream(path)) {
//							obj = objSer.read(in);
//						}
//						
//						txnHandler.commit(txn);
//						
//						return new ObjectInfo(obj, obj.hashCode(), status);
//					}
//					
//					@Override
//					public @Nullable ObjectInfo reload(Array<String> key, ObjectInfo oldValue) throws Exception {
//						// TODO Auto-generated method stub
//						return CacheLoader.super.reload(key, oldValue);
//					}
//				})
//				.build();
//		
		
		AsyncClaimingCache<org.aksw.commons.path.core.Path<String>, ObjectInfo> contentCache = AsyncClaimingCacheImpl
				.<org.aksw.commons.path.core.Path<String>, ObjectInfo>newBuilder(Caffeine.newBuilder())
				.setCacheLoader(new CacheLoader<org.aksw.commons.path.core.Path<String>, ObjectInfo>() {
					
					@Override
					public @Nullable ObjectInfo load(org.aksw.commons.path.core.Path<String> key) throws Exception {

						// Lock the file while we read from it
						Txn txn = txnMgr.newTxn(true, false);
						
						TxnResourceApi api = txn.getResourceApi(key);
						api.declareAccess();
						api.lock(false);
						Path path = api.getFileSync().getCurrentPath();

						PathDiffState status = FileSyncImpl.getState(api.getFileSync());

						Object obj;
						if (status.getCurrentState().getTimestamp() == null) { // file not exists; we should make that more explicit
							obj = null;
						} else {
							try (InputStream in = Files.newInputStream(path)) {
								obj = objectSerializer.read(in);
							}
						}						
						txnHandler.commit(txn);
						
						return new ObjectInfo(obj, Objects.hashCode(obj), status);
					}
					
					@Override
					public @Nullable ObjectInfo reload(org.aksw.commons.path.core.Path<String> key, ObjectInfo oldValue) throws Exception {
						// TODO Custom reload handling?
						return CacheLoader.super.reload(key, oldValue);
					}
				})
				.setEvictionListener((k, v, c)-> {
					try {
						save(txnMgr, objectSerializer, txnHandler, k, v);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.build();
				
		
		return new ObjectStoreImpl(txnMgr, txnHandler, objectSerializer, contentCache, null);
	}
	
	protected static void save(TxnMgr txnMgr, ObjectSerializer objectSerializer, TxnHandler txnHandler, org.aksw.commons.path.core.Path<String> key, ObjectInfo v) throws IOException {
		Txn txn;
		txn = txnMgr.newTxn(true, true);
		TxnResourceApi api = txn.getResourceApi(key);
		api.declareAccess();
		api.lock(true);
		api.getFileSync().putContent(out -> {
			try {
				objectSerializer.write(v.getObject(), out);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		txnHandler.commit(txn);
	}
	

	@Override
	public RefFuture<ObjectInfo> claim(org.aksw.commons.path.core.Path<String> key) {
		return contentCache.claim(key);
	}

	
	@Override
	public ObjectStoreConnection getConnection() {
		return new ObjectStoreConnectionImpl();
	}

	
	@Override
	public void close() throws Exception {
	}

	
	class ObjectStoreConnectionImpl
		implements ObjectStoreConnection {

		protected Txn txn = null;
		
		@Override
		public void begin(boolean write) {
			if (txn != null) {
				throw new RuntimeException("Already in a txn");
			}
			
			try {
				txn = txnMgr.newTxn(true, write);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void commit() {
			Objects.requireNonNull(txn, "Cannot commit because there is no active transaction; Perhaps missing call to .begin()?");
			txnHandler.commit(txn);
			txn = null;
		}

		@Override
		public void abort() {
			Objects.requireNonNull(txn, "Cannot abort because there is no active transaction; Perhaps missing call to .begin()?");
			txnHandler.abort(txn);
			txn = null;
		}

		@Override
		public ObjectResource access(org.aksw.commons.path.core.Path<String> keySegments) {
			TxnResourceApi api = txn.getResourceApi(keySegments);
			return new Kor(api);
		}
		
		
		@Override
		public void close() throws Exception {
			if (txn != null) {
				txnHandler.rollbackOrEnd(txn);
				txn = null;
			}
		}

		
		class Kor
			implements ObjectResource {

			protected TxnResourceApi res;
			
			public Kor(TxnResourceApi res) {
				super();
				this.res = res;
			}

			@Override
			public void close() throws Exception {
				// TODO Auto-generated method stub
				
			}

			@Override
			public PathDiffState fetchRecencyStatus() {
				PathDiffState status = FileSyncImpl.getState(res.getFileSync());
				return status;
			}

			@Override
			public Object loadNewInstance() {
				res.declareAccess();
				res.lock(true);

				Object result = null;
				if (res.getFileSync().exists()) {
					try (InputStream in = res.getFileSync().openCurrentContent()) {
						result = objectSerializer.read(in);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				res.unlock();
				return result;
			}
			
			@Override
			public void save(Object obj) {
				
				res.declareAccess();
				res.lock(true);
				
				try {
					res.getFileSync().putContent(out -> {
						// Object obj;
//						try {
//							obj = objectFuture.get().get();
//						} catch (InterruptedException | ExecutionException e1) {
//							throw new RuntimeException(e1);
//						}
						try {
							objectSerializer.write(out, obj);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				// TODO Auto-generated method stub
				
			}
			
		}
		
//		class KorOld
//			implements ObjectResource {
//			
//			protected TxnResourceApi res;
//			protected boolean isManuallySet;
//			protected boolean isMarkedAsDirty;
//			protected AtomicReference<CompletableFuture<Object>> objectFuture = new AtomicReference<>();
//			protected PathDiffState contentStatus;
//			
//			public Kor(TxnResourceApi res) {
//				super();
//				this.res = res;
//			}
//			
//			/** 
//			 * Reads the content from the source (creates a txn for that purpose if needed) and returns a reference to the
//			 * claimed cache entry.
//			 * 
//			 * The referent is not protected by a txn.
//			 * 
//			 *  
//			 */
//			@Override
//			public RefFuture<ObjectInfo> claimContent() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//			
//			@Override
//			public boolean hasChanged() {
//				return isManuallySet || FileSyncImpl.getState(res.getFileSync()).isDirty();
//			}
//
//			@Override
//			public Object reloadRaw() {
//				updateFuture(newCachedInstanceFuture());
//				return objectFuture.get();
//			}
//
//			@Override
//			public Object getRaw() {
//				try {
//					return getCachedInstanceFuture().get();
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//
////			@Override
////			public void write(Object obj) {
////				objectFuture.set(CompletableFuture.completedFuture(obj));
////				isManuallySet = true;
////			}
////			
////			// Write the current object to disk; noop if not dirty
////			public void save() throws IOException {
////				res.getFileSync().putContent(out -> {
////					try {
////						objectSerializer.write(getContent(), out);
////					} catch (IOException e) {
////						throw new RuntimeException(e);
////					}
////				});
////			}
//
//			@Override
//			public boolean isCachedInstancePresent() {
//				return objectFuture != null;
//			}
//
//			protected void updateFuture(CompletableFuture<Object> newFuture) {
//				CompletableFuture<Object> oldFuture = objectFuture.getAndSet(newFuture);
//				if (oldFuture != null) {
//					RefFutureImpl.cancelFutureOrCloseValue(oldFuture, null);
//				}
//			}
//			
//			protected CompletableFuture<Object> newCachedInstanceFuture() {
//				return CompletableFuture.supplyAsync(() -> loadNewInstance());
//			}
//
//			protected Object loadNewInstance() {
//				Object result;
//				if (res.getFileSync().exists()) {
//					try (InputStream in = res.getFileSync().openCurrentContent()) {
//						result = objectSerializer.read(in);
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				} else {
//					result = null;
//				}
//				return result;
//			}
//			
//			public CompletableFuture<Object> getCachedInstanceFuture() {
//				if (objectFuture.get() == null) {
//					synchronized (this) {
//						if (objectFuture.get() == null) {
//							updateFuture(newCachedInstanceFuture());
//						}
//					}
//				}
//				
//				return objectFuture.get();
//			}
//
//			@Override
//			public Object getContent() {
//				try {
//					return getCachedInstanceFuture().get();
//				} catch (InterruptedException | ExecutionException e) {
//					throw new RuntimeException(e);
//				}
//			}
//
//			@Override
//			public void setContent(Object obj) {
//				updateFuture(CompletableFuture.completedFuture(obj));
//			}
//
//			// Probably should return when the content was loaded into the content cache
//			@Override
//			public PathState getLoadTimeStatus() {
//				return null;
//				// ObjectInfo objectInfo = contentCache.claim(Array.wrap(res.getResourceKey()));
//				
//				
//				// FileSyncImpl.getState(res.getFileSync()).getCurrentState()
//			}
//
//			@Override
//			public PathDiffState getRecencyStatus() {
//				return contentStatus;
//			}
//
//			@Override
//			public void setRecencyStatus(PathDiffState status) {
//				this.contentStatus = status;
//			}
//
//			@Override
//			public PathDiffState fetchRecencyStatus() {
//				PathDiffState status = FileSyncImpl.getState(res.getFileSync());
//				return status;
//			}
//			
//			@Override
//			public void markAsDirty() {
//				isMarkedAsDirty = true;
//			}
//
//			@Override
//			public void close() throws Exception {
//				RefFutureImpl.cancelFutureOrCloseValue(objectFuture.get(), null);
//			}
//
//			@Override
//			public boolean isDirty() {
//				return isMarkedAsDirty;
//			}
//
//			@Override
//			public void save() {
//				
//				res.declareAccess();
//				res.lock(true);
//				
//				try {
//					res.getFileSync().putContent(out -> {
//						Object obj;
//						try {
//							obj = objectFuture.get().get();
//						} catch (InterruptedException | ExecutionException e1) {
//							throw new RuntimeException(e1);
//						}
//						try {
//							objectSerializer.write(out, obj);
//						} catch (IOException e) {
//							throw new RuntimeException(e);
//						}
//					});
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//				// TODO Auto-generated method stub
//				
//			}
//		}

	}
}

