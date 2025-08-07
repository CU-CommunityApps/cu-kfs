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
package org.kuali.kfs.module.cam.document.service;

import org.kuali.kfs.fp.businessobject.CapitalAssetAccountsGroupDetails;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;

import java.util.Collection;
import java.util.List;

public interface GlLineService {

    Collection<GeneralLedgerEntry> findAllGeneralLedgerEntry(String documentNumber);

    Collection<GeneralLedgerEntry> findMatchingGeneralLedgerEntries(Collection<GeneralLedgerEntry> allGLEntries, CapitalAssetAccountsGroupDetails accountingDetails);

    List<CapitalAssetInformation> findAllCapitalAssetInformation(String documentNumber);

    List<CapitalAssetInformation> findCapitalAssetInformationForGLLine(GeneralLedgerEntry entry);

    long findUnprocessedCapitalAssetInformation(String documentNumber);

    CapitalAssetInformation findCapitalAssetInformation(String documentNumber, Integer capitalAssetLineNumber);

    Document createAssetGlobalDocument(GeneralLedgerEntry primary, Integer capitalAssetLineNumber);

    Document createAssetPaymentDocument(GeneralLedgerEntry primary, Integer capitalAssetLineNumber);

    void setupCapitalAssetInformation(GeneralLedgerEntry entry);
    
    void setupMissingCapitalAssetInformation(String documentNumber);

}