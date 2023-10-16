# Authentication Library
The Authentication Library provides a set of utility classes and filters to handle authentication in a microservices architecture.

##Features
 - ServiceAuthenticationFilter: A filter that intercepts incoming requests and verifies the presence of a valid bearer token in the Authorization header. It then checks with the authentication service to fetch the user's role and sets up the authentication context.

 - AuthFeignClient: A Feign client for making API calls to the authentication service.

 - Exception Handling: RestApiClientException for 4xx, RestApiServerException for 5xx (Should be handled in ExceptionHandler)
