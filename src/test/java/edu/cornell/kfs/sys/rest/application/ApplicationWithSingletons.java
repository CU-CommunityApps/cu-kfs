package edu.cornell.kfs.sys.rest.application;

import java.util.Set;

import javax.ws.rs.core.Application;

public abstract class ApplicationWithSingletons extends Application {

    private final Set<Object> singletons;

    protected ApplicationWithSingletons(Object... singletons) {
        this.singletons = Set.of(singletons);
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
