/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.document.web.struts;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliationReport;
import com.rsmart.kuali.kfs.cr.document.service.CheckReconciliationReportService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl;
import net.sf.jasperreports.engine.JRParameter;

import org.apache.log4j.Level;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportGenerationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Check Reconciliation Action
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
@SuppressWarnings("deprecation")
public class CheckReconciliationReportAction extends KualiAction {
	private static final Logger LOG = LogManager.getLogger(CheckReconciliationReportAction.class);

    private static String COMMA = ",";
    
    /**
     * Generates the CR Report and returns pdf.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward performReport(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CheckReconciliationReportForm crForm = (CheckReconciliationReportForm)form;
        
        CheckReconciliationReportService serv = SpringContext.getBean(CheckReconciliationReportService.class);
        List<CheckReconciliationReport> reportSet = serv.buildReports(crForm.getStartDate(),crForm.getEndDate());
        
        if(LOG.isDebugEnabled()) {
        	int reportSize = reportSet != null ? reportSet.size() : 0;
        	LOG.debug("performReport, the size of the report: " + reportSize);
        }
        
        if( reportSet != null && reportSet.isEmpty() ) {
            GlobalVariables.getMessageMap().putError("startDate", KFSKeyConstants.ERROR_CUSTOM, "No Check Records Found");
        }
        else if( reportSet != null ) {
        	// Sort results by check number before building reports
        	Collections.sort(reportSet);
            
            if( "excel".equals(crForm.getFormat()) ) {
                response.setContentType("application/unknown");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Content-Disposition", "attachment; filename=checkreconreport.csv");

                java.io.PrintWriter out = response.getWriter();
            
                boolean newAccount = false;
                boolean newMonth   = true;
                double  acctAmtTotal = 0.00;
                double  monAmtTotal  = 0.00;
                int     monIssuedTotal  = 0;
                int     acctIssuedTotal = 0;
                
                CheckReconciliationReport temp = null;
                
                Iterator<CheckReconciliationReport> iter = reportSet.iterator();
                temp = iter.next();
                
                out.println("Check No,Payee Id,Payee Type,Payee Name,Status,Account No,Date,Amount");
                out.println(temp.getCheckNumber() + COMMA +temp.getPayeeId() + COMMA + temp.getPayeeType() +COMMA + "\""+temp.getPayeeName()+"\"" +COMMA+ temp.getStatusDesc() + COMMA + temp.getBankAccountNumber() + COMMA + temp.getCheckDate() + COMMA + temp.getAmount());
                
                String  tempAcct   = temp.getBankAccountNumber();
                String  tempMon    = temp.getCheckMonth();
                
                monAmtTotal     += temp.getSubTotal();
                acctAmtTotal    += temp.getSubTotal();
                monIssuedTotal  += temp.getIssued();
                acctIssuedTotal += temp.getIssued();
                
                while( iter.hasNext() ) {
                    temp = iter.next();

                    newMonth   = !tempMon.equals(temp.getCheckMonth());
                    newAccount = !tempAcct.equals(temp.getBankAccountNumber());

                    if( (newMonth || newAccount) ) {
                        out.println("Sub-total " + tempMon + COMMA + "Outstanding" + COMMA + monIssuedTotal + COMMA + COMMA + CheckReconciliationReport.DF.format(monAmtTotal));
                        monAmtTotal = 0.00;
                        monIssuedTotal  = 0;
                    }
                    if( newAccount ) {
                        out.println("Bank " + tempAcct + COMMA + "Total Outstanding" + COMMA + acctIssuedTotal + COMMA + COMMA + CheckReconciliationReport.DF.format(acctAmtTotal));
                        out.println(",,,,");
                        monAmtTotal = 0.00;
                        acctAmtTotal = 0.00;
                        monIssuedTotal  = 0;
                        acctIssuedTotal = 0;
                    }
                    
                    if( newMonth ) {
                        out.println("Check No,Payee Id,Payee Name,Payee Type,Status,Account No,Date,Amount");
                    }
                    
                    out.println(temp.getCheckNumber() +COMMA +temp.getPayeeId() + COMMA + "\""+temp.getPayeeName()+"\"" +COMMA + temp.getPayeeType() + COMMA + temp.getStatusDesc() + COMMA + temp.getBankAccountNumber() + COMMA + temp.getCheckDate() + COMMA + temp.getAmount());
                    
                    monAmtTotal     += temp.getSubTotal();
                    acctAmtTotal    += temp.getSubTotal();
                    monIssuedTotal  += temp.getIssued();
                    acctIssuedTotal += temp.getIssued();
                    
                    tempAcct = temp.getBankAccountNumber();
                    tempMon  = temp.getCheckMonth();
                }
            
                out.println("Sub-total " + tempMon + COMMA + "Outstanding" + COMMA + monIssuedTotal  + COMMA + COMMA + CheckReconciliationReport.DF.format(monAmtTotal));
                out.println("Bank " + tempAcct + COMMA + "Total Outstanding"+ COMMA + acctIssuedTotal + COMMA + COMMA + CheckReconciliationReport.DF.format(acctAmtTotal));
                
                out.close();
                return null;
            }
            else {
                // build pdf and stream back
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
                ResourceBundle resourceBundle = ResourceBundle.getBundle(CRConstants.REPORT_MESSAGES_CLASSPATH, Locale.getDefault());
                Map<String, Object> reportData = new HashMap<String, Object>();
                reportData.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
                reportData.put("REPORT_END_DATE", new SimpleDateFormat("MM/dd/yyyy").format(crForm.getEndDate()));
        
                SpringContext.getBean(ReportGenerationService.class).generateReportToOutputStream(reportData, reportSet, CRConstants.REPORT_TEMPLATE_CLASSPATH + CRConstants.REPORT_FILE_NAME, baos);
                WebUtils.saveMimeOutputStreamAsFile(response, ReportGeneration.PDF_MIME_TYPE, baos, CRConstants.REPORT_FILE_NAME + ReportGeneration.PDF_FILE_EXTENSION);
            
                return null;
            }
        }
        else {
            GlobalVariables.getMessageMap().putError("startDate", KFSKeyConstants.ERROR_CUSTOM, "No Check Records Found");
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward returnToIndex(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_CLOSE);
    }
}
