package edu.cornell.kfs.vnd.dataaccess;

import java.time.LocalDate;

public interface CemiVendorDao {

    void clearExistingListOfBaseVendorData();

    void clearExistingListOfExtractableVendorIds();

    void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate);

    void prepareBaseVendorDataNeededForMainVendorIdQuery();

    void queryAndStoreVendorIdsForSupplierExtract();

}
