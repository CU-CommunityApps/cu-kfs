package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.cemi.module.cg.CemiSponsorConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiSponsorFileSponsorTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

@SuppressWarnings("deprecation")
public class CemiSponsorFileSponsorTabRowBoFactory {
    
    private String jobRunDateString;
    private DateTimeService dateTimeService;
    private boolean maskSensitiveData = true;

    public CemiSponsorFileSponsorTabRowBoFactory (final String jobRunDateString, 
            final DateTimeService dateTimeService, final boolean maskSensitiveData) {
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null for CemiSponsorFileSponsorTabRowBoFactory.");
        Validate.validState(dateTimeService != null, "dateTimeService cannot be null for CemiSponsorFileSponsorTabRowBoFactory.");
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
    }
     
    public CemiSponsorFileSponsorTabRowBo createCemiSponsorFileSponsoreTabRowBo() {
        final CemiSponsorFileSponsorTabRowBo sponsorTabDataRow = new CemiSponsorFileSponsorTabRowBo();
        
//TODO FIXME add all the local variables to build out hte spreadsheet keys

        
        // Reference information related to business object being created that must be specified.
        // Both of these attributes are being set by abstract class CemiOrmDataBuilderBase when storeSheetRow is invoked.
        //      attribute jobRunRowIndex
        //      attribute jobRunDateString
//TODO FIXME add setting for all needed business object keys
        
        //Format and assign data values for these attributes as defined by the Huron mapping template specification.
        sponsorTabDataRow.setSpreadsheetKey("1-CGAgencyLoop_Counter");
        sponsorTabDataRow.setAddOnly(CemiBaseConstants.YES);
        sponsorTabDataRow.setSponsor(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setSponsorId("SPN_{CGAgencyNumber}");
        sponsorTabDataRow.setSponsorReferenceId("SPN_{CGAgencyNumber}");
        sponsorTabDataRow.setSponsorName("sponsor legal name");
        sponsorTabDataRow.setSponsorType("sponsor type");
        sponsorTabDataRow.setPaymentTerms(CemiSponsorConstants.NET_30_DAYS);
        sponsorTabDataRow.setDefaultPaymentType(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setBusinessEntityRowId(CemiSponsorConstants.NUMERIC_ONE);                 // template field name duplicated as rowId
        sponsorTabDataRow.setBusinessEntityName("sponsor legal name");
        sponsorTabDataRow.setBusinessEntityPhoneticName(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setBusinessEntityTaxId(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setExternalEntityId(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setAddressDataRowId("1-particularAddressForCGAgencyLoop_Counter");        // template field name duplicated as rowId
        sponsorTabDataRow.setAddressLineDataRowId("1or2-specificAddressLineForParticularAddressForCGAgencyLoop_Counter") ;  // template field name duplicated as rowId
        sponsorTabDataRow.setAddressLineDataDescriptor(CemiBaseConstants.EMPTY_STRING);             // template field name duplicated as descriptor
        sponsorTabDataRow.setAddressLineDataType(CemiBaseConstants.EMPTY_STRING);                   // template field name duplicated as type
        sponsorTabDataRow.setAddressLineData("AR_CUST_ADDR_T CUST_LINE_1_ADDR CUST_LINE_2_ADDR");
        sponsorTabDataRow.setMunicipality("AR_CUST_ADDR_T CUST_CITY_NM");
        sponsorTabDataRow.setCountryCity(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setIso31661Alpha2Code(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setSubmunicipalityDataRowId(CemiBaseConstants.EMPTY_STRING);              // template field name duplicated as rowId
        sponsorTabDataRow.setAddressComponentName(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setSubmunicipalityDataType(CemiBaseConstants.EMPTY_STRING);               // template field name duplicated as type
        sponsorTabDataRow.setSubmunicipalityData(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setCountryRegion("Need common CountryRegionID translate table created");
        sponsorTabDataRow.setCountryRegionDescriptor(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setSubRegionDataRowId(CemiBaseConstants.EMPTY_STRING);                    // template field name duplicated as rowId
        sponsorTabDataRow.setSubRegionDataDescriptor(CemiBaseConstants.EMPTY_STRING);               // template field name duplicated as descriptor
        sponsorTabDataRow.setSubregionDataType(CemiBaseConstants.EMPTY_STRING);                     // template field name duplicated as type
        sponsorTabDataRow.setSubregionData(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setPostalCode("AR_CUST_ADDT_T CUST_ZIP_CODE");

        sponsorTabDataRow.setAddressDataUsageDataRowId(CemiSponsorConstants.NUMERIC_ONE);           // template field name duplicated as rowId
        sponsorTabDataRow.setAddressDataUsageDataPublic(CemiBaseConstants.YES);                     // template field name duplicated as public
        sponsorTabDataRow.setAddressDataTypeDataRowId("Usage loop counter per address for address line");   // template field name duplicated as rowId
        sponsorTabDataRow.setAddressDataTypeDataPrimary("AR_CUST_ADDR_T CUST_ADDR_TYP_CD=P Make Y Else N"); // template field name duplicated as primary
        sponsorTabDataRow.setAddressDataTypeDataType("work-business-home-lockbox-academic_contact-institutional:: communicationUsageTypeID common crosswalk");  // template field name duplicated as type
        sponsorTabDataRow.setUseFor1(CemiSponsorConstants.USE_FOR_SHIPPING);
        sponsorTabDataRow.setUseFor2(CemiSponsorConstants.USE_FOR_BILLING);
        sponsorTabDataRow.setUseFor3(CemiSponsorConstants.USE_FOR_REMIT);
        sponsorTabDataRow.setUseForTenanted1(CemiSponsorConstants.USE_FOR_TENANTED_SHIPPING);
        sponsorTabDataRow.setUseForTenanted2(CemiSponsorConstants.USE_FOR_TENANTED_BILLING);
        sponsorTabDataRow.setUseForTenanted3(CemiSponsorConstants.USE_FOR_TENANTED_REMIT);
        sponsorTabDataRow.setAddressDataUsageDataComments(CemiBaseConstants.EMPTY_STRING);          // template field name duplicated as comments
        sponsorTabDataRow.setNumberOfDays(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setMunicipalityLocal(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setAddress(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setAddressId("ITH_{CustomerNumber}_{CustomerAddressId}");

        sponsorTabDataRow.setPhoneDataRowId("1-sequentialForEachPhoneNumberPerSponsor");            // template field name duplicated as rowId
        sponsorTabDataRow.setAreaCode(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setTenantFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setInternationalFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setPhoneNumberWithoutAreaCode(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setNationalFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setE164FormattedPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setWorkdayTraditionalFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setPhoneDataDelete(CemiBaseConstants.EMPTY_STRING);                       // template field name duplicated as delete
        sponsorTabDataRow.setPhoneDataDoNotReplaceAll(CemiBaseConstants.EMPTY_STRING);              // template field name duplicated as doNotReplaceAll
        sponsorTabDataRow.setCountryIsoCode("Use common Country translate table");
        sponsorTabDataRow.setInternationalPhoneCode("Need common InternationalPhoneCode translate table created");
        sponsorTabDataRow.setPhoneNumber("sponsor profile - phone");
        sponsorTabDataRow.setPhoneExtension("extract if available");
        sponsorTabDataRow.setPhoneDeviceType("ID value for:: lindline-mobile-fax-PHONE_DEVICE_TYPE-6-21");

        sponsorTabDataRow.setPhoneDataUsageDataRowId(CemiSponsorConstants.NUMERIC_ONE);             // template field name duplicated as rowId
        sponsorTabDataRow.setPhoneDataUsageDataPublic(CemiBaseConstants.YES);                       // template field name duplicated as public
        sponsorTabDataRow.setPhoneDataTypeDataRowId("Usage loop counter per address for phone line"); // template field name duplicated as rowId
        sponsorTabDataRow.setPhoneDataTypeDataPrimary("Y if primary-else N");                       // template field name duplicated as primary
        sponsorTabDataRow.setPhoneDataTypeDataType("work-business-home-lockbox-academic_contact-institutional"); // template field name duplicated as type
        sponsorTabDataRow.setPhoneDataUsageDataUseFor(CemiBaseConstants.EMPTY_STRING);              // template field name duplicated as useFor
        sponsorTabDataRow.setPhoneDataUsageDataUseForTenanted(CemiBaseConstants.EMPTY_STRING);      // template field name duplicated as useForTenanted
        sponsorTabDataRow.setPhoneDataUsageDataComments(CemiBaseConstants.EMPTY_STRING);            // template field name duplicated as comments
        sponsorTabDataRow.setPhone(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setPhoneDataId("{SponsorId}_Phone");                                      // template field name duplicated as id

        sponsorTabDataRow.setEmailAddressDataRowId(CemiSponsorConstants.NUMERIC_ONE);               // template field name duplicated as rowId
        sponsorTabDataRow.setEmailAddressDataDelete(CemiBaseConstants.EMPTY_STRING);                // template field name duplicated as delete
        sponsorTabDataRow.setEmailAddressDataDoNotReplaceAll(CemiBaseConstants.EMPTY_STRING);       // template field name duplicated as doNotReplaceAll
        sponsorTabDataRow.setEmailAddress("general email address");
        sponsorTabDataRow.setEmailComment(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setEmailAddressDataUsageDataRowId(CemiSponsorConstants.NUMERIC_ONE);      // template field name duplicated as rowId
        sponsorTabDataRow.setEmailAddressDataUsageDataPublic(CemiBaseConstants.YES);                // template field name duplicated as public
        sponsorTabDataRow.setEmailAddressDataTypeDataRowId(CemiSponsorConstants.NUMERIC_ONE);       // template field name duplicated as rowId
        sponsorTabDataRow.setEmailAddressDataTypeDataPrimary(CemiBaseConstants.YES);                // template field name duplicated as primary
        sponsorTabDataRow.setEmailAddressDataTypeDataType("work-business-home-lockbox-academic_contact-institutional"); // template field name duplicated as type
        sponsorTabDataRow.setEmailAddressDataUsageDataUseFor(CemiBaseConstants.EMPTY_STRING);       // template field name duplicated as useFor
        sponsorTabDataRow.setEmailAddressDataUsageDataUseForTenanted(CemiBaseConstants.EMPTY_STRING); // template field name duplicated as useForTenanted
        sponsorTabDataRow.setEmailAddressDataUsageDataComments(CemiBaseConstants.EMPTY_STRING);     // template field name duplicated as comments
        sponsorTabDataRow.setEmail(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setEmailAddressDataId("{SponsorId}_Email");                               // template field name duplicated as id

        sponsorTabDataRow.setWebAddressDataRowId(CemiBaseConstants.EMPTY_STRING);                   // template field name duplicated as rowId
        sponsorTabDataRow.setWebAddressDataDelete(CemiBaseConstants.EMPTY_STRING);                  // template field name duplicated as delete
        sponsorTabDataRow.setWebAddressDataDoNotReplaceAll(CemiBaseConstants.EMPTY_STRING);         // template field name duplicated as doNotReplaceAll
        sponsorTabDataRow.setWebAddressDataWebAddress("website URL");                               // template field name duplicated as webAddress
        sponsorTabDataRow.setWebAddressComment(CemiBaseConstants.EMPTY_STRING);

        sponsorTabDataRow.setWebAddressDataUsageDataRowId(CemiBaseConstants.EMPTY_STRING);          // template field name duplicated as rowId
        sponsorTabDataRow.setWebAddressDataUsageDataPublic(CemiBaseConstants.EMPTY_STRING);         // template field name duplicated as public
        sponsorTabDataRow.setWebAddressTypeDataRowId(CemiBaseConstants.EMPTY_STRING);               // template field name duplicated as rowId
        sponsorTabDataRow.setWebAddressTypeDataPrimary(CemiBaseConstants.EMPTY_STRING);             // template field name duplicated as primary
        sponsorTabDataRow.setWebAddressTypeDataType(CemiBaseConstants.EMPTY_STRING);                // template field name duplicated as type
        sponsorTabDataRow.setWebAddressDataUsageDataUseFor(CemiBaseConstants.EMPTY_STRING);         // template field name duplicated as useFor
        sponsorTabDataRow.setWebAddressDataUsageDataUseForTenanted(CemiBaseConstants.EMPTY_STRING); // template field name duplicated as useForTenanted
        sponsorTabDataRow.setWebAddressDataUsageDataComments(CemiBaseConstants.EMPTY_STRING);       // template field name duplicated as comments
        sponsorTabDataRow.setWebAddressDataUsageDataWebAddress(CemiBaseConstants.EMPTY_STRING);     // template field name duplicated as webAddress
        sponsorTabDataRow.setWebAddressDataUsageDataId(CemiBaseConstants.EMPTY_STRING);             // template field name duplicated as id
        sponsorTabDataRow.setFileName(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setImage(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setRemitFromSponsor(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setCurrency(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setAcceptedCurrencies(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setSponsorPreferredLocale(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setInvoiceDeliveryType(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setInvoiceNotificationEmailRecipients(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setStatementDeliveryType(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setStatementNotificationEmailRecipients(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setIncludedChildren("children sponsor IDs for sponsor being loaded");

        sponsorTabDataRow.setAlternateNameDataRowId(CemiBaseConstants.EMPTY_STRING);                // template field name duplicated as rowId
        sponsorTabDataRow.setAlternateName(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setAlternateNameUsage(CemiBaseConstants.EMPTY_STRING);
        sponsorTabDataRow.setReplaceAll(CemiBaseConstants.EMPTY_STRING);
        
        return sponsorTabDataRow;
    }
    
    
}
