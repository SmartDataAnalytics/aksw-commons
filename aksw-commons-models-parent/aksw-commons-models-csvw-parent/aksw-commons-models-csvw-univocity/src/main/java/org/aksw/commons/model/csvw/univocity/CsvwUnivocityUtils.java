package org.aksw.commons.model.csvw.univocity;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.model.csvw.domain.api.Dialect;
import org.aksw.commons.model.csvw.domain.impl.CsvwLib;
import org.aksw.commons.model.csvw.term.CsvwTerms;

import com.univocity.parsers.common.CommonParserSettings;
import com.univocity.parsers.csv.CsvFormat;

public class CsvwUnivocityUtils {

    /** Does not configure the format */
    public static Set<String> configureCommonSettings(CommonParserSettings<?> settings, Dialect dialect) {
        Set<String> affectedTerms = new HashSet<>();

        Boolean b;
        // Character c;
        Long l;
        String str;

        if ((b = dialect.getSkipBlankRows()) != null) {
            settings.setSkipEmptyLines(b);
            affectedTerms.add(CsvwTerms.skipBlankRows);
        }

        // TODO any value results in true - maybe not what we want?
        if ((str = dialect.getTrim()) != null && !str.isBlank()) {
            settings.trimValues(true);
            affectedTerms.add(CsvwTerms.trim);
        }

        if ((l = dialect.getHeaderRowCount()) != null) {
            if (l > 1) {
                throw new IllegalArgumentException("Only at most one header row presently supported");
            }
            settings.setHeaderExtractionEnabled(l > 0);
            affectedTerms.add(CsvwTerms.headerRowCount);
        }

        return null;
    }

    public static Set<String> configureCsvFormat(CsvFormat format, Dialect dialect) {
        Set<String> affectedTerms = new HashSet<>();

        Boolean b;
        String str;

        if (CsvwLib.isPresent(str = dialect.getQuoteChar())) {
            format.setQuote(CsvwLib.expectAtMostOneChar("quoteChar", str));
            affectedTerms.add(CsvwTerms.quoteChar);
        }

        if (CsvwLib.isPresent(str = dialect.getDelimiter())) {
            format.setDelimiter(str);
            affectedTerms.add(CsvwTerms.delimiter);
        }

        if (CsvwLib.isPresent(str = dialect.getCommentPrefix())) {
            format.setComment(CsvwLib.expectAtMostOneChar("commentChar", str));
            affectedTerms.add(CsvwTerms.commentPrefix);
        }

        if (CsvwLib.isPresent(str = dialect.getLineTerminators())) {
            format.setLineSeparator(str);
            affectedTerms.add(CsvwTerms.lineTerminators);
        }

        if (CsvwLib.isPresent(str = dialect.getQuoteEscapeChar())) {
            format.setQuoteEscape(CsvwLib.expectAtMostOneChar("quoteEscapeChar", str));
            affectedTerms.add(CsvwTerms.quoteChar);
        }

        return affectedTerms;
    }
}
