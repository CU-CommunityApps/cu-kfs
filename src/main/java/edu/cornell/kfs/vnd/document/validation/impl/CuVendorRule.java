package edu.cornell.kfs.vnd.document.validation.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorCommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.service.CommodityCodeService;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;
import edu.cornell.kfs.vnd.CUVendorKeyConstants;
import edu.cornell.kfs.vnd.CUVendorPropertyConstants;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorCreditCardMerchant;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuVendorRule extends CuVendorRuleBase {
    private CommodityCodeService commodityCodeService = (CommodityCodeService)  GlobalResourceLoader.getService("commodityCodeService");

    protected boolean processCustomRouteDocumentBusinessRules(final MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        if (ObjectUtils.isNotNull(((VendorDetail)getNewBo()).getVendorHeader().getVendorType())) {
        	// in vendorrule newVendor is set to newBo
            valid &= validateB2BDefaultCommodityCode(document);
        }
      
        valid &= processCuAddressValidation(document);
		valid &= checkGeneralLiabilityAmountAndExpiration(document);
		valid &= checkAutoLiabilityAmountAndExpiration(document);
		valid &= checkWorkmansCompAmountAndExpiration(document);
		valid &= checkUmbrellaPolicyAmountAndExpiration(document);
		valid &= checkHealthLicenseAndExpiration(document);
		valid &= checkSupplierDiversityExpirationDate(document);
		valid &= checkInsuranceRequired(document);
        
        return valid;
    }

    public boolean processCustomAddCollectionLineBusinessRules(final MaintenanceDocument document, String collectionName,
            PersistableBusinessObject bo) {
        boolean success = super.processCustomAddCollectionLineBusinessRules(document, collectionName, bo);


        if (bo instanceof VendorAddress) {
            final VendorAddress address = (VendorAddress) bo;           
            final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
            final VendorHeader vendorHeader = vendorDetail.getVendorHeader();
//            String propertyConstant = KFSConstants.ADD_PREFIX + "." + VendorPropertyConstants.VENDOR_ADDRESS + ".";
            final String propertyConstant = "add.vendorAddresses.";
			if (StringUtils.isBlank(vendorHeader.getVendorTypeCode())) {
				success = false;
				putFieldError(VendorPropertyConstants.VENDOR_TYPE_CODE,CUVendorKeyConstants.ERROR_DOCUMENT_VENDOR_TYPE_IS_REQUIRED_FOR_ADD_VENDORADRESS);

			} else {
				vendorHeader.refreshReferenceObject(KFSPropertyConstants.VENDOR_TYPE_CODE);
				 success &= this.checkAddressMethodOfPOTransmissionAndData(vendorHeader.getVendorTypeCode(), vendorHeader.getVendorType().getVendorAddressTypeRequiredCode(),address, propertyConstant);
			}
        
        }
        if (bo instanceof CuVendorCreditCardMerchant) {
            final CuVendorCreditCardMerchant vendorMerchant = (CuVendorCreditCardMerchant) bo;
            final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
            success &=validateCreditCardMerchantAddition(vendorDetail, vendorMerchant);
        }
        if (bo instanceof VendorSupplierDiversity) {
            final VendorSupplierDiversity vendorSupplierDiversity = (VendorSupplierDiversity) bo;
            success &=validateSupplierDiversityAddition( vendorSupplierDiversity);
        }

        return success;
    }

    public boolean processAddCollectionLineBusinessRules(final MaintenanceDocument document, final String collectionName,
            final PersistableBusinessObject bo) {
        boolean success = super.processAddCollectionLineBusinessRules(document, collectionName, bo);
        if (collectionName.equals(VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES)) {
            final VendorDetail vendorDetail = (VendorDetail)document.getDocumentBusinessObject();
            final VendorHeader vendorHeader = vendorDetail.getVendorHeader();
            final List<VendorSupplierDiversity> vendorSupplierDiversities = vendorHeader.getVendorSupplierDiversities();
    		if (vendorSupplierDiversities.size() > 0)
    		{
    			int i = 0;
    			for(final VendorSupplierDiversity vendor : vendorSupplierDiversities) {
    				if (vendor.getCertificationExpirationDate() == null ) {
    					success = false;
    					putFieldError(VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "[" + i + "]." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK);
    				}
    				else if (vendor.getCertificationExpirationDate().before( new Date() ) ) {
    		            // Only check expiration date on new vendors
    		            if(document.isNew()) {
    		            	success = false;
    		            	putFieldError(VendorConstants.VENDOR_HEADER_ATTR + "."+VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "[" + i + "]." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST);
    		            }
    				}
    				i++;
    			}
    		}
    		if (!success) {
    			return success;
    		}
        }
        if (collectionName.equals("vendorCommodities")) {
            final VendorCommodityCode codeToBeValidated = (VendorCommodityCode) bo;
            final CommodityCode persistedCommodity = commodityCodeService.getByPrimaryId(codeToBeValidated.getPurchasingCommodityCode());			
			if (persistedCommodity == null) {
				// a commodity code entered by a user does not exist
				putFieldError("add.vendorCommodities.purchasingCommodityCode", CUVendorKeyConstants.ERROR_VENDOR_COMMODITY_CODE_DOES_NOT_EXIST, codeToBeValidated.getPurchasingCommodityCode());
				success = false;
			}
			if (codeToBeValidated.isCommodityDefaultIndicator()) {
			    final VendorDetail vendorDetail = (VendorDetail) document.getDocumentBusinessObject();
			    final List<VendorCommodityCode> vendorCommodities = vendorDetail.getVendorCommodities();
			    final Iterator<VendorCommodityCode> commodities = vendorCommodities.iterator();
				int indice = 0;
				while (commodities.hasNext()) {
				    final VendorCommodityCode commodity = (VendorCommodityCode) commodities.next();
					if (commodity.isCommodityDefaultIndicator()){
						// more than one "default" commodity code has been specified
						putFieldError("add.vendorCommodities.commodityDefaultIndicator", CUVendorKeyConstants.ERROR_DEFAULT_VENDOR_COMMODITY_CODE_ALREADY_EXISTS, Integer.toString(indice));
						success = false;
					}
					indice++;
				}
			}
            final VendorDetail vendorDetail = (VendorDetail)document.getDocumentBusinessObject();
            boolean commodityAlreadyAssignedToThisVendor = false;
            final Iterator<VendorCommodityCode> codes = vendorDetail.getVendorCommodities().iterator();
            while (codes.hasNext()){
                final VendorCommodityCode vcc = codes.next();
            	if (vcc.getPurchasingCommodityCode().equals(codeToBeValidated.getPurchasingCommodityCode())){
            		commodityAlreadyAssignedToThisVendor = true;
            		break;
            	}
            }
            if ( commodityAlreadyAssignedToThisVendor ) {
            	putFieldError("add.vendorCommodities.purchasingCommodityCode", CUVendorKeyConstants.ERROR_VENDOR_COMMODITY_CODE_ALREADY_ASSIGNED_TO_VENDOR);
            	success = false;
            }
			return success;
		}
        return success;
    }

    // CU enhancement related po transmission KFSUPGRADE-348
    // can't override processAddressValidation because it has no modifier, so only class/package can access it.
    boolean processCuAddressValidation(final MaintenanceDocument document) {
    	boolean valid = true;
        final VendorDetail newVendor = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();

        final List<VendorAddress> addresses = newVendor.getVendorAddresses();
        final String vendorTypeCode = newVendor.getVendorHeader().getVendorTypeCode();
        final String vendorAddressTypeRequiredCode = newVendor.getVendorHeader().getVendorType()
                .getVendorAddressTypeRequiredCode();
        verifyPOTransmissionTypeAllowedForVendorType(vendorTypeCode, addresses);
        for (int i = 0; i < addresses.size(); i++) {
            final VendorAddress address = addresses.get(i);
            final String errorPath = MAINTAINABLE_ERROR_PREFIX + VendorPropertyConstants.VENDOR_ADDRESS + "[" + i + "]";
            GlobalVariables.getMessageMap().clearErrorPath();
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);
            final String propertyName = VendorPropertyConstants.VENDOR_ADDRESS + "[" + i + "].";
            valid &= checkAddressMethodOfPOTransmissionAndData(vendorTypeCode, vendorAddressTypeRequiredCode, address, propertyName);

            GlobalVariables.getMessageMap().clearErrorPath();
        }


        return valid;
    }

	protected boolean checkGeneralLiabilityAmountAndExpiration(final MaintenanceDocument document) {
		boolean success = true;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorDetailExtension vendorDetailExt = ((VendorDetailExtension)vendorDetail.getExtension());
		
		if (vendorDetailExt.getGeneralLiabilityCoverageAmount()!=null && vendorDetailExt.getGeneralLiabilityExpiration()==null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.GENERAL_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_NEEDED);
		}
		if (vendorDetailExt.getGeneralLiabilityCoverageAmount()==null && vendorDetailExt.getGeneralLiabilityExpiration()!=null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.GENERAL_LIABILITY_AMOUNT, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_COVERAGE_NEEDED);
		}
		if (vendorDetailExt.getGeneralLiabilityExpiration()!= null && vendorDetailExt.getGeneralLiabilityExpiration().before(new Date())) {
			// Only check expiration date on new vendors
			if(document.isNew()) {
				success = false;
				putFieldError(CUVendorPropertyConstants.GENERAL_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST);
			}
		}
		return success;
	}
    
	protected boolean checkAutoLiabilityAmountAndExpiration(final MaintenanceDocument document) {
		boolean success = true;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorDetailExtension vendorDetailExt = ((VendorDetailExtension)vendorDetail.getExtension());

		if (vendorDetailExt.getAutomobileLiabilityCoverageAmount()!=null && vendorDetailExt.getAutomobileLiabilityExpiration()==null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.AUTOMOBILE_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_NEEDED);
		}
		if (vendorDetailExt.getAutomobileLiabilityCoverageAmount()==null && vendorDetailExt.getAutomobileLiabilityExpiration()!=null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.AUTOMOBILE_LIABILITY_AMOUNT, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_AUTO_COVERAGE_NEEDED);
		}		
		if (vendorDetailExt.getAutomobileLiabilityExpiration()!= null && vendorDetailExt.getAutomobileLiabilityExpiration().before(new Date())) {
			// Only check expiration date on new vendors
			if(document.isNew()) {
				success = false;
				putFieldError(CUVendorPropertyConstants.AUTOMOBILE_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST);
			}
		}		
		return success;
	}

	protected boolean checkWorkmansCompAmountAndExpiration(final MaintenanceDocument document) {
		boolean success = true;

		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorDetailExtension vendorDetailExt = ((VendorDetailExtension)vendorDetail.getExtension());

		if (vendorDetailExt.getWorkmansCompCoverageAmount()!=null && vendorDetailExt.getWorkmansCompExpiration()==null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.WORKMANS_COMP_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_WC_EXPR_NEEDED);
		}
		if (vendorDetailExt.getWorkmansCompCoverageAmount()==null && vendorDetailExt.getWorkmansCompExpiration()!=null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.WORKMANS_COMP_AMOUNT, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_WC_COVERAGE_NEEDED);
		}		
		if (vendorDetailExt.getWorkmansCompExpiration()!= null && vendorDetailExt.getWorkmansCompExpiration().before(new Date())) {
			// Only check expiration date on new vendors
			if(document.isNew()) {
				success = false;
				putFieldError(CUVendorPropertyConstants.WORKMANS_COMP_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST);
			}
		}		
		
		return success;
	}

	protected boolean checkUmbrellaPolicyAmountAndExpiration(final MaintenanceDocument document) {
		boolean success = true;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorDetailExtension vendorDetailExt = ((VendorDetailExtension)vendorDetail.getExtension());

		if (vendorDetailExt.getExcessLiabilityUmbrellaAmount()!=null && vendorDetailExt.getExcessLiabilityUmbExpiration()==null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.EXCESS_LIABILITY_UMBRELLA_AMOUNT, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_NEEDED);
		}
		if (vendorDetailExt.getExcessLiabilityUmbrellaAmount()==null && vendorDetailExt.getExcessLiabilityUmbExpiration()!=null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.EXCESS_LIABILITY_UMBRELLA_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_UMB_COVERAGE_NEEDED);
		}		
		if (vendorDetailExt.getExcessLiabilityUmbExpiration()!= null && vendorDetailExt.getExcessLiabilityUmbExpiration().before(new Date())) {
			// Only check expiration date on new vendors
			if(document.isNew()) {
				success = false;
				putFieldError(CUVendorPropertyConstants.EXCESS_LIABILITY_UMBRELLA_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST);
			}
		}
		
		return success;
	}

	protected boolean checkHealthLicenseAndExpiration(final MaintenanceDocument document) {
		boolean success = true;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();

		Boolean offSiteCateringLicenseRequired = ((VendorDetailExtension)vendorDetail.getExtension()).getHealthOffSiteCateringLicenseReq();
		
		if (offSiteCateringLicenseRequired == null) {
			offSiteCateringLicenseRequired = false;
		}
		
		if ( offSiteCateringLicenseRequired && ((VendorDetailExtension)vendorDetail.getExtension()).getHealthOffSiteLicenseExpirationDate()==null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.HEALTH_OFFSITE_LICENSE_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_NEEDED);
		}
		if ( !offSiteCateringLicenseRequired && ((VendorDetailExtension)vendorDetail.getExtension()).getHealthOffSiteLicenseExpirationDate()!=null) {
			success = false;
			putFieldError(CUVendorPropertyConstants.HEALTH_OFFSITE_LICENSE_REQUIRED, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_NEEDED);
		}				
		if (((VendorDetailExtension)vendorDetail.getExtension()).getHealthOffSiteLicenseExpirationDate()!= null && ((VendorDetailExtension)vendorDetail.getExtension()).getHealthOffSiteLicenseExpirationDate().before(new Date())) {
			// Only check expiration date on new vendors
			if(document.isNew()) {
				success = false;
				putFieldError(CUVendorPropertyConstants.HEALTH_OFFSITE_LICENSE_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST);
			}
		}
		
		return success;
	}

	protected boolean checkSupplierDiversityExpirationDate(final MaintenanceDocument document) {
		boolean success = true;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorHeader vendorHeader = vendorDetail.getVendorHeader();
		final List<VendorSupplierDiversity> vendorSupplierDiversities = vendorHeader.getVendorSupplierDiversities();
				
		if (vendorSupplierDiversities.size() > 0)
		{
			int i = 0;
			for(final VendorSupplierDiversity vendor : vendorSupplierDiversities) {
				if (vendor.getCertificationExpirationDate() == null ) {
					success = false;
					putFieldError(VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "[" + i + "]." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK);				
				}
				else if (vendor.getCertificationExpirationDate().before( new Date() ) ) {
					// Only check expiration date on new vendors
					if(document.isNew()) {
						success = false;
						putFieldError(VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "[" + i + "]." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST);
					}
				}
				i++;
			}
		}
		
		return success;
	}
	
	protected boolean checkInsuranceRequired(final MaintenanceDocument document) {
		boolean success = true;
		boolean dataEntered = false;
		
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		final VendorDetailExtension vendorDetailExt = ((VendorDetailExtension)vendorDetail.getExtension());
		if (vendorDetailExt.isInsuranceRequiredIndicator())
		{
			dataEntered |= (vendorDetailExt.getAutomobileLiabilityCoverageAmount()==null?false:true);
			dataEntered |= (vendorDetailExt.getAutomobileLiabilityExpiration()==null?false:true);
			dataEntered |= (vendorDetailExt.getCornellAdditionalInsuredIndicator()==null?false:true);
			dataEntered |= (vendorDetailExt.getExcessLiabilityUmbExpiration()==null?false:true);
			dataEntered |= (vendorDetailExt.getExcessLiabilityUmbrellaAmount()==null?false:true);
			dataEntered |= (vendorDetailExt.getGeneralLiabilityCoverageAmount()==null?false:true);
			dataEntered |= (vendorDetailExt.getGeneralLiabilityExpiration()==null?false:true);
			dataEntered |= (vendorDetailExt.getWorkmansCompCoverageAmount()==null?false:true);
			dataEntered |= (vendorDetailExt.getWorkmansCompExpiration()==null?false:true);
			dataEntered |= (vendorDetailExt.getHealthOffSiteCateringLicenseReq()==null?false:true);
			dataEntered |= (vendorDetailExt.getHealthOffSiteLicenseExpirationDate()==null?false:true);
			dataEntered |= (vendorDetailExt.getInsuranceNotes()==null?false:true);

			if (!dataEntered) {
				putFieldError(CUVendorPropertyConstants.INSURANCE_REQUIRED, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_INSURANCE_REQUIRED_USED_WO_DATA);
				return false;
			}
		}		
		
		return success;
	}
	
	protected boolean checkMerchantNameUniqueness(final MaintenanceDocument document) {
	   boolean success = true;
	   final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
       final List<CuVendorCreditCardMerchant> vendorCreditCardMerchants = ((VendorDetailExtension)vendorDetail.getExtension()).getVendorCreditCardMerchants();
       final ArrayList<String> merchantNames = new ArrayList<String>();
       int i = 0;
       for (final CuVendorCreditCardMerchant vendorCreditCardMerchant : vendorCreditCardMerchants) {
           if (merchantNames.contains(vendorCreditCardMerchant.getCreditMerchantName())) {
               putFieldError("vendorCreditCardMerchants[" + i + "].creditMerchantName", CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_DUPLICATE);
               //can't have duplicate merchant names, part of the primary key for the table in the db
               success = false;
           }
           if (vendorCreditCardMerchant.getCreditMerchantName()==null || vendorCreditCardMerchant.getCreditMerchantName().equals("")) {
               //can't have a null or blank name, it's part of the primary key for this table in the db
               putFieldError("vendorCreditCardMerchants[" + i + "].creditMerchantName", CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_BLANK);
               success = false;
           }
           merchantNames.add(vendorCreditCardMerchant.getCreditMerchantName());
           i++;
       }
       
	   return success;
	}
	
	protected boolean validateCreditCardMerchantAddition(final VendorDetail vendorDetail, final CuVendorCreditCardMerchant vendorCreditCardMerchant) {
	    boolean success = true;
	    final List<CuVendorCreditCardMerchant> vendorCreditCardMerchants = ((VendorDetailExtension)vendorDetail.getExtension()).getVendorCreditCardMerchants();
	    for (final CuVendorCreditCardMerchant existingMerchant : vendorCreditCardMerchants) {
	        if (existingMerchant.getCreditMerchantName().equals(vendorCreditCardMerchant.getCreditMerchantName())) {
	               putFieldError("add.vendorCreditCardMerchants.creditMerchantName", CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_DUPLICATE);
	               //can't have duplicate merchant names, part of the primary key for the table in the db
	               success = false;
	        }
	    }
	    return success;
	}
	
	protected boolean validateSupplierDiversityAddition(final VendorSupplierDiversity vendorSupplierDiversity) {
	    boolean success = true;
	    if (vendorSupplierDiversity.getCertificationExpirationDate() == null) {
	    	success = false;
            putFieldError("add." + VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK);
            return success;
	    }
        if (vendorSupplierDiversity.getCertificationExpirationDate().before( new Date() ) ) {
            success = false;
            putFieldError("add." + VendorConstants.VENDOR_HEADER_ATTR + "." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST);
            return success;
        }

	    return success;
	}
	
	protected boolean validateB2BDefaultCommodityCode(final MaintenanceDocument document) {
		final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		boolean success = true;
		final List<VendorContract> vendorContracts = vendorDetail.getVendorContracts();
		final List<VendorCommodityCode> vendorCommodities = vendorDetail.getVendorCommodities();
		final Iterator<VendorContract> it = vendorContracts.iterator();
		boolean isB2b = false;
		while (it.hasNext()) {
		    final VendorContract contract = (VendorContract) it.next();
			if (contract.getVendorB2bIndicator()) {
				isB2b = true;
				break;
			}
		}
		if (isB2b) {
			if (vendorCommodities.size()==0) {
				success = false;
				// no vendor commodities exist
				putFieldError("vendorCommodities", CUVendorKeyConstants.ERROR_VENDOR_COMMODITY_CODE_DEFAULT_IS_REQUIRED_FOR_B2B);
				return success;
			}
			boolean defaultCommodityCodeSpecified = false;
			final Iterator<VendorCommodityCode> commodities = vendorCommodities.iterator();
			int indice = 0;
			while (commodities.hasNext()) {
			    final VendorCommodityCode commodity = (VendorCommodityCode) commodities.next();
				if (commodity.isCommodityDefaultIndicator() && !defaultCommodityCodeSpecified) {
					defaultCommodityCodeSpecified = true;
				} else if (commodity.isCommodityDefaultIndicator()){
					// more than one "default" commodity code has been specified
					putFieldError("vendorCommodities[" + indice + "].commodityDefaultIndicator", CUVendorKeyConstants.ERROR_DEFAULT_VENDOR_COMMODITY_CODE_ALREADY_EXISTS);
					success = false;
				}
				final CommodityCode persistedCommodity = commodityCodeService.getByPrimaryId(commodity.getPurchasingCommodityCode());
				if (persistedCommodity == null) {
					// a commodity code entered by a user does not exist
					putFieldError("vendorCommodities[" + indice + "].purchasingCommodityCode", CUVendorKeyConstants.ERROR_VENDOR_COMMODITY_CODE_DOES_NOT_EXIST, commodity.getPurchasingCommodityCode());
					success = false;
				}
				indice++;
			}
			if (!defaultCommodityCodeSpecified) {
				// no default commodity code has been specified and the vendor has a b2b contract
				putFieldError("vendorCommodities", CUVendorKeyConstants.ERROR_VENDOR_COMMODITY_CODE_DEFAULT_IS_REQUIRED_FOR_B2B);
				success = false;
			}
		}
		
		return success;
	}
	
	
	/**
	 * Method verifies that only vendors of type purchase order have method of PO transmission set for their address type.
	 */
	private void verifyPOTransmissionTypeAllowedForVendorType(final String vendorTypeCode, final List <VendorAddress> addresses) {
		
        //Check that there is at least one PO Address Type specified when the Vendor Type is PO               
        if ( (!StringUtils.isBlank(vendorTypeCode)) && StringUtils.equals(vendorTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) {        
        	boolean poAddressExistsForPOVendor = false;
        	int i = 0;
        	while ( (i < addresses.size()) && !poAddressExistsForPOVendor) {        		
        	    final VendorAddress address = addresses.get(i);
            	if ( (!StringUtils.isBlank(address.getVendorAddressTypeCode())) && (StringUtils.equals(address.getVendorAddressTypeCode(), PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) ){
            		poAddressExistsForPOVendor = true;
            	}
            	i++;
            }
        	if (!poAddressExistsForPOVendor) {
            	//display error message
        		putFieldError(VendorPropertyConstants.VENDOR_TYPE_CODE, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_PO_ADDRESS);
            }
        }
        
        //Check that there are no PO Transmission Method values set when the Vendor Type is not PO               
        if ( (!StringUtils.isBlank(vendorTypeCode)) && (!StringUtils.equals(vendorTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT))) {        
        	boolean poTransmissionMethodExistsForVendor = false;
        	int i = 0;
        	while ( (i < addresses.size()) && !poTransmissionMethodExistsForVendor) {        		
        	    final VendorAddress address = addresses.get(i);           	
            	if ( (!StringUtils.isBlank(((CuVendorAddressExtension)address.getExtension()).getPurchaseOrderTransmissionMethodCode())) && (!StringUtils.equals(((CuVendorAddressExtension)address.getExtension()).getPurchaseOrderTransmissionMethodCode(), PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) ){
            		poTransmissionMethodExistsForVendor = true;
            	}
            	i++;
            }
        	if (poTransmissionMethodExistsForVendor) {
            	//display error message
        		putFieldError(VendorPropertyConstants.VENDOR_TYPE_CODE, CUVendorKeyConstants.ERROR_PO_TRANMSISSION_NOT_ALLOWED_FOR_VENDOR_TYPE);
            }
        }
	}


	/**
	 * Method verifies that the vendor data is valid for the association between vendor type, required vendor address type, address type,
	 * method of PO transmission assigned to an address, and all of the data required for the method of PO transmission selected.
	 */
	private boolean checkAddressMethodOfPOTransmissionAndData (final String vendorTypeCode, final String vendorAddressTypeRequiredCode, final VendorAddress address, final String propertyConstant) {
		boolean success = true;
		
		final String methodOfPOTransmission = ((CuVendorAddressExtension)address.getExtension()).getPurchaseOrderTransmissionMethodCode();
		
		success &= validateVendorTypeToAddressType(methodOfPOTransmission, vendorTypeCode, vendorAddressTypeRequiredCode, address.getVendorAddressTypeCode(), propertyConstant);
		
		//address type matches required address type for vendor type, now check data for method of PO transmission
		if (success) {	        
	        //Now verify the data for the address type specified
			// TODO : following validation is not ready till purap service "PurchaseOrderTransmissionMethodDataRulesService" is available
			// uncomment following when it is ready
	        success &= validateVendorAddressForMethodOfPOTransmission(address, propertyConstant);
		}

		return success;
	}
	
	/**
	 * This routine ensures that there is a Method of PO Transmission selected for a Vendor Address when an Address Type of PO is specified for a Vendor Type of PO.
	 */
	private boolean validateVendorTypeToAddressType(final String methodOfPOTransmission, final String vendorTypeCode, final String vendorAddressTypeRequiredCode, final String addressTypeCode, final String propertyScope){
		 boolean valid = true;		
	     //(vendorTypeCode = PO) && (vendorAddressTypeRequiredCode = PO) && (addressTypeCode = PO) && (methodOfPOTransmission is blank ) == error
		 if ( (StringUtils.equals(vendorTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) && 
		      (StringUtils.equals(vendorAddressTypeRequiredCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) && 
		      (StringUtils.equals(addressTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) && 
		      ((methodOfPOTransmission == null) || (StringUtils.isBlank(methodOfPOTransmission))) ) {
			 
				 //User selected address type of PO but left method of PO transmission blank
				 final String propertyName = propertyScope + CUVendorPropertyConstants.VENDOR_ADDRESS_METHOD_OF_PO_TRANSMISSION;
				 final String[] parameters = { addressTypeCode };         
		         putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_PO_ADDRESS, parameters);
				 valid &= false;
		 }
		 else if ( (!StringUtils.equals(addressTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT)) && 
				   (StringUtils.isNotBlank(methodOfPOTransmission)) ) {
			     //User selected address type of not PO and a Method of PO transmission value is selected
				 final String propertyName = propertyScope + CUVendorPropertyConstants.VENDOR_ADDRESS_METHOD_OF_PO_TRANSMISSION;         
		         putFieldError(propertyName, CUVendorKeyConstants.ERROR_NO_PO_TRANSMISSION_WITH_NON_PO_ADDRESS);
				 valid &= false;
		 }
		 else {
			 //noop
		 } 
		 return valid;
	}

	/**
	 * This routine ensures that there is a Payment Terms value set when the Vendor is a PO type
	 */
	private boolean checkPOHasPaymentTerms(final MaintenanceDocument document) {
		 boolean valid = true;
         final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
         final String paymentTermsCode = vendorDetail.getVendorPaymentTermsCode();
         final String vendorTypeCode = vendorDetail.getVendorHeader().getVendorTypeCode();

	     //(vendorTypeCode = PO) && (payment terms is blank ) == error
		 if ( StringUtils.equalsIgnoreCase(vendorTypeCode, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT) &&
		      StringUtils.isBlank(paymentTermsCode) ) {
             putFieldError(CUVendorPropertyConstants.VENDOR_PAYMENT_TERMS, CUVendorKeyConstants.ERROR_PO_VENDOR_REQUIRES_PAYMENT_TERMS);
			 valid = false;
		 }

		 return valid;
	}

	/**
	 * This routine verifies that the data necessary for the Method of PO Transmission chosen exists and that data
	 * is in the proper format on the VendorAddress section of the Vendor maintenance document.  
	 * If the data does not exist or is not in the proper format, the field(s) in question on that maintenance
	 * document is/are noted as being in error.
	 * 
	 * NOTE: This method call the same rule checks (PurchaseOrderTransmissionMethodDataRulesService)
	 * for each data element on this maintenance document as the REQ, PO, and POA.  
	 * 
	 */
	private boolean validateVendorAddressForMethodOfPOTransmission(final VendorAddress address, String propertyScope){
		boolean valid = true;
		String propertyName;	
		String [] parameters;
		final String transmissionMethod = ((CuVendorAddressExtension)address.getExtension()).getPurchaseOrderTransmissionMethodCode();
		final String addressTypeCode = address.getVendorAddressTypeCode();
		final PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService = SpringContext.getBean(PurchaseOrderTransmissionMethodDataRulesService.class);
		
		if  ( (transmissionMethod == null) || (StringUtils.isBlank(transmissionMethod)) ) {
		//no-op, nothing to verify, default return value is valid
		}
		else if ( (StringUtils.isBlank(addressTypeCode)) || (transmissionMethod.equalsIgnoreCase(CUPurapConstants.POTransmissionMethods.CONVERSION)) ) {
		//no-op, nothing to verify, default return value is valid
	    }
		else if (transmissionMethod.equalsIgnoreCase(PurapConstants.POTransmissionMethods.FAX)) {
        	//requires fax number is entered in format (xxx-xxx-xxxx)  
			if (!purchaseOrderTransmissionMethodDataRulesService.isFaxNumberValid(address.getVendorFaxNumber())) {
            	propertyName = new String ( propertyScope + VendorPropertyConstants.VENDOR_FAX_NUMBER);
                putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_FAX_NUMBER);
                valid &= false;
			}
        }
        else if (transmissionMethod.equalsIgnoreCase(CUPurapConstants.POTransmissionMethods.EMAIL)) {
        	//requires an email address is entered
        	if (!purchaseOrderTransmissionMethodDataRulesService.isEmailAddressValid(address.getVendorAddressEmailAddress())) {
        		propertyName = new String ( propertyScope + CUVendorPropertyConstants.VENDOR_ADDRESS_EMAIL_ADDRESS);
                putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_EMAIL);
                valid &= false;        		        		
        	}        	
        }
        else if (transmissionMethod.equalsIgnoreCase(CUPurapConstants.POTransmissionMethods.MANUAL)) {
        	//requires a US postal address is entered (address1, city, state, postal code, and country)
        	//has all the required data been entered?
        	if ( !purchaseOrderTransmissionMethodDataRulesService.isCountryCodeValid(address.getVendorCountryCode()) ) {
	    		propertyName = new String ( propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_COUNTRY);
	    		putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);
	    		valid &= false;
	    	}
	        if ( !purchaseOrderTransmissionMethodDataRulesService.isStateCodeValid(address.getVendorStateCode()) ) {
	    		propertyName = new String ( propertyScope + CUVendorPropertyConstants.VENDOR_ADDRESS_STATE_CODE);
	    		putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);               
	    		valid &= false;        		
	    	}
	    	if ( !purchaseOrderTransmissionMethodDataRulesService.isZipCodeValid(address.getVendorZipCode()) ) {
	    		propertyName = new String ( propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_ZIP);
	    		putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);
	    		valid &= false;         		
	    	}
	    	if ( !purchaseOrderTransmissionMethodDataRulesService.isAddress1Valid(address.getVendorLine1Address()) ) {
	    		propertyName = new String ( propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_LINE_1);
	    		putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);
	    		valid &= false;
	    	}
	    	if ( !purchaseOrderTransmissionMethodDataRulesService.isCityValid(address.getVendorCityName()) ) {
	    		propertyName = new String ( propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_CITY);
	    		putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);
	    		valid &= false;
	    	}
	    	
	    	if (valid) {
	    		//user entered all the data, now verify relationships in the data	    		
	    		//verify US, state and zip are valid as they are related to each other, error message taken care of in called routine
	    		valid &= getPostalCodeValidationService().validateAddress(address.getVendorCountryCode(), address.getVendorStateCode(), address.getVendorZipCode(), propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_STATE, propertyScope + VendorPropertyConstants.VENDOR_ADDRESS_ZIP);
	    	}        	
        	
        }
        else {
        	//Method of PO Transmission maintenance table has a value that is not coded for processing
        	parameters = new String[] { transmissionMethod }; 
        	propertyName = new String ( propertyScope + CUVendorPropertyConstants.VENDOR_ADDRESS_METHOD_OF_PO_TRANSMISSION);
        	putFieldError(propertyName, CUVendorKeyConstants.ERROR_PO_TRANSMISSION_METHOD_UNKNOWN, parameters);
        	valid &= false;
        }	        
		return valid;
	 }

	@Override
	protected boolean processCustomApproveDocumentBusinessRules(
			final MaintenanceDocument document) {
        boolean valid = super.processCustomApproveDocumentBusinessRules(document);
        valid &= processCuAddressValidation(document);
        valid &= checkPOHasPaymentTerms(document);
		return valid;
	}

}
