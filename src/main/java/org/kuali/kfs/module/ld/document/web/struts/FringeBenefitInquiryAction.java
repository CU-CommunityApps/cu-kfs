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
package org.kuali.kfs.module.ld.document.web.struts;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.businessobject.BenefitInquiry;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// CU customization: backport FINP-12883
public class FringeBenefitInquiryAction extends KualiAction {
    
    private LaborBenefitsCalculationService laborBenefitsCalculationService;

    public ActionForward calculateFringeBenefit(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final FringeBenefitInquiryForm accountingLineForm = (FringeBenefitInquiryForm) form;

        final Integer payrollFiscalYear = Integer.valueOf(accountingLineForm.getPayrollEndDateFiscalYear());
        final String chartOfAccountsCode = accountingLineForm.getChartOfAccountsCode();
        final String objectCode = accountingLineForm.getFinancialObjectCode();
        final KualiDecimal amount = new KualiDecimal(accountingLineForm.getAmount());
        final Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(
                LaborPositionObjectBenefitService.class).getActivePositionObjectBenefits(payrollFiscalYear,
                chartOfAccountsCode, objectCode);

        final List<BenefitInquiry> fringeBenefitEntries = new ArrayList<>();
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            final BenefitsCalculation benefitsCalculation =
                    getLaborBenefitsCalculationService().getBenefitsCalculation(positionObjectBenefit);
            if (ObjectUtils.isNotNull(benefitsCalculation) && benefitsCalculation.isActive()) {
                final BenefitInquiry benefitInquiry = new BenefitInquiry();
                final String fringeBenefitObjectCode = benefitsCalculation.getPositionFringeBenefitObjectCode();
                benefitInquiry.setFringeBenefitObjectCode(fringeBenefitObjectCode);
                final KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class)
                        .calculateFringeBenefit(positionObjectBenefit, amount, accountingLineForm.getAccountNumber(),
                                accountingLineForm.getSubAccountNumber());
                benefitInquiry.setBenefitAmount(benefitAmount);
                fringeBenefitEntries.add(benefitInquiry);
            }
        }

        fringeBenefitEntries.sort(Collections.reverseOrder());

        accountingLineForm.setBenefitInquiry(fringeBenefitEntries);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    private LaborBenefitsCalculationService getLaborBenefitsCalculationService() {
        if (laborBenefitsCalculationService == null) {
            laborBenefitsCalculationService = SpringContext.getBean(LaborBenefitsCalculationService.class);
        }
        return laborBenefitsCalculationService;
    }
}
