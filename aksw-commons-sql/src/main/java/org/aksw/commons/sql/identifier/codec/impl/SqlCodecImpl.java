package org.aksw.commons.sql.identifier.codec.impl;

import org.aksw.commons.codec.entity.api.EntityCodec;
import org.aksw.commons.sql.identifier.codec.api.SqlCodec;

public class SqlCodecImpl
	implements SqlCodec
{
	protected EntityCodec<String> aliasCodec;
	protected EntityCodec<String> schemaNameCodec;
	protected EntityCodec<String> tableNameCodec;
	protected EntityCodec<String> columnNameCodec;
	protected EntityCodec<String> schemaCodec;
	protected EntityCodec<String> stringLiteralCodec;

	public SqlCodecImpl(
			EntityCodec<String> aliasCodec,
			EntityCodec<String> schemaNameCodec,
			EntityCodec<String> tableNameCodec,
			EntityCodec<String> columnNameCodec,
			EntityCodec<String> schemaCodec,
			EntityCodec<String> stringLiteralCodec) {
		super();
		this.aliasCodec = aliasCodec;
		this.schemaNameCodec = schemaNameCodec;
		this.tableNameCodec = tableNameCodec;
		this.columnNameCodec = columnNameCodec;
		this.schemaCodec = schemaCodec;
		this.stringLiteralCodec = stringLiteralCodec;
	}

	@Override
	public EntityCodec<String> forAlias() {
		return aliasCodec;
	}
	
	@Override
	public EntityCodec<String> forSchemaName() {
		return schemaNameCodec;
	}

	@Override
	public EntityCodec<String> forTableName() {
		return tableNameCodec;
	}

	@Override
	public EntityCodec<String> forColumnName() {
		return columnNameCodec;
	}

	@Override
	public EntityCodec<String> forStringLiteral() {
		return stringLiteralCodec;
	}

	public static SqlCodecImpl create(EntityCodec<String> codec) {
		return create(codec, codec);
	}

	public static SqlCodecImpl create(EntityCodec<String> identifierCodec, EntityCodec<String> stringLiteralCodec) {
		return new SqlCodecImpl(
				identifierCodec,
				identifierCodec,
				identifierCodec,
				identifierCodec,
				identifierCodec,
				stringLiteralCodec);
	}

}
