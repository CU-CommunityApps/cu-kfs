package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.coreservice.impl.parameter.ParameterBo;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actionrequest.service.ActionRequestService;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.node.service.RouteNodeService;
import org.kuali.rice.kew.routeheader.dao.DocumentRouteHeaderDAO;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

@ConfigureContext(session = kfs)
public class DocumentMaintenanceServiceImplTest  extends KualiTestBase {
    private DocumentMaintenanceService documentMaintenanceService;
    private DocumentMaintenanceDao documentMaintenanceDao;
    private DocumentRouteHeaderDAO documentRouteHeaderDAO;
    private ParameterService parameterService;
    private BusinessObjectService businessObjectService;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        documentMaintenanceService = SpringContext.getBean(DocumentMaintenanceService.class);
        documentMaintenanceDao = SpringContext.getBean(DocumentMaintenanceDao.class);
        documentRouteHeaderDAO = KEWServiceLocator.getBean("enDocumentRouteHeaderDAO");
        parameterService = SpringContext.getBean(ParameterService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
    }

    /*
     * test Requeue document.
     * 
     */
    public void testRequeueDocument() {
        
        Parameter parameter = parameterService.getParameter(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
        ParameterBo paramBo = ParameterBo.from(parameter);
        // trying to filter out that document may cause 'convertexception; and also try to limit the size to
        paramBo.setValue(paramBo.getValue()+";1849556;1173206;101216;5690573;3419899;101164;5814014;5690614;101055"); // add account type to avoid conversionexception
        businessObjectService.save(paramBo);
        String docTypeIds = parameterService.getParameterValueAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
        List<String> docIds = (List<String>)documentMaintenanceDao.getDocumentRequeueValues();
        if (CollectionUtils.isNotEmpty(docIds)) {
            String docId = docIds.get(0);
            List<RouteNodeInstance> activeNodes = getRouteNodeService().getActiveNodeInstances(docId);
            List<ActionRequestValue> originalForThisNode = 
                    getActionRequestService().findPendingRootRequestsByDocIdAtRouteNode(docId, activeNodes.get(0).getRouteNodeInstanceId());
            documentMaintenanceService.requeueDocuments();
            List<ActionRequestValue> neweForThisNode = 
                    getActionRequestService().findPendingRootRequestsByDocIdAtRouteNode(docId, activeNodes.get(0).getRouteNodeInstanceId());

            assertTrue("The action request should be regenerated " , !StringUtils.equals(originalForThisNode.get(0).getActionRequestId(), neweForThisNode.get(0).getActionRequestId()));            
        }
    }
  

    private ActionRequestService getActionRequestService() {
        return KEWServiceLocator.getActionRequestService();
    }
    
    private RouteNodeService getRouteNodeService() {
        return KEWServiceLocator.getRouteNodeService();
    }

}
