package edu.cornell.kfs.kim.batch.service;

public interface KimFeedService {

    void processPersonDataChanges();

    void processPersonDataMarkedForDisabling();

    void markPersonDataChangesAsRead();

    void flushPersonCache();

}
