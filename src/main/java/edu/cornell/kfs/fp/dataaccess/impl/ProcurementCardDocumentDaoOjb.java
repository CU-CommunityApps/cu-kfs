package edu.cornell.kfs.fp.dataaccess.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao;

public class ProcurementCardDocumentDaoOjb extends PlatformAwareDaoBaseOjb implements ProcurementCardDocumentDao {
	private static final Logger LOG = LogManager.getLogger(ProcurementCardDocumentDaoOjb.class);
	private static final String WILD_CARD = "%";			
	
	public List<ProcurementCardDocument> getDocumentByCarhdHolderAmountDateVendor(String cardHolder, String amount, Date transactionDate) {

        LOG.debug("getDocumentByAmountDateVendor() started");
		
		if (StringUtils.isBlank(cardHolder) || StringUtils.isBlank(amount) || transactionDate == null) {
			LOG.error("Unable to validate input. Card Holder Name: " + cardHolder + ", Amount: " + amount + ", Transaction Date: " + transactionDate);
			return null;
		}
		
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(transactionDate);
		gc.add(Calendar.DATE, 21);
		
		Criteria criteria = new Criteria();
        criteria.addLike("procurementCardHolder.cardHolderName", convertCardHolderName(cardHolder));
        criteria.addEqualTo("transactionEntries.transactionTotalAmount", amount);
        criteria.addGreaterOrEqualThan("transactionEntries.transactionDate", transactionDate);
		criteria.addLessOrEqualThan("transactionEntries.transactionDate", new Timestamp(gc.getTimeInMillis()));
        
        return (List<ProcurementCardDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ProcurementCardDocument.class, criteria));
    
	}
	
	private String convertCardHolderName(String cardHodlerName) {
		String regex ="([a-z])([A-Z])";
		String replacement ="$1%$2";
		
		return cardHodlerName.replaceAll(regex, replacement).toUpperCase(Locale.US) + WILD_CARD;
	}

	/**
	 * @see edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao#getDocumentByCarhdHolderNameAmountDateCardHolderNetID(java.lang.String, java.sql.Date, java.lang.String)
	 */
	@Override
	public List<ProcurementCardDocument> getDocumentByCarhdHolderNameAmountDateCardHolderNetID(String amount, Date transactionDate, String cardHolderNetID) {
		 LOG.debug("getDocumentByCarhdHolderNameAmountDateCardHolderNetID() started");
			
			if (StringUtils.isBlank(amount) || transactionDate == null || StringUtils.isBlank(cardHolderNetID)) {
				LOG.error("Unable to validate input. Amount: " + amount + ", Transaction Date: " + transactionDate + ", Card Holder NetID: " + cardHolderNetID + ".");
				return null;
			}
			
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(transactionDate);
			gc.add(Calendar.DATE, 21);
			
			Criteria criteria = new Criteria();
	        criteria.addLike("procurementCardHolder.cardHolderAlternateName", cardHolderNetID.toUpperCase(Locale.US) + WILD_CARD);
	        criteria.addEqualTo("transactionEntries.transactionTotalAmount", amount);
	        criteria.addGreaterOrEqualThan("transactionEntries.transactionDate", transactionDate);
			criteria.addLessOrEqualThan("transactionEntries.transactionDate", new Timestamp(gc.getTimeInMillis()));
	        
	        return (List<ProcurementCardDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ProcurementCardDocument.class, criteria));
	}

	/**
	 * @see edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao#getDocumentByEdocNumber(java.lang.String)
	 */
	@Override
	public List<ProcurementCardDocument> getDocumentByEdocNumber(String edocNumber) {
		LOG.debug("getDocumentByCarhdHolderNameAmountDateCardHolderNetID() started");
			
			if (StringUtils.isBlank(edocNumber)) {
				LOG.error("Unable to validate input");
				return null;
			}
			
			Criteria criteria = new Criteria();
	        criteria.addEqualTo("documentNumber", edocNumber);
	        
	        return (List<ProcurementCardDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ProcurementCardDocument.class, criteria));
	}

}
