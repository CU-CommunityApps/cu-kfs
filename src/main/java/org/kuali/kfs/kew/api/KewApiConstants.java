/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.kew.api;

import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kew.api.document.DocumentStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// CU Customization with the 5/25/2022 upgrade to change access to SearchableAttributeConstants.SEARCH_WILDCARD_CHARACTER from private to public 
// as it is used by our local customizations; add back Disapprove code
public final class KewApiConstants {

    public static final String USE_OUT_BOX = "USE_OUT_BOX%";

    public static final String DEFAULT_DOCUMENT_TYPE_LABEL = "Undefined";

    public static final String DOCUMENT_TYPE_INHERITED_VALUE_INDICATOR = "(Inherited from Parent)";

    public static final long DEFAULT_CACHE_REQUEUE_WAIT_TIME = 5000;

    // DelegationType values -- used in ActionRequest.jsp
    public static final String DELEGATION_PRIMARY = "PRIMARY";
    public static final String DELEGATION_SECONDARY = "SECONDARY";

    public static final String DOC_HANDLER_REDIRECT_PAGE = "DocHandler.do";
    public static final String DOCUMENT_ROUTING_REPORT_PAGE = "RoutingReport.do";

    // Routing Report constants
    public static final String DOCUMENT_TYPE_NAME_ATTRIBUTE_NAME = "documentTypeParam";
    public static final String INITIATOR_ID_ATTRIBUTE_NAME = "initiatorPrincipalId";
    public static final String DOCUMENT_CONTENT_ATTRIBUTE_NAME = "documentContent";
    public static final String RETURN_URL_ATTRIBUTE_NAME = "backUrl";
    public static final String DISPLAY_CLOSE_BUTTON_ATTRIBUTE_NAME = "showCloseButton";
    public static final String DISPLAY_CLOSE_BUTTON_TRUE_VALUE = "showCloseButton";

    public static final String YES_LABEL = "Yes";
    public static final String NO_LABEL = "No";
    public static final String INHERITED_CD = "I";
    public static final String INHERITED_LABEL = "Inherited";

    public static final String DOCUMENT_TYPE_BLANKET_APPROVE_POLICY_NONE = "none";
    public static final String DOCUMENT_TYPE_BLANKET_APPROVE_POLICY_ANY = "any";

    // alternate kew status policy constants.  Determines if route header will show the KEW Route Status
    // or the application doc status, or both
    public static final String DOCUMENT_STATUS_POLICY = "DOCUMENT_STATUS_POLICY";
    public static final String DOCUMENT_STATUS_POLICY_KEW_STATUS = "KEW";
    public static final String DOCUMENT_STATUS_POLICY_APP_DOC_STATUS = "APP";
    public static final String DOCUMENT_STATUS_POLICY_BOTH = "BOTH";
    public static final String[] DOCUMENT_STATUS_POLICY_VALUES = {
        DOCUMENT_STATUS_POLICY_KEW_STATUS,
        DOCUMENT_STATUS_POLICY_APP_DOC_STATUS,
        DOCUMENT_STATUS_POLICY_BOTH
    };

    public static final String SUPER_USER_CANCEL = "SU_CANCEL";
    public static final String SUPER_USER_APPROVE = "SU_APPROVE";
    public static final String SUPER_USER_DISAPPROVE = "SU_DISAPPROVE";
    public static final String SUPER_USER_ROUTE_LEVEL_APPROVE = "SU_ROUTE_LEVEL_APPROVE";
    public static final String SUPER_USER_ACTION_REQUEST_APPROVE = "SU_ACTION_REQUEST_APPROVE";
    public static final String SUPER_USER_RETURN_TO_PREVIOUS_ROUTE_LEVEL = "SU_RETURN_TO_PREVIOUS_ROUTE_LEVEL";

    /* email notification for action requests left in action list */
    public static final String EMAIL_RMNDR_KEY = "EMAIL_NOTIFICATION";
    public static final String DOCUMENT_TYPE_NOTIFICATION_PREFERENCE_SUFFIX = ".DocumentTypeNotification";
    public static final String DOCUMENT_TYPE_NOTIFICATION_DELIMITER = "|~|";
    public static final String EMAIL_RMNDR_NO_VAL = "no";
    public static final String EMAIL_RMNDR_DAY_VAL = "daily";
    public static final String EMAIL_RMNDR_WEEK_VAL = "weekly";
    public static final String EMAIL_RMNDR_IMMEDIATE = "immediate";
    /* end email notification constants */

    public static final String PREFERENCES_YES_VAL = "yes";

