package edu.cornell.kfs.module.ezra.batch;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.util.Calendar;

import edu.cornell.kfs.module.cg.businessobject.CuAward;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.service.EzraService;

import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.service.BusinessObjectService;

import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;


@ConfigureContext(session = ccs1)
public class ezraUpdateAwardImplTest extends KualiTestBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EzraService.class);
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
        java.sql.Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        Date sqlDate = today;
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
            Award award = (CuAward)businessObjectService.findByPrimaryKey(CuAward.class, fields);
            award.setAwardProjectTitle(awardProjectTitle);
            businessObjectService.save(award);
            assertTrue(ezraService.updateAwardsSince(sqlDate));
            Award awardAfter = (CuAward)businessObjectService.findByPrimaryKey(CuAward.class, fields);
            awardProjectTitleAfter = awardAfter.getAwardProjectTitle();
            LOG.info("Project Title before update "+awardProjectTitle+" Project Title after update "+awardProjectTitleAfter);
        }
        if (awardProjectTitle.equalsIgnoreCase(awardProjectTitleAfter)) {
            fail("Ezra failed to update Award");
        }
    }
    
}