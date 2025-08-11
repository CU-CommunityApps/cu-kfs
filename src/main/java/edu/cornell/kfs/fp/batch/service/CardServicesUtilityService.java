package edu.cornell.kfs.fp.batch.service;

import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public interface CardServicesUtilityService {

    public String changeFormatFromYYYYMMDDToSlashedMMDDYYYY(String dateAsYYYYMMDD);
    
    public KualiDecimal generateKualiDecimal(String stringToConvert);
    
    public void removeDoneFiles(List<String> dataFileNames);

}
