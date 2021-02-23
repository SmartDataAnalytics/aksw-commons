package org.aksw.commons.sql.codec.api;

import org.aksw.commons.codec.entity.api.EntityCodec;

/**
 * Class for (un-)escaping certain syntactic SQL elements.
 * 
 * @author Claus Stadler
 *
 */
public interface SqlCodec {
	
	/** Codec for aliases - must never return null */
	EntityCodec<String> forAlias();

    /** Codec for table names - must never return null */
	EntityCodec<String> forTableName();

    /** Codec for schema names - must never return null */
	EntityCodec<String> forSchemaName();

    /** Codec for column names - must never return null */
	EntityCodec<String> forColumnName();
    
    /**
     * Codec for string literals - must never return null.
     * 
     * Note that serializing non-string types may still use string escaping as part
     * of the serialization process. E.g. a Date may be serialized as;
     * 
     * DATE 'str'
     * 
     * http://dev.mysql.com/doc/refman/5.7/en/date-and-time-literals.html
     * 
     * @param str
     * @return
     */
	EntityCodec<String> forStringLiteral();
}
