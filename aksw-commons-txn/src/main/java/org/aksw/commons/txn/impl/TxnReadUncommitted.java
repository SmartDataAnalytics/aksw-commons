package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.path.core.PathNio;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.path.core.PathStr;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.api.TxnResourceApi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TxnReadUncommitted
    implements Txn
{
//	private static final Logger logger = LoggerFactory.getLogger(TxnReadUncommitted.class);

//	protected boolean isWrite;

    protected TxnMgrImpl txnMgr;
    protected String txnId;
//	protected Path txnFolder;

    //protected String preCommitFilename = ".precommit";
//	protected String commitFilename = "commit";
//	protected String finalizeFilename = "finalize";
//	protected String rollbackFilename = "rollback";

    //protected transient Path preCommitFile;
    //protected transient Path finalizeCommitFile;
//	protected transient Path commitFile;
//	protected transient Path finalizeFile;
//	protected transient Path rollbackFile;

//	protected IsolationLevel isolationLevel;

    //protected LockStore<String[], String> lockStore;

    protected TxnResourceApi createResourceApi(org.aksw.commons.path.core.Path<String> key) {
        return new TxnResourceApiReadUncommitted<>(this, key);
    }

    @Override
    public String getId() {
        return txnId;
    }

    @Override
    public void promote() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    protected LoadingCache<org.aksw.commons.path.core.Path<String>, TxnResourceApi> containerCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<org.aksw.commons.path.core.Path<String>, TxnResourceApi>() {
                @Override
                public TxnResourceApi load(org.aksw.commons.path.core.Path<String> key) throws Exception {
                    return createResourceApi(key);
                }
            });


    public TxnReadUncommitted(
            TxnMgrImpl txnMgr,
            String txnId) {
        super();
        this.txnMgr = txnMgr;
        this.txnId = txnId;
    }

    @Override
    public TxnMgr getTxnMgr() {
        return txnMgr;
    }


    @Override
    public Stream<TxnResourceApi> listVisibleFiles(org.aksw.commons.path.core.Path<String> prefix) {

        // TODO This pure listing of file resources should probably go to the repository
        Path rootPath = txnMgr.getRootPath();
        PathMatcher pathMatcher = txnMgr.getPathMatcher(); // rootPath.getFileSystem().getPathMatcher("glob:**/*.trig");
                // txnMgr.getResRepo().getRootPath().getFileSystem().getPathMatcher("glob:**/*.trig");

        // The root path may not exist if the store is empty
        Path basePath = PathUtils.resolve(rootPath, prefix.getSegments());

        // isVisible filters out graphs that were created after the transaction start
        Stream<TxnResourceApi> result;
        try {
            result = Files.exists(basePath)
                    ? Files.walk(basePath)
                        .filter(pathMatcher::matches)
                        // No longer valid: We are interested in the folder - not the file itself: Get the parent
                        // .map(Path::getParent)
                        .map(path -> rootPath.relativize(path))
                        .map(relPath -> PathOpsStr.newRelativePath().resolve(PathNio.wrap(relPath)))
                        // .map(PathUtils::getPathSegments)
                        .map(this::getResourceApi)
                        .filter(TxnResourceApi::isVisible)
                    : Stream.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // paths.stream().map(path -> )

        return result;
    }

    @Override
    public Instant getCreationDate() {
        // TODO Auto-generated method stub
        return null;
    }

//	public Instant getCreationInstant() {
//		try {
//		    BasicFileAttributes attr = Files.readAttributes(txnFolder, BasicFileAttributes.class);
//		    FileTime fileTime = attr.creationTime();
//		    return fileTime.toInstant();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

//	public TxnResourceApi getResourceApi(String resourceName) {
//		String[] relRelPath = txnMgr.getResRepo().getPathSegments(resourceName);
//		TxnResourceApi result = getResourceApi(relRelPath);
//		return result;
//	}

    @Override
    public TxnResourceApi getResourceApi(org.aksw.commons.path.core.Path<String> resRelPath) {
        TxnResourceApi result;
        try {
            result = containerCache.get(resRelPath);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public boolean isWrite() {
        return false;
    }

    public void cleanUpTxn() throws IOException {
    }

    public void addCommit() throws IOException {
    }

    public void addFinalize() throws IOException {
    }

    public void addRollback() throws IOException {
    }

    public boolean isFinalize() throws IOException {
        return true;
    }

    public boolean isCommit() throws IOException {
        return true;
    }

    public boolean isRollback() throws IOException {
        return false;
    }

    @Override
    public Stream<org.aksw.commons.path.core.Path<String>> streamAccessedResourcePaths() throws IOException {
        return Stream.empty();
    }

    @Override
    public void setActivityDate(Instant instant) throws IOException {
    }

    @Override
    public Instant getActivityDate() throws IOException {
        return null;
    }

    @Override
    public boolean isStale() throws IOException {
        return false;
    }

    @Override
    public boolean claim() throws IOException {
        return false;
    }

    @Override
    public void updateHeartbeat() throws IOException {
    }

    @Override
    public Instant getMostRecentHeartbeat() throws IOException {
        return null; // UnspportedOperation?
    }

    @Override
    public TemporalAmount getDurationToNextHeartbeat() throws IOException  {
        return null; // UnspportedOperation?
    }
}
