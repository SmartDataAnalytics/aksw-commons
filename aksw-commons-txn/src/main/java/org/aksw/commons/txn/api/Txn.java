package org.aksw.commons.txn.api;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;

import org.aksw.commons.path.core.Path;

/**
 * A transaction can hold locks to several resources.
 * The resources (potentially) locked by a transaction can be accessed using streamAccessedResourcePaths().
 *
 *
 * References to resources can be acquired via {@link #getResourceApi(String[])}.
 *
 *
 * TODO Consider replace of String[] with Array<String>; the former is a pain to use with e.g. guava cache
 * TODO Consider replace of Stream with Flowable; the former does not have proper resource management
 *
 * @author raven
 *
 */
public interface Txn
{
    TxnMgr getTxnMgr();

    /** Get the id of this transaction */
    String getId();


    /** Get access to a resource as seen from this txn */
    // TxnResourceApi getResourceApi(String[] resRelPath);

    TxnResourceApi getResourceApi(Path<String> resRelPath);

    Stream<TxnResourceApi> listVisibleFiles(Path<String> prefix);

    Stream<Path<String>> streamAccessedResourcePaths() throws IOException;


    /** Whether the transaction has become stale */
    boolean isStale() throws IOException;

    /**
     * Instruct this TxnMgr to try to claim (i.e. take ownership) of this txn. Should only be used on stale transactions.
     * A claim fails - indicated by a return value of false - if the txn's heartbeat timeout has not been reached.
     * This condition also occurs if another thread won the competition for claiming a txn.
     *
     */
    boolean claim() throws IOException;


    boolean isWrite();

    void addCommit() throws IOException;
    void addFinalize() throws IOException;
    void addRollback() throws IOException;

    boolean isCommit() throws IOException;
    boolean isRollback() throws IOException;
    boolean isFinalize() throws IOException;

    void cleanUpTxn() throws IOException;


    /**
     * Promote a read transaction to write.
     *
     */
    void promote();

    Instant getCreationDate();


    void updateHeartbeat() throws IOException;


    Instant getMostRecentHeartbeat() throws IOException;
    TemporalAmount getDurationToNextHeartbeat() throws IOException;


    /**
     * Update the transaction's most recent activity timestamp to given timestamp;
     * Used to prevent other processes from considering the transaction stale.
     */
    void setActivityDate(Instant instant) throws IOException;
    Instant getActivityDate() throws IOException;

    /**
     * Update the transaction's most recent activity timestamp to the current time;
     * Used to prevent other processes from considering the transaction stale.
     */
    default Instant updateActivityDate() throws IOException {
        Instant now = Instant.now();
        setActivityDate(now);
        return now;
    }
}
