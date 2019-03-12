package edu.cornell.kfs.gl.service.impl.fixture;

public enum EmailAdressTestValue {
    THREE_PART_ADDRESS_GOOD("ww5@a.b.c.org", true),
    ALPHANUMERIC_DOMAIN_GOOD("something.else@a2.com", true),
    UNDERSCORE_ADDRESS_GOOD("something_else@something.else.com", true),
    HYPHEN_DOCMAIN_GOOD("something-else@et-tu.com", true),
    NO_ADDRESS_BAD("@a.b.c.org", false),
    NO_DOMAIN_BAD("a", false),
    NO_ZONE_BAD("1@org", false),
    NO_ZONE2_BAD("1@a", false),
    SPACE_BAD("_@a", false),
    DOT_ADDRESS_BAD(".@a.org", false),
    HYPHEN_ADDRESS_BAD("\"-@a.org\"", false),
    HYPHEN_ZONE_BAD("something@a.o-rg", false),
    UNDERSCOREW_DOMAIN_BAD("someone@foo_bar.com", false);
    
    public final String emailAdress;
    public final boolean validAdress;
    
    private EmailAdressTestValue(String emailAddress, boolean validAddress) {
        this.emailAdress = emailAddress;
        this.validAdress = validAddress;
    }

}
