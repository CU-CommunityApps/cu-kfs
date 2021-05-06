package edu.cornell.kfs.krad.service.impl;

import org.kuali.kfs.krad.service.impl.KualiExceptionIncidentServiceImpl;

public class CuKualiExceptionIncidentServiceImpl extends KualiExceptionIncidentServiceImpl {
    
    //Cornell Customization: Base code implemenation override required due to AppSMTP 
    //                       restriction where From email address must be a registered EGA.
    @Override
    protected String getFromAddress() {
    /* Base code at time of cutomization creation
     * 
        Person actualUser = GlobalVariables.getUserSession().getActualPerson();

        String fromEmail = actualUser.getEmailAddress();
        if (StringUtils.isNotBlank(fromEmail)) {
            return fromEmail;
        } else {
            return this.getMessageTemplate().getFromAddress();
        }
     *
     */
        
        // "kr.incident.mailing.list" is set to @incident_email in file kfs-config.properties.erb
        // @incident_email is then defined in each environment's puppet configuration file
        return this.getMessageTemplate().getFromAddress();
    }
}
