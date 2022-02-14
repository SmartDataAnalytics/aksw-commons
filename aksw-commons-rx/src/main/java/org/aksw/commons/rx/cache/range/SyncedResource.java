package org.aksw.commons.rx.cache.range;

/**
 * 
 * @author raven
 *
 * @param <T> The type of the resource
 * @param <D> A delta object for that resource
 */
//public abstract class SyncedResource<T> {
//    private static final Logger logger = LoggerFactory.getLogger(SyncedResource.class);
//
//    protected FileSync fileSync;
//
//    protected PathDiffState state;
//    protected T originalState;
////    protected D delta;
////    protected T view;
//
//    public SyncedResource(FileSync fileSync) {
//        super();
//        this.fileSync = fileSync;
//    }
//
//
//    public void updateState() {
//        this.state = FileSyncImpl.getState(fileSync);
//    }
//
//    protected abstract T newResource();
//    
//    protected abstract T readData(T priorState, InputStream in);
//
//    protected abstract void writeData(OutputStream out, T obj);
//
//
//    public void forceLoad() {
//        state = getState();
//
//        // originalState = newDatasetGraph();
//
//        try (InputStream in = Files.newInputStream(state.getCurrentState().getPath())) {
//            readData(originalState, in);
//        } catch (AccessDeniedException ex) {
//            // FIXME The file may not exist but it may also be an authorization issue
//            logger.warn("Access denied: " + ExceptionUtils.getRootCauseMessage(ex));
//        } catch (NoSuchFileException ex) {
//            // Ignore - this leads to an empty dataset
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//
//        if (!state.getCurrentState().getPath().equals(state.getOriginalState().getPath())) {
//        	logger.info("Data changed on disk - reloading...");
//        	
//            // DatasetGraph n = newDatasetGraph();
//            T newResource;
//        	try (InputStream in = Files.newInputStream(state.getCurrentState().getPath())) {
//                newResource = readData(null, in);
//            } catch (Exception ex) {
//                throw new RuntimeException(ex);
//            }
//
//            // Create a diff between the original content and the newly loaded content
//            createDiff();
////            
////            Set<Quad> oldQuads = SetFromDatasetGraph.wrap(originalState);
////            Set<Quad> newQuads = SetFromDatasetGraph.wrap(n);
////
////            Set<Quad> addedQuads = Sets.difference(newQuads, oldQuads);
////            Set<Quad> removedQuads = Sets.difference(oldQuads, newQuads);
////
////            diff = newDatasetGraphDiff(originalState);
////            addedQuads.forEach(diff.getAdded()::add);
////            removedQuads.forEach(diff.getRemoved()::add);
//        } else {
//            // diff = newDatasetGraphDiff(originalState);
//        }
//    }
//
//    public void ensureLoaded() {
//        if (originalState == null) {
//            forceLoad();
//        }
//    }
//
//    public T getOriginalState() {
//        ensureLoaded();
//        return originalState;
//    }
//
//    public abstract T getCurrentState();
//
//
//    public ContentSync getEntity() {
//        return fileSync;
//    }
//
//    public void load() {
//        ensureLoaded();
//    }
//
////    public DatasetGraphDiff get() {
////        ensureLoaded();
////        return diff;
////    }
////
//    /**
//     * Returns true if there are pending changes in memory; i.e. the set of added/removed triples is non-empty.
//     *
//     * @return
//     */
//    public abstract boolean isDirty();
//
//
//    public boolean isUpToDate() {
//        // Check the time stamps of the source resources
//        PathDiffState verify;
//        boolean result = state != null && (verify = getState()).equals(state);
//    	return result;
//    }
//
//    public void ensureUpToDate() {
//        Objects.requireNonNull(state);
//
//        // Check the time stamps of the source resources
//        PathDiffState verify = getState();
//
//        if (!verify.equals(state)) {
//            throw new RuntimeException(
//                String.format("Content of files was changed externally since it was loaded:\nExpected:\n%s: %s\n%s: %s\nActual:\n%s: %s\n%s: %s",
//                state.getOriginalState().getPath(),
//                state.getOriginalState().getTimestamp(),
//                state.getCurrentState().getPath(),
//                state.getCurrentState().getTimestamp(),
//                verify.getOriginalState().getPath(),
//                verify.getOriginalState().getTimestamp(),
//                verify.getCurrentState().getPath(),
//                verify.getCurrentState().getTimestamp()
//            ));
//        }
//    }
//
////	public Synced set(T instance) {
////		this.instance = instance;
////	}
//
//
//    public void save() {
//        if (isDirty()) {
//            try {
//                ensureUpToDate();
//
//                fileSync.putContent(out -> {
//                    writeData(out, diff);
//                });
//
//                // Update metadata
//                updateState();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}

