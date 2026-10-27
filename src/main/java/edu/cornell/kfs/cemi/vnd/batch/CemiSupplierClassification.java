package edu.cornell.kfs.cemi.vnd.batch;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierClassificationMappingBo;

// TODO: Replace this enum with a mapping table.
public enum CemiSupplierClassification {

    FEDERAL_SBA_8_A("Federal_SBA_8(A)", "8A", "MI"),
    FEDERAL_LGBT_OWNED_BUSINESSES("Federal_LGBTBE_LGBT-Owned_Businesses", "LG"),
    FEDERAL_HUBZONE_UNDERUTILIZED_BUSINESS("Federal_Hub_Zone_Historically_Under-Utilized_Small_Business", "HZ", "SB"),
    FEDERAL_MINORITY_BUSINESS_ENTERPRISE("Federal_MBE_Minority_Business_Enterprise", "MI"),
    FEDERAL_SMALL_DISADVANTAGED_BUSINESS("Federal_SDB_Small_Disadvantaged_Business", "SD"),
    FEDERAL_DISABLED_VET_SMALL_BUSINESS("Federal_SDVOSB_Service-Disabled_Veteran-Owned_Small_Business", "DV", "SB"),
    FEDERAL_VETERAN_SMALL_BUSINESS("Federal_VOSB_Veteran-Owned_Small_Business", "VE", "SB"),
    FEDERAL_WOMAN_OWNED_SMALL_BUSINESS("Federal_WOSB_Woman-Owned_Small_Business", "WO", "SB"),
    FEDERAL_SMALL_BUSINESS("Federal_Small_Business", "SB"),
    FEDERAL_DISABLED("Federal_Disabled", "DB"),
    FEDERAL_DISABLED_VETERAN_OWNED("Federal_Disabled_Veteran_Owned", "DV"),
    FEDERAL_VETERAN_OWNED("Federal_Veteran_Owned", "VE"),
    FEDERAL_WOMAN_OWNED("Federal_Woman_Owned", "WO"),
    NYS_MINORITY_BUSINESS_ENTERPRISE("NYS_MBE_Minority_Business_Enterprise", "NM"),
    NYS_WOMENS_BUSINESS_ENTERPRISE("NYS_WBE_Womens_Business_Enterprise", "NW"),
    NYS_DISABLED_VETERAN_BUSINESS("NYS_Disabled_Veteran_Owned_Business", "NV");

    public final String workdayRefId;
    public final Set<String> kfsSupplierDiversityCodes;

    private CemiSupplierClassification(final String workdayRefId, final String... kfsSupplierDiversityCodes) {
        this.workdayRefId = workdayRefId;
        this.kfsSupplierDiversityCodes = Set.of(kfsSupplierDiversityCodes);
    }

    public CemiSupplierClassificationMappingBo toClassificationMappingBo() {
        final CemiSupplierClassificationMappingBo mappingBo = new CemiSupplierClassificationMappingBo();
        mappingBo.setWorkdayRefId(workdayRefId);
        mappingBo.setKfsSupplierDiversityCodes(kfsSupplierDiversityCodes);
        return mappingBo;
    }

    public int getNumberOfKfsSupplierDiversityCodes() {
        return kfsSupplierDiversityCodes.size();
    }

    /*
     * The downstream logic expects multi-diversity-code classifications to be listed first.
     * Such ordering will need to be added to the future mapping table's querying.
     */
    public static List<CemiSupplierClassificationMappingBo> getClassificationMappingsList() {
        final Comparator<CemiSupplierClassification> descendingSupplierDiversityCountComparator
                = Comparator.comparing(CemiSupplierClassification::getNumberOfKfsSupplierDiversityCodes).reversed();
        return Arrays.stream(CemiSupplierClassification.values())
                .sorted(descendingSupplierDiversityCountComparator)
                .map(CemiSupplierClassification::toClassificationMappingBo)
                .collect(Collectors.toUnmodifiableList());
    }

}
