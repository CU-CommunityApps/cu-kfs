package edu.cornell.kfs.pdp.web.struts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.FormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.FormatSelection;
import org.kuali.kfs.pdp.service.FormatService;
import org.kuali.kfs.pdp.web.struts.FormatAction;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.service.CuFormatService;

public class CuFormatAction extends FormatAction {

    public CuFormatAction() {
    	super();
    }
    
    @Override
    public ActionForward start(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CuFormatForm formatForm = (CuFormatForm) form;
        
        final Person kualiUser = GlobalVariables.getUserSession().getPerson();
        final FormatSelection formatSelection = formatService.getDataForFormat(kualiUser);

        formatForm.setCampus(kualiUser.getCampusCode());

        // no data for format because another format process is already running
        if (formatSelection.getStartDate() != null) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.Format.ERROR_PDP_FORMAT_PROCESS_ALREADY_RUNNING, 
                    getDateTimeService().toDateTimeString(formatSelection.getStartDate()));
        } else {
            final List<CustomerProfile> customers = formatSelection.getCustomerList();

            for (final CustomerProfile element : customers) {
                if (formatSelection.getCampus().equals(element.getFormatCampusCode())) {
                    element.setSelectedForFormat(Boolean.TRUE);
                } else {
                    element.setSelectedForFormat(Boolean.FALSE);
                }
            }

            formatForm.setPaymentDate(getDateTimeService().toDateString(getDateTimeService().getCurrentTimestamp()));
            formatForm.setPaymentTypes(PdpConstants.PaymentTypes.ALL);
            formatForm.setPaymentDistribution(CUPdpConstants.PaymentDistributions.PROCESS_ALL);
            formatForm.setCustomers(customers);
            formatForm.setRanges(formatSelection.getRangeList());
        }
        
        return mapping.findForward(PdpConstants.MAPPING_SELECTION);
    }
   
    @Override
    public ActionForward prepare(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CuFormatForm formatForm = (CuFormatForm) form;
    
        if (formatForm.getCampus() == null) {
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }
    
        // Figure out which ones they have selected
        final List<CustomerProfile> selectedCustomers = new ArrayList<>();
    
        for (final CustomerProfile customer : formatForm.getCustomers()) {
            if (customer.isSelectedForFormat()) {
                selectedCustomers.add(customer);
            }
        }
    
        final Date paymentDate = getDateTimeService().convertToSqlDate(formatForm.getPaymentDate());
        final Person kualiUser = GlobalVariables.getUserSession().getPerson();
    
        final FormatProcessSummary formatProcessSummary = ((CuFormatService) formatService).startFormatProcess(kualiUser, formatForm.getCampus(), 
                selectedCustomers, paymentDate, formatForm.getPaymentTypes(), formatForm.getPaymentDistribution());
        if (formatProcessSummary.getProcessSummaryList().size() == 0) {
            KNSGlobalVariables.getMessageList().add(PdpKeyConstants.Format.ERROR_PDP_NO_MATCHING_PAYMENT_FOR_FORMAT);
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }
    
        formatForm.setFormatProcessSummary(formatProcessSummary);
    
        return mapping.findForward(PdpConstants.MAPPING_CONTINUE);
    }
}
