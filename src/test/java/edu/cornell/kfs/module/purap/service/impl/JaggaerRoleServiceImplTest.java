package edu.cornell.kfs.module.purap.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.businessobject.JaggaerRoleLinkMapping;
import edu.cornell.kfs.module.purap.service.JaggaerRoleLinkMappingService;

class JaggaerRoleServiceImplTest {
    
    private JaggaerRoleServiceImpl jaggaerRoleService;
    private Person user;
    
    private static final String JAGGAER_ROLE_HAS = "jaggaer role has";
    private static final String JAGGAER_ROLE_DOES_NOT_HAVE = "jaggaer role does not have";

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
    
    private PermissionService buildMockPermissionService(boolean isBuyer, boolean isOffice, boolean isLab, boolean isFacilities) {
        PermissionService permissionService = Mockito.mock(PermissionService.class);
        
        Mockito.when(permissionService.hasPermission(user.getPrincipalId(),
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SUBMIT_ESHOP_CART_PERMISSION)).thenReturn(isBuyer);
        
        Mockito.when(permissionService.hasPermission(user.getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                CUPurapConstants.B2B_SHOPPER_OFFICE_PERMISSION)).thenReturn(isOffice);
        
        Mockito.when(permissionService.hasPermission(user.getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                CUPurapConstants.B2B_SHOPPER_LAB_PERMISSION)).thenReturn(isLab);
        
        Mockito.when(permissionService.hasPermission(user.getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                CUPurapConstants.B2B_SHOPPER_FACILITIES_PERMISSION)).thenReturn(isFacilities);
        
        addMockRolePermissionCheck(permissionService, JAGGAER_ROLE_HAS, true);
        addMockRolePermissionCheck(permissionService, JAGGAER_ROLE_DOES_NOT_HAVE, false);
        
        return permissionService;
    }
    
    private void addMockRolePermissionCheck(PermissionService permissionService, String roleName, boolean hasPermission) {
        Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(JaggaerConstants.JAGGAER_ATTRIBUTE_VALUE_KEY, roleName);
        Mockito.when(permissionService.hasPermissionByTemplate(user.getPrincipalId(),
                JaggaerConstants.JAGGAER_NAMESPACE,
                JaggaerConstants.JAGGAER_PERMISSION_TEMPLATE_NAME, permissionDetails)).thenReturn(hasPermission);
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
    void testIsDefaultJaggaerRole(String role, boolean expected) {
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
        jaggaerRoleService.setPermissionService(buildMockPermissionService(isBuyer, false, false, false));
        String actual = jaggaerRoleService.getEshopPreAuthValue(user.getPrincipalId());
        assertEquals(expectedRole, actual);
    }
    
    private static Stream<Arguments> provideRolesForGetEshopPreAuthValueTest() {
        return Stream.of(
          Arguments.of(true, CUPurapConstants.SCIQUEST_ROLE_BUYER),
          Arguments.of(false, CUPurapConstants.SCIQUEST_ROLE_SHOPPER)
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideRolesForGetEshopViewValueTest")
    void testGetEshopViewValue(boolean isOffice, boolean isLab, boolean isFacilities, String expectedRole) {
        jaggaerRoleService.setPermissionService(buildMockPermissionService(true, isOffice, isLab, isFacilities));
        String actual = jaggaerRoleService.getEshopViewValue(user.getPrincipalId());
        assertEquals(expectedRole, actual);
    }
    
    private static Stream<Arguments> provideRolesForGetEshopViewValueTest() {
        return Stream.of(
          Arguments.of(true, false, false, CUPurapConstants.SCIQUEST_ROLE_OFFICE),
          Arguments.of(false, true, false, CUPurapConstants.SCIQUEST_ROLE_LAB),
          Arguments.of(false, false, true, CUPurapConstants.SCIQUEST_ROLE_FACILITIES),
          Arguments.of(false, false, false, CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED)
        );
    }
    
    
    @ParameterizedTest
    @MethodSource("provideForTestGetJaggaerRoles")
    void testGetJaggaerRoles(boolean isBuyer, boolean isOffice, boolean isLab, boolean isFacilities, List<String> expectedRoles) {
        jaggaerRoleService.setJaggaerRoleLinkMappingService(buildMockJaggaerRoleLinkMappingService());
        jaggaerRoleService.setPermissionService(buildMockPermissionService(isBuyer, isOffice, isLab, isFacilities));
        List<String> actualRoles = jaggaerRoleService.getJaggaerRoles(user, JaggaerRoleSet.ADMINISTRATOR);
        assertEquals(expectedRoles.size(), actualRoles.size());
        for (String role : actualRoles) {
            assertTrue(expectedRoles.contains(role));
        }
    }
    
    private static Stream<Arguments> provideForTestGetJaggaerRoles() {
        return Stream.of(
          Arguments.of(true, true, false, false, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_BUYER,
                  CUPurapConstants.SCIQUEST_ROLE_OFFICE)),
          Arguments.of(false, true, false, false, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_SHOPPER,
                  CUPurapConstants.SCIQUEST_ROLE_OFFICE)),
          Arguments.of(false, true, true, true, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_SHOPPER,
                  CUPurapConstants.SCIQUEST_ROLE_OFFICE)),
          Arguments.of(true, false, true, false, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_BUYER,
                  CUPurapConstants.SCIQUEST_ROLE_LAB)),
          Arguments.of(true, false, false, true, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_BUYER,
                  CUPurapConstants.SCIQUEST_ROLE_FACILITIES)),
          Arguments.of(true, false, false, false, List.of(JAGGAER_ROLE_HAS, 
                  JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_BUYER,
                  CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED))
        );
    }
    
    private JaggaerRoleLinkMappingService buildMockJaggaerRoleLinkMappingService() {
        JaggaerRoleLinkMappingService service = Mockito.mock(JaggaerRoleLinkMappingService.class);
        List<String> roles = List.of(JAGGAER_ROLE_HAS, JAGGAER_ROLE_DOES_NOT_HAVE, 
                JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY, CUPurapConstants.SCIQUEST_ROLE_BUYER,
                CUPurapConstants.SCIQUEST_ROLE_SHOPPER, CUPurapConstants.SCIQUEST_ROLE_OFFICE,
                CUPurapConstants.SCIQUEST_ROLE_LAB, CUPurapConstants.SCIQUEST_ROLE_FACILITIES,
                CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED);
        Mockito.when(service.getJaggaerLinkRoles(Mockito.any())).thenReturn(buildLinkRoleMappingCollection(roles));
        return service;
    }
    
    private Collection<JaggaerRoleLinkMapping> buildLinkRoleMappingCollection(List<String> roles) {
        Collection<JaggaerRoleLinkMapping> links = new ArrayList<>();
        for (String role : roles) {
            JaggaerRoleLinkMapping link = new JaggaerRoleLinkMapping();
            link.setActive(true);
            link.setJaggaerRoleName(role);
            links.add(link);
        }
        return links;
    }

}
