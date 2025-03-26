package com.liveperson.faas.client;

import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.CsdsRetrievalException;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.response.lambda.FunctionResponse;
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
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and
     *                       timeOuts
     * @param <T>            Class of the response type
     * @return the response object of type responseType
     * @throws FaaSException when error occurs during lambda invocation
     */
    <T> T invokeByEvent(String lpEventSource, FaaSEvent event, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @param <T>            Class of the response type
     * @return the response object of type responseType
     * @throws FaaSException when error occurs during lambda invocation
     */
    <T> T invokeByEvent(String lpEventSource, String event, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID but does not
     * return response of invocation
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException when error occurs during lambda invocation
     */
    void invokeByEvent(String lpEventSource, FaaSEvent event, FaaSInvocation data, OptionalParams optionalParams)
            throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by event ID but does not
     * return response of invocation
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException when error occurs during lambda invocation
     */
    void invokeByEvent(String lpEventSource, String event, FaaSInvocation data, OptionalParams optionalParams)
            throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by lambda UUID
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param functionUUID   the global unique identifier of a lambda
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param responseType   the type of the response object
     * @param optionalParams optional Parameters for request - requestId and
     *                       timeOuts
     * @param <T>            Class of the response type
     * @return the response object of type responseType
     * @throws FaaSException when error occurs during lambda invocation
     */
    <T> T invokeByUUID(String lpEventSource, String functionUUID, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException;

    /**
     * Invoking a lambda per brand via the RESTful api by lambda UUID but does not
     * return response of invocation
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param functionUUID   the global unique identifier of a lambda
     * @param data           the invocation payload containing timestamp (ms),
     *                       headers and payload send to the lambda
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @throws FaaSException when error occurs during lambda invocation
     */
    void invokeByUUID(String lpEventSource, String functionUUID, FaaSInvocation data, OptionalParams optionalParams)
            throws FaaSException;

    /**
     * Checking if lambda implementation exist for a given event
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @return boolean
     * @throws FaaSException when error occurs during isImplemented request
     */
    boolean isImplemented(String lpEventSource, FaaSEvent event, OptionalParams optionalParams) throws FaaSException;

    /**
     * Checking if lambda implementation exist for a given event
     *
     * @param lpEventSource  the name of the source system doing the invocation
     * @param event          the faas event
     * @param optionalParams optional Parameters for request - requestId and timeOut
     * @return boolean
     * @throws FaaSException when error occurs during isImplemented request
     */
    boolean isImplemented(String lpEventSource, String event, OptionalParams optionalParams) throws FaaSException;

    /**
     * Get a list of lambdas. Filters can be applied by state, eventId or name.
     * EventId and name supports substring.
     *
     * @param userId              LE user id
     * @param optionalQueryParams optional query params that are used for filtering
     *                            - supported params: state,
     *                            eventId, name
     * @param optionalParams      optional Parameters for request - requestId and
     *                            timeOut
     * @return A list of lambdas that belong that the above account filtered by the
     *         optionalQueryParams
     * @throws FaaSException when error occurs during getLambda request
     * @deprecated compatible with V1 and V2 Functions, using 'getFunctions' is
     *             recomended for V2 functions,
     * 
     */
    List<LambdaResponse> getLambdas(String userId, Map<String, String> optionalQueryParams,
            OptionalParams optionalParams) throws FaaSException;

    /**
     * Get a list of Functions. Only V2 Filters can be applied by state, eventId or
     * name.
     * EventId and name supports substring.
     *
     * @param userId              LE user id
     * @param optionalQueryParams optional query params that are used for filtering
     *                            - supported params: state,
     *                            eventId, functionName
     * @param optionalParams      optional Parameters for request - requestId and
     *                            timeOut
     * @return A list of Functions that belong that the above account filtered by
     *         the
     *         optionalQueryParams
     * @throws FaaSException when error occurs during getLambda request
     */
    List<FunctionResponse> getFunctions(String userId, Map<String, String> optionalQueryParams,
            OptionalParams optionalParams) throws FaaSException;

    /**
     * Determines if the accounts associated to the client instance has V2 domain
     * from the CSDS.
     * 
     * @return true is Client instance account is V2
     * @throws CsdsRetrievalException
     */
    public boolean isV2Domain() throws CsdsRetrievalException;
}
