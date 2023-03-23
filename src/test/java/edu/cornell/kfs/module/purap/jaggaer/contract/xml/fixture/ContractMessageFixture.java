package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.util.List;

public enum ContractMessageFixture {

    MESSAGE01("1.0", null);

    public final String version;
    public final HeaderFixture header;
    public final List<ContractFixture> contracts;

    private ContractMessageFixture(String version, HeaderFixture header, ContractFixture... contracts) {
        this.version = version;
        this.header = header;
        this.contracts = List.of(contracts);
    }

}
