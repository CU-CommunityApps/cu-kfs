package edu.cornell.kfs.concur.batch.fixture;

import java.util.HashMap;

import edu.cornell.kfs.concur.ConcurParameterConstants;

public class ConcurParameterConstantsFixture {

    HashMap<String, String> definedParameters;        

    public ConcurParameterConstantsFixture() {
        this.definedParameters = new HashMap<String, String>();
        this.definedParameters.put(ConcurParameterConstants.CONCUR_AP_PDP_ORIGINATION_CODE, "Z6");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_SAE_PDP_DOCUMENT_TYPE, "APTR");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID, "CORNELL");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION, "IT");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT, "CRNL");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT, "CNCR");
        this.definedParameters.put(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE, "1400");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_PENDING_CLIENT_OBJECT_CODE_OVERRIDE, "6750");
        this.definedParameters.put(ConcurParameterConstants.CONCUR_REQUEST_EXTRACT_PDP_DOCUMENT_TYPE, "APTA");
    }
    
    public String getValueForConcurParameter(String parameterKey) {
        return definedParameters.get(parameterKey);
    }

}
