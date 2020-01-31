package com.liveperson.faas.client;

import org.junit.Test;

public class FaaSWebClientBuilderTest {

    private static final String ACCOUNT_ID = "accountId";

    private FaaSWebClient.Builder builder = new FaaSWebClient.Builder(ACCOUNT_ID);


    @Test(expected = IllegalStateException.class)
    public void buildWithNoAuthSignatureBuilderAndMissingClientSecretAndID() {
        FaaSWebClient faaSClient = builder.build();
    }

}