package org.aksw.commons.model.csvw.univocity;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.commons.model.csvw.domain.api.Dialect;
import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.CsvwLib;
import org.aksw.commons.model.csvw.term.CsvwTerms;

import com.univocity.parsers.common.CommonParserSettings;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class CsvwUnivocityUtils {

    /** Does not configure the format */
    public static Set<String> configureCommonSettings(CommonParserSettings<?> settings, Dialect dialect) {
        Set<String> affectedTerms = new LinkedHashSet<>();

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

        return affectedTerms;
    }

    public static Set<String> configureDetection(CsvParserSettings settings, Dialect dialect) {
        Set<String> affectedTerms = new HashSet<>();

        if (dialect.getLineTerminators() == null) {
            settings.setLineSeparatorDetectionEnabled(true);
            affectedTerms.add(CsvwTerms.lineTerminators);
        }

        if (dialect.getDelimiter() == null) {
            settings.setDelimiterDetectionEnabled(true);
            affectedTerms.add(CsvwTerms.delimiter);
        }

        if (dialect.getQuoteChar() == null) {
            settings.setQuoteDetectionEnabled(true);
            affectedTerms.add(CsvwTerms.quoteChar);
        }

        return affectedTerms;
    }

    public static Set<String> configureCsvFormat(CsvFormat format, Dialect dialect) {
        Set<String> affectedTerms = new HashSet<>();
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

    /** Configure a dialect with the attributes detected by univocity */
    public static Set<String> configureDialect(DialectMutable dialect, CsvFormat format) {
        Set<String> affectedTerms = new LinkedHashSet<>();
        String str;

        if ((str = format.getDelimiterString()) != null && !Objects.equals(str, dialect.getDelimiter())) {
            dialect.setDelimiter(str);
            affectedTerms.add(CsvwTerms.delimiter);
        }

        // FIXME Line terminators need to be a json array
        if ((str = format.getLineSeparatorString()) != null && !Objects.equals(str, dialect.getLineTerminators())) {
            dialect.setLineTerminators(str);
            affectedTerms.add(CsvwTerms.lineTerminators);
        }

        str = Character.toString(format.getQuote());
        if (!Objects.equals(str, dialect.getQuoteChar())) {
            dialect.setQuoteChar(str);
            affectedTerms.add(CsvwTerms.quoteChar);
        }

        str = Character.toString(format.getQuoteEscape());
        if (!Objects.equals(str, dialect.getQuoteEscapeChar())) {
            dialect.setQuoteEscapeChar(str);
            affectedTerms.add(CsvwTerms.quoteEscapeChar);
        }

        return affectedTerms;
    }

    public static boolean isDetectionNeeded(CsvParserSettings settings) {
        boolean result = settings.isLineSeparatorDetectionEnabled()
                || settings.isDelimiterDetectionEnabled()
                || settings.isQuoteDetectionEnabled();

        return result;
    }

    public static boolean isDetectionNeeded(CommonParserSettings<?> settings) {
        boolean result = settings.isLineSeparatorDetectionEnabled();
        return result;
    }

    public static CsvFormat detectFormat(CsvParser parser, Callable<Reader> readerSupp) throws Exception {
        CsvFormat result;
        try (Reader reader = readerSupp.call()) {
             parser.beginParsing(reader);
             result = parser.getDetectedFormat();
        } finally {
             parser.stopParsing();
        }
        return result;
    }

    /** Checks for certain non-configured univocity settings and if there are any then
     * a parser is started for probing.
     * Those settings are: line terminators, field delimiters and quote char.
     * If they are given then nothing is done.
     */
    public static Set<String> configureDialect(DialectMutable dialect, CsvParserSettings settings, Callable<CsvParser> parserFactory, Callable<Reader> readerSupp) throws Exception {
        Set<String> result = Collections.emptySet();
        if (isDetectionNeeded(settings)) {
            CsvParser parser = parserFactory.call();
            CsvFormat format = detectFormat(parser, readerSupp);
            result = configureDialect(dialect, format);
        }
        return result;
    }
}