    private static final String ACTION_LIST_ALL_REQUESTS = "all";
    private static final String ACTION_LIST_DELEGATED_REQUESTS = "delegated";
    private static final String ACTION_LIST_NONDELEGATED_REQUESTS = "nondelegated";
    public static final String DELEGATORS_ON_FILTER_PAGE = "Secondary Delegators only on Filter Page";
    public static final String DELEGATORS_ON_ACTION_LIST_PAGE = "Secondary Delegators on Action List Page";
    public static final String PRIMARY_DELEGATES_ON_FILTER_PAGE = "Primary Delegates only on Filter Page";
    public static final String PRIMARY_DELEGATES_ON_ACTION_LIST_PAGE = "Primary Delegates on Action List Page";

    public static final Map<String, String> ACTION_LIST_CONTENT;

    static {
        ACTION_LIST_CONTENT = new HashMap<>();
        ACTION_LIST_CONTENT.put(ACTION_LIST_ALL_REQUESTS, "All Requests");
        ACTION_LIST_CONTENT.put(ACTION_LIST_NONDELEGATED_REQUESTS, "No Delegations");
        ACTION_LIST_CONTENT.put(ACTION_LIST_DELEGATED_REQUESTS, "Delegations Only");
    }

    public static final String ALL_CODE = "All";
    // Used in ActionList.jsp
    public static final String ALL_SECONDARY_DELEGATIONS = "All Secondary Delegations";
    public static final String ALL_PRIMARY_DELEGATES = "All Primary Delegates";
    public static final String NO_FILTERING = "No Filtering";
    public static final String DELEGATION_DEFAULT = "Choose Secondary Delegation";
    public static final String PRIMARY_DELEGATION_DEFAULT = "Choose Primary Delegate";

    public static final String ACTIVE_LABEL = "ACTIVE";
    public static final String ACTIVE_LABEL_LOWER = "Active";

    public static final String INACTIVE_LABEL_LOWER = "Inactive";

    public static final String TRUE_CD = "1";

    public static final String FALSE_CD = "0";

    public static final int TITLE_MAX_LENGTH = 255;

    public static final Map<String, String> DOCUMENT_STATUSES;

