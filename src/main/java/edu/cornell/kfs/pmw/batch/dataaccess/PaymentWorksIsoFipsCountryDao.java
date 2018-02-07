package edu.cornell.kfs.pmw.batch.dataaccess;

import java.util.List;
import java.util.Map;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;

public interface PaymentWorksIsoFipsCountryDao {

    Map<String, List<PaymentWorksIsoFipsCountryItem>> buildIsoToFipsMapFromDatabase();

}
