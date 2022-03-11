package org.aksw.commons.model.csvw.picocli;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableForwardingBase;

import picocli.CommandLine.Option;

/**
 * Mixin for csv configuration with picocli. Aimed for drop-in replacement with tarql.
 *
 */
public class PicocliMixinCsvw<D extends DialectMutable>
    extends DialectMutableForwardingBase<D>
{

    public PicocliMixinCsvw(D delegate) {
        super(delegate);
    }

    public static PicocliMixinCsvw<?> create(DialectMutable delegate) {
        return new PicocliMixinCsvw<DialectMutable>(delegate);
    }

    @Option(names={"-d", "--delimiter"}, description="Delimiter")
    @Override
    public DialectMutable setDelimiter(String delimiter) { return super.setDelimiter(delimiter); }

    @Option(names={"-e", "--encoding"}, description="Encoding")
    @Override
    public DialectMutable setEncoding(String encoding) {return super.setEncoding(encoding); }

    @Option(names={"-H", "--no-header-row"}, description="no header row; use variable names ?a, ?b, ...")
    public DialectMutable setNoHeader(Boolean noHeader) {
        if (Boolean.TRUE.equals(noHeader)) {
            super.setHeader(false);
        }
        return this;
    }

    @Option(names={"--header-row"}, description="Input file's first row is a header with variable names (default)")
    public DialectMutable setHeaderRow(Boolean noHeader) {
        if (!Boolean.FALSE.equals(noHeader)) {
            setHeaderRowCount(1l);
        }
        return this;
    }

    @Option(names={"-d", "--quotechar"}, description="Quote character")
    @Override
    public DialectMutable setQuoteChar(String quoteChar) { return super.setQuoteChar(quoteChar); }



}
