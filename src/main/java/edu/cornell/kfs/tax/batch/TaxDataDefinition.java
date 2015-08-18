package edu.cornell.kfs.tax.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper object defining how to retrieve the tax data from the database,
 * as well as defining the mappings from data columns to field aliases.
 * Encapsulates TaxDataRow objects that contain the relevant mapping info.
 */
public class TaxDataDefinition {

    private List<TaxDataRow> dataRows;

    public TaxDataDefinition() {
        this.dataRows = new ArrayList<TaxDataRow>();
    }

    public List<TaxDataRow> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<TaxDataRow> dataRows) {
        this.dataRows = dataRows;
    }

    public void addDataRow(TaxDataRow dataRow) {
        dataRows.add(dataRow);
    }

    /**
     * Helper method to retrieve the TaxDataRow objects as a Map
     * from row IDs to data rows.
     * 
     * @return A Map with the row IDs as keys and their data rows as values.
     */
    public Map<String,TaxDataRow> getDataRowsAsMap() {
        Map<String,TaxDataRow> dataRowsMap = new HashMap<String,TaxDataRow>();
        for (TaxDataRow dataRow : dataRows) {
            dataRowsMap.put(dataRow.getRowId(), dataRow);
        }
        return dataRowsMap;
    }

}
