package edu.cornell.kfs.sys.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;


public class KualiDecimalXmlDifferenceEvaluator implements DifferenceEvaluator {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome != ComparisonResult.EQUAL) {
            String control = (String) comparison.getControlDetails().getValue();
            String test = (String) comparison.getTestDetails().getValue();
            try {
                KualiDecimal controlAmount = new KualiDecimal(control);
                KualiDecimal testAmount = new KualiDecimal(test);
                if(controlAmount.equals(testAmount)) {
                    outcome = ComparisonResult.EQUAL;
                    LOG.debug("evaluate, found equal control ({}) and test ({}) amounts", control, test);
                }
            } catch (Exception e) {
                LOG.debug("evaluate, found non-numeric control ({}) and test ({}) amounts", control, test);
            }
        }
        
        return outcome;
    }

}
