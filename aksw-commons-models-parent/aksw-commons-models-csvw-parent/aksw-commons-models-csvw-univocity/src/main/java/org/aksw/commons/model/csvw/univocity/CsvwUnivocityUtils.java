package org.aksw.commons.model.csvw.univocity;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.model.csvw.domain.api.Dialect;
import org.aksw.commons.model.csvw.domain.impl.CsvwLib;
import org.aksw.commons.model.csvw.term.CsvwTerms;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParserSettings;

public class CsvwUnivocityUtils {

    public static Set<String> configure(Dialect dialect, CsvParserSettings settings) {
        Set<String> affectedTerms = new HashSet<>();
        CsvFormat format = settings.getFormat();

        String value;
        if (CsvwLib.isPresent(value = dialect.getQuoteChar())) {
            format.setQuote(CsvwLib.expectOneChar("quoteChar", value));
            affectedTerms.add(CsvwTerms.quoteChar);
        }

        if (CsvwLib.isPresent(value = dialect.getDelimiter())) {
            format.setDelimiter(value);
            affectedTerms.add(CsvwTerms.delimiter);
        }

        if (CsvwLib.isPresent(value = dialect.getCommentPrefix())) {
            format.setComment(CsvwLib.expectOneChar("commentChar", value));
            affectedTerms.add(CsvwTerms.commentPrefix);
        }

        if (CsvwLib.isPresent(value = dialect.getLineTerminators())) {
            format.setLineSeparator(value);
            affectedTerms.add(CsvwTerms.lineTerminators);
        }

        return affectedTerms;
    }
}
