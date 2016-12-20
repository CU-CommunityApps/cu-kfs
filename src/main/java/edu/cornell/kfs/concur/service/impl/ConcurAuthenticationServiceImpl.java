package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.concur.service.ConcurAuthenticationService;

public class ConcurAuthenticationServiceImpl implements ConcurAuthenticationService {

    private String concurToken;

    @Override
    public boolean isConcurTokenValid(String userPasswordToken) {
        boolean isValid = false;

        if (StringUtils.isNotBlank(userPasswordToken) && userPasswordToken.equalsIgnoreCase(concurToken)) {
            isValid = true;
        }

        return isValid;
    }

    public void setConcurToken(String concurToken) {
        this.concurToken = concurToken;
    }

}
