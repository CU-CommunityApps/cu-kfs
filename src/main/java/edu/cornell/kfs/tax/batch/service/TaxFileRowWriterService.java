package edu.cornell.kfs.tax.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.function.FailableRunnable;

import edu.cornell.kfs.tax.batch.dto.TaxFileRow;

public interface TaxFileRowWriterService {

    void doWithNewOutputFile(final int reportYear, final Date processingStartDate,
            final FailableRunnable<Exception> task) throws IOException, SQLException;

    void writeHeaderRow(final String sectionName) throws IOException;

    void writeDataRow(final TaxFileRow taxFileRow, final String sectionName) throws IOException;

}
