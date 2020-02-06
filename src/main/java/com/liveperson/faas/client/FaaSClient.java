package com.liveperson.faas.client;

import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.response.lambda.LambdaResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FaaS client for invoking lambdas of a account/brand
 *
 * @author sschwarz
 * @author arotaru
 */
public interface FaaSClient {
    /**
     * Invoking a lambda per brand via the RESTful api by event ID
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and timeOuts
     * @param <T> Class of the response type
     * @return  the response object of type responseType
     * @throws FaaSException when error occurs during lambda invocation
     */
    <T> T invokeByEvent(String externalSystem, FaaSEvent event, FaaSInvocation data, Class<T> responseType,
                        OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @param <T> Class of the response type
     * @return the response object of type responseType
     * @throws FaaSException  when error occurs during lambda invocation
     */
    <T> T invokeByEvent(String externalSystem, String event, FaaSInvocation data, Class<T> responseType,
                        OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID but does not return response of invocation
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException  when error occurs during lambda invocation
     */
    void invokeByEvent(String externalSystem, FaaSEvent event, FaaSInvocation data, OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID but does not return response of invocation
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException  when error occurs during lambda invocation
     */
    void invokeByEvent(String externalSystem, String event, FaaSInvocation data, OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by lambda UUID
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param lambdaUUID     the global unique identifier of a lambda
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and timeOuts
     * @param <T> Class of the response type
     * @return the response object of type responseType
     * @throws FaaSException  when error occurs during lambda invocation
     */
    <T> T invokeByUUID(String externalSystem, String lambdaUUID, FaaSInvocation data, Class<T> responseType,
                       OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by lambda UUID but does not return response of invocation
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param lambdaUUID     the global unique identifier of a lambda
     * @param data           the invocation payload containing timestamp (ms), headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException  when error occurs during lambda invocation
     */
    void invokeByUUID(String externalSystem, String lambdaUUID, FaaSInvocation data, OptionalParams optionalParams) throws FaaSException;

    /**
     * Checking if lambda implementation exist for a given event
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @return boolean
     * @throws FaaSException  when error occurs during isImplemented request
     */
    boolean isImplemented(String externalSystem, FaaSEvent event, OptionalParams optionalParams) throws FaaSException;

    /**
     * Checking if lambda implementation exist for a given event
     *
     * @param externalSystem the name of the external system doing the invocation
     * @param event          the faas event
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @return boolean
     * @throws FaaSException when error occurs during isImplemented request
     */
    boolean isImplemented(String externalSystem, String event, OptionalParams optionalParams) throws FaaSException;

    /**
     * Get a list of lambdas. Filters can be applied by state, eventId or name. EventId and name supports substring.
     *
     * @param userId              LE user id
     * @param optionalQueryParams optional query params that are used for filtering - supported params: state,
     *                            eventId, name
     * @param optionalParams      optional Parameters for request - requestId and timeOut
     * @return A list of lambdas that belong that the above account filtered by the optionalQueryParams
     * @throws FaaSException when error occurs during getLambda request
     */
    List<LambdaResponse> getLambdas(String userId, Map<String, String> optionalQueryParams,
                                    OptionalParams optionalParams) throws FaaSException;
}
