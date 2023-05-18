package edu.cornell.kfs.module.purap.batch.service;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;

public interface JaggaerGenerateSupplierXmlService {

    public List<SupplierSyncMessage> getSupplierSyncMessages(JaggaerUploadSuppliersProcessingMode processingMode,
            Date processingDate, int maximumNumberOfSuppliersPerListItem);

    public void generateXMLForSyncMessages(List<SupplierSyncMessage> messages);
}
