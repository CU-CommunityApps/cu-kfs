package edu.cornell.kfs.vnd;

import java.util.Arrays;
import java.util.Optional;

public enum CemiForeignTaxIdTypes {
        INDONESIA("Indonesia", "ID", "IDN-LEGALF"),
        ITALY("Italy", "IT", "ITA-LEGALF"),
        NEW_ZEALAND("New Zealand", "NZ", "NZL-LEGALF"),
        TAIWAN("Taiwan", "TW", "TWN-LEGALF"),
        NORWAY("Norway", "NO", "NOR-LEGALF"),
        KOREA("Korea, Republic of", "KR", "KOR-LEGALF"),
        SINGAPORE("Singapore", "SG", "SGP-LEGALF"),
        CANADA("Canada", "CA", "CAN-LEGALF"),
        MALAYSIA("Malaysia", "MY", "MYS-LEGALF"),
        CHINA("China", "CN", "CHN-LEGALF"),
        UNITED_STATES("United States of America", "US", "USA-LEGALF"),
        SOUTH_AFRICA("South Africa", "ZA", "ZAF-LEGALF"),
        HONG_KONG("Hong Kong", "HK", "HKG-LEGALF"),
        INDIA("India", "IN", "IND-LEGALF"),
        THAILAND("Thailand", "TH", "THA-LEGALF"),
        MEXICO("Mexico", "MX", "MEX-LEGALF"),
        JAPAN("Japan", "JP", "JPN-LEGALF"),
        SWITZERLAND("Switzerland", "CH", "CHE-LEGALF"),
        SLOVAKIA("Slovakia", "SK", "SVK-LEGALF"),
        CZECHIA("Czechia", "CZ", "CZE-LEGALF"),
        SPAIN("Spain", "ES", "ESP-LEGALF"),
        LUXEMBOURG("Luxembourg", "LU", "LUX-LEGALF"),
        ROMANIA("Romania", "RO", "ROM-LEGALF"),
        IRELAND("Ireland", "IE", "IRE-LEGALF"),
        POLAND("Poland", "PL", "POL-LEGALF"),
        LITHUANIA("Lithuania", "LT", "LTU-LEGALF"),
        GREECE("Greece", "GR", "GRC-LEGALF"),
        UNITED_KINGDOM("United Kingdom", "UK", "UK-LEGALF"),
        GERMANY("Germany", "DE", "DEU-LEGALF"),
        FRANCE("France", "FR", "FRA-LEGALF"),
        FINLAND("Finland", "FI", "FIN-LEGALF"),
        ESTONIA("Estonia", "EE", "EST-LEGALF"),
        BULGARIA("Bulgaria", "BG", "BGR-LEGALF"),
        PORTUGAL("Portugal", "PT", "PRT-LEGALF"),
        HUNGARY("Hungary", "HU", "HUN-LEGALF"),
        DENMARK("Denmark", "DK", "DNK-LEGALF"),
        LATVIA("Latvia", "LV", "LVA-LEGALF"),
        CROATIA("Croatia", "HR", "HRV-LEGALF"),
        MALTA("Malta", "MT", "MLT-LEGALF"),
        SWEDEN("Sweden", "SE", "SWE-LEGALF"),
        AUSTRIA("Austria", "AT", "AUT-LEGALF"),
        BELGIUM("Belgium", "BE", "BEL-LEGALF"),
        NETHERLANDS("Netherlands", "NL", "NLD-LEGALF"),
        SLOVENIA("Slovenia", "SI", "SVN-LEGALF"),
        BRAZIL("Brazil", "BR", "BRA-LEGALF"),
        JORDAN("Jordan", "JO", "JOR-LEGALF"),
        COLOMBIA("Colombia", "CO", "COL-LEGALF"),
        CAMBODIA("Cambodia", "KH", "KHM-LEGALF"),
        IRAN("Iran", "IR", "IRN-LEGALF"),
        HAITI("Haiti", "HT", "HTI-LEGALF"),
        BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina", "BA", "BIH-LEGALF"),
        ECUADOR("Ecuador", "EC", "ECU-LEGALF"),
        DOMINICAN_REPUBLIC("Dominican Republic", "DO", "DOM-LEGALF"),
        GUATEMALA("Guatemala", "GT", "GTM-LEGALF"),
        ALBANIA("Albania", "AL", "ALB-LEGALF"),
        COSTA_RICA("Costa Rica", "CR", "COS-LEGALF"),
        ISRAEL("Israel", "IL", "ISR-LEGALF"),
        LEBANON("Lebanon", "LB", "LBN-LEGALF"),
        KAZAKHSTAN("Kazakhstan", "KZ", "KAZ-LEGALF"),
        ETHIOPIA("Ethiopia", "ET", "ETH-LEGALF"),
        ICELAND("Iceland", "IS", "ISL-LEGALF"),
        MONACO("Monaco", "MC", "MCO-LEGALF"),
        EGYPT("Egypt", "EG", "EGY-LEGALF"),
        BRUNEI("Brunei", "BN", "BRN-LEGALF"),
        NORTH_MACEDONIA("North Macedonia", "MK", "MKD-LEGALF"),
        BELARUS("Belarus", "BY", "BLR-LEGALF"),
        CHILE("Chile", "CL", "CHL-LEGALF"),
        RUSSIAN_FEDERATION("Russian Federation", "RU", "RUS-LEGALF"),
        SAUDI_ARABIA("Saudi Arabia", "SA", "SAU-LEGALF"),
        VIETNAM("Vietnam", "VN", "VNM-LEGALF"),
        PAKISTAN("Pakistan", "PK", "PAK-LEGALF"),
        NAMIBIA("Namibia", "NA", "NAM-LEGALF"),
        NICARAGUA("Nicaragua", "NI", "NIC-LEGALF"),
        PERU("Peru", "PE", "PER-LEGALF"),
        MOROCCO("Morocco", "MA", "MAR-LEGALF"),
        OMAN("Oman", "OM", "OMN-LEGALF"),
        NEPAL("Nepal", "NP", "NPL-LEGALF"),
        ARGENTINA("Argentina", "AR", "ARG-LEGALF"),
        TURKIYE("Türkiye", "TR", "TUR-LEGALF"),
        TUNISIA("Tunisia", "TN", "TUN-LEGALF"),
        SERBIA("Serbia", "RS", "SRB-LEGALF"),
        NIGERIA("Nigeria", "NG", "NGA-LEGALF"),
        MONTENEGRO("Montenegro", "ME", "MNE-LEGALF"),
        PHILIPPINES("Philippines", "PH", "PHL-LEGALF"),
        UKRAINE("Ukraine", "UA", "UKR-LEGALF"),
        UZBEKISTAN("Uzbekistan", "UZ", "UZB-LEGALF"),
        MONGOLIA("Mongolia", "MN", "MNG-LEGALF");

