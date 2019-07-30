package com.liveperson.faas.client;

import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.response.lambda.LambdaResponse;

import java.util.List;
import java.util.Map;

/**
 * FaaS client for invoking lambdas of a account/brand
 *
 * @author arotaru
 */
public interface FaaSClient {
    /**
     * Invoking a lambda per brand via the RESTful api by lambda UUID
     * @param externalSystem the name of the external system doing the invocation
     * @param accountId the brand/account id
     * @param lambdaUUID the global unique identifier of a lambda
     * @param data the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param responseType the type of the response object
     * @return the response object of type responseType
     * @throws FaaSException
     */
    public <T> T invoke(String externalSystem, String accountId, String lambdaUUID, FaaSInvocation data, Class<T> responseType) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID
     * @param externalSystem the name of the external system doing the invocation
     * @param accountId the brand/account id
     * @param event the faas event
     * @param data the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param responseType the type of the response object
     * @return the response object of type responseType
     * @throws FaaSException
     */
    public <T> T invoke(String externalSystem, String accountId, FaaSEvent event, FaaSInvocation data, Class<T> responseType) throws FaaSException;


    /**
     * Checking if lambda implementation exist for a given event
     * @param externalSystem the name of the external system doing the invocation
     * @param accountId the brand/account id
     * @param event the faas event
     * @return boolean
     * @throws FaaSException
     */
    public boolean isImplemented(String externalSystem, String accountId, FaaSEvent event) throws FaaSException;

    /**
     * Get a list of lambdas. Filters can be applied by state, eventId or name. EventId & name supports substring.
     * @param accountId brand/site/account id
     * @param userId LE user id
     * @param optionalQueryParams optional query params that are used for filtering - supported params: state, eventId, name
     * @return A list of lambdas that belong that the above account filtered by the optionalQueryParams
     * @throws FaaSException
     */
    List<LambdaResponse> getLambdasOfAccount(String accountId, String userId, Map<String, String> optionalQueryParams) throws FaaSException;
}
