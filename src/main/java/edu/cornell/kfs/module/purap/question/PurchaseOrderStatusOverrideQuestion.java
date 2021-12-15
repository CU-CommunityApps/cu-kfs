package edu.cornell.kfs.module.purap.question;

import java.util.ArrayList;

import org.kuali.kfs.kns.question.QuestionBase;

/**
 * This Question implementation is used by a CU-KFS-specific feature that allows users to move
 * "CXER"-status POs into "OPEN" or "VOID" status. This class is only used to generate a
 * confirmation page for when the status transition is successful, and as such is not really
 * a "question" page per se but just a page with a message and a "close" button.
 * 
 * <p>This class is needed for the KFSPTS-1457 feature.
 */
public class PurchaseOrderStatusOverrideQuestion extends QuestionBase {

	public static final String CLOSE = "0";
	
	public PurchaseOrderStatusOverrideQuestion() {
		super("Press 'close' to continue.", new ArrayList<String>(1));
		this.getButtons().add("close");
	}
}
