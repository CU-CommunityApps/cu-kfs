package edu.cornell.kfs.coa.rest.resource;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.util.TestSpringContextExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class AccountAttachmentControllerTest {

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/coa/cu-spring-coa-account-attachment-test.xml");

}
