package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public interface DialectMutableForwarding<D extends DialectMutable>
    extends DialectForwarding<D>, DialectMutable
{
    @Override
    D getDelegate();

    @Override default DialectMutable setCommentPrefix(String commentPrefix)        { getDelegate().setCommentPrefix(commentPrefix); return this; }
    @Override default DialectMutable setDelimiter(String delimiter)                { getDelegate().setDelimiter(delimiter); return this; }
    @Override default DialectMutable setDoubleQuote(Boolean doubleQuote)           { getDelegate().setDoubleQuote(doubleQuote); return this; }
    @Override default DialectMutable setEncoding(String encoding)                  { getDelegate().setEncoding(encoding); return this; }
    @Override default DialectMutable setHeader(Boolean header)                     { getDelegate().setHeader(header); return this; }
    @Override default DialectMutable setHeaderRowCount(Long headerRowCount)        { getDelegate().setHeaderRowCount(headerRowCount); return this; }
    @Override default DialectMutable setLineTerminators(String lineTerminators)    { getDelegate().setLineTerminators(lineTerminators); return this; }
    @Override default DialectMutable setQuoteChar(String quoteChar)                { getDelegate().setQuoteChar(quoteChar); return this; }
    @Override default DialectMutable setSkipBlankRows(Boolean skipBlankRows)       { getDelegate().setSkipBlankRows(skipBlankRows); return this; }
    @Override default DialectMutable setSkipColumns(Long skipColumns)              { getDelegate().setSkipColumns(skipColumns); return this; }
    @Override default DialectMutable setSkipInitialSpace(Boolean skipInitialSpace) { getDelegate().setSkipInitialSpace(skipInitialSpace); return this; }
    @Override default DialectMutable setSkipRows(Long skipRows)                    { getDelegate().setSkipRows(skipRows); return this; }
    @Override default DialectMutable setTrim(String trim)                          { getDelegate().setTrim(trim); return this; }
    @Override default DialectMutable setQuoteEscapeChar(String quoteEscapeChar)    { getDelegate().setQuoteEscapeChar(quoteEscapeChar); return this; }
}
