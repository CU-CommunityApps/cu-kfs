package edu.cornell.kfs.concur.batch.service.impl.fixture;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4MainDestinationDTO;

public enum ConcurV4DestinationFixture {
    MIAMI_FL("A12BC34DE56FG78HI90JK12LM34NO56P", "US", "US-FL", "Miami, Florida", 24680);

    public final String id;
    public final String countryCode;
    public final String countrySubDivisionCode;
    public final String city;
    public final int lnKey;

    private ConcurV4DestinationFixture(String id, String countryCode, String countrySubDivisionCode,
            String city, int lnKey) {
        this.id = id;
        this.countryCode = countryCode;
        this.countrySubDivisionCode = countrySubDivisionCode;
        this.city = city;
        this.lnKey = lnKey;
    }

    public ConcurRequestV4MainDestinationDTO toConcurRequestV4MainDestinationDTO() {
        ConcurRequestV4MainDestinationDTO destinationDTO = new ConcurRequestV4MainDestinationDTO();
        destinationDTO.setId(id);
        destinationDTO.setCountryCode(countryCode);
        destinationDTO.setCountrySubDivisionCode(countrySubDivisionCode);
        destinationDTO.setCity(city);
        destinationDTO.setName(city);
        destinationDTO.setLnKey(Integer.valueOf(lnKey));
        return destinationDTO;
    }

}
