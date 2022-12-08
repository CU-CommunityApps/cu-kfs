package edu.cornell.kfs.pdp.service;

import java.util.Date;
import java.util.List;

import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.FormatProcessSummary;
import org.kuali.kfs.pdp.service.FormatService;
import org.kuali.kfs.kim.impl.identity.Person;

public interface CuFormatService extends FormatService {
    public FormatProcessSummary startFormatProcess(Person user, String campus, List<CustomerProfile> customers, Date paydate, 
            String paymentTypes, String paymentDistribution);

}
