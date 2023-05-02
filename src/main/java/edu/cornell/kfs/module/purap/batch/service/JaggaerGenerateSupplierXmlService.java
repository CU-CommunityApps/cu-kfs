package edu.cornell.kfs.module.purap.batch.service;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;

public interface JaggaerGenerateSupplierXmlService {
    
    public List<SupplierSyncMessage> getJaggaerContractsDto(JaggaerContractUploadProcessingMode processingMode, Date processingDate, int maximumNumberOfSuppliersPerListItem);
    public void generateXMLForSyncMessages(List<SupplierSyncMessage> messages);
}