    static {
        /*
         * see values in RouteHeader inner class; this HashMap is the definitive list used for the Document Route
         * Statuses in ActionList preferences
         */
        DOCUMENT_STATUSES = new HashMap<>();
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_CANCEL_CD,
                KewApiConstants.ROUTE_HEADER_CANCEL_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD,
                KewApiConstants.ROUTE_HEADER_DISAPPROVED_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_FINAL_CD,
                KewApiConstants.ROUTE_HEADER_FINAL_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_INITIATED_CD,
                KewApiConstants.ROUTE_HEADER_INITIATED_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_PROCESSED_CD,
                KewApiConstants.ROUTE_HEADER_PROCESSED_LABEL);
        DOCUMENT_STATUSES.put(
                KewApiConstants.ROUTE_HEADER_SAVED_CD,
                KewApiConstants.ROUTE_HEADER_SAVED_LABEL);
        DOCUMENT_STATUSES.put(DocumentStatus.RECALLED.getCode(), DocumentStatus.RECALLED.getLabel());
    }

    // below must be negative to be 30 days in the past... positive number will push date into future
    public static final Integer DOCUMENT_SEARCH_DOC_TITLE_CREATE_DATE_DAYS_AGO = -30;
    public static final Integer DOCUMENT_SEARCH_NO_CRITERIA_CREATE_DATE_DAYS_AGO = 0;

    public static final int DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP = 500;

    /**
     * The initial state of a document. Only state in which a delete is allowed.
     */
    public static final String ROUTE_HEADER_INITIATED_CD = "I";
    public static final String ROUTE_HEADER_INITIATED_LABEL = "INITIATED";

    // CU customization: add back Disapprove code
    public static final String ROUTE_HEADER_CANCEL_DISAPPROVE_CD = "C";
    public static final String ROUTE_HEADER_DISAPPROVED_LABEL = "DISAPPROVED";
    /**
     * Document has been disapproved
     */
    public static final String ROUTE_HEADER_DISAPPROVED_CD = "D";
    public static final String ROUTE_HEADER_PROCESSED_LABEL = "PROCESSED";
    /**
     * Document has been processed by the post processor
     */
    public static final String ROUTE_HEADER_PROCESSED_CD = "P";
    public static final String ROUTE_HEADER_EXCEPTION_LABEL = "EXCEPTION";
    /**
     * Document has had an exception in routing and needs to be processed
     */
    public static final String ROUTE_HEADER_EXCEPTION_CD = "E";
    public static final String ROUTE_HEADER_CANCEL_LABEL = "CANCELED";
    /**
     * Document has been canceled and no further action should be taken on it.
     */
    public static final String ROUTE_HEADER_CANCEL_CD = "X";
    public static final String ROUTE_HEADER_FINAL_LABEL = "FINAL";
    /**
     * Document has finalized and no changes are allowed to take place to it.
     */
    public static final String ROUTE_HEADER_FINAL_CD = "F";
    public static final String ROUTE_HEADER_SAVED_LABEL = "SAVED";
    /**
     * The document has been saved, but has not started to route.
     */
    public static final String ROUTE_HEADER_SAVED_CD = "S";
    public static final String ROUTE_HEADER_ENROUTE_LABEL = "ENROUTE";
    /**
     * The document is currently being routed.
     */
    public static final String ROUTE_HEADER_ENROUTE_CD = "R";

    public static final String UNKNOWN_STATUS = "";

    /**
     * Actions Taken Constants
     **/
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_ACKNOWLEDGED_CD = "k";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_ACKNOWLEDGED = "SUPER USER ACTION REQUEST ACKNOWLEDGED";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_FYI_CD = "f";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_FYI = "SUPER USER ACTION REQUEST FYI";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_COMPLETED_CD = "m";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_COMPLETED = "SUPER USER ACTION REQUEST COMPLETED";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_APPROVED_CD = "v";
    public static final String ACTION_TAKEN_SU_ACTION_REQUEST_APPROVED = "SUPER USER ACTION REQUEST APPROVED";
    public static final String ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED_CD = "r";
    public static final String ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED = "SUPER USER ROUTE LEVEL APPROVED";
    public static final String ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD = "z";
    public static final String ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS = "SUPER USER RETURNED TO PREVIOUS ROUTE LEVEL";
    public static final String ACTION_TAKEN_SU_DISAPPROVED_CD = "d";
    public static final String ACTION_TAKEN_SU_DISAPPROVED = "SUPER USER DISAPPROVED";
    public static final String ACTION_TAKEN_SU_CANCELED_CD = "c";
    public static final String ACTION_TAKEN_SU_CANCELED = "SUPER USER CANCELED";
    public static final String ACTION_TAKEN_SU_APPROVED_CD = "a";
    public static final String ACTION_TAKEN_SU_APPROVED = "SUPER USER APPROVED";
    public static final String ACTION_TAKEN_BLANKET_APPROVE_CD = "B";
    public static final String ACTION_TAKEN_BLANKET_APPROVE = "BLANKET APPROVED";
    public static final String ACTION_TAKEN_FYI = "FYI";
    public static final String ACTION_TAKEN_ADHOC_CD = "H";
    /**
     * User has generated an action request to another user
     */
    public static final String ACTION_TAKEN_ADHOC = "ADHOC ROUTED";
    public static final String ACTION_TAKEN_SAVED_CD = "S";
    /**
     * Document has been saved by the user for later work
     */
    public static final String ACTION_TAKEN_SAVED = "SAVED";
    /**
     * Document has been canceled.
     */
    public static final String ACTION_TAKEN_CANCELED = "CANCELED";
    /**
     * Document has been denied.
     */
    public static final String ACTION_TAKEN_DENIED = "DISAPPROVED";
    /**
     * Document has been opened by the designated recipient.
     */
    public static final String ACTION_TAKEN_ACKNOWLEDGED = "ACKNOWLEDGED";
    /**
     * Document has been completed as requested.
     */
    public static final String ACTION_TAKEN_COMPLETED = "COMPLETED";
    /**
     * Document has been completed as requested.
     */
    public static final String ACTION_TAKEN_ROUTED = "ROUTED";
    /**
     * The document has been approved.
     */
    public static final String ACTION_TAKEN_APPROVED = "APPROVED";
    /**
     * The document is being returned to a previous routelevel
     **/
    public static final String ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD = "Z";
    public static final String ACTION_TAKEN_RETURNED_TO_PREVIOUS = "RETURNED TO PREVIOUS ROUTE LEVEL";
    /**
     * The document has non-routed activity against it that is recorded in the route log
     **/
    public static final String ACTION_TAKEN_LOG_DOCUMENT_ACTION_CD = "R";
    public static final String ACTION_TAKEN_LOG_DOCUMENT_ACTION = "LOG MESSAGE";
    /**
     * The document is routed to a workgroup and a user in the workgroup wants to take authority from the workgroup
     **/
    public static final String ACTION_TAKEN_TAKE_WORKGROUP_AUTHORITY_CD = "w";
    public static final String ACTION_TAKEN_TAKE_WORKGROUP_AUTHORITY = "WORKGROUP AUTHORITY TAKEN";
    /**
     * The person who took workgroup authority is releasing it
     **/
    public static final String ACTION_TAKEN_RELEASE_WORKGROUP_AUTHORITY_CD = "y";
    public static final String ACTION_TAKEN_RELEASE_WORKGROUP_AUTHORITY = "WORKGROUP AUTHORITY RELEASED";

    public static final String ROUTE_LEVEL_ROUTE_MODULE = "RM";
    /**
     * No route module available for this route level *
     */
    public static final String ROUTE_LEVEL_NO_ROUTE_MODULE = "NONE";
    /**
     * The route level value for the AdHoc route level. AdHoc like Exception route level does not have a route module
     * and is processed directly be the engine.
     */
    public static final int ADHOC_ROUTE_LEVEL = 0;

    /**
     * The route level value for the Exception route level. The Exception route level does not have a route module and
     * the core engine processes these requests since they have special rules, such as an exception request can not
     * itself throw an exception request.
     */
    public static final int EXCEPTION_ROUTE_LEVEL = -1;
    public static final int INVALID_ROUTE_LEVEL = -2;

    /**
     * Routing should process the associated ActionRequests in sequence
     */
    public static final String ROUTE_LEVEL_SEQUENCE = "S";
    public static final String ROUTE_LEVEL_SEQUENTIAL_NAME = "Sequential";
    public static final String ROUTE_LEVEL_SEQUENCE_LABEL = "SEQUENCE";

    /**
     * Routing should process the associated ActionRequests in parallel
     */
    public static final String ROUTE_LEVEL_PARALLEL = "P";
    public static final String ROUTE_LEVEL_PARALLEL_NAME = "Parallel";
    public static final String ROUTE_LEVEL_PARALLEL_LABEL = "PARALLEL";

    /**
     * Routing should process the associated ActionRequests in parallel accoring to priority
     */
    public static final String ROUTE_LEVEL_PRIORITY_PARALLEL = "R";
    public static final String ROUTE_LEVEL_PRIORITY_PARALLEL_NAME = "Priority-Parallel";
    public static final String ROUTE_LEVEL_PRIORITY_PARALLEL_LABEL = "PRIORITY-PARALLEL";

    /**
     * Priority used if no priority is specified
     */
    public static final int ACTION_REQUEST_DEFAULT_PRIORITY = 1;

    public static final String ACTION_REQUEST_CANCEL_REQ_LABEL = "CANCEL";
    public static final String ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL = "ACKNOWLEDGE";
    /**
     * Requested action is ACKKNOWLEDGE. This action does not hold up routing, but action request will not be marked
     * DONE until an ACKNOWLEDGE actiontaken is recorded.
     */
    public static final String ACTION_REQUEST_ACKNOWLEDGE_REQ = "K";
    public static final String ACTION_REQUEST_FYI_REQ_LABEL = "FYI";
    /**
     * The action is an FYI notification only. This action request is marked DONE as soon as it is activated.
     */
    public static final String ACTION_REQUEST_FYI_REQ = "F";
    public static final String ACTION_REQUEST_APPROVE_REQ_LABEL = "APPROVE";
    /**
     * Requested action is to approve the document.
     */
    public static final String ACTION_REQUEST_APPROVE_REQ = "A";
    public static final String ACTION_REQUEST_COMPLETE_REQ_LABEL = "COMPLETE";
    /**
     * Requested action is to complete the document, however that is defined by the application.
     */
    public static final String ACTION_REQUEST_COMPLETE_REQ = "C";

    public static final Map<String, String> ACTION_REQUEST_CODES = new HashMap<>();

    static {
        ACTION_REQUEST_CODES.put(ACTION_REQUEST_COMPLETE_REQ, ACTION_REQUEST_COMPLETE_REQ_LABEL);
        ACTION_REQUEST_CODES.put(ACTION_REQUEST_APPROVE_REQ, ACTION_REQUEST_APPROVE_REQ_LABEL);
        ACTION_REQUEST_CODES.put(ACTION_REQUEST_ACKNOWLEDGE_REQ, ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL);
        ACTION_REQUEST_CODES.put(ACTION_REQUEST_FYI_REQ, ACTION_REQUEST_FYI_REQ_LABEL);
    }

    // Used in AppSpecificRoute.jsp
    public static final String WORKGROUP = "workgroup";
    public static final String PERSON = "person";
    public static final String ROLE = "role";

    //document operation constants
    public static final String NOOP = "noop";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";

    public static final Map<String, String> ACTION_REQUEST_CD;

    static {
        ACTION_REQUEST_CD = new HashMap<>();
        ACTION_REQUEST_CD.put(
                ACTION_REQUEST_ACKNOWLEDGE_REQ,
                ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL);
        ACTION_REQUEST_CD.put(
                ACTION_REQUEST_APPROVE_REQ,
                ACTION_REQUEST_APPROVE_REQ_LABEL);
        ACTION_REQUEST_CD.put(
                ACTION_REQUEST_COMPLETE_REQ,
                ACTION_REQUEST_COMPLETE_REQ_LABEL);
        ACTION_REQUEST_CD.put(
                ACTION_REQUEST_FYI_REQ,
                ACTION_REQUEST_FYI_REQ_LABEL);
    }

    public static final Map<String, String> ACTION_TAKEN_CD;

    static {
        ACTION_TAKEN_CD = new HashMap<>();
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD,
                ACTION_TAKEN_ACKNOWLEDGED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_ADHOC_CD,
                ACTION_TAKEN_ADHOC);
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_APPROVED_CD,
                ACTION_TAKEN_APPROVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_BLANKET_APPROVE_CD,
                ACTION_TAKEN_BLANKET_APPROVE);
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_CANCELED_CD,
                ACTION_TAKEN_CANCELED);
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_COMPLETED_CD,
                ACTION_TAKEN_COMPLETED);
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_DENIED_CD,
                ACTION_TAKEN_DENIED);
        ACTION_TAKEN_CD.put(
                KewApiConstants.ACTION_TAKEN_FYI_CD,
                ACTION_TAKEN_FYI);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SAVED_CD,
                ACTION_TAKEN_SAVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD,
                ACTION_TAKEN_RETURNED_TO_PREVIOUS);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_LOG_DOCUMENT_ACTION_CD,
                ACTION_TAKEN_LOG_DOCUMENT_ACTION);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_APPROVED_CD,
                ACTION_TAKEN_SU_APPROVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_CANCELED_CD,
                ACTION_TAKEN_SU_CANCELED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_DISAPPROVED_CD,
                ACTION_TAKEN_SU_DISAPPROVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED_CD,
                ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_ACTION_REQUEST_APPROVED_CD,
                ACTION_TAKEN_SU_ACTION_REQUEST_APPROVED);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD,
                ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS);
        ACTION_TAKEN_CD.put(
                ACTION_TAKEN_TAKE_WORKGROUP_AUTHORITY_CD,
                ACTION_TAKEN_TAKE_WORKGROUP_AUTHORITY);
        ACTION_TAKEN_CD.put(WorkflowAction.RECALL.getCode(), WorkflowAction.RECALL.getLabel());
    }

    public static final String RULE_ATTRIBUTE_TYPE = "RuleAttribute";
    public static final String RULE_XML_ATTRIBUTE_TYPE = "RuleXmlAttribute";
    public static final String SEARCHABLE_ATTRIBUTE_TYPE = "SearchableAttribute";
    public static final String SEARCHABLE_XML_ATTRIBUTE_TYPE = "SearchableXmlAttribute";
    public static final String DOCUMENT_SEARCH_CUSTOMIZER_ATTRIBUTE_TYPE = "DocumentSearchCustomizer";
    private static final String DOCUMENT_SECURITY_ATTRIBUTE_TYPE = "DocumentSecurityAttribute";
    private static final String EXTENSION_ATTRIBUTE_TYPE = "ExtensionAttribute";
    public static final String EMAIL_ATTRIBUTE_TYPE = "EmailAttribute";
    private static final String NOTE_ATTRIBUTE_TYPE = "NoteAttribute";
    public static final String ACTION_LIST_ATTRIBUTE_TYPE = "ActionListAttribute";
    private static final String QUALIFIER_RESOLVER_ATTRIBUTE_TYPE = "QualifierResolver";

    private static final String RULE_ATTRIBUTE_TYPE_LABEL = "Rule Attribute";
    private static final String RULE_XML_ATTRIBUTE_TYPE_LABEL = "Rule Xml Attribute";
    private static final String SEARCHABLE_ATTRIBUTE_TYPE_LABEL = "Searchable Attribute";
    private static final String SEARCHABLE_XML_ATTRIBUTE_TYPE_LABEL = "Searchable Xml Attribute";
    private static final String DOCUMENT_SEARCH_CUSTOMIZER_ATTRIBUTE_TYPE_LABEL = "Document Search Customizer";
    private static final String DOCUMENT_SECURITY_ATTRIBUTE_TYPE_LABEL = "Document Security Attribute";
    private static final String EXTENSION_ATTRIBUTE_TYPE_LABEL = "Extension Attribute";
    private static final String EMAIL_ATTRIBUTE_TYPE_LABEL = "Email Attribute";
    private static final String NOTE_ATTRIBUTE_TYPE_LABEL = "Note Attribute";
    private static final String ACTION_LIST_ATTRIBUTE_TYPE_LABEL = "Action List Attribute";
    private static final String QUALIFIER_RESOLVER_ATTRIBUTE_TYPE_LABEL = "Qualifier Resolver";

    public static final String[] RULE_ATTRIBUTE_TYPES = {
        RULE_ATTRIBUTE_TYPE,
        RULE_XML_ATTRIBUTE_TYPE,
        SEARCHABLE_ATTRIBUTE_TYPE,
        SEARCHABLE_XML_ATTRIBUTE_TYPE,
        DOCUMENT_SEARCH_CUSTOMIZER_ATTRIBUTE_TYPE,
        DOCUMENT_SECURITY_ATTRIBUTE_TYPE,
        EXTENSION_ATTRIBUTE_TYPE,
        EMAIL_ATTRIBUTE_TYPE,
        NOTE_ATTRIBUTE_TYPE,
        ACTION_LIST_ATTRIBUTE_TYPE,
        QUALIFIER_RESOLVER_ATTRIBUTE_TYPE
    };

    public static final Map<String, String> RULE_ATTRIBUTE_TYPE_MAP;

    static {
        RULE_ATTRIBUTE_TYPE_MAP = new HashMap<>();
        RULE_ATTRIBUTE_TYPE_MAP.put(RULE_ATTRIBUTE_TYPE, RULE_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(RULE_XML_ATTRIBUTE_TYPE, RULE_XML_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(SEARCHABLE_ATTRIBUTE_TYPE, SEARCHABLE_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(SEARCHABLE_XML_ATTRIBUTE_TYPE, SEARCHABLE_XML_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(DOCUMENT_SEARCH_CUSTOMIZER_ATTRIBUTE_TYPE,
                DOCUMENT_SEARCH_CUSTOMIZER_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(DOCUMENT_SECURITY_ATTRIBUTE_TYPE, DOCUMENT_SECURITY_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(EXTENSION_ATTRIBUTE_TYPE, EXTENSION_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(EMAIL_ATTRIBUTE_TYPE, EMAIL_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(NOTE_ATTRIBUTE_TYPE, NOTE_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(ACTION_LIST_ATTRIBUTE_TYPE, ACTION_LIST_ATTRIBUTE_TYPE_LABEL);
        RULE_ATTRIBUTE_TYPE_MAP.put(QUALIFIER_RESOLVER_ATTRIBUTE_TYPE, QUALIFIER_RESOLVER_ATTRIBUTE_TYPE_LABEL);
    }

    public static final int MAX_ANNOTATION_LENGTH = 2000;

    public static final String MACHINE_GENERATED_RESPONSIBILITY_ID = "0";
    public static final String ADHOC_REQUEST_RESPONSIBILITY_ID = "-1";
    public static final String EXCEPTION_REQUEST_RESPONSIBILITY_ID = "-2";
    public static final String SAVED_REQUEST_RESPONSIBILITY_ID = "-3";

    public static final Set<String> SPECIAL_RESPONSIBILITY_ID_SET;

    static {
        SPECIAL_RESPONSIBILITY_ID_SET = new HashSet<>();
        SPECIAL_RESPONSIBILITY_ID_SET.add(MACHINE_GENERATED_RESPONSIBILITY_ID);
        SPECIAL_RESPONSIBILITY_ID_SET.add(ADHOC_REQUEST_RESPONSIBILITY_ID);
        SPECIAL_RESPONSIBILITY_ID_SET.add(EXCEPTION_REQUEST_RESPONSIBILITY_ID);
        SPECIAL_RESPONSIBILITY_ID_SET.add(SAVED_REQUEST_RESPONSIBILITY_ID);
    }

    public static final String PRIMARY_PROCESS_NAME = "PRIMARY";
    public static final String PRIMARY_BRANCH_NAME = "PRIMARY";

    // Document type versions
    public static final String ROUTING_VERSION_ROUTE_LEVEL = "1";
    public static final String ROUTING_VERSION_NODAL = "2";
    public static final String CURRENT_ROUTING_VERSION = ROUTING_VERSION_NODAL;

    public static final String POST_PROCESSOR_FAILURE_MESSAGE = "PostProcessor failed to process document: ";

    // system branch state keys
    public static final String POST_PROCESSOR_PROCESSED_KEY = "System.PostProcessorProcessed";
    public static final String POST_PROCESSOR_FINAL_KEY = "System.PostProcessorFinal";

    public static final String ACTION_LIST_NO_REFRESH = "ActionList.norefresh";
    public static final String REQUERY_ACTION_LIST_KEY = "requeryActionList";

    // receive future action request constants
    public static final String RECEIVE_FUTURE_REQUESTS_BRANCH_STATE_KEY = "_receive_future_requests";
    public static final String DEACTIVATED_FUTURE_REQUESTS_BRANCH_STATE_KEY = "_deactivated_future_requests";
    public static final String DONT_RECEIVE_FUTURE_REQUESTS_BRANCH_STATE_VALUE = "NO";
    public static final String RECEIVE_FUTURE_REQUESTS_BRANCH_STATE_VALUE = "YES";
    public static final String CLEAR_FUTURE_REQUESTS_BRANCH_STATE_VALUE = "CLEAR";

    public static final String ACTIONLIST_COMMAND = "displayActionListView";

    public static final String DOCSEARCH_COMMAND = "displayDocSearchView";

    public static final String SUPERUSER_COMMAND = "displaySuperUserView";

    public static final String HELPDESK_ACTIONLIST_COMMAND = "displayHelpDeskActionListView";

    public static final String INITIATE_COMMAND = "initiate";

    public static final String COMMAND_PARAMETER = "command";

    public static final String DOCUMENT_ID_PARAMETER = "docId";

    public static final String BACKDOOR_ID_PARAMETER = "backdoorId";

    public static final String DOCTYPE_PARAMETER = "docTypeName";

    public static final String DEFAULT_RESPONSIBILITY_TEMPLATE_NAME = "Review";

    public static final String EXCEPTION_ROUTING_RESPONSIBILITY_TEMPLATE_NAME = "Resolve Exception";

    public static final String APP_DOC_ID_PARAMETER = "appDocId";

    // Permission Details
    public static final String DOCUMENT_TYPE_NAME_DETAIL = "documentTypeName";
    public static final String ACTION_REQUEST_CD_DETAIL = "actionRequestCd";
    public static final String ROUTE_NODE_NAME_DETAIL = "routeNodeName";
    public static final String DOCUMENT_STATUS_DETAIL = "routeStatusCode";
    public static final String APP_DOC_STATUS_DETAIL = "appDocStatus";

    // Permissions
    public static final String BLANKET_APPROVE_PERMISSION = "Blanket Approve Document";
    public static final String AD_HOC_REVIEW_PERMISSION = "Ad Hoc Review Document";
    public static final String ADMINISTER_ROUTING_PERMISSION = "Administer Routing for Document";
    public static final String SUPER_USER_APPROVE_SINGLE_ACTION_REQUEST = "Super User Approve Single Action Request";
    public static final String SUPER_USER_APPROVE_DOCUMENT = "Super User Approve Document";
    public static final String SUPER_USER_DISAPPROVE_DOCUMENT = "Super User Disapprove Document";

    public static final String CANCEL_PERMISSION = "Cancel Document";
    public static final String RECALL_PERMISSION = "Recall Document";
    public static final String INITIATE_PERMISSION = "Initiate Document";
    public static final String ROUTE_PERMISSION = "Route Document";
    public static final String SAVE_PERMISSION = "Save Document";

    // signifies the delimiter character for ingested KIM groups
    public static final String KIM_GROUP_NAMESPACE_NAME_DELIMITER_CHARACTER = ":";

    // system parameters
    public static final String KIM_PRIORITY_ON_DOC_TYP_PERMS_IND = "KIM_PRIORITY_ON_DOC_TYP_PERMS_IND";
    public static final String ACTION_LIST_SEND_EMAIL_NOTIFICATION_IND = "SEND_EMAIL_NOTIFICATION_IND";
    public static final String SHOW_BACK_DOOR_LOGIN_IND = "SHOW_BACK_DOOR_LOGIN_IND";
    public static final String EMAIL_REMINDER_FROM_ADDRESS = "FROM_ADDRESS";
    public static final String MAX_NODES_BEFORE_RUNAWAY_PROCESS = "MAXIMUM_NODES_BEFORE_RUNAWAY";
    public static final String NOTIFICATION_EXCLUDED_USERS_WORKGROUP_NAME_IND = "NOTIFY_GROUPS";
    public static final String DOC_SEARCH_FETCH_MORE_ITERATION_LIMIT = "FETCH_MORE_ITERATION_LIMIT";
    public static final String DOC_SEARCH_RESULT_CAP = "RESULT_CAP";
    public static final String RULE_CACHE_REQUEUE_DELAY = "RULE_CACHE_REQUEUE_DELAY";
    public static final String ACTIONLIST_EMAIL_TEST_ADDRESS = "EMAIL_NOTIFICATION_TEST_ADDRESS";

    // System parameter value comparisons
    public static final String ACTION_LIST_SEND_EMAIL_NOTIFICATION_VALUE = "Y";

    public static final String ROLEROUTE_QUALIFIER_RESOLVER_ELEMENT = "qualifierResolver";
    public static final String ROLEROUTE_QUALIFIER_RESOLVER_CLASS_ELEMENT = "qualifierResolverClass";
    public static final String ROLEROUTE_RESPONSIBILITY_TEMPLATE_NAME_ELEMENT = "responsibilityTemplateName";
    public static final String ROLEROUTE_NAMESPACE_ELEMENT = "namespace";

    public static final String ACTION_LIST_FILTER_ATTR_NAME = "ActionListFilter";
    public static final String UPDATE_ACTION_LIST_ATTR_NAME = "updateActionList";
    public static final String SORT_ORDER_ATTR_NAME = "sortOrder";
    public static final String SORT_CRITERIA_ATTR_NAME = "sortCriteria";
    public static final String CURRENT_PAGE_ATTR_NAME = "currentPage";

    public static final String HELP_DESK_ACTION_LIST_PRINCIPAL_ATTR_NAME = "helpDeskActionListPrincipal";
    public static final String HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME = "helpDeskActionListPerson";
    public static final String PREFERENCES = "Preferences";
    public static final String WORKFLOW_DOCUMENT_MAP_ATTR_NAME = "workflowDocumentMap";

    public static final String USER_OPTIONS_DEFAULT_USE_OUTBOX_PARAM = "userOptions.default.useOutBox";

    /**
     * Defines the prefix to add to document attribute field names on the document search screens.
     */
    public static final String DOCUMENT_ATTRIBUTE_FIELD_PREFIX = "documentAttribute.";
    public static final String DOCUMENT_CONTENT_ELEMENT = "documentContent";
    public static final String ATTRIBUTE_CONTENT_ELEMENT = "attributeContent";
    public static final String SEARCHABLE_CONTENT_ELEMENT = "searchableContent";
    public static final String APPLICATION_CONTENT_ELEMENT = "applicationContent";
    public static final String DEFAULT_DOCUMENT_CONTENT = "<" + DOCUMENT_CONTENT_ELEMENT + "/>";

    public static final String ACTION_TAKEN_APPROVED_CD = "A";
    public static final String ACTION_TAKEN_COMPLETED_CD = "C";
    public static final String ACTION_TAKEN_ACKNOWLEDGED_CD = "K";
    public static final String ACTION_TAKEN_FYI_CD = "F";
    public static final String ACTION_TAKEN_DENIED_CD = "D";
    public static final String ACTION_TAKEN_CANCELED_CD = "X";
    public static final String ACTION_TAKEN_ROUTED_CD = "O";

    private KewApiConstants() {
        throw new UnsupportedOperationException("Should never be called.");
    }

    // Cu customization to change access from private to public
    public static final class SearchableAttributeConstants {
        public static final String SEARCH_WILDCARD_CHARACTER = "*";
        public static final String SEARCH_WILDCARD_CHARACTER_REGEX_ESCAPED = "\\" + SEARCH_WILDCARD_CHARACTER;
        public static final String DEFAULT_RANGE_SEARCH_LOWER_BOUND_LABEL = "From";
        public static final String DEFAULT_RANGE_SEARCH_UPPER_BOUND_LABEL = "To";

        private SearchableAttributeConstants() {
        }
    }

    public static final class GroupMembershipChangeOperations {
        public static final String ADDED = "ADDED";
        public static final String REMOVED = "REMOVED";
    }

    public static final class DocumentContentVersions {
        public static final int ROUTE_LEVEL = 0;
        public static final int NODAL = 1;
        public static final int CURRENT = NODAL;
    }

    public static final class PermissionNames {
        public static final String VIEW_OTHER_ACTION_LIST = "View Other Action List";
        public static final String UNRESTRICTED_DOCUMENT_SEARCH = "Unrestricted Document Search";

        private PermissionNames() {
            throw new UnsupportedOperationException("do not call");
        }
    }
}
