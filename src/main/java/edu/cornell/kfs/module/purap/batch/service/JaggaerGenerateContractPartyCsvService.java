package edu.cornell.kfs.module.purap.batch.service;

import java.util.List;

import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public interface JaggaerGenerateContractPartyCsvService {
    
    public List<JaggaerContractUploadBaseDto> getJaggerContractsDto();
    
    public void generateCsvFile(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos);

}
