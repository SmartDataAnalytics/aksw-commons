package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public class DialectMutableImpl
    extends DialectImpl
{
    private static final long serialVersionUID = 1L;

    @Override
    public DialectMutable setCommentPrefix(String commentPrefix) {
        this.commentPrefix = commentPrefix;
        return this;
    }

    @Override
    public DialectMutable setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    @Override
    public DialectMutable setDoubleQuote(Boolean doubleQuote) {
        this.doubleQuote = doubleQuote;
        return this;
    }

    @Override
    public DialectMutable setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public DialectMutable setHeader(Boolean header) {
        this.header = header;
        return this;
    }

    @Override
    public DialectMutable setHeaderRowCount(Long headerRowCount) {
        this.headerRowCount = headerRowCount;
        return this;
    }

    @Override
    public DialectMutable setLineTerminators(String lineTerminators) {
        this.lineTerminators = lineTerminators;
        return this;
    }

    @Override
    public DialectMutable setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    @Override
    public DialectMutable setSkipBlankRows(Boolean skipBlankRows) {
        this.skipBlankRows = skipBlankRows;
        return this;
    }

    @Override
    public DialectMutable setSkipColumns(Long skipColumns) {
        this.skipColumns = skipColumns;
        return this;
    }

    @Override
    public DialectMutable setSkipInitialSpace(Boolean skipInitialSpace) {
        this.skipInitialSpace = skipInitialSpace;
        return this;
    }

    @Override
    public DialectMutable setSkipRows(Long skipRows) {
        this.skipRows = skipRows;
        return this;
    }

    @Override
    public DialectMutable setTrim(String trim) {
        this.trim = trim;
        return this;
    }
}
