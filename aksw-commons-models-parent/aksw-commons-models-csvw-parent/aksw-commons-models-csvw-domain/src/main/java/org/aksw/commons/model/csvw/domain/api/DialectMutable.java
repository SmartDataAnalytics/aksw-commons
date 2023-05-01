package org.aksw.commons.model.csvw.domain.api;

import java.util.List;

import com.google.gson.JsonArray;

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
    DialectMutable setQuoteEscapeChar(String quoteEscapeChar);

    default DialectMutable setLineTerminatorList(List<String> lineTerminators) {
    	String str = null;
    	if (lineTerminators != null) {
	    	JsonArray array = new JsonArray();
	    	lineTerminators.forEach(array::add);
	    	str = array.toString();
    	}
		return setLineTerminators(str);
    }
    
//    default void copyFrom(Dialect src) {
//        setCommentPrefix(src.getCommentPrefix());
//        setDelimiter(src.getDelimiter());
//        setDoubleQuote(src.isDoubleQuote());
//        setEncoding(src.getEncoding());
//        setHeader(src.getHeader());
//        setHeaderRowCount(src.getHeaderRowCount());
//        setLineTerminators(src.getLineTerminators());
//        setQuoteChar(src.getQuoteChar());
//        setQuoteEscapeChar(src.getQuoteEscapeChar());
//        setSkipBlankRows(src.getSkipBlankRows());
//        setSkipColumns(src.getSkipColumns());
//        setSkipInitialSpace(src.getSkipInitialSpace());
//        setSkipRows(src.getSkipRows());
//        setTrim(src.getTrim());
//    }
}
