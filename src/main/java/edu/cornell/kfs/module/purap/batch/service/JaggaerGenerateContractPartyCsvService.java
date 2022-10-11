package edu.cornell.kfs.module.purap.batch.service;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public interface JaggaerGenerateContractPartyCsvService {
    
    public List<JaggaerContractUploadBaseDto> getJaggaerContractsDto(JaggaerContractUploadProcessingMode processingMode, Date processingDate);
    
    public void generateCsvFile(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos, JaggaerContractUploadProcessingMode processingMode);

}
