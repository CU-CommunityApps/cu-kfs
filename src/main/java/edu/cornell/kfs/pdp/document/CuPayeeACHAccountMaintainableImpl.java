package edu.cornell.kfs.pdp.document;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

import edu.cornell.kfs.pdp.CUPdpConstants;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;

public class CuPayeeACHAccountMaintainableImpl extends PayeeACHAccountMaintainableImpl {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        
        super.refresh(refreshCaller, fieldValues, document);
        
        if (StringUtils.isNotEmpty(refreshCaller) && refreshCaller.equalsIgnoreCase("achPayeeLookupable")) {
            PayeeACHAccount payeeAchAccount = (PayeeACHAccount) document.getNewMaintainableObject().getBusinessObject();
            String payeeIdNumber = payeeAchAccount.getPayeeIdNumber();
            String payeeIdentifierTypeCode = payeeAchAccount.getPayeeIdentifierTypeCode();
            
            if(StringUtils.isNotEmpty(payeeAchAccount.getPayeeIdNumber())){
              // for Employee, retrieve from Person table by employee ID
              if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
                  Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
                  if (ObjectUtils.isNotNull(person)) {
                      payeeAchAccount.setPayeeEmailAddress( person.getEmailAddress());
                  }
              }
              // for Entity, retrieve from Entity table by entity ID then from Person table
              else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
                  if (ObjectUtils.isNotNull(payeeIdNumber)) {
                      Entity entity = KimApiServiceLocator.getIdentityService().getEntity(payeeIdNumber);
                      if (ObjectUtils.isNotNull(entity)) {
                          List<Principal> principals = entity.getPrincipals();
                          if (principals.size() > 0 && ObjectUtils.isNotNull(principals.get(0))) {
                              String principalId = principals.get(0).getPrincipalId();
                              Person person = SpringContext.getBean(PersonService.class).getPerson(principalId);
                              if (ObjectUtils.isNotNull(person)) {
                                  payeeAchAccount.setPayeeEmailAddress( person.getEmailAddress());
                              }
                          }
                      }
                  }
              }
            }
        }
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) {
        if (StringUtils.equalsIgnoreCase(CUPdpConstants.PAYEE_ACH_ACCOUNT_REQUIRES_PDP_APPROVAL_NODE, nodeName)) {
            return payeeACHAccountMaintenanceRequiresPdpApproval();
        }
        return super.answerSplitNodeQuestion(nodeName);
    }

    protected boolean payeeACHAccountMaintenanceRequiresPdpApproval() {
        try {
            DocumentService documentService = SpringContext.getBean(DocumentService.class);
            FinancialSystemMaintenanceDocument document = (FinancialSystemMaintenanceDocument) documentService.getByDocumentHeaderId(this.getDocumentNumber());
            if (isDocumentInitiatorSystemUser(document)) {
                return false;
            }
        } catch (Exception exception) {
            LOG.error("payeeACHAccountMaintenanceRequiresPdpApproval " + exception.getMessage(), exception);
        }
        return StringUtils.equalsIgnoreCase(KFSConstants.MAINTENANCE_EDIT_ACTION, getMaintenanceAction());
    }

    protected boolean isDocumentInitiatorSystemUser(FinancialSystemMaintenanceDocument document) {
        Person documentInitiator = KimApiServiceLocator.getPersonService().getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId());
        return documentInitiator != null && documentInitiator.getPrincipalName().equalsIgnoreCase(KFSConstants.SYSTEM_USER);
    }

}
