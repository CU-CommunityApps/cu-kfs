package edu.cornell.kfs.module.purap.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.service.JaggaerRoleLinkMappingService;

class JaggaerRoleServiceImplTest {
    
    private JaggaerRoleServiceImpl jaggaerRoleService;
    private Person user;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(JaggaerRoleServiceImpl.class.getName(), Level.DEBUG);
        user = buildMockPerson();
        jaggaerRoleService = new JaggaerRoleServiceImpl();
    }
    
    private Person buildMockPerson() {
        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getPrincipalId()).thenReturn("123456789");
        return person;
    }
    
    private JaggaerRoleLinkMappingService buildMockJaggaerRoleLinkMappingService() {
        JaggaerRoleLinkMappingService service = Mockito.mock(JaggaerRoleLinkMappingService.class);
        return service;
    }
    
    private PermissionService buildMockPermissionService(boolean isBuyer) {
        PermissionService permissionService = Mockito.mock(PermissionService.class);
        Mockito.when(permissionService.hasPermission(user.getPrincipalId(),
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SUBMIT_ESHOP_CART_PERMISSION)).thenReturn(isBuyer);
        return permissionService;
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerRoleService = null;
        user = null;
    }

    @ParameterizedTest
    @MethodSource("provideRolesForIsEshopPreAuthTest")
    void testIsEshopPreAuthRole(String role, boolean expected) {
        boolean actual = jaggaerRoleService.isEshopPreAuthRole(role);
        assertEquals(expected, actual);
    }
    
    private static Stream<Arguments> provideRolesForIsEshopPreAuthTest() {
        return Stream.of(
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_BUYER, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_SHOPPER, true),
          Arguments.of(JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, false),
          Arguments.of(StringUtils.EMPTY, false),
          Arguments.of(null, false)
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideRolesForIsEshopViewTest")
    void testisEshopViewRole(String role, boolean expected) {
        boolean actual = jaggaerRoleService.isEshopViewRole(role);
        assertEquals(expected, actual);
    }
    
    private static Stream<Arguments> provideRolesForIsEshopViewTest() {
        return Stream.of(
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_OFFICE, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_LAB, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_FACILITIES, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_SHOPPER, false),
          Arguments.of(JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, false),
          Arguments.of(StringUtils.EMPTY, false),
          Arguments.of(null, false)
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideRolesForIsDefaultJaggaerRoleTest")
    void testisDefaultJaggaerRole(String role, boolean expected) {
        boolean actual = jaggaerRoleService.isDefaultJaggaerRole(role);
        assertEquals(expected, actual);
    }
    
    private static Stream<Arguments> provideRolesForIsDefaultJaggaerRoleTest() {
        return Stream.of(
          Arguments.of(JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, true),
          Arguments.of(CUPurapConstants.SCIQUEST_ROLE_SHOPPER, false),
          Arguments.of(StringUtils.EMPTY, false),
          Arguments.of(null, false)
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideRolesForGetEshopPreAuthValueTest")
    void testGetEshopPreAuthValue(boolean isBuyer, String expectedRole) {
        jaggaerRoleService.setPermissionService(buildMockPermissionService(isBuyer));
        String actual = jaggaerRoleService.getEshopPreAuthValue(user.getPrincipalId());
        assertEquals(expectedRole, actual);
    }
    
    private static Stream<Arguments> provideRolesForGetEshopPreAuthValueTest() {
        return Stream.of(
          Arguments.of(true, CUPurapConstants.SCIQUEST_ROLE_BUYER),
          Arguments.of(false, CUPurapConstants.SCIQUEST_ROLE_SHOPPER)
        );
    }

}
