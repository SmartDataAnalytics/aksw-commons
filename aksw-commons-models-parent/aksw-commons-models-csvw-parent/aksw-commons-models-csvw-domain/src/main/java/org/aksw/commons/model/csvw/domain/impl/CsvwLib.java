package org.aksw.commons.model.csvw.domain.impl;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.model.csvw.domain.api.Dialect;
import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public class CsvwLib {

    public static List<String> DFT_LINE_TERMINATORS = Arrays.asList("\n", "\r\n", "\n\r", "\r\n\r");

    public static final String END_OF_DATA_KEY = "endOfData";
    public static final String FIELD_SEPARATOR_KEY = "fieldSeparator";
    public static final String LINE_TERMINATOR_KEY = "lineTerminator";


//    public static List<String> getLineTerminators(Dialect dialect) {
//        List<String> result = dialect.getLineTerminatorList();
//        if (result == null || result.isEmpty()) {
//            result = CsvwLib.DFT_LINE_TERMINATORS; // null; // \r?\n\r?
//        }
//        return result;
//    }

    public static boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }

//    public static char expectOneChar(String contextLabel, String value) {
//        if (value.length() != 1) {
//            throw new IllegalArgumentException(contextLabel + ": Expected exactly one character - got: " + value);
//        }
//
//        return value.charAt(0);
//    }

    public static Character expectAtMostOneChar(String contextLabel, String str, Character fallbackChar) {
        Character result;
        if (str == null) {
            result = fallbackChar;
            if (result == null) {
                throw new RuntimeException(contextLabel + ": Expected a character but none given (and no fallback specified)");
            }
        } else {
            result = expectAtMostOneChar(contextLabel, str);
        }
        return result;
    }

    public static Character expectAtMostOneChar(String contextLabel, String str) {
        Character result = null;
        if (str != null) {
            int n = str.length();
            if (n > 1) {
                throw new IllegalArgumentException(contextLabel + ": At most one character expected, got: " + str);
            } else if (n == 1) {
                result = str.charAt(0);
            }
        }
        return result;
    }

    /** Apply defaults according to the csvw spec
     * (https://www.w3.org/ns/csvw#class-definitions).
     * The target may be the same as the source. */
    public static Dialect buildEffectiveModel(Dialect src, DialectMutable dest) {
        if (Boolean.TRUE.equals(src.isDoubleQuote())) {
            dest.setQuoteChar("\"");
        }

        if (Boolean.TRUE.equals(src.getHeader())) {
            dest.setHeaderRowCount(1l);
        }

        if (Boolean.TRUE.equals(src.getSkipInitialSpace())) {
            dest.setTrim("start");
        }

        return dest;
    }

    public static Charset getEncoding(Dialect dialect, Charset fallback) {
        return getEncoding(dialect.getEncoding(), fallback);
    }

    /** Raises IllegalArgumentException if neither encoding nor fallback is specified */
    public static Charset getEncoding(String encoding, Charset fallback) {
        Charset result;

        if (!CsvwLib.isPresent(encoding)) {
            result = fallback;
            if (fallback == null) {
                throw new IllegalArgumentException("No charset specified");
            }
        } else {
            result = Charset.forName(encoding);
        }

        return result;
    }

    /** Create an excel-like column label for a given zero-based column index<p>
     * 0: 'a'<p>
     * 25: 'z'<p>
     * 26: 'aa', ...
     */
    public static String getExcelColumnLabel(int columnIndex) {
        int v = columnIndex;

        StringBuilder sb = new StringBuilder();
        while (v >= 0) {
            char c = (char)('a' + (v % 26));
            sb.append(c);
            v /= 26;
            --v;
        }
        String result = sb.reverse().toString();
        return result;
    }

}
