/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kew.actionlist.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.kew.actionitem.ActionItemActionListExtension;
import org.kuali.kfs.kew.actionitem.OutboxItemActionListExtension;
import org.kuali.kfs.kew.actionlist.ActionListFilter;
import org.kuali.kfs.kew.actionlist.dao.ActionListDAO;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.group.GroupService;
import org.springmodules.orm.ojb.PersistenceBrokerCallback;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ====
 * CU Customization: Added elements for the action list last modified date filter.
 * ====
 */

/**
 * OJB implementation of the {@link ActionListDAO}.
 */
public class ActionListDAOOjbImpl extends PersistenceBrokerDaoSupport implements ActionListDAO {

    private static final Logger LOG = LogManager.getLogger();
    private static final String ACTION_LIST_COUNT_QUERY =
            "select count(distinct(ai.doc_hdr_id)) "
            + "from krew_actn_itm_t ai where ai.PRNCPL_ID = ? and (ai.dlgn_typ is null or ai.dlgn_typ = 'P')";

    private GroupService groupService;
    private DateTimeService dateTimeService;

    @Override
    public Collection<ActionItemActionListExtension> getActionList(
            final String principalId,
            final ActionListFilter filter
    ) {
        return getActionItemsInActionList(ActionItemActionListExtension.class, principalId, filter);
    }

