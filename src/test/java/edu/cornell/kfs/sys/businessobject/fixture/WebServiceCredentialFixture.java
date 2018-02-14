package edu.cornell.kfs.sys.businessobject.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;

public enum WebServiceCredentialFixture {
    TESTGRP_BASE_URL(CuFPTestConstants.TEST_CREDENTIAL_GROUP_CODE, CuFPConstants.CREDENTIAL_BASE_URL + "1", "www.cornell.edu", true),
    TESTGRP_TEST_KEY(CuFPTestConstants.TEST_CREDENTIAL_GROUP_CODE, "testkey", "abc123", true),
    AWS_BILL_BASE_URL(CuFPTestConstants.AWS_CREDENTIAL_GROUP_CODE, CuFPConstants.CREDENTIAL_BASE_URL + "1", "kfsaws-support.cd.cucloud.net", true);

    public final String credentialGroupCode;
    public final String credentialKey;
    public final String credentialValue;
    public final boolean active;

    private WebServiceCredentialFixture(String credentialGroupCode, String credentialKey,
            String credentialValue, boolean active) {
        this.credentialGroupCode = credentialGroupCode;
        this.credentialKey = credentialKey;
        this.credentialValue = credentialValue;
        this.active = active;
    }

    public WebServiceCredential toWebServiceCredential() {
        WebServiceCredential webServiceCredential = new WebServiceCredential();
        webServiceCredential.setCredentialGroupCode(credentialGroupCode);
        webServiceCredential.setCredentialKey(credentialKey);
        webServiceCredential.setCredentialValue(credentialValue);
        webServiceCredential.setActive(active);
        return webServiceCredential;
    }

    public static List<WebServiceCredential> getCredentialsByCredentialGroupCode(String groupCode) {
        return Arrays.stream(WebServiceCredentialFixture.values())
                .filter((fixture) -> StringUtils.equals(groupCode, fixture.credentialGroupCode))
                .map(WebServiceCredentialFixture::toWebServiceCredential)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
