package edu.cornell.kfs.fp.batch.service.impl;

import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.fp.businessobject.IncomingWireAchMapping;
import edu.iu.ebs.kfs.fp.FinancialProcessingConstants;
import edu.iu.ebs.kfs.fp.FinancialProcessingParameterConstants;
import edu.iu.ebs.kfs.fp.batch.GenerateAdvanceDepositDocumentsStep;
import edu.iu.ebs.kfs.fp.batch.service.impl.AdvanceDepositServiceImpl;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeNote;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeTransaction;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
Copyright Indiana University
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
public class CuAdvanceDepositServiceImpl extends AdvanceDepositServiceImpl {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAdvanceDepositServiceImpl.class);

    protected void createSourceAccountingLine(AchIncomeTransaction transaction , AdvanceDepositDocument advanceDepositDocument) {
        IncomingWireAchMapping matchingIncomingWireAchMapping = null;

        Collection<IncomingWireAchMapping> incomingWireAchMappings = businessObjectService.findAll(IncomingWireAchMapping.class);

        for (IncomingWireAchMapping mapping: incomingWireAchMappings) {
            List<AchIncomeNote> notes = transaction.getNotes();
            if (doNotesMatch(mapping, notes)) {
                matchingIncomingWireAchMapping = mapping;
                break;
            }
        }

        if (ObjectUtils.isNotNull(matchingIncomingWireAchMapping)) {
            String chart = matchingIncomingWireAchMapping.getChartOfAccountsCode();
            String objectCode = matchingIncomingWireAchMapping.getFinancialObjectCode();
            String account = matchingIncomingWireAchMapping.getAccountNumber();
            setupSourceAccountingLine(transaction, advanceDepositDocument, chart, objectCode, account);
        } else {
            super.createSourceAccountingLine(transaction, advanceDepositDocument);
        }
    }

    private boolean doNotesMatch(IncomingWireAchMapping mapping, List<AchIncomeNote> notes) {
        final String regex = ".*" + mapping.getShortDescription() + ".*";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        for (AchIncomeNote note: notes) {
            Matcher m = p.matcher(note.getNoteText());
            if (m.matches()) {
                //note.getNoteText().matches(".*" + mapping.getShortDescription() + ".*")
                return true;
            }
        }
        return false;
    }

}
