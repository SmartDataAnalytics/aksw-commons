package org.aksw.commons.model.csvw.domain.api;

/** Resource view of the Dialect class according to
 *  https://www.w3.org/ns/csvw#class-definitions
 *
 *  All attributes mapped.
 */
public interface Dialect
{
    String  getCommentPrefix();
    String  getDelimiter();

    /** A boolean atomic property that, if `true`, sets the escape character
     *  flag to `"`. */
    Boolean isDoubleQuote();

    /** An atomic property that sets the encoding flag to the single provided
     *  string value, which MUST be a defined in [encoding].
     *  The default is "utf-8". */
    String  getEncoding();
    Boolean getHeader();
    Long    getHeaderRowCount();
    String  getLineTerminators();
    String  getQuoteChar();
    Boolean getSkipBlankRows();
    Long    getSkipColumns();
    Boolean getSkipInitialSpace();
    Long    getSkipRows();

    /**
     * An atomic property that, if the boolean `true`, sets the trim flag to `true` and if the boolean `false` to `false`.
     * If the value provided is a string, sets the trim flag to the provided value, which must be one of "true", "false",
     * "start" or "end".
     *
     * rdfs:range xsd:boolean
     * rdfs:domain csvw:Dialect
     *
     * Holy cow... so let's just assume the literal strings "true"/"false" represent the
     * corresponding boolean value and thus the range is string. Otherwise we'd either have to
     * result to Object or
     */
    String  getTrim();
}
