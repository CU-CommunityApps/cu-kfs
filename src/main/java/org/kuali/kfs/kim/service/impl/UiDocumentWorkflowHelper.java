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

package org.kuali.kfs.kim.service.impl;

import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CU Customization: Backported this class from the FINP-9525 fix in the 2023-05-17 financials patch.
 * This file should be removed when we upgrade to the 2023-05-17 financials patch.
 * 
 * Provides support to the UiDocumentServiceImpl class to help determine when the workflow is affected by the changes
 * processed by UiDocumentServiceImpl.
 */
class UiDocumentWorkflowHelper {

    /* A comparator that returns 0 if the objects are the same or equal and otherwise uses identityHashCode. */
    private static final Comparator<Object> COMPARATOR_EQUALITY = Comparator.nullsLast((x, y) -> {
        if (x == y || x.equals(y)) {
            return 0;
        }
        return Integer.compare(System.identityHashCode(x), System.identityHashCode(y));
    });

    private static final Comparator<DelegateMember> COMPARATOR_DELEGATE_MEMBER =
            Comparator.comparing(DelegateMember::getDelegationId, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getRoleMemberId, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getMemberId, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getTypeCode, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getDelegationType, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getActiveFromDateValue, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getActiveToDateValue, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateMember::getAttributes, COMPARATOR_EQUALITY);
    private static final Comparator<DelegateType> COMPARATOR_DELEGATE =
            Comparator.comparing(DelegateType::getRoleId, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateType::isActive)
                    .thenComparing(DelegateType::getKimTypeId, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateType::getDelegationTypeCode, COMPARATOR_EQUALITY)
                    .thenComparing(DelegateType::getDelegationMembers, listComparator(COMPARATOR_DELEGATE_MEMBER));
    private static final Comparator<List<? extends DelegateType>> COMPARATOR_DELEGATE_LIST =
            listComparator(COMPARATOR_DELEGATE);

    private static final Comparator<RoleResponsibilityAction> COMPARATOR_ROLE_RESPONSIBILITY_ACTION =
            Comparator.comparing(RoleResponsibilityAction::getRoleMemberId, COMPARATOR_EQUALITY)
                    .thenComparing(RoleResponsibilityAction::getActionTypeCode, COMPARATOR_EQUALITY)
                    .thenComparing(RoleResponsibilityAction::getActionPolicyCode, COMPARATOR_EQUALITY)
                    .thenComparing(RoleResponsibilityAction::getForceAction)
                    .thenComparing(RoleResponsibilityAction::getPriorityNumber);
    private static final Comparator<List<? extends RoleResponsibilityAction>> COMPARATOR_ROLE_RESPONSIBILITY_ACTION_LIST
            = listComparator(COMPARATOR_ROLE_RESPONSIBILITY_ACTION);
    private static final Comparator<RoleResponsibility> COMPARATOR_ROLE_RESPONSIBILITY =
            Comparator.comparing(RoleResponsibility::getResponsibilityId, COMPARATOR_EQUALITY)
                    .thenComparing(RoleResponsibility::getRoleId, COMPARATOR_EQUALITY)
                    .thenComparing(RoleResponsibility::isActive)
                    .thenComparing(
                            RoleResponsibility::getResponsibilityActions,
                            COMPARATOR_ROLE_RESPONSIBILITY_ACTION_LIST);
    private static final Comparator<List<? extends RoleResponsibility>> COMPARATOR_ROLE_RESPONSIBILITY_LIST_COMPARATOR =
            listComparator(COMPARATOR_ROLE_RESPONSIBILITY);

    /**
     * Determine if the role responsibilities have changed by comparing their responsibilities, roles, active state
     * and associated actions.
     */
    boolean roleResponsibilitiesChanged(
            final List<? extends RoleResponsibility> newRoleResponsibilities,
            final List<? extends RoleResponsibility> origRoleResponsibilities
    ) {
        // Filter out original role resps which were inactive as they are not reused and cannot appear on the list
        // of new role resps - without this we get false positives
        final var activeOrigRoleResponsibilities = origRoleResponsibilities.stream()
                .filter(RoleResponsibility::isActive)
                .collect(Collectors.toList());
        return COMPARATOR_ROLE_RESPONSIBILITY_LIST_COMPARATOR
                       .compare(newRoleResponsibilities, activeOrigRoleResponsibilities) != 0;
    }

    /**
     * Determine if the role delegations have changed by comparing their role ids, active state, kim type id,
     * delegation type codes and delegation members.
     */
    boolean delegationsChanged(
            final List<? extends DelegateType> newDelegates,
            final List<? extends DelegateType> origDelegates
    ) {
        return COMPARATOR_DELEGATE_LIST.compare(newDelegates, origDelegates) != 0;
    }

    private static <T> Comparator<List<? extends T>> listComparator(final Comparator<? super T> elementComparator) {
        return Comparator.nullsFirst(new ListComparator<>(elementComparator));
    }

    /*
     * A comparator for Lists that considers the lists equal if they contain the same set of elements without regard to
     * order.  Otherwise just use identityHashCode for ordering.
     */
    private static class ListComparator<T> implements Comparator<List<? extends T>>, Serializable {
        private final Comparator<? super T> elementComparator;

        ListComparator(final Comparator<? super T> elementComparator) {
            this.elementComparator = elementComparator;
        }

        @Override
        public int compare(final List<? extends T> x, final List<? extends T> y) {
            if (x == y) {
                return 0;
            }

            final List<T> yCopy = new LinkedList<>(y);
            for (final T xElement : x) {
                T candidate = null;
                for (final Iterator<T> yIter = yCopy.iterator(); yIter.hasNext(); ) {
                    final T yElement = yIter.next();
                    if (elementComparator.compare(xElement, yElement) == 0) {
                        candidate = yElement;
                        yIter.remove();
                        break;
                    }
                }
                if (candidate == null) {
                    return Integer.compare(System.identityHashCode(x), System.identityHashCode(y));
                }
            }
            if (yCopy.isEmpty()) {
                return 0;
            }
            return Integer.compare(System.identityHashCode(x), System.identityHashCode(y));
        }
    }
}
