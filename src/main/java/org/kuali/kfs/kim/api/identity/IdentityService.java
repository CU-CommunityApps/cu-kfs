/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.api.identity;

import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.impl.identity.address.EntityAddressType;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;
import org.kuali.kfs.kim.impl.identity.email.EntityEmailType;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentStatus;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentType;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.external.EntityExternalIdentifierType;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.name.EntityNameType;
import org.kuali.kfs.kim.impl.identity.phone.EntityPhoneType;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.identity.privacy.EntityPrivacyPreferences;

import java.util.List;

/**
 * This service provides operations to query for principal and identity data
 *
 * <p>A principal represents an identity that can authenticate. In essence, a principal can be thought of as an
 * "account" or as an identity's authentication credentials. A principal has an id which is used to uniquely identify
 * it. It also has a name which represents the principal's username and is typically what is entered when
 * authenticating. All principals are associated with one and only one identity.
 *
 * <p>An identity represents a person or system. Additionally, other "types" of entities can be defined in KIM.
 * Information like name, phone number, etc. is associated with an identity. It is the representation of a concrete
 * person or system. While an identity will typically have a single principal associated with it, it is possible for
 * an identity to have more than one principal or even no principals at all (in the case where the identity does not
 * actually authenticate).
 *
 * <p>This service also provides operations for querying various pieces of reference data, such as address types,
 * affiliation types, phone types, etc.
 */
// CU customization: add back getPrincipalsByEntityId method
public interface IdentityService {

    /**
     * This method finds Entity data based on a query criteria. The criteria cannot be null.
     *
     * @param query the criteria.  Cannot be null.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if the queryByCriteria is null
     */
    GenericQueryResults<Entity> findEntities(QueryByCriteria query) throws IllegalArgumentException;

    /**
     * Gets a {@link Entity} from an id.
     * <p>This method will return null if the Entity does not exist.
     *
     * @param id the unique id to retrieve the entity by. cannot be null.
     * @return a {@link Entity} or null
     * @throws IllegalArgumentException if the id is blank
     */
    Entity getEntity(String id) throws IllegalArgumentException;

    /**
     * Gets a {@link Entity} from a principalId.
     *
     * <p>This method will return null if the Entity does not exist.
     *
     * @param principalId the unique id to retrieve the entity by. cannot be null.
     * @return a {@link Entity} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    Entity getEntityByPrincipalId(String principalId) throws IllegalArgumentException;

    /**
     * Gets a {@link Entity} from a principalName.
     * <p>This method will return null if the Entity does not exist.
     *
     * @param principalName the unique id to retrieve the entity by. cannot be null.
     * @return a {@link Entity} or null
     * @throws IllegalArgumentException if the id is blank
     */
    Entity getEntityByPrincipalName(String principalName) throws IllegalArgumentException;

    /**
     * Gets a {@link Entity} from a employeeId.
     * <p>This method will return null if the Entity does not exist.
     *
     * @param employeeId the unique id to retrieve the entity by. cannot be null.
     * @return a {@link Entity} or null
     * @throws IllegalArgumentException if the employeeId is blank
     */
    Entity getEntityByEmployeeId(String employeeId) throws IllegalArgumentException;

    /**
     * Gets a {@link Principal} from an principalId.
     * <p>This method will return null if the Principal does not exist.
     *
     * @param principalId the unique id to retrieve the principal by. cannot be null.
     * @return a {@link Principal} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    Principal getPrincipal(String principalId) throws IllegalArgumentException;

    /**
     * Gets a list of {@link Principal} from a string list of principalId.
     * <p>This method will only return principals that exist.
     *
     * @param principalIds the unique id to retrieve the principal by. cannot be null.
     * @return a list of {@link Principal}
     * @throws IllegalArgumentException if the principalId is blank
     */
    List<Principal> getPrincipals(List<String> principalIds);

    /**
     * Gets a list of {@link Principal} from an entityId.
     * <p>This method will only return principals that exist.
     *
     * @param entityId the unique id to retrieve the principals by. cannot be null.
     * @return a list of {@link Principal}
     * @throws IllegalArgumentException if the entityId is blank
     */
    List<Principal> getPrincipalsByEntityId(String entityId);

    /**
     * Gets a list of {@link Principal} from an employeeId
     * <p>This method will only return principals that exist.
     *
     * @param employeeId the employee id to retrieve the principals by. cannot be null.
     * @return a list of {@link Principal}
     * @throws IllegalArgumentException if the employeeId is blank
     */
    List<Principal> getPrincipalsByEmployeeId(String employeeId);

    /**
     * Gets a {@link Principal} from an principalName.
     * <p>This method will return null if the Principal does not exist.
     *
     * @param principalName the unique id to retrieve the principal by. cannot be null.
     * @return a {@link Principal} or null
     */
    Principal getPrincipalByPrincipalName(String principalName) throws IllegalArgumentException;

    /**
     * This returns the display name information for the given principal without loading the full person object.
     *
     * @param principalId The principal ID to find the name information for
     * @return The default name information for the principal
     */
    EntityName getDefaultNamesForPrincipalId(String principalId);

    /**
     * Gets a {@link EntityPrivacyPreferences} for a given id.
     * <p>This method will return null if the EntityPrivacyPreferences does not exist.
     *
     * @param id the unique id to retrieve the EntityPrivacyPreferences by. Cannot be null.
     * @return a {@link EntityPrivacyPreferences} or null
     * @throws IllegalArgumentException if the entityId is blank
     */
    EntityPrivacyPreferences getEntityPrivacyPreferences(String id) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityAddressType} for a given EntityAddressType code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityAddressType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityAddressType getAddressType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityAffiliationType} for a given EntityAffiliationType code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the EntityAffiliationType by. Cannot be null.
     * @return a {@link EntityAffiliationType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityAffiliationType getAffiliationType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityEmploymentType} for a given EntityEmployment type code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityEmploymentType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityEmploymentType getEmploymentType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityEmploymentStatus} for a given EntityEmployment status code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityEmploymentStatus} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityEmploymentStatus getEmploymentStatus(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityExternalIdentifierType} for a given type code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the EntityExternalIdentifierType by. Cannot be null.
     * @return a {@link EntityExternalIdentifierType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityExternalIdentifierType getExternalIdentifierType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityNameType} for a given EntityName type code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityNameType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityNameType getNameType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityPhoneType} for a given EntityPhone type code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityPhoneType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityPhoneType getPhoneType(String code) throws IllegalArgumentException;

    /**
     * Gets the {@link EntityEmailType} for a given EntityEmail type code.
     * <p>This method will return null if the code does not exist.
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link EntityEmailType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    EntityEmailType getEmailType(String code) throws IllegalArgumentException;

    /**
     * This method finds Principals based on a query criteria. The criteria cannot be null.
     *
     * @param query the criteria.  Cannot be null.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if the queryByCriteria is null
     */
    GenericQueryResults<Principal> findPrincipals(QueryByCriteria query) throws IllegalArgumentException;
}
