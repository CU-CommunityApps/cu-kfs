package edu.cornell.kfs.cemi.sys.dataaccess;

public interface CemiIsoCountryDao {

    String getIso3CharCountryCode(final String iso2CharCountryCode);

    String getIso2CharCountryCode(final String iso3CharCountryCode);

}
