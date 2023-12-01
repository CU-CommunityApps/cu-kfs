package edu.cornell.kfs.sec.document;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.sec.SecPropertyConstants;
import org.kuali.kfs.sec.businessobject.SecurityModel;
import org.kuali.kfs.sec.businessobject.SecurityModelDefinition;
import org.kuali.kfs.sec.document.SecurityModelMaintainableImpl;
import org.kuali.kfs.sys.context.SpringContext;

public class CuSecurityModelMaintainableImpl extends SecurityModelMaintainableImpl {

    private static final String MODEL_DEFINITION_ID_SEQUENCE_NAME = "SEC_SCRTY_MDL_DEFN_ID_SEQ";

    private transient SequenceAccessorService sequenceAccessorService;

    @Override
    protected String buildModelRoleId(final SecurityModel securityModel) {
        return null;
    }

    @Override
    public void addNewLineToCollection(final String collectionName) {
        super.addNewLineToCollection(collectionName);
        if (StringUtils.equalsIgnoreCase(collectionName, SecPropertyConstants.MODEL_DEFINITIONS)) {
            populatePrimaryKeyOnNewModelDefinition();
        }
    }

    protected void populatePrimaryKeyOnNewModelDefinition() {
        final SecurityModel securityModel = (SecurityModel) getBusinessObject();
        final List<SecurityModelDefinition> modelDefinitions = securityModel.getModelDefinitions();
        if (CollectionUtils.isNotEmpty(modelDefinitions)) {
            final int lastElementIndex = modelDefinitions.size() - 1;
            final SecurityModelDefinition newModelDefinition = modelDefinitions.get(lastElementIndex);
            final Long newModelDefinitionId = getSequenceAccessorService().getNextAvailableSequenceNumber(
                    MODEL_DEFINITION_ID_SEQUENCE_NAME);
            newModelDefinition.setModelDefinitionId(new KualiInteger(newModelDefinitionId));
        }
    }

    protected SequenceAccessorService getSequenceAccessorService() {
        if (sequenceAccessorService == null) {
            sequenceAccessorService = SpringContext.getBean(SequenceAccessorService.class);
        }
        return sequenceAccessorService;
    }

}
