package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerXmlConstants;

public enum ContractMessageFixture {

    SINGLE_TEST_CONTRACT(HeaderFixture.SINGLE_TEST_CONTRACT, ContractFixture.EXAMPLE_TEST_ADOBE_SIGN);

    public final String version;
    public final HeaderFixture header;
    public final List<ContractFixture> contracts;

    private ContractMessageFixture(HeaderFixture header, ContractFixture... contracts) {
        this(JaggaerXmlConstants.DEFAULT_MESSAGE_VERSION, header, contracts);
    }

    private ContractMessageFixture(String version, HeaderFixture header, ContractFixture... contracts) {
        this.version = version;
        this.header = header;
        this.contracts = List.of(contracts);
    }

}
