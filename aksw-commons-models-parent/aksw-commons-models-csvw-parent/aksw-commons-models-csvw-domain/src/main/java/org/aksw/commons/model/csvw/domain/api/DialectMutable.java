package org.aksw.commons.model.csvw.domain.api;

public interface DialectMutable
    extends Dialect
{
    DialectMutable setCommentPrefix(String commentPrefix);
    DialectMutable setDelimiter(String delimiter);
    DialectMutable setDoubleQuote(Boolean doubleQuote);
    DialectMutable setEncoding(String encoding);
    DialectMutable setHeader(Boolean header);
    DialectMutable setHeaderRowCount(Long headerRowCount);
    DialectMutable setLineTerminators(String lineTerminators);
    DialectMutable setQuoteChar(String quoteChar);
    DialectMutable setSkipBlankRows(Boolean skipBlankRows);
    DialectMutable setSkipColumns(Long skipColumns);
    DialectMutable setSkipInitialSpace(Boolean skipInitialSpace);
    DialectMutable setSkipRows(Long skipRows);
    DialectMutable setTrim(String trim);
}
