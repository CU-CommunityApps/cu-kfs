package edu.cornell.kfs.krad.service.impl;

import org.kuali.kfs.krad.service.impl.KualiExceptionIncidentServiceImpl;

public class CuKualiExceptionIncidentServiceImpl extends KualiExceptionIncidentServiceImpl {
    
    //Cornell Customization: Base code implementation override required due to AppSMTP 
    //                       restriction where From email address must be a registered EGA.
    @Override
    protected String getFromAddress() {
        /* KRADSpringBeans.xml contains the bean definition that sets the fromAddress to the value for property "kr.incident.mailing.list". 
         * File "kfs-config.properties" contains this property reference :   kr.incident.mailing.list=<%= @incident_email %>
         * The incident_email property is then defined in each environment's puppet configuration file to be a specific email address.
         */
        return this.getMessageTemplate().getFromAddress();
    }
}
