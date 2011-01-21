package org.aksw.commons.util.experiments;

import com.jamonapi.Monitor;
import org.aksw.commons.util.Files;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class Table implements Serializable {
    private static final Logger logger = Logger.getLogger(Table.class);

    private SortedSet<String> experimentNames = new TreeSet<String>();
    private SortedSet<String> labels = new TreeSet<String>();

    private int length = 0;
    private List<TableRowColumn> tableRowColumns = new ArrayList<TableRowColumn>();

    public List<TableRowColumn> getTableRowColumns() {
        return tableRowColumns;
    }


    public int size(){
        return length;
    }

    /**
     * passes each it TableRowColumn one by one to addTableRowColumn
     *
     * @param t
     */
    public void addTable(Table t) {
        for (TableRowColumn trc : t.tableRowColumns) {
            addTableRowColumn(trc);
        }
    }

    /**
     * passes it one by one to addTableRowColumn
     *
     * @param trcs
     */
    public void addTableRowColumns(List<TableRowColumn> trcs) {
        for (TableRowColumn tableRowColumn : trcs) {
            addTableRowColumn(tableRowColumn);
        }
    }

    public void addTableRowColumn(TableRowColumn trc) {
        labels.add(trc.getLabel());
        experimentNames.add(trc.getExperimentName());
        /*try {
            trc.toLatexRow();
        } catch (NullPointerException e) {
            logger.error("TableRowColumn was not initialized, ignoring it: " + trc);
            e.printStackTrace();
        }*/

        if (tableRowColumns.isEmpty()) {
            length = trc.size();
        }

        if (trc.size() != length) {
            logger.error("Added TableRowColumn does not match previous set length (" + length + ") but has size " + trc.size() + "), \nignoring it: " + trc);
        }
        tableRowColumns.add(trc);
    }


    /*
   * Stupid things below
   * */


/*
    boolean replaceCommaByPoints = true;




    private int length;

    public int getLength() {
        return length;
    }

    public boolean isReplaceCommaByPoints() {
        return replaceCommaByPoints;
    }

*/


    /*public static Table deserialize(String filename) {
        return (Table) Files.readObjectfromFile(new File(filename));
	}*/

}

