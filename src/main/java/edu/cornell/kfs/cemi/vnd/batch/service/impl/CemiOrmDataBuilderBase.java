package edu.cornell.kfs.cemi.vnd.batch.service.impl;
//CHANGE extends TO USE edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
//MAKE ALL RELATED CODING CHANGES NEEDED TO MAKE abstract class FOLLOW PATTERN. 

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableLong;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiIndexedBusinessObjectBase;

public abstract class CemiOrmDataBuilderBase {

    protected final BusinessObjectService businessObjectService;
    protected final String jobRunDate;
    protected final Map<Class<? extends CemiIndexedBusinessObjectBase>, MutableLong> sheetRowCounts;

    @SafeVarargs
    protected CemiOrmDataBuilderBase(final BusinessObjectService businessObjectService, final String jobRunDate,
            final Class<? extends CemiIndexedBusinessObjectBase>... sheetRowClasses) {
        Validate.notNull(businessObjectService, "businessObjectService cannot be null");
        Validate.notBlank(jobRunDate, "jobRunDate string cannot be blank");
        Validate.notEmpty(sheetRowClasses, "sheetRowClasses cannot be null or empty");
        this.businessObjectService = businessObjectService;
        this.jobRunDate = jobRunDate;
        this.sheetRowCounts = Stream.of(sheetRowClasses)
                .collect(Collectors.toUnmodifiableMap(Function.identity(), sheetRowClass -> new MutableLong(0L)));
    }

    protected void storeSheetRow(final CemiIndexedBusinessObjectBase sheetRow) {
        Validate.notNull(sheetRow, "sheetRow cannot be null");
        final MutableLong rowCount = sheetRowCounts.get(sheetRow.getClass());
        Validate.validState(rowCount != null, "Could not find sheet row counter for %s", sheetRow.getClass().getName());

        final long nextCountValue = rowCount.incrementAndGet();
        sheetRow.setJobRunDate(jobRunDate);
        sheetRow.setJobRunRowIndex(nextCountValue);
        businessObjectService.save(sheetRow);
    }

}
