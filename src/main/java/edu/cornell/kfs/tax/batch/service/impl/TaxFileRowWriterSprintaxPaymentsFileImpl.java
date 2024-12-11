package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;

import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;

public class TaxFileRowWriterSprintaxPaymentsFileImpl extends TaxFileRowWriterSprintaxBase {

    public TaxFileRowWriterSprintaxPaymentsFileImpl(final String outputFileName, final String taxFileType,
            final TaxOutputDefinition taxOutputDefinition) throws IOException {
        super(outputFileName, taxFileType, taxOutputDefinition);
    }

    @Override
    public void writeDataRow(final SprintaxInfo1042S taxFileRow, final String sectionName) throws IOException {
        
    }

}
