package edu.cornell.kfs.concur.batch.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDetailDTO;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConcurExpenseV3AllocationFixture {

    String id() default Utils.RANDOM_ID_INDICATOR;

    String entryId() default Utils.RANDOM_ID_INDICATOR;

    String uri() default Utils.LOCALHOST_8080;

    String percentage() default Utils.PERCENT_100;

    boolean percentEdited() default false;

    boolean hidden() default false;

    String objectCode() default KFSConstants.EMPTY_STRING;

    String chart() default KFSConstants.EMPTY_STRING;

    String account() default KFSConstants.EMPTY_STRING;
    
    String subAccount() default KFSConstants.EMPTY_STRING;
    
    String subObject() default KFSConstants.EMPTY_STRING;
    
    String projectCode() default KFSConstants.EMPTY_STRING;
    
    String orgRefId() default KFSConstants.EMPTY_STRING;

    boolean expectValidationSuccess() default true;

    public static final class Utils {
        public static final String RANDOM_ID_INDICATOR = "RANDOM";
        public static final String LOCALHOST_8080 = "localhost:8080";
        public static final String PERCENT_100 = "100.00000000";
        
        private enum DetailType {
            CHART("Chart", true),
            ACCOUNT("Account", true),
            SUB_ACCOUNT("Sub-Account", true),
            SUB_OBJECT("Sub-Object", false),
            PROJECT_CODE("Project Code", true),
            ORG_REF_ID("Org Ref ID", false);

            private final String label;
            private final boolean valueIsInCodeField;

            private DetailType(final String label, final boolean valueIsInCodeField) {
                this.label = label;
                this.valueIsInCodeField = valueIsInCodeField;
            }
        }

        public static ConcurExpenseAllocationV3ListItemDTO toAllocationDto(
                final ConcurExpenseV3AllocationFixture fixture) {
            final ConcurExpenseAllocationV3ListItemDTO dto = new ConcurExpenseAllocationV3ListItemDTO();
            dto.setId(generateRandomIdIfNecessary(fixture.id()));
            dto.setEntryId(generateRandomIdIfNecessary(fixture.entryId()));
            dto.setUri(fixture.uri());
            dto.setPercentage(new KualiDecimal(fixture.percentage()));
            dto.setPercentEdited(fixture.percentEdited());
            dto.setHidden(fixture.hidden());
            dto.setObjectCode(fixture.objectCode());
            dto.setChart(generateDetailDtoIfNecessary(DetailType.CHART, fixture.chart(), null));
            dto.setAccount(generateDetailDtoIfNecessary(DetailType.ACCOUNT, fixture.account(), null));
            dto.setSubAccount(generateDetailDtoIfNecessary(DetailType.SUB_ACCOUNT, fixture.subAccount(), null));
            dto.setSubObject(generateDetailDtoIfNecessary(DetailType.SUB_OBJECT, null, fixture.subObject()));
            dto.setProjectCode(generateDetailDtoIfNecessary(DetailType.PROJECT_CODE, fixture.projectCode(), null));
            dto.setOrgRefId(generateDetailDtoIfNecessary(DetailType.ORG_REF_ID, null, fixture.orgRefId()));
            return dto;
        }

        private static String generateRandomIdIfNecessary(final String id) {
            return StringUtils.equalsIgnoreCase(id, RANDOM_ID_INDICATOR) ? UUID.randomUUID().toString() : id;
        }

        private static ConcurExpenseAllocationV3ListItemDetailDTO generateDetailDtoIfNecessary(
                final DetailType detailType, final String code, final String value) {
            if ((detailType.valueIsInCodeField && StringUtils.isBlank(code))
                    || (!detailType.valueIsInCodeField && StringUtils.isBlank(value))) {
                return null;
            }
            final ConcurExpenseAllocationV3ListItemDetailDTO dto = new ConcurExpenseAllocationV3ListItemDetailDTO();
            dto.setListItemId(generateRandomIdIfNecessary(RANDOM_ID_INDICATOR));
            dto.setLabel(detailType.label);
            dto.setCode(code);
            if (StringUtils.isNotBlank(value)) {
                dto.setValue(value);
            } else if (detailType.valueIsInCodeField) {
                dto.setValue(detailType.label + KFSConstants.BLANK_SPACE + code);
            }
            return dto;
        }
    }

}
