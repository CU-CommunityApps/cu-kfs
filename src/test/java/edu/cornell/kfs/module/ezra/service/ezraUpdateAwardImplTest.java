package edu.cornell.kfs.module.ezra.service;

import static org.junit.Assert.assertNotEquals;
import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;

@ConfigureContext(session = ccs1)
public class ezraUpdateAwardImplTest extends KualiTestBase {

	private static final Logger LOG = LogManager.getLogger(ezraUpdateAwardImplTest.class);
    private EzraService ezraService;    
    private BusinessObjectService businessObjectService;
    private EzraAwardProposalDao ezraAwardProposalDao;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ezraService = SpringContext.getBean(EzraService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        ezraAwardProposalDao = SpringContext.getBean(EzraAwardProposalDao.class);
    }
    
    public void testUpdateAwards () {
        Map fields = new HashMap();
        String awardProjectTitle = "ezraUpdateTest";
        String awardProjectTitleAfter = "";
        Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        Date sqlDate = today;
        Date budgetStartDate = generateYear1970StartDate();
        Date budgetEndDate = generateDateOneDayLater(budgetStartDate);
        Date budgetStartDateAfter = generateDateOneDayLater(budgetEndDate);
        Date budgetEndDateAfter = generateDateOneDayLater(budgetStartDateAfter);
        KualiDecimal budgetTotal = new KualiDecimal(-1);
        KualiDecimal budgetTotalAfter = new KualiDecimal(-2);
        EzraProposalAward ezraAward = null;

        List<EzraProposalAward> awards = ezraAwardProposalDao.getAwardsUpdatedSince(sqlDate);
        int count = 0;
        if (awards.isEmpty()) {
            do {
                count--;
                Calendar c = Calendar.getInstance(); 
                c.setTime(today); 
                c.add(Calendar.DATE, count);
                sqlDate = new java.sql.Date(c.getTimeInMillis());
                awards = ezraAwardProposalDao.getAwardsUpdatedSince(sqlDate);
                    if (!awards.isEmpty()) {
                        count = -12;
                    }
                } while ( count > -11);
        }
        
        LOG.info("The Sql Date is "+sqlDate);
        awards = ezraAwardProposalDao.getAwardsUpdatedSince(sqlDate);                
        if (!awards.isEmpty()) {
            ezraAward = awards.get(0);
            fields.clear();
            fields.put("proposalNumber", ezraAward.getProjectId());             
            Award award = businessObjectService.findByPrimaryKey(Award.class, fields);
            award.setAwardProjectTitle(awardProjectTitle);
            getAwardExtension(award).setBudgetBeginningDate(new Date(budgetStartDate.getTime()));
            getAwardExtension(award).setBudgetEndingDate(new Date(budgetEndDate.getTime()));
            getAwardExtension(award).setBudgetTotalAmount(budgetTotal);
            businessObjectService.save(award);
            assertTrue(ezraService.updateAwardsSince(sqlDate));
            Award awardAfter = businessObjectService.findByPrimaryKey(Award.class, fields);
            awardProjectTitleAfter = awardAfter.getAwardProjectTitle();
            budgetStartDateAfter = getAwardExtension(awardAfter).getBudgetBeginningDate();
            budgetEndDateAfter = getAwardExtension(awardAfter).getBudgetEndingDate();
            budgetTotalAfter = getAwardExtension(awardAfter).getBudgetTotalAmount();
            LOG.info("Project Title before update "+awardProjectTitle+" Project Title after update "+awardProjectTitleAfter);
        }

        if (awardProjectTitle.equalsIgnoreCase(awardProjectTitleAfter)) {
            fail("Ezra failed to update Award");
        }
        assertNotEquals("Ezra failed to update Award Budget Start Date", budgetStartDate, budgetStartDateAfter);
        assertNotEquals("Ezra failed to update Award Budget Stop Date", budgetEndDate, budgetEndDateAfter);
        assertNotEquals("Ezra failed to update Award Budget Total Amount", budgetTotal, budgetTotalAfter);
    }
    
    private Date generateYear1970StartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 20);
        return new Date(calendar.getTimeInMillis());
    }

    private Date generateDateOneDayLater(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, 1);
        return new Date(calendar.getTimeInMillis());
    }

    private AwardExtendedAttribute getAwardExtension(Award award) {
        return (AwardExtendedAttribute) award.getExtension();
    }
}