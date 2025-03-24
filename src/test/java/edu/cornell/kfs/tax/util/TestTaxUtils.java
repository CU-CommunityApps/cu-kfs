package edu.cornell.kfs.tax.util;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.tax.batch.TestTransactionDetailCsvInputFileType;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public final class TestTaxUtils {

    @SuppressWarnings("unchecked")
    public static List<TransactionDetail> buildTransactionDetailsFromCsvData(
            final TestTransactionDetailCsvInputFileType transactionDetailCsvFileType,
                    final String filePath) throws Exception {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(filePath)) {
            final byte[] fileContents = IOUtils.toByteArray(fileStream);
            return (List<TransactionDetail>) transactionDetailCsvFileType.parse(fileContents);
        }
    }

}
