package org.aksw.commons.model.csvw.domain.api;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/** Resource view of the Dialect class according to
 *  https://www.w3.org/ns/csvw#class-definitions
 *
 *  All attributes mapped.
 */
public interface Dialect
{
    /** An atomic property that sets the comment prefix flag to the single
     * provided value, which MUST be a string. */
    String  getCommentPrefix();

    /** An atomic property that sets the delimiter flag to the single provided
     * value, which MUST be a string. */
    String  getDelimiter();

    /** A boolean atomic property that, if `true`, sets the escape character
     *  flag to `"`. */
    Boolean isDoubleQuote();

    /** An atomic property that sets the encoding flag to the single provided
     *  string value, which MUST be a defined in [encoding].
     *  The default is "utf-8". */
    String  getEncoding();

    /** A boolean atomic property that, if `true`, sets the header row count
     * flag to `1`, and if `false` to `0`, unless headerRowCount is provided,
     * in which case the value provided for the header property is ignored. */
    Boolean getHeader();

    /**
     * An numeric atomic property that sets the header row count flag to the
     * single provided value, which must be a non-negative integer.
     */
    Long    getHeaderRowCount();

    /**
     * An atomic property that sets the line terminators flag to either an
     * array containing the single provided string value, or the provided array.
     */
    String  getLineTerminators();

    /** An atomic property that sets the quote character flag to the single
     * provided value, which must be a string or `null`.
     */
    String  getQuoteChar();

    /**
     * An boolean atomic property that sets the `skip blank rows` flag to the
     * single provided boolean value.
     */
    Boolean getSkipBlankRows();

    /** An numeric atomic property that sets the `skip columns` flag to the
     * single provided numeric value, which MUST be a non-negative integer. */
    Long    getSkipColumns();

    /** A boolean atomic property that, if `true`, sets the trim flag to
     * "start". If `false`, to `false`. */
    Boolean getSkipInitialSpace();

    /** An numeric atomic property that sets the `skip rows` flag to the
     * single provided numeric value, which MUST be a non-negative integer. */
    Long    getSkipRows();

    /**
     * An atomic property that, if the boolean `true`, sets the trim flag to
     * `true` and if the boolean `false` to `false`.
     * If the value provided is a string, sets the trim flag to the provided
     * value, which must be one of "true", "false",
     * "start" or "end".
     *
     * rdfs:range xsd:boolean
     * rdfs:domain csvw:Dialect
     *
     * Holy cow... so let's just assume the literal strings "true"/"false"
     * represent the corresponding boolean value and thus the range is string.
     * Otherwise we'd either have to result to Object or use a BooleanOrString
     * class.
     */
    String  getTrim();


    /** Extension: Quote escape char */
    String  getQuoteEscapeChar();

    default void copyInto(DialectMutable dest, boolean copyNulls) {
        String str;
        Boolean b;
        Long l;

        if ((str = getCommentPrefix()) != null || copyNulls) {
            dest.setCommentPrefix(str);
        }
        if((str = getDelimiter()) != null || copyNulls) {
            dest.setDelimiter(str);
        }
        if((b = isDoubleQuote()) != null || copyNulls) {
            dest.setDoubleQuote(b);
        }
        if((str = getEncoding()) != null || copyNulls) {
            dest.setEncoding(str);
        }
        if((b = getHeader()) != null || copyNulls) {
            dest.setHeader(b);
        }
        if((l = getHeaderRowCount()) != null || copyNulls) {
            dest.setHeaderRowCount(l);
        }
        if((str = getLineTerminators()) != null || copyNulls) {
            dest.setLineTerminators(str);
        }
        if((str = getQuoteChar()) != null || copyNulls) {
            dest.setQuoteChar(str);
        }
        if((str = getQuoteEscapeChar()) != null || copyNulls) {
            dest.setQuoteEscapeChar(str);
        }
        if((b = getSkipBlankRows()) != null || copyNulls) {
            dest.setSkipBlankRows(b);
        }
        if((l = getSkipColumns()) != null || copyNulls) {
            dest.setSkipColumns(l);
        }
        if((b = getSkipInitialSpace()) != null || copyNulls) {
            dest.setSkipInitialSpace(b);
        }
        if((l = getSkipRows()) != null || copyNulls) {
            dest.setSkipRows(l);
        }
        if((str = getTrim()) != null || copyNulls) {
            dest.setTrim(str);
        }
    }

    /**
     * Attempts to parse the line terminators as a json array and return an array of the strings.
     * If parsing fails then a single item with original string value is returned.
     */
    default List<String> getLineTerminatorList() {
        List<String> result = null;
        String str = getLineTerminators();
        if (str != null) {
            try {
                Type type = new TypeToken<List<String>>() { /* empty */ }.getType();
                Gson gson = GsonUtils.createGson();
                result = gson.fromJson(str, type);
            } catch (Exception e) {
                // Ignore?
            }

            if (result == null) {
                result = Arrays.asList(str);
            }
        }
        return result;
    }
}
