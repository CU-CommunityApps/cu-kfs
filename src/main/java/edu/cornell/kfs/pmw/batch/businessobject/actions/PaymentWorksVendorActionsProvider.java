package edu.cornell.kfs.pmw.batch.businessobject.actions;

import java.util.LinkedList;
import java.util.List;

import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.datadictionary.ActionsProvider;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorAuthorizationService;

public class PaymentWorksVendorActionsProvider implements ActionsProvider {

    private PaymentWorksVendorAuthorizationService paymentWorksVendorAuthorizationService;

    @Override
    public List<Action> getActionLinks(BusinessObjectBase businessObject, Person user) {
        final PaymentWorksVendor paymentWorksVendor = (PaymentWorksVendor) businessObject;
        final List<Action> actionLinks = new LinkedList<>();

        if (canRestageForPaymentWorksUpload(paymentWorksVendor, user)) {
            
        }

        return actionLinks;
    }

    private boolean canRestageForPaymentWorksUpload(final PaymentWorksVendor paymentWorksVendor, final Person user) {
        return paymentWorksVendorAuthorizationService.canRestageForPaymentWorksUpload(paymentWorksVendor, user);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getCreateUrl(final Class businessObjectClass) {
        return null;
    }

}
