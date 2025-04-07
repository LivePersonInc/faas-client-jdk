package com.liveperson.faas.client;

import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.CsdsRetrievalException;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.response.lambda.FunctionResponse;
import com.liveperson.faas.response.lambda.LambdaResponse;

import java.util.List;
import java.util.Map;

/**
 * FaaS client for invoking Functions of a account/brand
 *
 * @author sschwarz
 * @author arotaru
 */
public interface FaaSClient {
        /**
         * Invoking a function per brand via the RESTful api by event ID
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param responseType   the type of the response object
         * @param optionalParams optional Parameters for request - requestId and
         *                       timeOuts
         * @param <T>            Class of the response type
         * @return the response object of type responseType
         * @throws FaaSException when error occurs during function invocation
         */
        <T, R> T invokeByEvent(String lpEventSource, FaaSEvent event, FaaSInvocation data, Class<T> responseType,
                        OptionalParams optionalParams) throws FaaSException;

        /**
         * Invoking a function per brand via the RESTful api by event ID
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param responseType   the type of the response object
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @param <T>            Class of the response type
         * @return the response object of type responseType
         * @throws FaaSException when error occurs during function invocation
         */
        <T, R> T invokeByEvent(String lpEventSource, String event, FaaSInvocation data, Class<T> responseType,
                        OptionalParams optionalParams) throws FaaSException;

        /**
         * Invoking a function per brand via the RESTful api by event ID but does not
         * return response of invocation
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @throws FaaSException when error occurs during function invocation
         */
        void invokeByEvent(String lpEventSource, FaaSEvent event, FaaSInvocation data, OptionalParams optionalParams)
                        throws FaaSException;

        /**
         * Invoking a function per brand via the RESTful api by event ID but does not
         * return response of invocation
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @throws FaaSException when error occurs during function invocation
         */
        void invokeByEvent(String lpEventSource, String event, FaaSInvocation data, OptionalParams optionalParams)
                        throws FaaSException;

        /**
         * Invoking a function per brand via the RESTful api by function UUID
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param functionUUID   the global unique identifier of a function
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param responseType   the type of the response object
         * @param optionalParams optional Parameters for request - requestId and
         *                       timeOuts
         * @param <T>            Class of the response type
         * @return the response object of type responseType
         * @throws FaaSException when error occurs during function invocation
         */
        <T, R> T invokeByUUID(String lpEventSource, String functionUUID, FaaSInvocation data, Class<T> responseType,
                        OptionalParams optionalParams) throws FaaSException;

        /**
         * Invoking a function per brand via the RESTful api by function UUID but does
         * not
         * return response of invocation
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param functionUUID   the global unique identifier of a function
         * @param data           the invocation payload containing timestamp (ms),
         *                       headers and payload send to the function
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @throws FaaSException when error occurs during function invocation
         */
        void invokeByUUID(String lpEventSource, String functionUUID, FaaSInvocation data, OptionalParams optionalParams)
                        throws FaaSException;

        /**
         * Checking if function implementation exist for a given event
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @return boolean
         * @throws FaaSException when error occurs during isImplemented request
         */
        boolean isImplemented(String lpEventSource, FaaSEvent event, OptionalParams optionalParams)
                        throws FaaSException;

        /**
         * Checking if function implementation exist for a given event
         *
         * @param lpEventSource  the name of the source system doing the invocation
         * @param event          the faas event
         * @param optionalParams optional Parameters for request - requestId and timeOut
         * @return boolean
         * @throws FaaSException when error occurs during isImplemented request
         */
        boolean isImplemented(String lpEventSource, String event, OptionalParams optionalParams) throws FaaSException;

        /**
         * Get a list of V1 Functions. Filters can be applied by state, eventId or name.
         * EventId and name supports substring.
         *
         * @param userId              LE user id
         * @param optionalQueryParams optional query params that are used for filtering
         *                            - supported params: state,
         *                            eventId, name
         * @param optionalParams      optional Parameters for request - requestId and
         *                            timeOut
         * @return A list of functions that belong that the above account filtered by
         *         the
         *         optionalQueryParams
         * @throws FaaSException when error occurs during getLambdas request
         * @deprecated Compatible Only with V1 Functions. For V2 use 'getFunctions'
         *             instead.
         * 
         */
        List<LambdaResponse> getLambdas(String userId, Map<String, String> optionalQueryParams,
                        OptionalParams optionalParams) throws FaaSException;

        /**
         * Get a list of V2 Functions. Filters can be applied by state, eventId or
         * functionName.
         * EventId and functionName supports substring.
         *
         * @param userId              LE user id
         * @param optionalQueryParams optional query params that are used for filtering
         *                            - supported params: state,
         *                            eventId, functionName
         * @param optionalParams      optional Parameters for request - requestId and
         *                            timeOut
         * @return A list of Functions that belong to the above account filtered by
         *         the optionalQueryParams
         * @throws FaaSException when error occurs during getFunctions request
         */
        List<FunctionResponse> getFunctions(String userId, Map<String, String> optionalQueryParams,
                        OptionalParams optionalParams) throws FaaSException;

        /**
         * Determines whether the accounts associated with the client instance have a V2
         * domain from the CSDS. It only considers the GW domain. If the GW domain is
         * V2, it is assumed that the UI domain is also V2.
         * 
         * @return true if Client instance account is V2.
         * @throws CsdsRetrievalException
         * @deprecated Once transition to V2 is completed will be removed.
         */
        boolean isV2Domain() throws CsdsRetrievalException;
}
