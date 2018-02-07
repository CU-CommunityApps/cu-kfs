package edu.cornell.kfs.pmw.batch.dataaccess;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.vnd.businessobject.SupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;

public interface KfsSupplierDiversityDao {

    Map<String, SupplierDiversity> buildPmwToKfsSupplierDiversityMap();

}