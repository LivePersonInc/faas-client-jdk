package com.liveperson.faas.csds;

import com.liveperson.faas.exception.CsdsRetrievalException;

/**
 * Retrieves base domains from LivePerson api
 *
 * @author sschwarz
 */
public interface CsdsClient {

    /**
     * @param service whose name is to be retrieved
     * @return the base Domain for given service
     * @throws CsdsRetrievalException when service that is queried for does not exist
     */
    String getDomain(String service) throws CsdsRetrievalException;

}
