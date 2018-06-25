# faas-client
FaaS client for invoking lambdas via the eventsource gateway (a.k.a Asgard)

## Preparing data for invocation
```java
String accountId = "le49829325";
String lambdaUUID = "545fc8a2-b9d9-4e76-9b55-17b38e6181c9";
String externalSystem = "botStudio";
String apiKey = "d7e8ddd8995344cb8a8373a060958e7f";
String apiSecret = "b16d4dee1f7ab6e4";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};

//Set payload
User payload = new User();
payload.name = "John Doe";

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);
```

## Invoking a lambda via the RESTful API
```java
try {
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain, apiKey, apiSecret);

    User result = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, User.class);
    ...

} catch (FaaSException e) {...}
```