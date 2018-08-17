package edu.cornell.kfs.module.ezra.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.krad.service.BusinessObjectService;


@ConfigureContext(session = ccs1)
public class ezraUpdateProposalImplTest extends KualiTestBase {

	private static final Logger LOG = LogManager.getLogger(ezraUpdateProposalImplTest.class);
	private EzraService ezraService;	
	private BusinessObjectService businessObjectService;
	private UnitTestSqlDao unitTestSqlDao;

	private static String GET_PROPOSAL_NUMBER_SQL ="select PROJ_ID from AWARD_PROP where AWARD_PROP_ID like 'A%' and BUDG_TOTAL > 0 and STATUS_CD = 'ASAP' and ROWNUM =1";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ezraService = SpringContext.getBean(EzraService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
    }
    
    public void testUpdateProposal () {

		List ezraProposals =  unitTestSqlDao.sqlSelect(GET_PROPOSAL_NUMBER_SQL);
		Map proposalNumberResult = (Map)ezraProposals.get(0);
		Object proposalNumber = proposalNumberResult.get("PROJ_ID");
        Map fields = new HashMap();
        fields.clear();
        fields.put("proposalNumber", proposalNumber);
        String grantIdTest = "ezraUpdateTest";
        String grantIdAfter = "";
        Proposal proposal = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
        String startGrant = proposal.getGrantNumber();
        proposal.setGrantNumber(grantIdTest);
        businessObjectService.save(proposal);
        assertTrue(ezraService.updateProposals());
        Proposal proposalAfterUpdate = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
        grantIdAfter = proposalAfterUpdate.getGrantNumber();
        LOG.info("Grant Number before update "+grantIdTest+" Grant Number after update "+grantIdAfter+" Started with "+startGrant);
        if (grantIdTest.equalsIgnoreCase(grantIdAfter)) {
            fail("Ezra failed to update proposal");
        }
    }
    
}
