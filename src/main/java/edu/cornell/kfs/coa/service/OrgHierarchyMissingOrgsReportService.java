package edu.cornell.kfs.coa.service;

import java.io.File;
import java.util.Set;

import org.kuali.kfs.coa.businessobject.Organization;

public interface OrgHierarchyMissingOrgsReportService {

	public Set<Organization> findMissing(File baseOrgs, File orgHierarchies);
	
}
