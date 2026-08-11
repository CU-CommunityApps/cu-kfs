package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactFileEntityContactTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.MergedVendorContact;
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
    private Map<String, String> tenantedContactTypeMappings;

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
        this.tenantedContactTypeMappings = cemiEntityContactExtractDao.getTenantedContactTypeMappings();
    }

    /*
     * NOTE: It is assumed that the iterator will return all in-scope contacts for a particular vendor/supplier
     *       BEFORE returning any in-scope contacts for the next vendor/supplier.
     */
    @Override
    public void writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage(
            final Iterator<VendorContact> legacyVendorContacts) {
        final Map<String, MergedVendorContact> mergedContactsForSameVendor = new LinkedHashMap<>();
        int vendorContactCount = 0;
        int supplierCount = 0;
        int totalRowsWritten = 0;
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
                if (!mergedContactsForSameVendor.isEmpty()) {
                    totalRowsWritten += createAndStoreEntityContactRowsFor(
                            mergedContactsForSameVendor.values(), currentSupplierId);
                    mergedContactsForSameVendor.clear();
                }
                currentSupplierId = cemiEntityContactExtractDao.findSupplierIdForVendorContact(
                        vendorContact.getVendorContactGeneratedIdentifier(), supplierJobRunDateString);
                currentVendorHeaderId = vendorContact.getVendorHeaderGeneratedIdentifier();
                currentVendorDetailId = vendorContact.getVendorDetailAssignedIdentifier();
                supplierCount++;
            }

            final String vendorContactKey = CemiUtils.generateKeyForGroupingDuplicates(
                    vendorContact.getVendorContactName());
            final MergedVendorContact mergedVendorContact = mergedContactsForSameVendor.computeIfAbsent(
                    vendorContactKey, key -> new MergedVendorContact());
            mergedVendorContact.merge(vendorContact);
        }

        if (!mergedContactsForSameVendor.isEmpty()) {
            totalRowsWritten += createAndStoreEntityContactRowsFor(
                    mergedContactsForSameVendor.values(), currentSupplierId);
        }

        LOG.info("writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Business Entity Contact data rows for {} Vendor Contacts across {} Suppliers",
                totalRowsWritten, vendorContactCount, supplierCount);
    }

    private int createAndStoreEntityContactRowsFor(final Collection<MergedVendorContact> mergedVendorContacts,
            final String supplierId) {
        int contactCount = 0;
        int numContactRowsGenerated = 0;

        for (final MergedVendorContact mergedVendorContact : mergedVendorContacts) {
            final String contactIds = getMergedVendorContactIds(mergedVendorContact);
            final Map<String, List<VendorContact>> mergedEmails = mergedVendorContact.getMergedEmails();
            final Map<String, List<VendorContactPhoneNumber>> mergedPhones = mergedVendorContact.getMergedPhoneNumbers();
            if (mergedEmails.isEmpty() && mergedPhones.isEmpty()) {
                LOG.warn("writeEntityContactRowsFor, Vendor Contact(s) {} related to Supplier {} have neither an email "
                        + "address nor an active phone number. The Contact(s) will be excluded from the extract.",
                        contactIds, supplierId);
                continue;
            }

            contactCount++;
            final CemiEntityContactHeaderBo headerBo = CemiEntityContactHeaderBoFactory.createHeaderBoFrom(
                    mergedVendorContact.getMergedContacts(), tenantedContactTypeMappings, supplierId, contactCount);

            final int tenantedContactTypeCount = CollectionUtils.size(headerBo.getMergedTenantedContactTypes());
            if (headerBo.getMergedTenantedContactTypes().size() > CemiEntityContactConstants.MAX_TENANTED_TYPES) {
                LOG.warn("writeEntityContactRowsFor, Vendor Contact(s) {} related to Supplier {} have {} unique "
                        + "tenanted contact types. Only the first {} will be used in the extract.",
                        contactIds, supplierId, tenantedContactTypeCount, CemiEntityContactConstants.MAX_TENANTED_TYPES);
            }

            int emailCount = 0;
            for (final List<VendorContact> itemsForMergedEmail : mergedEmails.values()) {
                emailCount++;
                final CemiEntityContactPhoneBo emptyPhoneBo = CemiEntityContactPhoneBoFactory
                        .createCemiEntityContactPhoneBoFrom(List.of(), -1);
                final CemiEntityContactEmailBo emailBo = CemiEntityContactEmailBoFactory
                        .createEmailBoFrom(itemsForMergedEmail, emailCount);
                createAndStoreEntityContactBo(headerBo, emptyPhoneBo, emailBo);
                numContactRowsGenerated++;
            }

            int phoneCount = 0;
            for (final List<VendorContactPhoneNumber> itemsForMergedPhone : mergedPhones.values()) {
                phoneCount++;
                final CemiEntityContactPhoneBo phoneBo = CemiEntityContactPhoneBoFactory
                        .createCemiEntityContactPhoneBoFrom(itemsForMergedPhone, phoneCount);
                final CemiEntityContactEmailBo emptyEmailBo = CemiEntityContactEmailBoFactory
                        .createEmailBoFrom(List.of(), -1);
                createAndStoreEntityContactBo(headerBo, phoneBo, emptyEmailBo);
                numContactRowsGenerated++;
            }
        }

        return numContactRowsGenerated;
    }

    private String getMergedVendorContactIds(final MergedVendorContact mergedVendorContact) {
        return mergedVendorContact.getMergedContacts().stream()
                .map(VendorContact::getVendorContactGeneratedIdentifier)
                .filter(ObjectUtils::isNotNull)
                .map(idValue -> idValue.toString())
                .collect(Collectors.joining(KFSConstants.COMMA));
    }

    private void createAndStoreEntityContactBo(
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo) {
        final CemiEntityContactFileEntityContactTabRowBo tabRowBo = CemiEntityContactFileEntityContactTabRowBoFactory
                .createTabRowBoFrom(headerBo, phoneBo, emailBo, maskSensitiveData);
        storeSheetRow(tabRowBo);
    }

}
