package edu.cornell.kfs.module.purap.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.identity.PurapKimAttributes;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kim.api.role.RoleMembership;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuRelatedDocumentDerivedRoleTypeServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();

    private CuRelatedDocumentDerivedRoleTypeServiceImpl cuRelatedDocumentDerivedRoleTypeServiceImpl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cuRelatedDocumentDerivedRoleTypeServiceImpl = (CuRelatedDocumentDerivedRoleTypeServiceImpl) IntegTestUtils.getUnproxiedService("relatedDocumentDerivedRoleTypeService");

    }

    public void testGetRoleMembersFromDerivedRole() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);

        Map<String, String> qualification = new HashMap<String, String>();

        qualification.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "true");

        qualification.put(PurapKimAttributes.SENSITIVE_DATA_CODE, "CHEM");
        qualification.put("documentNumber", "1129294");

        qualification.put(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER, "74037");

        List<RoleMembership> roleMembers = cuRelatedDocumentDerivedRoleTypeServiceImpl.getRoleMembersFromDerivedRole("KFS-PURAP", "Sensitive Related Document Initiator Or Reviewer", qualification);
        assertNotNull(roleMembers);
        assertTrue(roleMembers.size() >0);

    }

}
