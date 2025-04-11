package edu.cornell.kfs.rass.batch.util;

import org.apache.commons.lang3.function.FailableSupplier;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.util.CuMockBuilder;

public final class RassTestUtils {

    public static <E, T extends Throwable> E doWithMockHandlingOfProjectDirectorRefreshes(
            final FailableSupplier<E, T> task) throws T {
        try (final MockedStatic<KRADServiceLocator> serviceLocatorMock =
                Mockito.mockStatic(KRADServiceLocator.class)) {
            final PersistenceStructureService persistenceStructureService = buildMockPersistenceStructureService();
            final PersistenceService persistenceService = buildMockPersistenceService();
            serviceLocatorMock.when(() -> KRADServiceLocator.getPersistenceStructureService())
                    .thenReturn(persistenceStructureService);
            serviceLocatorMock.when(() -> KRADServiceLocator.getPersistenceService())
                    .thenReturn(persistenceService);
            return task.get();
        }
    }

    private static PersistenceStructureService buildMockPersistenceStructureService() {
        return new CuMockBuilder<>(PersistenceStructureService.class)
                .withReturn(
                        service -> service.isReferenceUpdatable(
                                ProposalProjectDirector.class, KFSPropertyConstants.PROJECT_DIRECTOR),
                        true)
                .build();
    }

    private static PersistenceService buildMockPersistenceService() {
        return new CuMockBuilder<>(PersistenceService.class)
                .withAnswer(
                        service -> service.retrieveReferenceObject(Mockito.any(ProposalProjectDirector.class),
                                Mockito.eq(KFSPropertyConstants.PROJECT_DIRECTOR)),
                        invocation -> initializeMockPersonOnDirector(invocation.getArgument(0)))
                .build();
    }

    private static Object initializeMockPersonOnDirector(final ProposalProjectDirector director) {
        final Person mockDirectorPerson = new CuMockBuilder<>(Person.class)
                .withReturn(Person::getPrincipalId, director.getPrincipalId())
                .withReturn(Person::getPrincipalName, director.getPrincipalId())
                .build();
        director.setProjectDirector(mockDirectorPerson);
        // The method being mocked is a void method, so just return null here.
        return null;
    }

}
