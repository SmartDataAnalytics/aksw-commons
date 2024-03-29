package org.aksw.commons.model.csvw.domain.impl;

import java.io.Serializable;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;

import com.google.common.base.StandardSystemProperty;

public class DialectImpl
    implements DialectMutable, Serializable
{
    private static final long serialVersionUID = 1L;

    protected String  commentPrefix;
    protected String  delimiter;
    protected Boolean doubleQuote;
    protected String  encoding;
    protected Boolean header;
    protected Long    headerRowCount;
    protected String  lineTerminators;
    protected String  quoteChar;
    protected Boolean skipBlankRows;
    protected Long    skipColumns;
    protected Boolean skipInitialSpace;
    protected Long    skipRows;
    protected String  trim;

    protected String quoteEscapeChar;

    @Override
    public String getCommentPrefix() {
        return commentPrefix;
    }

    @Override
    public DialectMutable setCommentPrefix(String commentPrefix) {
        this.commentPrefix = commentPrefix;
        return this;
    }

    @Override
    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public DialectMutable setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    @Override
    public Boolean isDoubleQuote() {
        return doubleQuote;
    }

    @Override
    public DialectMutable setDoubleQuote(Boolean doubleQuote) {
        this.doubleQuote = doubleQuote;
        return this;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public DialectMutable setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public Boolean getHeader() {
        return header;
    }

    @Override
    public DialectMutable setHeader(Boolean header) {
        this.header = header;
        return this;
    }

    @Override
    public Long getHeaderRowCount() {
        return headerRowCount;
    }

    @Override
    public DialectMutable setHeaderRowCount(Long headerRowCount) {
        this.headerRowCount = headerRowCount;
        return this;
    }

    @Override
    public String getLineTerminators() {
        return lineTerminators;
    }

    @Override
    public DialectMutable setLineTerminators(String lineTerminators) {
        this.lineTerminators = lineTerminators;
        return this;
    }

    @Override
    public String getQuoteChar() {
        return quoteChar;
    }

    @Override
    public DialectMutable setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    @Override
    public Boolean getSkipBlankRows() {
        return skipBlankRows;
    }

    @Override
    public DialectMutable setSkipBlankRows(Boolean skipBlankRows) {
        this.skipBlankRows = skipBlankRows;
        return this;
    }

    @Override
    public Long getSkipColumns() {
        return skipColumns;
    }

    @Override
    public DialectMutable setSkipColumns(Long skipColumns) {
        this.skipColumns = skipColumns;
        return this;
    }

    @Override
    public Boolean getSkipInitialSpace() {
        return skipInitialSpace;
    }

    @Override
    public DialectMutable setSkipInitialSpace(Boolean skipInitialSpace) {
        this.skipInitialSpace = skipInitialSpace;
        return this;
    }

    @Override
    public Long getSkipRows() {
        return skipRows;
    }

    @Override
    public DialectMutable setSkipRows(Long skipRows) {
        this.skipRows = skipRows;
        return this;
    }

    @Override
    public String getTrim() {
        return trim;
    }

    @Override
    public DialectMutable setTrim(String trim) {
        this.trim = trim;
        return this;
    }

    @Override
    public String getQuoteEscapeChar() {
        return quoteEscapeChar;
    }

    @Override
    public DialectMutable setQuoteEscapeChar(String quoteEscapeChar) {
        this.quoteEscapeChar = quoteEscapeChar;
        return this;
    }

    @Override
    public String toString() {
        String result = String.join(StandardSystemProperty.LINE_SEPARATOR.value(),
            "quoteChar=" + quoteChar,
            "quoteEscapeChar=" + quoteEscapeChar,
            "delimiter=" + delimiter,
            "lineTerminators=" + lineTerminators,
            "commentPrefix=" + commentPrefix,
            "doubleQuote=" + doubleQuote,
            "encoding=" + encoding,
            "header=" + header,
            "headerRowCount=" + headerRowCount,
            "skipBlankRows=" + skipBlankRows,
            "skipColumns=" + skipColumns,
            "skipInitialSpace=" + skipInitialSpace,
            "skipRows=" + skipRows,
            "trim=" + trim);
        return result;
    }
}
