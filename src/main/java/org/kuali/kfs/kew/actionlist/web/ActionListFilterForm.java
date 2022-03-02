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
package org.kuali.kfs.kew.actionlist.web;

import org.kuali.kfs.kew.actionlist.ActionListFilter;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;

import java.text.ParseException;
import java.util.List;

/**
 * ====
 * CU Customization: Added elements for the action list last modified date filter.
 * ====
 */

/**
 * Struts form class for ActionListFilterAction
 */
public class ActionListFilterForm extends KualiForm {

    private static final long serialVersionUID = -1149636352016711445L;
    private static final String CREATE_DATE_FROM = "createDateFrom";
    private static final String CREATE_DATE_TO = "createDateTo";
    private static final String LAST_ASSIGNED_DATE_FROM = "lastAssignedDateFrom";
    private static final String LAST_ASSIGNED_DATE_TO = "lastAssignedDateTo";
    private ActionListFilter filter;
    private String createDateFrom;
    private String createDateTo;
    private String lastAssignedDateTo;
    private String lastAssignedDateFrom;
    private String methodToCall = "";
    private String lookupableImplServiceName;
    private String lookupType;
    private String docTypeFullName;
    private List userWorkgroups;
    private String cssFile = "kuali.css";
    private String test = "";

    private static String LAST_MODIFIED_DATE_FROM = "lastModifiedDateFrom";
    private static String LAST_MODIFIED_DATE_TO = "lastModifiedDateTo";
    private String lastModifiedDateTo;
    private String lastModifiedDateFrom;

