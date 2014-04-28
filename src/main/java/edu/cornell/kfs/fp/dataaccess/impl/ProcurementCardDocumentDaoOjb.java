package edu.cornell.kfs.fp.dataaccess.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;

public class ProcurementCardDocumentDaoOjb extends PlatformAwareDaoBaseOjb implements ProcurementCardDocumentDao {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardDocumentDaoOjb.class);
	private static final String WILD_CARD = "%";			
	
	public List<ProcurementCardDocument> getDocumentByCarhdHolderAmountDateVendor(String cardHolder, String amount, Date transactionDate) {

        LOG.debug("getDocumentByAmountDateVendor() started");
		
		if (StringUtils.isBlank(cardHolder) || StringUtils.isBlank(amount) || transactionDate == null) {
			LOG.error("Unable to validate input");
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
		
		return cardHodlerName.replaceAll(regex, replacement).toUpperCase() + WILD_CARD;
	}

}
