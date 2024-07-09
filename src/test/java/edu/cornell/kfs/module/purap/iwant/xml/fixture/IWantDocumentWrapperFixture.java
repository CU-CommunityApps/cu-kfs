package edu.cornell.kfs.module.purap.iwant.xml.fixture;

import java.util.List;

import edu.cornell.kfs.module.purap.iwant.xml.IWantDocumentWrapperXml;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;

public enum IWantDocumentWrapperFixture {

    FULL_EXAMPLE(documents(IWantDocumentFixture.FULL_EXAMPLE));

    public final List<IWantDocumentFixture> iWantDocuments;

    private IWantDocumentWrapperFixture(IWantDocumentFixture[] documents) {
        this.iWantDocuments = XmlDocumentFixtureUtils.toImmutableList(documents);
    }

    public IWantDocumentWrapperXml toIWantDocumentWrapperXml() {
        IWantDocumentWrapperXml wrapper = new IWantDocumentWrapperXml();

        for (IWantDocumentFixture doc : iWantDocuments) {
            wrapper.getiWantDocuments().add(doc.toIWantDocumentXml());
        }

        return wrapper;
    }

    private static IWantDocumentFixture[] documents(IWantDocumentFixture... fixtures) {
        return fixtures;
    }

}