    public ActionListFilterForm() {
        filter = new ActionListFilter();
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getCreateDateTo() {
        return createDateTo;
    }

    public void setCreateDateTo(String createDateTo) {
        if (createDateTo == null) {
            createDateTo = "";
        } else {
            this.createDateTo = createDateTo.trim();
        }
    }
    
    public String getLastAssignedDateFrom() {
        return lastAssignedDateFrom;
    }

    public void setLastAssignedDateFrom(String lastAssignedDateFrom) {
        if (lastAssignedDateFrom == null) {
            lastAssignedDateFrom = "";
        } else {
            this.lastAssignedDateFrom = lastAssignedDateFrom.trim();
        }
    }

    public String getLastModifiedDateFrom() {
        return lastModifiedDateFrom;
    }

    public void setLastModifiedDateFrom(String lastModifiedDateFrom) {
        if (lastModifiedDateFrom == null) {
            lastModifiedDateFrom = "";
        } else {
            this.lastModifiedDateFrom = lastModifiedDateFrom.trim();
        }
    }

    public String getCreateDateFrom() {
        return createDateFrom;
    }

    public void setCreateDateFrom(String createDate) {
        if (createDate == null) {
            createDate = "";
        } else {
            this.createDateFrom = createDate.trim();
        }
    }

    public ActionListFilter getFilter() {
        return filter;
    }

    public String getMethodToCall() {
        return methodToCall;
    }

    public void setMethodToCall(String methodToCall) {
        this.methodToCall = methodToCall;
    }

    public void setFilter(ActionListFilter filter) {
        this.filter = filter;
        if (filter.getCreateDateFrom() != null) {
            setCreateDateFrom(KFSConstants.getDefaultDateFormat().format(filter.getCreateDateFrom()));
        }
        if (filter.getCreateDateTo() != null) {
            setCreateDateTo(KFSConstants.getDefaultDateFormat().format(filter.getCreateDateTo()));
        }
        if (filter.getLastAssignedDateFrom() != null) {
            setLastAssignedDateFrom(KFSConstants.getDefaultDateFormat().format(filter.getLastAssignedDateFrom()));
        }
        if (filter.getLastAssignedDateTo() != null) {
            setLastAssignedDateTo(KFSConstants.getDefaultDateFormat().format(filter.getLastAssignedDateTo()));
        }
        if (filter.getLastModifiedDateFrom() != null) {
            setLastModifiedDateFrom(KFSConstants.getDefaultDateFormat().format(filter.getLastModifiedDateFrom()));
        }
        if (filter.getLastModifiedDateTo() != null) {
            setLastModifiedDateTo(KFSConstants.getDefaultDateFormat().format(filter.getLastModifiedDateTo()));
        }
    }

    public String getLastAssignedDateTo() {
        return lastAssignedDateTo;
    }

    public void setLastAssignedDateTo(String lastAssignedDate) {
        if (lastAssignedDate == null) {
            lastAssignedDate = "";
        } else {
            this.lastAssignedDateTo = lastAssignedDate.trim();
        }
    }
    
    public String getLastModifiedDateTo() {
        return lastModifiedDateTo;
    }

    public void setLastModifiedDateTo(String lastModifiedDateTo) {
        if (lastModifiedDateTo == null) {
            lastModifiedDateTo = "";
        } else {
            this.lastModifiedDateTo = lastModifiedDateTo.trim();
        }
    }

    public void validateDates() {
        if (getCreateDateFrom() != null && getCreateDateFrom().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getCreateDateFrom());
            } catch (ParseException e) {
                GlobalVariables.getMessageMap()
                        .putError(CREATE_DATE_FROM, "general.error.fieldinvalid", "Create Date From");
            }
        }
        if (getCreateDateTo() != null && getCreateDateTo().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getCreateDateTo());
            } catch (ParseException e) {
                GlobalVariables.getMessageMap()
                        .putError(CREATE_DATE_TO, "general.error.fieldinvalid", "Create Date To");
            }
        }
        if (getLastAssignedDateFrom() != null && getLastAssignedDateFrom().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getLastAssignedDateFrom());
            } catch (ParseException e1) {
                GlobalVariables.getMessageMap()
                        .putError(LAST_ASSIGNED_DATE_FROM, "general.error.fieldinvalid", "Last Assigned Date From");
            }
        }
        if (getLastAssignedDateTo() != null && getLastAssignedDateTo().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getLastAssignedDateTo());
            } catch (ParseException e1) {
                GlobalVariables.getMessageMap()
                        .putError(LAST_ASSIGNED_DATE_TO, "general.error.fieldinvalid", "Last Assigned Date To");
            }
        }
        if (getLastModifiedDateFrom() != null && getLastModifiedDateFrom().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getLastModifiedDateFrom());
            } catch (ParseException e1) {
                GlobalVariables.getMessageMap()
                        .putError(LAST_MODIFIED_DATE_FROM, "general.error.fieldinvalid", "Date Last Modified From");
            }
        }
        if (getLastModifiedDateTo() != null && getLastModifiedDateTo().length() != 0) {
            try {
                KFSConstants.getDefaultDateFormat().parse(getLastModifiedDateTo());
            } catch (ParseException e1) {
                GlobalVariables.getMessageMap()
                        .putError(LAST_MODIFIED_DATE_TO, "general.error.fieldinvalid", "Date Last Modified To");
            }
        }
    }

    public ActionListFilter getLoadedFilter()/* throws ParseException*/ {
        try {
            if (getCreateDateFrom() != null && getCreateDateFrom().length() != 0) {
                filter.setCreateDateFrom(KFSConstants.getDefaultDateFormat().parse(getCreateDateFrom()));
            }
            if (getCreateDateTo() != null && getCreateDateTo().length() != 0) {
                filter.setCreateDateTo(KFSConstants.getDefaultDateFormat().parse(getCreateDateTo()));
            }
            if (getLastAssignedDateFrom() != null && getLastAssignedDateFrom().length() != 0) {
                filter.setLastAssignedDateFrom(KFSConstants.getDefaultDateFormat().parse(getLastAssignedDateFrom()));
            }
            if (getLastAssignedDateTo() != null && getLastAssignedDateTo().length() != 0) {
                filter.setLastAssignedDateTo(KFSConstants.getDefaultDateFormat().parse(getLastAssignedDateTo()));
            }
            if (getLastModifiedDateFrom() != null && getLastModifiedDateFrom().length() != 0) {
                filter.setLastModifiedDateFrom(KFSConstants.getDefaultDateFormat().parse(getLastModifiedDateFrom()));
            }
            if (getLastModifiedDateTo() != null && getLastModifiedDateTo().length() != 0) {
                filter.setLastModifiedDateTo(KFSConstants.getDefaultDateFormat().parse(getLastModifiedDateTo()));
            }
            if (getDocTypeFullName() != null && !"".equals(getDocTypeFullName())) {
                filter.setDocumentType(getDocTypeFullName());
            }
        } catch (ParseException e) {
            //error caught and displayed in validateDates()
        }

        return filter;
    }

    public String getLookupableImplServiceName() {
        return lookupableImplServiceName;
    }

    public void setLookupableImplServiceName(String lookupableImplServiceName) {
        this.lookupableImplServiceName = lookupableImplServiceName;
    }

    public String getLookupType() {
        return lookupType;
    }

    public void setLookupType(String lookupType) {
        this.lookupType = lookupType;
    }

    public String getDocTypeFullName() {
        return docTypeFullName;
    }

    public void setDocTypeFullName(String docTypeFullName) {
        this.docTypeFullName = docTypeFullName;
    }

    public List getUserWorkgroups() {
        return userWorkgroups;
    }

    public void setUserWorkgroups(List userWorkgroups) {
        this.userWorkgroups = userWorkgroups;
    }

    public String getCssFile() {
        return this.cssFile;
    }

    public void setCssFile(String cssFile) {
        this.cssFile = cssFile;
    }

}
