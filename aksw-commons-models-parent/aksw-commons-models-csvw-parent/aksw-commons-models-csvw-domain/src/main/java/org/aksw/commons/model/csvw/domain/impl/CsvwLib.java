package org.aksw.commons.model.csvw.domain.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.aksw.commons.model.csvw.domain.api.Dialect;
import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public class CsvwLib {

    public static boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }

    public static char expectOneChar(String contextLabel, String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException(contextLabel + ": Expected exactly one character - got: " + value);
        }

        return value.charAt(0);
    }


    /** Apply defaults according to the csvw spec
     * (https://www.w3.org/ns/csvw#class-definitions).
     * The target may be the same as the source. */
    public static void buildEffectiveModel(Dialect src, DialectMutable dest) {
        if (Boolean.TRUE.equals(src.isDoubleQuote())) {
            dest.setQuoteChar("\"");
        }

        if (Boolean.TRUE.equals(src.getSkipInitialSpace())) {
            dest.setTrim("start");
        }

    }

    public static Charset getEncoding(Dialect dialect) {
        return getEncoding(dialect.getEncoding());
    }

    public static Charset getEncoding(String encoding) {
        Charset result;

        if (!CsvwLib.isPresent(encoding)) {
            result = StandardCharsets.UTF_8;
        } else {
            result = Charset.forName(encoding);
        }

        return result;

    }
}
