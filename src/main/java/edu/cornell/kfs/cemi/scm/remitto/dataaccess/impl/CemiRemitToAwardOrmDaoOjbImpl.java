package edu.cornell.kfs.cemi.scm.remitto.dataaccess.impl;

import java.util.stream.Stream;

import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.scm.remitto.dataaccess.CemiRemitToSupplierOrmDao;

public class CemiRemitToAwardOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiRemitToSupplierOrmDao {

    @Override
    public Stream<VendorHeader> getVendorsForCemiRemitToExtractAsCloseableStream() {
        // TODO Auto-generated method stub
        return null;
    }

}
