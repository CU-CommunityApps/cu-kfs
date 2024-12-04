package edu.cornell.kfs.tax.batch.dto;

import java.util.Map;

public interface TaxFileRow {

    Map<String, String> generateFileRowValues(final String sectionName);

}
