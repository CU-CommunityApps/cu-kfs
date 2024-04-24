/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.kim.impl.identity;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 * CU Customization: Backported this file from the FINP-9235 changes.
 * This overlay can be removed when we upgrade to the 2023-03-08 financials patch.
 */
public class PersonSearchService extends DefaultSearchService {
    private static final Logger LOG = LogManager.getLogger();

    private static final String[] ROLE_PARAMS = {"lookupRoleNamespaceCode", "lookupRoleName"};

    private PersonService personService;
    private RoleService roleService;

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final MultiValueMap<String, String> fieldValues,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending
    ) {
        LOG.debug("getSearchResults(...): Custom search service for Person");
        if (Arrays.stream(ROLE_PARAMS).noneMatch(fieldValues::containsKey) && !Objects.equals(sortField, KIMPropertyConstants.Person.NAME)) {
            LOG.debug("No Role criteria specified, and not sorting on name, running originalPerson lookup as normal.");
            return super.getSearchResults(businessObjectClass, fieldValues, skip, limit, sortField, sortAscending);
        }

        final Map<String, String> criteria = fieldValues.toSingleValueMap();
        criteria.remove("skip");
        criteria.remove("limit");
        criteria.remove("sort");

        return findPeopleRelaxedRoleCriteria(criteria, skip, limit, sortField, sortAscending);
    }

    private Pair<Collection<? extends BusinessObjectBase>, Integer> findPeopleRelaxedRoleCriteria(
            Map<String, String> criteria,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending
    ) {
        // protect from NPEs
        if (criteria == null) {
            criteria = Collections.emptyMap();
        }
        // make a copy, so it can be modified safely in this method
        criteria = new HashMap<>(criteria);

        // extract the role lookup parameters and then remove them since later code will not know what to do with them
        final String roleName = criteria.get("lookupRoleName");
        final String namespaceCode = criteria.get("lookupRoleNamespaceCode");
        criteria.remove("lookupRoleName");
        criteria.remove("lookupRoleNamespaceCode");
        if (StringUtils.isNotBlank(roleName) || StringUtils.isNotBlank(namespaceCode)) {
            LOG.debug("Performing Person search including role filter: {}/{}", namespaceCode, roleName);
            if (criteria.isEmpty() || criteria.size() == 1 && criteria.containsKey(KRADPropertyConstants.ACTIVE)) {
                LOG.debug("Only role and active criteria specified, running role search first");
                // in this case, run the role lookup first and pass those results to the originalPerson lookup
                final Collection<String> principalIds = roleService.getRoleMemberPrincipalIdsAllowNull(
                        namespaceCode,
                        roleName,
                        Collections.emptyMap()
                );
                if (principalIds.isEmpty()) {
                    // If we don't return here, nothing will be passed to the query to limit ids, so all users will
                    // be returned, which is not what we want.
                    return Pair.of(List.of(), 0);
                }
                final String principalIdsJoin = String.join("|", principalIds);
                // add the list of principal IDs to the lookup so that only matching Person objects are returned
                criteria.put(KIMPropertyConstants.Principal.PRINCIPAL_ID, principalIdsJoin);
                // can allow internal method to filter here since no more filtering necessary
                // Need to create this and return it as a new pair, as it doesn't like implicit or explicit casting
                // from Collection<Person> to Collection<? extends BusinessObjectBase>
                final Pair<Collection<Person>, Integer> people;
                if (Objects.equals(sortField, KIMPropertyConstants.Person.NAME)) {
                    // Name is a composite field, so it can't be sorted at the db level, so need to sort manually here
                    final Pair<Collection<Person>, Integer> interim =
                            lookupDao.findObjects(Person.class, criteria, skip, limit, null, sortAscending);
                    people = Pair.of(interim.getLeft()
                            .stream()
                            .sorted(new PersonComparator(KIMPropertyConstants.Person.NAME, sortAscending))
                            .collect(Collectors.toList()), interim.getRight());
                } else {
                    people = lookupDao.findObjects(Person.class, criteria, skip, limit, sortField, sortAscending);
                }
                return Pair.of(people.getLeft(), people.getRight());
            } else {
                LOG.debug("Person criteria also specified, running that search first");
                // run the originalPerson lookup first
                // get all, since may need to be filtered
                // TODO - now check if these people have the given role
                // build a principal list
                List<String> principalIds =
                        lookupDao.findObjects(Person.class, criteria, 0, Integer.MAX_VALUE, null, sortAscending)
                                .getLeft()
                                .stream()
                                .map(Person::getPrincipalId)
                                .collect(Collectors.toList());
                // get sublist of principals that have the given roles
                principalIds = roleService.getPrincipalIdSubListWithRoleAllowNull(principalIds,
                        namespaceCode,
                        roleName,
                        Collections.emptyMap()
                );
                return Pair.of(principalIds.stream()
                        .map(principalId -> personService.getPerson(principalId))
                        .sorted(new PersonComparator(sortField, sortAscending))
                        .skip((long) skip * limit)
                        .limit(limit)
                        .collect(Collectors.toList()), principalIds.size());
            }
        }
        // Sorting on name
        final Pair<Collection<Person>, Integer> people =
                lookupDao.findObjects(Person.class, criteria, skip, limit, null, sortAscending);
        return Pair.of(people.getLeft()
                .stream()
                .sorted(new PersonComparator(sortField, sortAscending))
                .collect(Collectors.toList()), people.getRight());
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setRoleService(final RoleService roleService) {
        this.roleService = roleService;
    }

    // Need comparator to sort when applying role query after person query
    private static class PersonComparator implements Comparator<Person>, Serializable {
        private final boolean sortAscending;
        private final String sortField;

        PersonComparator(final String sortField, final boolean sortAscending) {
            this.sortAscending = sortAscending;
            this.sortField = sortField;
        }

        @Override
        public int compare(final Person person1, final Person person2) {
            try {
                final Object value1 = PropertyUtils.getProperty(person1, sortField);
                final Object value2 = PropertyUtils.getProperty(person2, sortField);
                if (Number.class.isAssignableFrom(value1.getClass())) {
                    final BigDecimal number1 = new BigDecimal(value1.toString());
                    final BigDecimal number2 = new BigDecimal(value2.toString());
                    return sortAscending ? number1.compareTo(number2) : number2.compareTo(number1);
                } else {
                    return sortAscending
                            ? value1.toString()
                            .toUpperCase(Locale.ROOT)
                            .compareTo(value2.toString().toUpperCase(Locale.ROOT))
                            : value2.toString()
                                    .toUpperCase(Locale.ROOT)
                                    .compareTo(value1.toString().toUpperCase(Locale.ROOT));
                }
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (final InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (final NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
