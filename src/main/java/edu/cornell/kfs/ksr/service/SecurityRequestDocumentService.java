/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package edu.cornell.kfs.ksr.service;

import java.util.List;

import edu.cornell.kfs.ksr.businessobject.SecurityGroup;

public interface SecurityRequestDocumentService {

    /**
     * Prepares a new <code>SecurityRequestDocument</code> instance for viewing by retrieving information from KIM and the security request tables based on the
     * current state of the document
     * 
     * @param document
     *            - SecurityRequestDocument instance to initiate
     * @param user
     *            - current user requesting the document instance
     */
    // public void initiateSecurityRequestDocument(SecurityRequestDocument document, Person user);

    /**
     * Prepares a <code>SecurityRequestDocument</code> instance after loading from the database
     * 
     * <p>
     * If document state is saved, then editing is still allowed and new role qualification lines need to be prepared
     * </p>
     * 
     * @param document
     *            - SecurityRequestDocument instance to prepare
     */
    // public void prepareSecurityRequestDocument(SecurityRequestDocument document);

    /**
     * Builds a <code>SecurityRequestRoleQualification</code> instance for the given security request role
     * 
     * <p>
     * New instance will be populated with attribute values from the given qualification set, or if the qualification is null will contain empty values
     * </p>
     * 
     * @param requestRole
     *            - SecurityRequestRole instance to pull type for
     * @param qualification
     *            - any existing qualifications to populate new line with
     * @return SecurityRequestRoleQualification instance
     */
    // public SecurityRequestRoleQualification buildRoleQualificationLine(SecurityRequestRole requestRole, Map<String,String> qualification);

    /**
     * Gets all active SecurityGroup.
     * 
     * @return list of active SecurityGroup objects
     */
    public List<SecurityGroup> getActiveSecurityGroups();
}
