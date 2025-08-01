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
package org.kuali.kfs.module.ld.businessobject;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride.COMPONENT;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Labor business object for Labor Accounting Line Override
 */
public final class LaborAccountingLineOverride {

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private LaborAccountingLineOverride() {
    }

    /**
     * On the given AccountingLine, converts override input checkboxes from a Struts Form into a persistable override
     * code.
     *
     * @param line
     */
    public static void populateFromInput(final AccountingLine line) {
        // todo: this logic won't work if a single account checkbox might also stands for NON_FRINGE_ACCOUNT_USED

        final Set<Integer> overrideInputComponents = new HashSet<>();
        if (line.getAccountExpiredOverride()) {
            overrideInputComponents.add(COMPONENT.EXPIRED_ACCOUNT);
        }
        if (line.isObjectBudgetOverride()) {
            overrideInputComponents.add(COMPONENT.NON_BUDGETED_OBJECT);
        }
        if (line.getNonFringeAccountOverride()) {
            overrideInputComponents.add(COMPONENT.NON_FRINGE_ACCOUNT_USED);
        }

        final Integer[] inputComponentArray = overrideInputComponents.toArray(new Integer[overrideInputComponents.size()]);
        line.setOverrideCode(AccountingLineOverride.valueOf(inputComponentArray).getCode());
    }

    /**
     * Prepares the given AccountingLine in a Struts Action for display by a JSP. This means converting the override
     * code to checkboxes for display and input, as well as analyzing the accounting line and determining which
     * override checkboxes are needed.
     *
     * @param line
     */
    public static void processForOutput(final AccountingDocument document, final AccountingLine line) {
        final AccountingLineOverride fromCurrentCode = AccountingLineOverride.valueOf(line.getOverrideCode());
        final AccountingLineOverride needed = determineNeededOverrides(document, line);
        // KFSMI-9133 : updating system to automatically check expired account boxes on the source side of the
        // transaction, since those are read only.  Otherwise, amounts in expired accounts could never be transferred
        line.setAccountExpiredOverrideNeeded(needed.hasComponent(COMPONENT.EXPIRED_ACCOUNT));
        if (line.getAccountExpiredOverrideNeeded()) {
            line.setAccountExpiredOverride(fromCurrentCode.hasComponent(COMPONENT.EXPIRED_ACCOUNT));
        }
        line.setObjectBudgetOverride(fromCurrentCode.hasComponent(COMPONENT.NON_BUDGETED_OBJECT));
        line.setObjectBudgetOverrideNeeded(needed.hasComponent(COMPONENT.NON_BUDGETED_OBJECT));
        line.setNonFringeAccountOverride(fromCurrentCode.hasComponent(COMPONENT.NON_FRINGE_ACCOUNT_USED));
        line.setNonFringeAccountOverrideNeeded(needed.hasComponent(COMPONENT.NON_FRINGE_ACCOUNT_USED));
    }

    /**
     * Determines what overrides the given line needs.
     *
     * @param line
     * @return what overrides the given line needs.
     */
    public static AccountingLineOverride determineNeededOverrides(
            @Nullable final AccountingDocument document, final AccountingLine line
    ) {
        boolean isDocumentFinalOrProcessed = false;
        if (ObjectUtils.isNotNull(document)) {
            final AccountingDocument accountingDocument = document;
            isDocumentFinalOrProcessed = accountingDocument.isDocumentFinalOrProcessed();
        }
        final Set<Integer> neededOverrideComponents = new HashSet<>();
        if (AccountingLineOverride.needsExpiredAccountOverride(line, isDocumentFinalOrProcessed)) {
            neededOverrideComponents.add(COMPONENT.EXPIRED_ACCOUNT);
        }
        if (AccountingLineOverride.needsObjectBudgetOverride(line.getAccount(), line.getObjectCode())) {
            neededOverrideComponents.add(COMPONENT.NON_BUDGETED_OBJECT);
        }
        if (AccountingLineOverride.needsNonFringAccountOverride(line.getAccount())) {
            neededOverrideComponents.add(COMPONENT.NON_FRINGE_ACCOUNT_USED);
        }
        final Integer[] inputComponentArray = neededOverrideComponents.toArray(new Integer[neededOverrideComponents.size()]);

        return AccountingLineOverride.valueOf(inputComponentArray);
    }

}
