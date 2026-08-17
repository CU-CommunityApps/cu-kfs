package org.apache.hc.core5;

import org.junit.platform.launcher.LauncherSessionListener;

/**
 * CU Customization:
 * 
 * Newer versions of the "httpcore5-testing" library now include base httpcore5's "tests" library,
 * which contains a file for using Java's ServiceLoader feature to register this class as a provider
 * for the LauncherSessionListener interface/service. However, this specific LoggingInitializationListener
 * class appears to be absent from the "httpcore5-tests" JAR for some reason, causing JUnit to fail
 * on startup due to the missing class. To work around the issue, we've included a no-op implementation
 * of LoggingInitializationListener here so that the unit/integration tests can proceed.
 */
public class LoggingInitializationListener implements LauncherSessionListener {

}
