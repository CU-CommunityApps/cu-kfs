package edu.cornell.kfs.module.purap.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;

public interface JaggaerUploadDao {
    
    public List<JaggaerContractPartyUploadDto> findJaggaerContractParty();
    
    public List<JaggaerContractAddressUploadDto> findJaggaerContractAddress();

}
