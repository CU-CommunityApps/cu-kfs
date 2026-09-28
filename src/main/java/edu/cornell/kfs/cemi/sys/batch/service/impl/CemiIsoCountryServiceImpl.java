package edu.cornell.kfs.cemi.sys.batch.service.impl;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.sys.batch.service.CemiIsoCountryService;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiIsoCountryDao;

public class CemiIsoCountryServiceImpl implements CemiIsoCountryService {

    private CemiIsoCountryDao cemiIsoCountryDao;

    @Override
    public String getIso3CharCountryCode(final String iso2CharCountryCode) {
        Validate.notBlank(iso2CharCountryCode, "iso2CharCountryCode cannot be blank");
        return cemiIsoCountryDao.getIso3CharCountryCode(iso2CharCountryCode);
    }

    @Override
    public String getIso2CharCountryCode(final String iso3CharCountryCode) {
        Validate.notBlank(iso3CharCountryCode, "iso3CharCountryCode cannot be blank");
        return cemiIsoCountryDao.getIso2CharCountryCode(iso3CharCountryCode);
    }

    public void setCemiIsoCountryDao(final CemiIsoCountryDao cemiIsoCountryDao) {
        this.cemiIsoCountryDao = cemiIsoCountryDao;
    }

}
