package edu.cornell.kfs.pmw.batch.businessobject.admin;

import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.businessobject.admin.DefaultBoAdminService;

public class PaymentWorksVendorAdminService extends DefaultBoAdminService {

    @Override
    public boolean allowsNew(final Class<? extends BusinessObjectBase> businessObjectClass, final Person person) {
        return false;
    }

    @Override
    public boolean allowsEdit(final BusinessObjectBase businessObject, final Person person) {
        return false;
    }

    @Override
    public boolean allowsCopy(final BusinessObjectBase businessObject, final Person person) {
        return false;
    }

    @Override
    public boolean allowsCreate(final Class<? extends BusinessObjectBase> businessObjectClass, final Person person) {
        return false;
    }

}
