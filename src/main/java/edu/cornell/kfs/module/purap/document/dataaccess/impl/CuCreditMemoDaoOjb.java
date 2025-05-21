package edu.cornell.kfs.module.purap.document.dataaccess.impl;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.dataaccess.impl.CreditMemoDaoOjb;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;

@SuppressWarnings("unchecked")
public class CuCreditMemoDaoOjb extends CreditMemoDaoOjb {
    private static final Logger LOG = LogManager.getLogger();


    private DataDictionaryService dataDictionaryService;
    
    @Override
    public List<VendorCreditMemoDocument> getCreditMemosToExtract(final String campusCode) {
        LOG.debug("getCreditMemosToExtract() started");

        final Criteria criteria = new Criteria();
        criteria.addEqualTo("processingCampusCode", campusCode);
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);
        criteria.addEqualTo("paymentMethodCode", "P");
        
        return (List<VendorCreditMemoDocument>)getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(creditMemoDocumentClass(), criteria));
    }
    
    @Override
    public Collection<VendorCreditMemoDocument> getCreditMemosToExtractByVendor(
            final String campusCode, final VendorGroupingHelper vendor ) {
        LOG.debug("getCreditMemosToExtractByVendor() started");

        final Criteria criteria = new Criteria();
        criteria.addEqualTo( "processingCampusCode", campusCode );
        criteria.addIsNull( "extractedTimestamp" );
        criteria.addEqualTo( "holdIndicator", Boolean.FALSE );
        // Cornell customization
        criteria.addEqualTo("paymentMethodCode", "P");
        
        criteria.addEqualTo( "vendorHeaderGeneratedIdentifier", vendor.getVendorHeaderGeneratedIdentifier() );
        criteria.addEqualTo( "vendorDetailAssignedIdentifier", vendor.getVendorDetailAssignedIdentifier() );
        criteria.addEqualTo( "vendorCountryCode", vendor.getVendorCountry() );
        if (vendor.getVendorPostalCode() == null) {
            criteria.addIsNull("vendorPostalCode");
        } else {
            criteria.addLike("vendorPostalCode", vendor.getVendorPostalCode() + "%");
        }

        return (List<VendorCreditMemoDocument>)getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(creditMemoDocumentClass(), criteria));
    }
    
    protected Class<? extends Document> creditMemoDocumentClass() {
        final Class<? extends Document> creditMemoDocumentClass =
                dataDictionaryService.getDocumentClassByTypeName(PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT);
        return creditMemoDocumentClass != null ? creditMemoDocumentClass : CuVendorCreditMemoDocument.class;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
