package edu.cornell.kfs.module.ezra.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.util.Calendar;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.ezra.dataaccess.SponsorDao;
import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.service.EzraService;

@ConfigureContext(session = ccs1)
public class ezraUpdateSponsorImplTest extends KualiTestBase {

	private static final Logger LOG = LogManager.getLogger(ezraUpdateSponsorImplTest.class);
    private EzraService ezraService;    
    private BusinessObjectService businessObjectService;
    private SponsorDao sponsorDao;
    private int agencyCount;
    private java.sql.Date today; 
    private int agencyNumber;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ezraService = SpringContext.getBean(EzraService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        sponsorDao = SpringContext.getBean(SponsorDao.class);
        int agencyCount = 0;
        today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        int agencyNumber = 0;
        
    }
    
    public boolean agencyInKfs (List<Sponsor> sponsors) {
        Agency agency = null;
        do {
            Map fields = new HashMap();            
            Sponsor sponsor = sponsors.get(agencyCount);
            fields.clear();
            Long sponsorId =  sponsor.getSponsorId();
            fields.put("agencyNumber", sponsorId.toString());            
            agency = (Agency)businessObjectService.findByPrimaryKey(Agency.class, fields);
            agencyCount++;             
        } while ((ObjectUtils.isNull(agency)) && (agencyCount < sponsors.size()));
        
        agencyNumber = agencyCount-1;
        agencyCount = 0;
        if (ObjectUtils.isNull(agency)) {
            return false;
        }
        else {
            return true;
        }
    }
    
    public List<Sponsor> getSponsors (java.sql.Date sqlDate) {
        List<Sponsor> sponsors = sponsorDao.getSponsorsUpdatedSince(sqlDate);
        return sponsors;
    }
    
    public void testUpdateSponsors () {
        Map fields = new HashMap();
        String sponsorName = "ezraUpdateTest";
        String sponsorNameAfter = ""; 
        Date sqlDate = today;
        Agency agency = null; 
        List<Sponsor> sponsors = getSponsors(sqlDate);
        int count = 0;
        if (sponsors.isEmpty() || !agencyInKfs(sponsors)) {
            do {
                count--;
                Calendar c = Calendar.getInstance(); 
                c.setTime(today); 
                c.add(Calendar.DATE, count);
                sqlDate = new java.sql.Date(c.getTimeInMillis());
                sponsors = sponsorDao.getSponsorsUpdatedSince(sqlDate);                    
                } while (sponsors.isEmpty() || !agencyInKfs(sponsors));
        }
        count = 0;
        LOG.info("The Sql Date is "+sqlDate);
        sponsors = sponsorDao.getSponsorsUpdatedSince(sqlDate);                         
        Sponsor sponsor = sponsors.get(agencyNumber);
        fields.clear();
        Long sponsorId =  sponsor.getSponsorId();
        fields.put("agencyNumber", sponsorId);            
        agency = (Agency)businessObjectService.findByPrimaryKey(Agency.class, fields);;
        agency.setFullName(sponsorName);
        businessObjectService.save(agency);
        assertTrue(ezraService.updateSponsorsSince(sqlDate));
        Agency agencyAfter = (Agency)businessObjectService.findByPrimaryKey(Agency.class, fields);
        sponsorNameAfter = agency.getFullName();
        LOG.info("Sponsor name before update "+sponsorName+" Sponsor name after update "+sponsorNameAfter);

        if (sponsorName.equalsIgnoreCase(sponsorNameAfter)) {
            fail("Ezra failed to update Sponsor");
        }
    }
    
}