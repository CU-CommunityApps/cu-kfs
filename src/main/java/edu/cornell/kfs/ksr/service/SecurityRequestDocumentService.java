package edu.cornell.kfs.ksr.service;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

public interface SecurityRequestDocumentService {

    List<SecurityGroup> getActiveSecurityGroups();
    
    void prepareSecurityRequestDocument(SecurityRequestDocument document);
    
    SecurityRequestRoleQualification buildRoleQualificationLine(SecurityRequestRole requestRole, Map<String,String> qualification);
    
    void initiateSecurityRequestDocument(SecurityRequestDocument document, Person user);
}
