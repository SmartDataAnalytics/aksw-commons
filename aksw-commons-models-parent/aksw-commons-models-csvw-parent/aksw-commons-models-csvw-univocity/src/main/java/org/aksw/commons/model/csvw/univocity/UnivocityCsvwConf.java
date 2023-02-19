package org.aksw.commons.model.csvw.univocity;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.aksw.commons.model.csvw.jackson.DialectMutableForwardingJackson;
import org.aksw.commons.model.csvw.jackson.DialectMutableForwardingJacksonString;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonMerge;

/**
 * This class 'extends' a dialect with a flag for 'tabs'.
 * Probably a dialect with a tab field separator could reasonable replace this class.
 * However, because univocity uses somewhat different infrastructures for TSV and CSV
 * this class exists for now.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class UnivocityCsvwConf
{
    protected DialectMutableForwardingJackson<?> dialect;

    protected boolean isTabs = false;
    // protected List<String> columnNamingSchemes;

    public UnivocityCsvwConf() {
        this(new DialectMutableImpl());
    }

    public UnivocityCsvwConf(DialectMutable dialectStore) {
        this.dialect = new DialectMutableForwardingJacksonString<>(dialectStore);
    }

    /** The csvw dialect roughly corresponds to univocity's CsvFormat class */
    @JsonMerge
    public DialectMutable getDialect() {
        return dialect;
    }

    public boolean isTabs() {
        return isTabs;
    }

    public void setTabs(boolean tabs) {
        isTabs = tabs;
    }

//    public void setColumnNamingSchemes(List<String> columnNamingSchemes) {
//        this.columnNamingSchemes = columnNamingSchemes;
//    }
//
//    public List<String> getColumnNamingSchemes() {
//        return columnNamingSchemes;
//    }
}