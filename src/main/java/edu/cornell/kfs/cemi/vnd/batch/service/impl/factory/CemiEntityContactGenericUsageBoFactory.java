package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;

public class CemiEntityContactGenericUsageBoFactory {

    private String usageRowId;
    private String usageType;
    private String usageComments;

    public CemiEntityContactGenericUsageBoFactory(final String usageRowId, final String usageType,
            final String usageComments) {
        this.usageRowId = StringUtils.defaultString(usageRowId);
        this.usageType = StringUtils.defaultString(usageType);
        this.usageComments = StringUtils.defaultString(usageComments);
    }

    public static CemiEntityContactGenericUsageBo createEmptyUsageBo() {
        return createUsageBoFrom(CemiBaseConstants.EMPTY_STRING, CemiBaseConstants.EMPTY_STRING,
                CemiBaseConstants.EMPTY_STRING);
    }

    public static CemiEntityContactGenericUsageBo createUsageBoFrom(final String usageRowId, final String usageType,
            final String usageComments) {
        final CemiEntityContactGenericUsageBoFactory factory = new CemiEntityContactGenericUsageBoFactory(
                usageRowId, usageType, usageComments);
        return factory.createCemiEntityContactGenericUsageBo();
    }

    public CemiEntityContactGenericUsageBo createCemiEntityContactGenericUsageBo() {
        final CemiEntityContactGenericUsageBo usageBo = new CemiEntityContactGenericUsageBo();
        final boolean createEmptyBo = StringUtils.isAllBlank(usageRowId, usageType, usageComments);

        usageBo.setUsageRowId(usageRowId);
        usageBo.setUsagePublic(createEmptyBo ? CemiBaseConstants.EMPTY_STRING : CemiBaseConstants.YES);
        usageBo.setUsageTypeRowId(createEmptyBo ? CemiBaseConstants.EMPTY_STRING : CemiEntityContactConstants.ROW_ID_1);
        usageBo.setUsageTypePrimary(createEmptyBo ? CemiBaseConstants.EMPTY_STRING : CemiBaseConstants.YES);
        usageBo.setUsageType(usageType);
        usageBo.setUseFor(CemiBaseConstants.EMPTY_STRING);
        usageBo.setUseForTenanted(CemiBaseConstants.EMPTY_STRING);
        usageBo.setUsageComments(usageComments);

        return usageBo;
    }

}
