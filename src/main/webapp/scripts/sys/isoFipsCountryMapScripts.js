/**
 * CU Generic ISO-FIPS Country modification
 */

function onblur_lookupFipsCountry(fipsCountryCode) {
  singleKeyLookup(
    CountryService.getByPrimaryId,
    fipsCountryCode,
    'fipsCountry',
    'fipsCountry.name'
  )
}

function onblur_lookupIsoCountry(isoCountryCode) {
  singleKeyLookup(
    ISOCountryService.getByPrimaryId,
    isoCountryCode,
    'isoCountry',
    'isoCountry.name'
  )
}
