package edu.cornell.kfs.gl.service.impl.fixture;

public enum EmailAddressTestValue {
    THREE_PART_DOMAIN_GOOD("ww5@a.b.c.org", true),
    ALPHANUMERIC_DOMAIN_DOT_ADDRESS_GOOD("something.else@a2.com", true),
    ALPHANUMERIC_DOMAIN_UNDERSCORE_ADDRESS_GOOD("something_else@a2.com", true),
    ALPHANUMERIC_DOMAIN_HYPHEN_ADDRESS_GOOD("something-else@a2.com", true),
    UNDERSCORE_ADDRESS_GOOD("something_else@something.else.com", true),
    HYPHEN_DOMAIN_GOOD("something-else@et-tu.com", true),
    NO_ADDRESS_BAD("@a.b.c.org", false),
    NO_DOMAIN_BAD("a", false),
    SPACE_BAD(" @a", false),
    HYPHEN_ADDRESS_BAD("\"-@a.org\"", false),
    UNDERSCORE_DOMAIN_BAD("someone@foo_bar.com", false);
    
    public final String emailAddress;
    public final boolean validAddress;
    
    private EmailAddressTestValue(String emailAddress, boolean validAddress) {
        this.emailAddress = emailAddress;
        this.validAddress = validAddress;
    }

}
