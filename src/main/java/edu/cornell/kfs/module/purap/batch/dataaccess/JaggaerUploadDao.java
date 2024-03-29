package edu.cornell.kfs.module.purap.batch.dataaccess;

import java.sql.Date;
import java.util.List;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;

public interface JaggaerUploadDao {
    
    public List<JaggaerContractPartyUploadDto> findJaggaerContractParty(JaggaerContractUploadProcessingMode processingMode, Date processingDate);
    
    public List<JaggaerContractAddressUploadDto> findJaggaerContractAddress(JaggaerContractUploadProcessingMode processingMode, Date processingDate);
    
    public List<VendorDetail> findVendors(JaggaerUploadSuppliersProcessingMode processingMode, Date processingDate);

}
