package com.liveperson.faas.systemtest;

import com.liveperson.faas.client.FaaSClient;
import com.liveperson.faas.client.FaaSEvent;
import com.liveperson.faas.client.FaaSWebClient;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class WebClientSystemTest {
    private final String REQUEST_ID = "RequestId";
    private final String EXTERNAL_SYSTEM = "externalSystem";

    private Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing().
                    load();
    private final FaaSClient CLIENT = new FaaSWebClient.Builder(dotenv.get("ACCOUNT_ID"))
            .withClientSecret(dotenv.get("CLIENT_SECRET"))
            .withClientId(dotenv.get("CLIENT_ID"))
            .build();
    private OptionalParams optionalParams;

    @Before
    public void init() {
        optionalParams = new OptionalParams();
    }

    @Test
    public void invokeByUUID() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));

        // lambda has to echo input
        String[] response = CLIENT.invokeByUUID(EXTERNAL_SYSTEM, dotenv.get("SUCCESS_LAMBDA_UUID"), input,
                String[].class, optionalParams);

        assertArrayEquals(inputData.toArray(new String[0]), response);
    }

    @Test
    public void invokeByUUIDWithRequestId() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));
        optionalParams.setRequestId(REQUEST_ID);

        // lambda has to echo input
        String[] response = CLIENT.invokeByUUID(EXTERNAL_SYSTEM, dotenv.get("SUCCESS_LAMBDA_UUID"), input,
                String[].class, optionalParams);

        assertArrayEquals(inputData.toArray(new String[0]), response);
    }

    @Test(expected = FaaSException.class)
    public void invokeByUUIDFaaSExceptionThrown() throws FaaSException {
        FaaSInvocation<String> input = new FaaSInvocation<>();
        String inputString = "failure";
        input.setPayload(inputString);

        // Lambda has to throw an error
        CLIENT.invokeByUUID(EXTERNAL_SYSTEM, dotenv.get("FAILURE_LAMBDA_UUID"), input,
                String.class, optionalParams);
    }

    @Test
    public void invokeByEvent() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));

        // lambda has to echo input
        Response[] response = CLIENT.invokeByEvent(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("EVENT")), input,
                Response[].class, optionalParams);

        assertArrayEquals(inputData.toArray(new String[0]), response[0].getResult());
        assertArrayEquals(inputData.toArray(new String[0]), response[1].getResult());
    }

    @Test
    public void invokeByEventwithRequestId() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));
        optionalParams.setRequestId(REQUEST_ID);

        // lambda have to echo input
        Response[] response = CLIENT.invokeByEvent(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("EVENT")),
                input, Response[].class, optionalParams);

        assertArrayEquals(inputData.toArray(new String[0]), response[0].getResult());
        assertArrayEquals(inputData.toArray(new String[0]), response[1].getResult());
    }

    @Test
    public void invokeByEventString() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));

        // lambda have to echo input
        Response[] response = CLIENT.invokeByEvent(EXTERNAL_SYSTEM, dotenv.get("EVENT_AS_STRING"), input,
                Response[].class, optionalParams);

        assertArrayEquals(inputData.toArray(new String[0]), response[0].getResult());
        assertArrayEquals(inputData.toArray(new String[0]), response[1].getResult());
    }

    @Test(expected = FaaSException.class)
    public void invokeByEventExceptionThrown() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();

        // lambda has to throw error
        CLIENT.invokeByEvent(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("FAILURE_EVENT")), input, Response[].class
                , optionalParams);
    }

    @Test
    public void isImplementedTrue() throws FaaSException {
        // lambda with given event has to be implemented
        assertTrue(CLIENT.isImplemented(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("EVENT")), optionalParams));
    }

    @Test
    public void isImplementedEvenAsStringTrue() throws FaaSException {
        // lambda with given event has to be implemented
        assertTrue(CLIENT.isImplemented(EXTERNAL_SYSTEM, dotenv.get("EVENT_AS_STRING"), optionalParams));
    }

    @Test
    public void isImplementedFalse() throws FaaSException {
        // lambda with given event must not be implemented
        assertFalse(CLIENT.isImplemented(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("UNIMPLEMENTED_EVENT")),
                optionalParams));
    }

    @Test
    public void isImplementedEvenAsStringFalse() throws FaaSException {
        // lambda with given event must not be implemented
        assertFalse(CLIENT.isImplemented(EXTERNAL_SYSTEM, dotenv.get("UNIMPLEMENTED_EVENT_AS_STRING"), optionalParams));
    }

    @Test(expected = FaaSException.class)
    public void isImplementedTimesOut() throws FaaSException {
        // lambda with given event has to be implemented
        optionalParams.setTimeOutInMs(3);
        assertTrue(CLIENT.isImplemented(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("EVENT")), optionalParams));
    }

    @Test(expected = FaaSException.class)
    public void invokeByEventTimesOut() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));
        optionalParams.setTimeOutInMs(3);

        // lambda have to echo input
        CLIENT.invokeByEvent(EXTERNAL_SYSTEM, FaaSEvent.valueOf(dotenv.get("EVENT")),
                input, Response[].class, optionalParams);
    }

    @Test(expected = FaaSException.class)
    public void invokeByUUIDTimesOut() throws FaaSException {
        FaaSInvocation<String[]> input = new FaaSInvocation<>();
        List<String> inputData = new ArrayList<>();
        inputData.add("This");
        inputData.add("is");
        inputData.add("a");
        inputData.add("Test");
        input.setPayload((inputData.toArray(new String[0])));
        optionalParams.setTimeOutInMs(3);

        // lambda has to echo input
        CLIENT.invokeByUUID(EXTERNAL_SYSTEM, dotenv.get("SUCCESS_LAMBDA_UUID"), input,
                String[].class, optionalParams);
    }

}
