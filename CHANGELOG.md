# pusher-java-client changelog

## Version 0.2.0
2013-11-08 mdpye
 * Fix up pom for inclusion in Maven Central

2013-11-06
 * Initiate ping messages from client on activity timeout
   Teardown connection if no response seen from server

2013-11-04 mdpye
 * Import a logging framework (slf4j)
 * Log error with additional info and continue when trying to 
   transition from disconnected->disconnected state

2013-11-01 mdpye
 * Fix state leakage between Pusher instances
 * Support setting alternative endpoint
