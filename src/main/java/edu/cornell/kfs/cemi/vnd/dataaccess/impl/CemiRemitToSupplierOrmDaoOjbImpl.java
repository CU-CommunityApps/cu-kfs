package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.stream.Stream;

import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierOrmDao;

public class CemiRemitToSupplierOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiRemitToSupplierOrmDao {

    @Override
    public Stream<CemiSupplierBo> getCemiSuppliersExtractAsCloseableStream() {
        // TODO Auto-generated method stub
        return null;
    }


}
