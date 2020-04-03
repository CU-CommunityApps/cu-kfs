/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject.lookup;

import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.krad.bo.BusinessObject;

import java.util.List;
import java.util.Map;

public class AccountsReceivablesDocumentHeaderLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        setBackLocation(fieldValues.get(KFSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KFSConstants.DOC_FORM_KEY));
        setReferencesToRefresh(fieldValues.get(KFSConstants.REFERENCES_TO_REFRESH));

        // we only want invoices, so let's add that to the fieldValues
        fieldValues.put(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME, ArConstants.INV_DOCUMENT_TYPE);

        return (List<AccountsReceivableDocumentHeader>) getLookupService().findCollectionBySearchHelper(
                getBusinessObjectClass(), fieldValues, false);
    }
}
