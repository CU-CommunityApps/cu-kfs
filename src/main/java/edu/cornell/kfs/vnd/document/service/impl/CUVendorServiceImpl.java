package edu.cornell.kfs.vnd.document.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.businessobject.lookup.VendorLookupableHelperServiceImpl;
import org.kuali.kfs.vnd.document.service.impl.VendorServiceImpl;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.vnd.CUVendorPropertyConstants;
import edu.cornell.kfs.vnd.document.service.CUVendorService;

@Transactional
public class CUVendorServiceImpl extends VendorServiceImpl implements CUVendorService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private VendorLookupableHelperServiceImpl vendorLookupableHelperServiceImpl;
    protected DocumentService documentService;

    public VendorDetail getVendorByVendorName(final String vendorName) {
        LOG.info("Entering getVendorByVendorName for vendorName:" + vendorName);
        final Map criteria = new HashMap();
        criteria.put(VendorPropertyConstants.VENDOR_NAME, vendorName);
        final List<VendorDetail> vds = (List) businessObjectService.findMatching(VendorDetail.class, criteria);
        LOG.debug("Exiting getVendorByVendorName.");
        if (vds.size() < 1) {
            return null;
        }
        else {
            return vds.get(0);
        }
    }

    public VendorDetail getVendorByNamePlusLastFourOfTaxID(final String vendorName, final String lastFour) {
        LOG.debug("Entering getVendorByNamePlusLastFourOfTaxID for vendorName:" + vendorName + ", last four :" + lastFour);

//        Map criteria = new HashMap();
//        criteria.put(VendorPropertyConstants.VENDOR_NAME, vendorName);
//        List<VendorDetail> vds = (List) businessObjectService.findMatching(VendorDetail.class, criteria);
        final HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put(VendorPropertyConstants.VENDOR_NAME, "*"+vendorName+"*");
        vendorLookupableHelperServiceImpl.setBusinessObjectClass(VendorDetail.class);
        final List<VendorDetail> vds = (List) vendorLookupableHelperServiceImpl.getSearchResults(attributes);
        
        final List<VendorDetail> matches = new ArrayList<VendorDetail>();
        
        for(final VendorDetail detail : vds) {
            final String taxId = detail.getVendorHeader().getVendorTaxNumber();
        	if(StringUtils.isNotBlank(taxId)) {
        	    final String compareTo = taxId.substring(taxId.length()-4);
        		if(StringUtils.equals(lastFour, compareTo)) {
        			matches.add(detail);
        		}
        	}
        }
        
        LOG.debug("Exiting getVendorByNamePlusLastFourOfTaxID.");

        // TODO : not sure about this.  vendor may have division and have the same tax id ?
        // if > 1, then should we also return matches.get(0) ?
        
        if(matches.size() > 1 || matches.isEmpty()) {
        	return null;
        }
        
        return matches.get(0);
    }
    
    public void setBusinessObjectService(final BusinessObjectService boService) {
        this.businessObjectService = boService;
        // TODO : FIX need this to set up super, other wise boservice will be null in vendorserviceimpl
        super.setBusinessObjectService(boService);
    }

    public void setVendorLookupableHelperServiceImpl(final VendorLookupableHelperServiceImpl vendorLookupableHelperServiceImpl) {
        this.vendorLookupableHelperServiceImpl = vendorLookupableHelperServiceImpl;
    }

    public VendorHeader getVendorByEin(final String vendorEin) {
        LOG.info("Entering getVendorByEin f:" );
        final Map criteria = new HashMap();
        criteria.put(CUVendorPropertyConstants.VENDOR_TAX_NUMBER_ONLY, vendorEin);
        criteria.put("VNDR_TAX_TYP_CD", "FEIN");
        final List<VendorHeader> vds = (List) businessObjectService.findMatching(VendorHeader.class, criteria);
        LOG.debug("Exiting getVendorByEin.");
        if (vds.size() < 1) {
            return null;
        }
        else {
            return vds.get(0);
        }
    }

    //TODO UPGRADE-911
    public VendorAddress getVendorDefaultAddress(final List<VendorAddress> addresses,
            final String addressType, final String campus) {
      return null;
    }


    private List<VendorRoutingComparable> convertToRountingComparable(final List<? extends PersistableBusinessObjectBase> vendorCollection) {
        final List<VendorRoutingComparable> retList = new ArrayList<VendorRoutingComparable>();
        if (CollectionUtils.isNotEmpty(vendorCollection)) {
            for (final PersistableBusinessObjectBase pbo : vendorCollection) {
                retList.add((VendorRoutingComparable)pbo);                
            }
        }
        return retList;
    }
   
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
        super.setDocumentService(documentService);
   }
}

