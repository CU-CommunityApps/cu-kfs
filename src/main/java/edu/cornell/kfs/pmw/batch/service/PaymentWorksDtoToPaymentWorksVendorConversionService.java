package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;

public interface PaymentWorksDtoToPaymentWorksVendorConversionService {
    
    PaymentWorksVendor createPaymentWorksVendorFromPaymentWorksNewVendorRequestDetailDTO(PaymentWorksNewVendorRequestDetailDTO pmwNewVendorRequestDetailDTO, PaymentWorksNewVendorRequestsBatchReportData reportData);
}
