package org.aksw.commons.model.csvw.picocli;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableForwardingBase;

import picocli.CommandLine.Option;

/**
 * Mixin for csv configuration with picocli. Aimed for compatibility with tarql.
 *
 */
public class PicocliMixinCsvw<D extends DialectMutable>
    extends DialectMutableForwardingBase<D>
{
    protected PicocliMixinCsvw(D delegate) {
        super(delegate);
    }

    public static PicocliMixinCsvw<?> of(DialectMutable delegate) {
        return new PicocliMixinCsvw<>(delegate);
    }

    @Option(names={"-d", "--delimiter"}, description="Delimiter")
    @Override
    public DialectMutable setDelimiter(String delimiter) { return super.setDelimiter(delimiter); }

    @Option(names={"-e", "--encoding"}, description="Encoding (e.g. UTF-8, ISO-8859-1)")
    @Override
    public DialectMutable setEncoding(String encoding) {return super.setEncoding(encoding); }

    @Option(names={"-H", "--no-header-row"}, description="no header row; use variable names ?a, ?b, ...")
    public DialectMutable setNoHeader(Boolean noHeader) {
        if (Boolean.TRUE.equals(noHeader)) {
            setHeaderRowCount(0l);
        }
        return this;
    }

    @Option(names={"--header-row"}, description="Input file's first row is a header with variable names (default)")
    public DialectMutable setHeaderRow(Boolean headerRow) {
        if (!Boolean.FALSE.equals(headerRow)) {
            setHeaderRowCount(1l);
        }
        return this;
    }

    @Option(names={"-q", "--quotechar"}, description="Quote character")
    @Override
    public DialectMutable setQuoteChar(String quoteChar) { return super.setQuoteChar(quoteChar); }

    @Option(names={"-p", "--escapechar"}, description="Quote escape character")
    @Override
    public DialectMutable setQuoteEscapeChar(String quoteEscapeChar) { return super.setQuoteEscapeChar(quoteEscapeChar); }
}
