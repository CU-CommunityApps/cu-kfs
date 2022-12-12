package edu.cornell.kfs.sys.web.mock;

/**
 * Helper interface for use by Spring MVC Controllers that can reset their state in between unit test methods.
 * This is meant to be used in conjunction with a MockMvcWebServerExtension instance that has been defined
 * at the class/static level.
 */
public interface ResettableController {

    void reset();

}
