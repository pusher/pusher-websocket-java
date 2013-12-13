# pusher-java-client changelog

## Version 0.3.0
2013-12-13 mdpye
 * BREAKING CHANGE User.getUserInfo() returns valid JSON encoded String rather than the encoding provided by java.util.AbstractMap.toString()

## Version 0.2.3
2013-12-06 mdpye
 * Simply ping-pong by switching to a model of cancellable timers rather than scheduled checks

## Version 0.2.2
2013-11-18 mdpye
 * Use more robust method for finding artifact version

## Version 0.2.1
2013-11-14 mdpye
 * EventQueue made a daemon thread and also shutdown on disconnect

## Version 0.2.0
2013-11-08 mdpye
 * Fix up pom for inclusion in Maven Central

2013-11-07
 * Do not choke on user_id if it is JSON encoded as a number
   rather than a string

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
