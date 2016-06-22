package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.fp.CuFPConstants;

public class ScheduleTypeKeyValuesFinder extends KeyValuesBase {
	private static final long serialVersionUID = -3111219830819264052L;

	@Override
	public List<KeyValue> getKeyValues() {
		List<KeyValue> chartKeyLabels = new ArrayList<KeyValue>();
        chartKeyLabels.add(new ConcreteKeyValue( "", ""));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY.name, "Daily"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.WEEKLY.name, "Weekly"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY.name, "Monthly"));
        chartKeyLabels.add(new ConcreteKeyValue(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY.name, "Yearly"));
        return chartKeyLabels;
	}
}
