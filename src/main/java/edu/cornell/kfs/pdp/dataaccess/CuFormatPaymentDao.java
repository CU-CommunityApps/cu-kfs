package edu.cornell.kfs.pdp.dataaccess;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.pdp.dataaccess.FormatPaymentDao;

public interface CuFormatPaymentDao extends FormatPaymentDao {
    public Iterator markPaymentsForFormat(List customers, Timestamp paydate, String paymentTypes, String paymentDistribution);
}
