package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.Dialect;

public interface DialectForwarding<D extends Dialect>
    extends Dialect
{
    D getDelegate();

    default String  getCommentPrefix()    { return getDelegate().getCommentPrefix(); }
    default String  getDelimiter()        { return getDelegate().getDelimiter(); }
    default Boolean isDoubleQuote()       { return getDelegate().isDoubleQuote(); }
    default String  getEncoding()         { return getDelegate().getEncoding(); }
    default Boolean getHeader()           { return getDelegate().getHeader(); }
    default Long    getHeaderRowCount()   { return getDelegate().getHeaderRowCount(); }
    default String  getLineTerminators()  { return getDelegate().getLineTerminators(); }
    default String  getQuoteChar()        { return getDelegate().getQuoteChar(); }
    default Boolean getSkipBlankRows()    { return getDelegate().getSkipBlankRows(); }
    default Long    getSkipColumns()      { return getDelegate().getSkipColumns(); }
    default Boolean getSkipInitialSpace() { return getDelegate().getSkipInitialSpace(); }
    default Long    getSkipRows()         { return getDelegate().getSkipRows(); }
    default String  getTrim()             { return getDelegate().getTrim(); }

}
