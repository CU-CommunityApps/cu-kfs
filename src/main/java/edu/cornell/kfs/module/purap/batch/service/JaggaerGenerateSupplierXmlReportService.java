package edu.cornell.kfs.module.purap.batch.service;

import java.util.List;

import edu.cornell.kfs.module.purap.batch.service.impl.JaggaerUploadSupplierXmlFileDetailsDto;

public interface JaggaerGenerateSupplierXmlReportService {
    public void generateAndEmailResultsReport(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos);
}
