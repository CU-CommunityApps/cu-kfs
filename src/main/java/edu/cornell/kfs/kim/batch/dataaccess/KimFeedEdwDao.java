package edu.cornell.kfs.kim.batch.dataaccess;

import java.util.List;
import java.util.stream.Stream;

import edu.cornell.kfs.kim.businessobject.EdwPerson;

public interface KimFeedEdwDao {

    Stream<EdwPerson> getEdwDataAsCloseableStream();

    List<String> getIdsOfPersonsToMarkAsDisabled();

    int markEdwDataAsRead();

}
