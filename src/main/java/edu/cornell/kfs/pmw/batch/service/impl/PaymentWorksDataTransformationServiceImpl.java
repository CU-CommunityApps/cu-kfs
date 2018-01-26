package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksTinType;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDataTransformationService;

public class PaymentWorksDataTransformationServiceImpl implements PaymentWorksDataTransformationService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksDataTransformationServiceImpl.class);
    
    @Override
    public String convertPmwTinTypeCodeToPmwTinTypeText(String pmwTinTypeCodeToConvert) {
        String returnValue = KFSConstants.EMPTY_STRING;
        List<PaymentWorksTinType> matchingValues = Arrays.asList(PaymentWorksConstants.PaymentWorksTinType.values())
                                                         .stream()
                                                         .filter(tinType ->  tinType.getPmwCodeAsString().equalsIgnoreCase(pmwTinTypeCodeToConvert))
                                                         .collect(Collectors.toList());
        returnValue = matchingValues.size() == 1 ? matchingValues.get(0).getPmwText() : pmwTinTypeCodeToConvert;
        return returnValue;
    }

}
