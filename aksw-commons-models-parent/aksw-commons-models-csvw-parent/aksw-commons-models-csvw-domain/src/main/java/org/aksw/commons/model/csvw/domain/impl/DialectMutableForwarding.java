package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public interface DialectMutableForwarding<D extends DialectMutable>
    extends DialectForwarding<D>, DialectMutable
{
    @Override
    D getDelegate();

    default DialectMutable setCommentPrefix(String commentPrefix)        { getDelegate().setCommentPrefix(commentPrefix); return this; }
    default DialectMutable setDelimiter(String delimiter)                { getDelegate().setDelimiter(delimiter); return this; }
    default DialectMutable setDoubleQuote(Boolean doubleQuote)           { getDelegate().setDoubleQuote(doubleQuote); return this; }
    default DialectMutable setEncoding(String encoding)                  { getDelegate().setEncoding(encoding); return this; }
    default DialectMutable setHeader(Boolean header)                     { getDelegate().setHeader(header); return this; }
    default DialectMutable setHeaderRowCount(Long headerRowCount)        { getDelegate().setHeaderRowCount(headerRowCount); return this; }
    default DialectMutable setLineTerminators(String lineTerminators)    { getDelegate().setLineTerminators(lineTerminators); return this; }
    default DialectMutable setQuoteChar(String quoteChar)                { getDelegate().setQuoteChar(quoteChar); return this; }
    default DialectMutable setSkipBlankRows(Boolean skipBlankRows)       { getDelegate().setSkipBlankRows(skipBlankRows); return this; }
    default DialectMutable setSkipColumns(Long skipColumns)              { getDelegate().setSkipColumns(skipColumns); return this; }
    default DialectMutable setSkipInitialSpace(Boolean skipInitialSpace) { getDelegate().setSkipInitialSpace(skipInitialSpace); return this; }
    default DialectMutable setSkipRows(Long skipRows)                    { getDelegate().setSkipRows(skipRows); return this; }
    default DialectMutable setTrim(String trim)                          { getDelegate().setTrim(trim); return this; }
}
