package edu.cornell.kfs.cemi.sys.batch.service;

public interface CemiIsoCountryService {

    String getIso3CharCountryCode(final String iso2CharCountryCode);

    String getIso2CharCountryCode(final String iso3CharCountryCode);

}
