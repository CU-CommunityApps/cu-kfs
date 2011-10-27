package org.kuali.kfs.sys.document.validation.impl;

import org.kuali.rice.kns.service.impl.ParameterEvaluatorImpl;

/**
 * Special instance of this class which does not have to be re-retrieved from 
 * the database for every value which needs to be tested.
 * 
 * @author Jonathan Keller
 *
 */
public class ReusableParameterEvaluator {

	protected ParameterEvaluatorImpl evaluator;
	
	public ReusableParameterEvaluator( ParameterEvaluatorImpl evaluator ) {
		this.evaluator = evaluator;
	}

	public boolean constraintIsAllow() {
		return evaluator.constraintIsAllow();
	}

	public boolean evaluateAndAddError( String constrainedValue, Class<? extends Object> businessObjectOrDocumentClass,
			String constrainedPropertyName, String userEditablePropertyName) {
		evaluator.setConstrainedValue(constrainedValue);
		return evaluator.evaluateAndAddError(businessObjectOrDocumentClass, constrainedPropertyName, userEditablePropertyName);
	}

	public boolean evaluateAndAddError(String constrainedValue, Class<? extends Object> businessObjectOrDocumentClass, String constrainedPropertyName) {
		evaluator.setConstrainedValue(constrainedValue);
		return evaluator.evaluateAndAddError(businessObjectOrDocumentClass, constrainedPropertyName);
	}

	public boolean evaluationSucceeds(String constrainedValue) {
		evaluator.setConstrainedValue(constrainedValue);
		return evaluator.evaluationSucceeds();
	}

	public String getParameterValuesForMessage() {
		return evaluator.getParameterValuesForMessage();
	}

	public String getValue() {
		return evaluator.getValue();
	}
	
	
}
