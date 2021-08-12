package edu.cornell.kfs.ksr.service;

import java.util.List;

import edu.cornell.kfs.ksr.businessobject.SecurityGroup;

public interface SecurityRequestDocumentService {

    List<SecurityGroup> getActiveSecurityGroups();
}
