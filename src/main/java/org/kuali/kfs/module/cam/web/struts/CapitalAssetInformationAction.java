/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2017 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.cam.web.struts;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.document.service.GlAndPurApHelperService;
import org.kuali.kfs.module.cam.document.service.GlLineService;
import org.kuali.kfs.module.cam.document.web.struts.CabActionBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Struts action class that handles Capital Asset Information Screen actions
 */
public class CapitalAssetInformationAction extends CabActionBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CapitalAssetInformationAction.class);
    
    /**
     * Action "process" from CAB GL Lookup screen is processed by this method
     *
     * @param mapping {@link ActionMapping}
     * @param form {@link ActionForm}
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return {@link ActionForward}
     * @throws Exception
     */
    public ActionForward process(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        CapitalAssetInformationForm capitalAssetForm = (CapitalAssetInformationForm) form;
        String glAcctId = request.getParameter(CamsPropertyConstants.GeneralLedgerEntry.GENERAL_LEDGER_ACCOUNT_IDENTIFIER);
        Long cabGlEntryId = Long.valueOf(glAcctId);
        capitalAssetForm.setGeneralLedgerAccountIdentifier(cabGlEntryId);

        GeneralLedgerEntry entry = findGeneralLedgerEntry(request);
        if (ObjectUtils.isNotNull(entry)) {
            prepareRecordsForDisplay(capitalAssetForm, entry);
        }
      //  if (!entry.isActive()) {
      //      KNSGlobalVariables.getMessageList().add(CabKeyConstants.WARNING_GL_PROCESSED);
      //  }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    protected void prepareRecordsForDisplay(CapitalAssetInformationForm capitalAssetForm, GeneralLedgerEntry entry) {
        GlLineService glLineService = SpringContext.getBean(GlLineService.class);

        entry.setSelected(true);
        capitalAssetForm.setGeneralLedgerEntry(entry);
        capitalAssetForm.setPrimaryGlAccountId(entry.getGeneralLedgerAccountIdentifier());
        
        List<CapitalAssetInformation> capitalAssetInformations = glLineService.findAllCapitalAssetInformation(entry.getDocumentNumber());
        Collection<GeneralLedgerEntry> glEntries = glLineService.findAllGeneralLedgerEntry(entry.getDocumentNumber()); 
        
        if(shouldBuildMissingCapitalAssetInformation(capitalAssetInformations, glEntries)){
        	// we need to generate missing capital asset info
            LOG.debug("prepareRecordsForDisplay, Need to creating missing information objects.");
        	glLineService.setupMissingCapitalAssetInformation(entry.getDocumentNumber());
        }
        
        List<CapitalAssetInformation> capitalAssetInformation = glLineService.findCapitalAssetInformationForGLLine(entry);
        


        // KFSMI-9881
        // For GL Entries without capital asset information (ex: loaded by enterprise feed or Vendor Credit Memo),
        // we need to create that information (at least a shell record) for the Capital Asset Information screen
        // to render and subsequent processing to occur successfully.
        if (capitalAssetInformation.isEmpty()) {
            glLineService.setupCapitalAssetInformation(entry);
            capitalAssetInformation = glLineService.findCapitalAssetInformationForGLLine(entry);
        }

        capitalAssetForm.setCapitalAssetInformation(capitalAssetInformation);
    }

    private boolean shouldBuildMissingCapitalAssetInformation(List<CapitalAssetInformation> capitalAssetInformations,
            Collection<GeneralLedgerEntry> glEntries) {
        boolean isCapitalAssetInformationAndGLEntriesDifferentSizes = !capitalAssetInformations.isEmpty() && capitalAssetInformations.size() != glEntries.size();
        if (isGeneralErrorCorrectionDocument(glEntries)) {
            return isCapitalAssetInformationAndGLEntriesDifferentSizes && isCapitalAssetInformationAndGLEntryTotalsDifferent(capitalAssetInformations, glEntries);
        } else {
            return isCapitalAssetInformationAndGLEntriesDifferentSizes;
        }
    }
    
    private boolean isGeneralErrorCorrectionDocument(Collection<GeneralLedgerEntry> glEntries) {
        boolean isGeneralErrorCorrection = false;
        if (CollectionUtils.isNotEmpty(glEntries)) {
            GeneralLedgerEntry firstEntry = glEntries.iterator().next();
            isGeneralErrorCorrection = StringUtils.equalsIgnoreCase(firstEntry.getFinancialDocumentTypeCode(), 
                    KFSConstants.FinancialDocumentTypeCodes.GENERAL_ERROR_CORRECTION);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("isGeneralErrorCorrectionDocument: " + isGeneralErrorCorrection);
        }
        return isGeneralErrorCorrection;
    }
    
    private boolean isCapitalAssetInformationAndGLEntryTotalsDifferent(List<CapitalAssetInformation> capitalAssetInformations,
            Collection<GeneralLedgerEntry> glEntries) {
        KualiDecimal entriesTotal = findTotalGLEntries(glEntries);
        KualiDecimal informationTotal = findTotalAmountForAssetInformation(capitalAssetInformations);
        if (LOG.isDebugEnabled()) {
            int capAssetInfoSize = CollectionUtils.isNotEmpty(capitalAssetInformations) ? capitalAssetInformations.size() : 0;
            int glEntreisSzie = CollectionUtils.isNotEmpty(glEntries) ? glEntries.size() : 0;
            StringBuilder sb = new StringBuilder("isCapitalAssetInformationAndGLEntryTotalsDifferent, the number of capitalAssetInformations size: ");
            sb.append(capAssetInfoSize).append(" glEntries size: ").append(glEntreisSzie);
            sb.append(" informationTotal: ").append(informationTotal).append(" entriesTotal: ").append(entriesTotal);
            LOG.debug(sb.toString());
        }
        return !entriesTotal.equals(informationTotal);
    }
    
    private KualiDecimal findTotalAmountForAssetInformation(List<CapitalAssetInformation> informationList) {
        KualiDecimal amount = KualiDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(informationList)) {
            for (CapitalAssetInformation info : informationList) {
                amount = amount.abs().add(info.getCapitalAssetLineAmount().abs());
            }
        }
        return amount;
    }
    
    private KualiDecimal findTotalGLEntries(Collection<GeneralLedgerEntry> glEntries) {
        KualiDecimal amount = KualiDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(glEntries)) {
            for (GeneralLedgerEntry entry : glEntries)  {
                amount = amount.abs().add(entry.getAmount().abs());
            }
        }
        return amount;
    }

    /**
     * Finds GL entry using the key from request
     *
     * @param request HttpServletRequest
     * @return GeneralLedgerEntry
     */
    protected GeneralLedgerEntry findGeneralLedgerEntry(HttpServletRequest request) {
        String glAcctId = request.getParameter(CamsPropertyConstants.GeneralLedgerEntry.GENERAL_LEDGER_ACCOUNT_IDENTIFIER);
        Long cabGlEntryId = Long.valueOf(glAcctId);
        return findGeneralLedgerEntry(cabGlEntryId, false);
    }

    /**
     * Retrieves the CAB General Ledger Entry from DB
     *
     * @param generalLedgerEntryId Entry Id
     * @return GeneralLedgerEntry
     */
    protected GeneralLedgerEntry findGeneralLedgerEntry(Long generalLedgerEntryId, boolean requireNew) {
        BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
        Map<String, Object> pkeys = new HashMap<String, Object>();
        pkeys.put(CamsPropertyConstants.GeneralLedgerEntry.GENERAL_LEDGER_ACCOUNT_IDENTIFIER, generalLedgerEntryId);
        if (requireNew) {
            pkeys.put(CamsPropertyConstants.GeneralLedgerEntry.ACTIVITY_STATUS_CODE, CamsConstants.ActivityStatusCode.NEW);
        }
        GeneralLedgerEntry entry = boService.findByPrimaryKey(GeneralLedgerEntry.class, pkeys);
        return entry;
    }

    /**
     * Cancels the action and returns to portal main page
     *
     * @param mapping {@link ActionMapping}
     * @param form {@link ActionForm}
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return {@link ActionForward}
     * @throws Exception
     */
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#showAllTabs(ActionMapping,
     * ActionForm, HttpServletRequest, HttpServletResponse)
     */
    @Override
    public ActionForward showAllTabs(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CapitalAssetInformationForm capitalAssetForm = (CapitalAssetInformationForm) form;
        GeneralLedgerEntry generalLedgerEntry = capitalAssetForm.getGeneralLedgerEntry();
        generalLedgerEntry.setSelected(true);

        return super.showAllTabs(mapping, form, request, response);
    }

    /**
     * reloads the capital asset information screen
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward reload(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CapitalAssetInformationForm capitalAssetForm = (CapitalAssetInformationForm) form;

        GeneralLedgerEntry entry = capitalAssetForm.getGeneralLedgerEntry();

     //   GeneralLedgerEntry entry = findGeneralLedgerEntry(request);
     //   if (entry != null) {
            prepareRecordsForDisplay(capitalAssetForm, entry);
      //  }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    protected GlAndPurApHelperService getGlAndPurApHelperService() {
        return SpringContext.getBean(GlAndPurApHelperService.class);
    }
}