package edu.cornell.kfs.tax.service.impl;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.tax.CUTaxPropertyConstants;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.service.TransactionOverrideService;

public class TransactionOverrideServiceImpl implements TransactionOverrideService {

    private CriteriaLookupService criteriaLookupService;

    @Override
    public List<TransactionOverride> getTransactionOverrides(final String taxType, final java.sql.Date startDate,
            final java.sql.Date endDate) {
        Validate.notBlank(taxType, "taxType cannot be blank");
        Validate.notNull(startDate, "startDate cannot be null");
        Validate.notNull(endDate, "endDate cannot be null");
        
        return criteriaLookupService.lookup(TransactionOverride.class, QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(CUTaxPropertyConstants.TAX_TYPE, taxType),
                PredicateFactory.greaterThanOrEqual(KFSPropertyConstants.UNIVERSITY_DATE, startDate),
                PredicateFactory.lessThanOrEqual(KFSPropertyConstants.UNIVERSITY_DATE, endDate),
                PredicateFactory.equal(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR)
        )).getResults();
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
