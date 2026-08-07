package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
//import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactFileEntityContactTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiEntityContactFileExtractDataBuilder;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiEntityContactEmailBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiEntityContactFileEntityContactTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiEntityContactHeaderBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiEntityContactPhoneBoFactory;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiEntityContactExtractDao;

public class CemiEntityContactFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiEntityContactFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    private String supplierJobRunDateString;
    private CemiEntityContactExtractDao cemiEntityContactExtractDao;
    private boolean maskSensitiveData;
    private Comparator<VendorContactPhoneNumber> phoneNumberComparator;

    public CemiEntityContactFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final String supplierJobRunDateString, final CemiEntityContactExtractDao cemiEntityContactExtractDao,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiEntityContactFileEntityContactTabRowBo.class);
        Validate.notBlank(supplierJobRunDateString, "supplierJobRunDateString cannot be blank");
        Validate.notNull(cemiEntityContactExtractDao, "cemiEntityContactExtractDao cannot be null");
        this.supplierJobRunDateString = supplierJobRunDateString;
        this.cemiEntityContactExtractDao = cemiEntityContactExtractDao;
        this.maskSensitiveData = maskSensitiveData;
        this.phoneNumberComparator = createPhoneNumberComparator();
    }

    private Comparator<VendorContactPhoneNumber> createPhoneNumberComparator() {
        return Comparator.comparing(VendorContactPhoneNumber::getVendorPhoneTypeCode, this::comparePhoneTypes)
                .thenComparing(VendorContactPhoneNumber::getVendorContactPhoneGeneratedIdentifier);
    }

    private int comparePhoneTypes(final String phoneType1, final String phoneType2) {
        if (Strings.CS.equals(phoneType1, CemiEntityContactConstants.KFS_MAIN_PHONE_NUMBER_TYPE)) {
            return Strings.CS.equals(phoneType2, CemiEntityContactConstants.KFS_MAIN_PHONE_NUMBER_TYPE) ? 0 : -1;
        } else if (Strings.CS.equals(phoneType2, CemiEntityContactConstants.KFS_MAIN_PHONE_NUMBER_TYPE)) {
            return 1;
        } else {
            return Strings.CS.compare(phoneType1, phoneType2);
        }
    }

    /*
     * NOTE: It is assumed that the iterator will return all in-scope contacts for a particular vendor/supplier
     *       BEFORE returning any in-scope contacts for the next vendor/supplier.
     */
    @Override
    public void writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage(
            final Iterator<VendorContact> legacyVendorContacts) {
        int vendorContactCount = 0;
        int supplierCount = 0;
        int totalRowsWritten = 0;
        int contactIndex = 0;
        String currentSupplierId = null;
        Integer currentVendorHeaderId = Integer.valueOf(0);
        Integer currentVendorDetailId = Integer.valueOf(0);

        for (final VendorContact vendorContact : IteratorUtils.asIterable(legacyVendorContacts)) {
            vendorContactCount++;
            if (vendorContactCount % 1000 == 0) {
                LOG.info("writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage, Processing {} "
                        + "Vendor Contacts and counting...", vendorContactCount);
            }
            if (!currentVendorHeaderId.equals(vendorContact.getVendorHeaderGeneratedIdentifier())
                    || !currentVendorDetailId.equals(vendorContact.getVendorDetailAssignedIdentifier())) {
                currentSupplierId = cemiEntityContactExtractDao.findSupplierIdForVendorContact(
                        vendorContact.getVendorContactGeneratedIdentifier(), supplierJobRunDateString);
                currentVendorHeaderId = vendorContact.getVendorHeaderGeneratedIdentifier();
                currentVendorDetailId = vendorContact.getVendorDetailAssignedIdentifier();
                supplierCount++;
                contactIndex = 0;
            }
            contactIndex++;
            totalRowsWritten += writeEntityContactRowsFor(vendorContact, currentSupplierId, contactIndex);
        }

        LOG.info("writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Business Entity Contact data rows for {} Vendor Contacts across {} Suppliers",
                totalRowsWritten, vendorContactCount, supplierCount);
    }

    private int writeEntityContactRowsFor(final VendorContact vendorContact, final String supplierId,
            final int contactIndex) {
        final List<VendorContactPhoneNumber> phoneNumbers = getOrderedActivePhoneNumbers(vendorContact);
        final boolean hasEmailAddress = StringUtils.isNotBlank(vendorContact.getVendorContactEmailAddress());
        if (phoneNumbers.isEmpty() && !hasEmailAddress) {
            LOG.warn("writeEntityContactRowsFor, Vendor Contact {} related to Supplier {} has neither "
                    + "an email address nor an active phone number. The Contact will be excluded from the extract.",
                    vendorContact.getVendorContactGeneratedIdentifier(), supplierId);
            return 0;
        }

        final CemiEntityContactHeaderBo headerBo = CemiEntityContactHeaderBoFactory.createHeaderBoFrom(
                vendorContact, supplierId, contactIndex);
        int numRowsForContact = 0;
        int phoneIndex = 0;

        for (final VendorContactPhoneNumber phoneNumber : phoneNumbers) {
            phoneIndex++;
            final CemiEntityContactPhoneBo phoneBo = CemiEntityContactPhoneBoFactory.createCemiEntityContactPhoneBoFrom(
                    Optional.of(phoneNumber), phoneIndex);
            final CemiEntityContactEmailBo emailBo = CemiEntityContactEmailBoFactory.createEmailBoFrom(
                    vendorContact, false);
            Validate.validState(!phoneBo.getPhoneUsages().isEmpty(),
                    "Did not derive any usage BOs for Vendor Phone %s on Vendor Contact %s; this should NEVER happen!",
                    phoneNumber.getVendorContactPhoneGeneratedIdentifier(),
                    vendorContact.getVendorContactGeneratedIdentifier());
            Validate.validState(!emailBo.getEmailUsages().isEmpty(),
                    "Empty email BO is missing an empty usage BO; this should NEVER happen!");
            
            for (final CemiEntityContactGenericUsageBo phoneUsage : phoneBo.getPhoneUsages()) {
                numRowsForContact++;
                final Map<Class<?>, CemiEntityContactGenericUsageBo> usages = Map.ofEntries(
                        Map.entry(CemiEntityContactPhoneBo.class, phoneUsage),
                        Map.entry(CemiEntityContactEmailBo.class, emailBo.getEmailUsages().get(0))
                );
                createAndStoreEntityContactBo(vendorContact, headerBo, phoneBo, emailBo, usages);
            }
        }

        if (hasEmailAddress) {
            final CemiEntityContactPhoneBo phoneBo = CemiEntityContactPhoneBoFactory.createCemiEntityContactPhoneBoFrom(
                    Optional.empty(), -1);
            final CemiEntityContactEmailBo emailBo = CemiEntityContactEmailBoFactory.createEmailBoFrom(
                    vendorContact, true);
            Validate.validState(!phoneBo.getPhoneUsages().isEmpty(),
                    "Empty phone BO is missing an empty usage BO; this should NEVER happen!");
            Validate.validState(!emailBo.getEmailUsages().isEmpty(),
                    "Did not derive any usage BOs for the email on Vendor Contact %s; this should NEVER happen!",
                    vendorContact.getVendorContactGeneratedIdentifier());

            for (final CemiEntityContactGenericUsageBo emailUsage : emailBo.getEmailUsages()) {
                numRowsForContact++;
                final Map<Class<?>, CemiEntityContactGenericUsageBo> usages = Map.ofEntries(
                        Map.entry(CemiEntityContactPhoneBo.class, phoneBo.getPhoneUsages().get(0)),
                        Map.entry(CemiEntityContactEmailBo.class, emailUsage)
                );
                createAndStoreEntityContactBo(vendorContact, headerBo, phoneBo, emailBo, usages);
            }
        }

        return numRowsForContact;
    }

    private List<VendorContactPhoneNumber> getOrderedActivePhoneNumbers(final VendorContact vendorContact) {
        final List<VendorContactPhoneNumber> phoneNumbers = vendorContact.getVendorContactPhoneNumbers();
        if (CollectionUtils.isNotEmpty(phoneNumbers)) {
            return phoneNumbers.stream()
                    .filter(VendorContactPhoneNumber::isActive)
                    .sorted(phoneNumberComparator)
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return List.of();
        }
    }

    private void createAndStoreEntityContactBo(final VendorContact vendorContact,
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo, final Map<Class<?>, CemiEntityContactGenericUsageBo> usages) {
        final CemiEntityContactFileEntityContactTabRowBo tabRowBo = CemiEntityContactFileEntityContactTabRowBoFactory
                .createTabRowBoFrom(vendorContact, headerBo, phoneBo, emailBo, usages, maskSensitiveData);
        storeSheetRow(tabRowBo);
    }

}
