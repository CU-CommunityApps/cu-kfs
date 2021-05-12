package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.fp.CuFPConstants;

public class ScheduleTypeKeyValuesFinder extends KeyValuesBase {
	private static final long serialVersionUID = -3111219830819264052L;

	@Override
	public List<KeyValue> getKeyValues() {
		List<KeyValue> chartKeyLabels = new ArrayList<KeyValue>();
        chartKeyLabels.add(new ConcreteKeyValue( "", ""));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY.name, "Daily"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY.name, "Bi-Weekly"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY.name, "Monthly"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY.name, "Yearly"));
        return chartKeyLabels;
	}
}
