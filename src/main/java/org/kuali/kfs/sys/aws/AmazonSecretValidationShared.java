package org.kuali.kfs.sys.aws;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AmazonSecretValidationShared {
    private String login_username;
    private String login_password;

    public String getLogin_username() {
        return login_username;
    }

    public String getLogin_password() {
        return login_password;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
