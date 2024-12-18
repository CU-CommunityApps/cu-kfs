package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.util.Map;

import edu.cornell.kfs.tax.batch.SprintaxBioFileField;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;

public class TaxFileRowWriterSprintaxBioFileImpl extends TaxFileRowWriterSprintaxBase {

    public TaxFileRowWriterSprintaxBioFileImpl(final String outputFileName, final String taxFileType,
            final TaxOutputDefinition taxOutputDefinition) throws IOException {
        super(outputFileName, taxFileType, taxOutputDefinition);
    }

    @Override
    public void writeDataRow(final SprintaxInfo1042S taxFileRow, final String sectionName) throws IOException {
        final Map<String, String> dataValues = Map.ofEntries(
                buildEntry(SprintaxBioFileField.vendorFirstName, taxFileRow.getVendorFirstName()),
                buildEntry(SprintaxBioFileField.vendorLastName, taxFileRow.getVendorLastName()),
                buildEntry(SprintaxBioFileField.vendorEmailAddress, taxFileRow.getVendorEmailAddress()),
                buildEntry(SprintaxBioFileField.studentOrVendorId, taxFileRow.getPayeeId()),
                buildEntry(SprintaxBioFileField.ssn, taxFileRow.getFormattedSSNValue()),
                buildEntry(SprintaxBioFileField.itin, taxFileRow.getFormattedITINValue()),
                buildEntry(SprintaxBioFileField.vendorUSAddressLine1, taxFileRow.getVendorUSAddressLine1()),
                buildEntry(SprintaxBioFileField.vendorUSAddressLine2, taxFileRow.getVendorUSAddressLine2()),
                buildEntry(SprintaxBioFileField.vendorUSCityName, taxFileRow.getVendorUSCityName()),
                buildEntry(SprintaxBioFileField.vendorUSStateCode, taxFileRow.getVendorUSStateCode()),
                buildEntry(SprintaxBioFileField.vendorUSZipCode, taxFileRow.getVendorUSZipCode()),
                buildEntry(SprintaxBioFileField.vendorForeignAddressLine1, taxFileRow.getVendorForeignAddressLine1()),
                buildEntry(SprintaxBioFileField.vendorForeignCityName, taxFileRow.getVendorForeignCityName()),
                buildEntry(SprintaxBioFileField.vendorForeignProvinceName, taxFileRow.getVendorForeignProvinceName()),
                buildEntry(SprintaxBioFileField.vendorForeignZipCode, taxFileRow.getVendorForeignZipCode()),
                buildEntry(SprintaxBioFileField.vendorForeignCountryCode, taxFileRow.getVendorForeignCountryCode())
        );
        writeCsvDataRow(dataValues, sectionName);
    }

}