    @Override
    public Collection<ActionItemActionListExtension> getActionListForSingleDocument(final String documentId) {
        LOG.debug("getting action list for document id {}", documentId);
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        final Collection<ActionItemActionListExtension> collection =
                getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(
                        ActionItemActionListExtension.class, criteria));
        LOG.debug("found {} action items for document id {}", collection::size, () -> documentId);
        return createActionListForRouteHeader(collection);
    }

    private Criteria setUpActionListCriteria(final String principalId, final ActionListFilter filter) {
        LOG.debug("setting up Action List criteria");
        final Criteria criteria = new Criteria();
        boolean filterOn = false;
        String filteredByItems = "";

        if (StringUtils.isNotBlank(filter.getActionRequestCd())
                && !filter.getActionRequestCd().equals(KewApiConstants.ALL_CODE)) {
            if (filter.isExcludeActionRequestCd()) {
                criteria.addNotEqualTo("actionRequestCd", filter.getActionRequestCd());
            } else {
                criteria.addEqualTo("actionRequestCd", filter.getActionRequestCd());
            }
            filteredByItems += "Action Requested";
        }

        if (filter.getCreateDateFrom() != null || filter.getCreateDateTo() != null) {
            if (filter.isExcludeCreateDate()) {
                if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() != null) {
                    criteria.addNotBetween("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getCreateDateFrom())
                                    .getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getCreateDateTo()).getTime()));
                } else if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() == null) {
                    criteria.addLessOrEqualThan("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getCreateDateFrom()).getTime())
                    );
                } else if (filter.getCreateDateFrom() == null && filter.getCreateDateTo() != null) {
                    criteria.addGreaterOrEqualThan("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getCreateDateTo()).getTime()));
                }
            } else {
                if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() != null) {
                    criteria.addBetween("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getCreateDateFrom())
                                    .getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getCreateDateTo()).getTime()));
                } else if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() == null) {
                    criteria.addGreaterOrEqualThan("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getCreateDateFrom()).getTime())
                    );
                } else if (filter.getCreateDateFrom() == null && filter.getCreateDateTo() != null) {
                    criteria.addLessOrEqualThan("routeHeader.createDate",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getCreateDateTo()).getTime()));
                }
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Date Created";
        }

        if (StringUtils.isNotBlank(filter.getDocRouteStatus())
                && !filter.getDocRouteStatus().equals(KewApiConstants.ALL_CODE)) {
            if (filter.isExcludeRouteStatus()) {
                criteria.addNotEqualTo("routeHeader.docRouteStatus", filter.getDocRouteStatus());
            } else {
                criteria.addEqualTo("routeHeader.docRouteStatus", filter.getDocRouteStatus());
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Document Route Status";
        }

        if (StringUtils.isNotBlank(filter.getDocumentTitle())) {
            String docTitle = filter.getDocumentTitle();
            if (docTitle.trim().endsWith("*")) {
                docTitle = docTitle.substring(0, docTitle.length() - 1);
            }

            if (filter.isExcludeDocumentTitle()) {
                criteria.addNotLike("docTitle", "%" + docTitle + "%");
            } else {
                criteria.addLike("docTitle", "%" + docTitle + "%");
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Document Title";
        }

        if (StringUtils.isNotBlank(filter.getDocumentType())) {
            if (filter.isExcludeDocumentType()) {
                criteria.addNotLike("docName", "%" + filter.getDocumentType() + "%");
            } else {
                final String documentTypeName = filter.getDocumentType();
                final DocumentType documentType =
                        KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
                if (documentType == null) {
                    criteria.addLike("docName", "%" + filter.getDocumentType() + "%");
                } else {
                    // search this document type plus it's children
                    final Criteria docTypeCrit = new Criteria();
                    constructDocumentTypeCriteria(docTypeCrit, documentType);
                    criteria.addAndCriteria(docTypeCrit);
                }
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Document Type";
        }

        if (filter.getLastAssignedDateFrom() != null || filter.getLastAssignedDateTo() != null) {
            if (filter.isExcludeLastAssignedDate()) {
                if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() != null) {
                    criteria.addNotBetween("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastAssignedDateFrom())
                                    .getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastAssignedDateTo())
                                    .getTime())
                    );
                } else if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() == null) {
                    criteria.addLessOrEqualThan("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastAssignedDateFrom())
                                    .getTime())
                    );
                } else if (filter.getLastAssignedDateFrom() == null && filter.getLastAssignedDateTo() != null) {
                    criteria.addGreaterOrEqualThan("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastAssignedDateTo())
                                    .getTime())
                    );
                }
            } else {
                if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() != null) {
                    criteria.addBetween("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastAssignedDateFrom())
                                    .getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastAssignedDateTo())
                                    .getTime())
                    );
                } else if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() == null) {
                    criteria.addGreaterOrEqualThan("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastAssignedDateFrom())
                                    .getTime())
                    );
                } else if (filter.getLastAssignedDateFrom() == null && filter.getLastAssignedDateTo() != null) {
                    criteria.addLessOrEqualThan("dateAssigned",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastAssignedDateTo())
                                    .getTime())
                    );
                }
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Date Last Assigned";
        }
        
        //CU customization: Logic for filtering by last modified date   
        if (filter.getLastModifiedDateFrom() != null || filter.getLastModifiedDateTo() != null) {
            if (filter.isExcludeLastModifiedDate()) {
                if (filter.getLastModifiedDateFrom() != null && filter.getLastModifiedDateTo() != null) {
                    criteria.addNotBetween("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastModifiedDateFrom()).getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastModifiedDateTo()).getTime()));
                } else if (filter.getLastModifiedDateFrom() != null && filter.getLastModifiedDateTo() == null) {
                    criteria.addLessOrEqualThan("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastModifiedDateFrom()).getTime()));
                } else if (filter.getLastAssignedDateFrom() == null && filter.getLastModifiedDateTo() != null) {
                    criteria.addGreaterOrEqualThan("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastModifiedDateTo()).getTime()));
                }
            } else {
                if (filter.getLastModifiedDateFrom() != null && filter.getLastModifiedDateTo() != null) {
                    criteria.addBetween("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastModifiedDateFrom()).getTime()),
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastModifiedDateTo()).getTime()));
                } else if (filter.getLastModifiedDateFrom() != null && filter.getLastModifiedDateTo() == null) {
                    criteria.addGreaterOrEqualThan("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtStartOfDay(filter.getLastModifiedDateFrom()).getTime()));
                } else if (filter.getLastModifiedDateFrom() == null && filter.getLastModifiedDateTo() != null) {
                    criteria.addLessOrEqualThan("routeHeader.dateModified",
                            new Timestamp(dateTimeService.getUtilDateAtEndOfDay(filter.getLastModifiedDateTo()).getTime()));
                }
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Date Last Modified";
        }

        filter.setGroupId(null);
        if (StringUtils.isNotBlank(filter.getGroupIdString())
                && !filter.getGroupIdString().trim().equals(KewApiConstants.NO_FILTERING)) {
            filter.setGroupId(filter.getGroupIdString().trim());

            if (filter.isExcludeGroupId()) {
                final Criteria critNotEqual = new Criteria();
                critNotEqual.addNotEqualTo("groupId", filter.getGroupId());
                final Criteria critNull = new Criteria();
                critNull.addIsNull("groupId");
                critNotEqual.addOrCriteria(critNull);
                criteria.addAndCriteria(critNotEqual);
            } else {
                criteria.addEqualTo("groupId", filter.getGroupId());
            }
            filteredByItems += filteredByItems.isEmpty() ? "" : ", ";
            filteredByItems += "Action Request Workgroup";
        }

        if (!filteredByItems.isEmpty()) {
            filterOn = true;
        }

        boolean addedDelegationCriteria = false;
        if (StringUtils.isBlank(filter.getDelegationType()) && StringUtils.isBlank(filter.getPrimaryDelegateId())
                && StringUtils.isBlank(filter.getDelegatorId())) {
            criteria.addEqualTo("principalId", principalId);
            addedDelegationCriteria = true;
        } else if (StringUtils.isNotBlank(filter.getDelegationType())
                   && DelegationType.PRIMARY.getCode().equals(filter.getDelegationType())
                   || StringUtils.isNotBlank(filter.getPrimaryDelegateId())) {
            // using a primary delegation
            if (StringUtils.isBlank(filter.getPrimaryDelegateId())
                || filter.getPrimaryDelegateId().trim().equals(KewApiConstants.ALL_CODE)) {
                // user wishes to see all primary delegations
                final Criteria userCrit = new Criteria();
                final Criteria groupCrit = new Criteria();
                final Criteria orCrit = new Criteria();
                userCrit.addEqualTo("delegatorPrincipalId", principalId);
                final List<String> delegatorGroupIds = groupService.getGroupIdsByPrincipalId(
                        principalId);
                if (delegatorGroupIds != null && !delegatorGroupIds.isEmpty()) {
                    groupCrit.addIn("delegatorGroupId", delegatorGroupIds);
                }
                orCrit.addOrCriteria(userCrit);
                orCrit.addOrCriteria(groupCrit);
                criteria.addAndCriteria(orCrit);
                criteria.addEqualTo("delegationType", DelegationType.PRIMARY.getCode());
                filter.setDelegationType(DelegationType.PRIMARY.getCode());
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Primary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            } else if (!filter.getPrimaryDelegateId().trim().equals(KewApiConstants.PRIMARY_DELEGATION_DEFAULT)) {
                // user wishes to see primary delegation for a single user
                criteria.addEqualTo("principalId", filter.getPrimaryDelegateId());
                final Criteria userCrit = new Criteria();
                final Criteria groupCrit = new Criteria();
                final Criteria orCrit = new Criteria();
                userCrit.addEqualTo("delegatorPrincipalId", principalId);
                final List<String> delegatorGroupIds = groupService.getGroupIdsByPrincipalId(
                        principalId);
                if (delegatorGroupIds != null && !delegatorGroupIds.isEmpty()) {
                    groupCrit.addIn("delegatorGroupId", delegatorGroupIds);
                }
                orCrit.addOrCriteria(userCrit);
                orCrit.addOrCriteria(groupCrit);
                criteria.addAndCriteria(orCrit);
                criteria.addEqualTo("delegationType", DelegationType.PRIMARY.getCode());
                filter.setDelegationType(DelegationType.PRIMARY.getCode());
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Primary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            }
        }
        if (!addedDelegationCriteria
                && (StringUtils.isNotBlank(filter.getDelegationType())
                    && DelegationType.SECONDARY.getCode().equals(filter.getDelegationType())
                    || StringUtils.isNotBlank(filter.getDelegatorId()))) {
            // using a secondary delegation
            criteria.addEqualTo("principalId", principalId);
            if (StringUtils.isBlank(filter.getDelegatorId())) {
                filter.setDelegationType(DelegationType.SECONDARY.getCode());
                // if isExcludeDelegationType() we want to show the default action list which is set up later in this
                // method
                if (!filter.isExcludeDelegationType()) {
                    criteria.addEqualTo("delegationType", DelegationType.SECONDARY.getCode());
                    addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                    addedDelegationCriteria = true;
                    filterOn = true;
                }
            } else if (filter.getDelegatorId().trim().equals(KewApiConstants.ALL_CODE)) {
                // user wishes to see all secondary delegations
                criteria.addEqualTo("delegationType", DelegationType.SECONDARY.getCode());
                filter.setDelegationType(DelegationType.SECONDARY.getCode());
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            } else if (!filter.getDelegatorId().trim().equals(
                    KewApiConstants.DELEGATION_DEFAULT)) {
                // user has specified an id to see for secondary delegation
                filter.setDelegationType(DelegationType.SECONDARY.getCode());
                filter.setExcludeDelegationType(false);
                final Criteria userCrit = new Criteria();
                final Criteria groupCrit = new Criteria();
                if (filter.isExcludeDelegatorId()) {
                    final Criteria userNull = new Criteria();
                    userCrit.addNotEqualTo("delegatorPrincipalId", filter.getDelegatorId());
                    userNull.addIsNull("delegatorPrincipalId");
                    userCrit.addOrCriteria(userNull);
                    final Criteria groupNull = new Criteria();
                    groupCrit.addNotEqualTo("delegatorGroupId", filter.getDelegatorId());
                    groupNull.addIsNull("delegatorGroupId");
                    groupCrit.addOrCriteria(groupNull);
                    criteria.addAndCriteria(userCrit);
                    criteria.addAndCriteria(groupCrit);
                } else {
                    final Criteria orCrit = new Criteria();
                    userCrit.addEqualTo("delegatorPrincipalId", filter.getDelegatorId());
                    groupCrit.addEqualTo("delegatorGroupId", filter.getDelegatorId());
                    orCrit.addOrCriteria(userCrit);
                    orCrit.addOrCriteria(groupCrit);
                    criteria.addAndCriteria(orCrit);
                }
                addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            }
        }

        // if we haven't added delegation criteria then use the default criteria below
        if (!addedDelegationCriteria) {
            criteria.addEqualTo("principalId", principalId);
            filter.setDelegationType(DelegationType.SECONDARY.getCode());
            filter.setExcludeDelegationType(true);
            final Criteria critNotEqual = new Criteria();
            final Criteria critNull = new Criteria();
            critNotEqual.addNotEqualTo("delegationType", DelegationType.SECONDARY.getCode());
            critNull.addIsNull("delegationType");
            critNotEqual.addOrCriteria(critNull);
            criteria.addAndCriteria(critNotEqual);
        }

        if (StringUtils.isNotEmpty(filteredByItems)) {
            filteredByItems = "Filtered by " + filteredByItems;
        }
        filter.setFilterLegend(filteredByItems);
        filter.setFilterOn(filterOn);

        LOG.debug("returning from Action List criteria");
        return criteria;
    }

    private static void constructDocumentTypeCriteria(final Criteria criteria, final DocumentType documentType) {
        // search this document type plus it's children
        final Criteria docTypeBaseCrit = new Criteria();
        docTypeBaseCrit.addEqualTo("docName", documentType.getName());
        criteria.addOrCriteria(docTypeBaseCrit);
        final Collection<DocumentType> children = documentType.getChildrenDocTypes();
        if (children != null) {
            for (final DocumentType childDocumentType : children) {
                constructDocumentTypeCriteria(criteria, childDocumentType);
            }
        }
    }

    private void addToFilterDescription(String filterDescription, final String labelToAdd) {
        filterDescription += filterDescription.isEmpty() ? "" : ", ";
        filterDescription += labelToAdd;
    }

    // TODO: Fix this!
    //@Override
    public int getCount(final String workflowId) {
        return (Integer) getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            @Override
            public Object doInPersistenceBroker(final PersistenceBroker broker) {
                try {
                    // The documentation for the getConnection() method of ConnectionManagerImpl (the only
                    // implementation of ConnectionManagerIF) documents that the caller should never call close() on the
                    // returned Connection object. It'll be returned to the pool and re-used.
                    final Connection connection = broker.serviceConnectionManager().getConnection();
                    try (PreparedStatement statement = connection.prepareStatement(ACTION_LIST_COUNT_QUERY)) {
                        statement.setString(1, workflowId);

                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (!resultSet.next()) {
                                throw new WorkflowRuntimeException("Error determining Action List Count.");
                            }
                            return resultSet.getInt(1);
                        }
                    }
                } catch (SQLException | LookupException e) {
                    throw new WorkflowRuntimeException("Error determining Action List Count.", e);
                }
            }
        });
    }

    /**
     * Creates an Action List from the given collection of Action Items.  The Action List should contain only one
     * action item per document.  The action item chosen should be the most "critical" or "important" one on the
     * document.
     *
     * @return the Action List as a Collection of ActionItems
     */
    private static <T extends ActionItemActionListExtension> Collection<T> createActionListForUser(
            final Collection<T> actionItems) {
        final Map<String, T> actionItemMap = new HashMap<>();
        final ActionListPriorityComparator comparator = new ActionListPriorityComparator();
        for (final T potentialActionItem : actionItems) {
            final T existingActionItem = actionItemMap.get(potentialActionItem.getDocumentId());
            if (existingActionItem == null || comparator.compare(potentialActionItem, existingActionItem) > 0) {
                actionItemMap.put(potentialActionItem.getDocumentId(), potentialActionItem);
            }
        }
        return actionItemMap.values();
    }

    /**
     * Creates an Action List from the given collection of Action Items. The Action List should contain only one
     * action item per user.  The action item chosen should be the most "critical" or "important" one on the document.
     *
     * @return the Action List as a Collection of ActionItems
     */
    private static Collection<ActionItemActionListExtension> createActionListForRouteHeader(
            final Collection<ActionItemActionListExtension> actionItems) {
        final Map<String, ActionItemActionListExtension> actionItemMap = new HashMap<>();
        final ActionListPriorityComparator comparator = new ActionListPriorityComparator();
        for (final ActionItemActionListExtension potentialActionItem : actionItems) {
            final ActionItemActionListExtension existingActionItem =
                    actionItemMap.get(potentialActionItem.getPrincipalId());
            if (existingActionItem == null || comparator.compare(potentialActionItem, existingActionItem) > 0) {
                actionItemMap.put(potentialActionItem.getPrincipalId(), potentialActionItem);
            }
        }
        return actionItemMap.values();
    }

    private <T extends ActionItemActionListExtension> Collection<T> getActionItemsInActionList(
            final Class<T> objectsToRetrieve, final String principalId, final ActionListFilter filter) {
        LOG.debug("getting action list for user {}", principalId);
        final Criteria crit;
        if (filter == null) {
            crit = new Criteria();
            crit.addEqualTo("principalId", principalId);
        } else {
            crit = setUpActionListCriteria(principalId, filter);
        }
        LOG.debug("running query to get action list for criteria {}", crit);
        final Collection<T> collection = getPersistenceBrokerTemplate()
                .getCollectionByQuery(new QueryByCriteria(objectsToRetrieve, crit));
        LOG.debug("found {} action items for user {}", collection::size, () -> principalId);
        return createActionListForUser(collection);
    }

    @Override
    public Collection<OutboxItemActionListExtension> getOutbox(
            final String principalId,
            final ActionListFilter filter
    ) {
        return getActionItemsInActionList(OutboxItemActionListExtension.class, principalId, filter);
    }

    /**
     * Deletes all outbox items specified by the list of ids
     */
    @Override
    public void removeOutboxItems(final String principalId, final List<String> outboxItems) {
        final Criteria criteria = new Criteria();
        criteria.addIn("id", outboxItems);
        getPersistenceBrokerTemplate().deleteByQuery(new QueryByCriteria(
                OutboxItemActionListExtension.class,
                criteria
        ));
    }

    /**
     * Saves an outbox item
     */
    @Override
    public void saveOutboxItem(final OutboxItemActionListExtension outboxItem) {
        getPersistenceBrokerTemplate().store(outboxItem);
    }

    @Override
    public OutboxItemActionListExtension getOutboxByDocumentIdUserId(final String documentId, final String userId) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        criteria.addEqualTo("principalId", userId);
        return (OutboxItemActionListExtension) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(
                OutboxItemActionListExtension.class, criteria));
    }

    public void setGroupService(final GroupService groupService) {
        this.groupService = groupService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
