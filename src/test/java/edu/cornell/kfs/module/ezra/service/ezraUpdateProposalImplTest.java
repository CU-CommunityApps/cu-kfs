package edu.cornell.kfs.module.ezra.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.service.EzraService;
import org.kuali.rice.krad.service.BusinessObjectService;

import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;


@ConfigureContext(session = ccs1)
public class ezraUpdateProposalImplTest extends KualiTestBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EzraService.class);
	private EzraService ezraService;	
	private BusinessObjectService businessObjectService;
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ezraService = SpringContext.getBean(EzraService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
    }
    
    public void testUpdateProposal () {
        Map fields = new HashMap();
        fields.clear();
        fields.put("proposalNumber", "13800");        
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