        private final String countryName;
        private final String isoCode;
        private final String taxIdType;

        CemiForeignTaxIdTypes(String countryName, String isoCode, String taxIdType) {
            this.countryName = countryName;
            this.isoCode = isoCode;
            this.taxIdType = taxIdType;
        }

        public String getCountryName() { return countryName; }
        public String getIsoCode() { return isoCode; }
        public String getTaxIdType() { return taxIdType; }

        // Lookup by ISO code (e.g. "US", "DE")
        public static Optional<CemiForeignTaxIdTypes> fromIsoCode(String isoCode) {
            if (isoCode == null) return Optional.empty();
            return Arrays.stream(values())
                    .filter(e -> isoCode.equalsIgnoreCase(e.isoCode))
                    .findFirst();
        }

        // Lookup by tax ID type string (e.g. "USA-LEGALF")
        public static Optional<CemiForeignTaxIdTypes> fromTaxIdType(String taxIdType) {
            if (taxIdType == null) return Optional.empty();
            return Arrays.stream(values())
                    .filter(e -> taxIdType.equalsIgnoreCase(e.taxIdType))
                    .findFirst();
        }

        // Lookup by country name (case-insensitive)
        public static Optional<CemiForeignTaxIdTypes> fromCountryName(String name) {
            if (name == null) return Optional.empty();
            return Arrays.stream(values())
                    .filter(e -> name.equalsIgnoreCase(e.countryName))
                    .findFirst();
        }
    }