# Changelog

## 2.4.4

- [CHANGED] pusher.pro narrower proguard rules.

## 2.4.3

- [FIXED] Fix issue with json serialization when using proguard

## Version 2.4.2 - 23th Sep 2022
* Fixes a crash when an event comes in without and escaped data json member.
* update to com.google.code.gson:gson:2.9.1
* update to org.java-websocket:Java-WebSocket:1.5.3

## Version 2.4.1 - 13th Sep 2022
* Refactoring and code cleanup of event handling in the SDK
* Fixes subscription_count events

## Version 2.4.0 - 15th July 2022
* Add support for Subscription count events

## Version 2.3.0 - 4th July 2022
* Added support for user sign in and server to user messages
* Fixed issue with calling disconnect while the client is attempting reconnection
* Fixed issue with calling connect while the client is disconnecting

## Version 2.2.8 - 30th November 2021
* Add global [event listner](https://github.com/pusher/pusher-websocket-java/issues/305)

## Version 2.2.7 - 11th November 2021
* Fixed an [issue Vulnerability with legacy Gson dependency](https://github.com/pusher/pusher-websocket-java/issues/305)

## Version 2.2.6 - 17th March 2021
* Fixed an [issue causing a NullPointerException if event data on a Presence channel was malformed or missing](https://github.com/pusher/pusher-websocket-java/pull/290)

## Version 2.2.5 - 11th August 2020
* Fixed an [issue with the Java-WebSocket library with android pre-sdk24 devices causing a NoSuchMethod exception](https://github.com/pusher/pusher-websocket-java/pull/284)

## Version 2.2.4 - 3rd August 2020
* Removed unnecessary code in the TweetNaClFast implementation
* Updated the Java-WebSocket library to 1.5.1 as the [current version 1.4.0 has a known critical vulnerability.](https://github.com/pusher/pusher-websocket-java/issues/279)

## Version 2.2.3 - 8th July 2020

* Fixed LazySodium causing a crash on Android devices by swapping it out for [TweetNaCl](https://github.com/InstantWebP2P/tweetnacl-java)

## Version 2.2.2 - 6th July 2020

* Fixed an issue where some private encrypted messages were not decrypted accurately by swapping our implementation of SecretBoxOpener for one provided by [Lazy Sodium](https://github.com/terl/lazysodium-java)

## Version 2.2.1 - 22nd April 2020

* Changed PusherOptions `setForceTLS` and `isForceTLS` to `setUseTLS` and `isUseTLS` to align with the other client SDKs.

## Version 2.2.0 - 22nd April 2020

* Changed PusherOptions `setEncrypted` and `isEncrypted` to `setForceTLS` and `isForceTLS` to reduce confusion between this option and private encrypted channels.

## Version 2.1.1 - 15th April 2020

* Fix a case where multiple websocket connections could be opened at once
  if reconnection was triggered by an activity timeout.

## Version 2.1.0 - 8th April 2020

* Added support for [private encrypted channels](https://pusher.com/docs/channels/using_channels/encrypted-channels)

## Version 2.0.2

* Make a no-op state change non-fatal, allowing transitions to/from the same state

## Version 2.0.1

* Forward `channel_data` returned by the auth endpoint when subscribing to private channels

## Version 2.0.0

* The onEvent handler now gets called with one parameter, called the PusherEvent. This PusherEvent has all the same information available as was available before with the 3 parameters, but now is accessible by calling getChannelName(), getData() or getEventName() on the PusherEvent object you receive. In addition, for presence channel client events, you can now retrieve an authenticated User ID by calling getUserId() on the PusherEvent object. To read more on Authenticated users, see the README or our docs [here](https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events).
* Update Java sourceCompatibility and targetCompatibility to 1.8.
* Fix an issue where the reconnect logic would not be reset after connect() is called again.
* Depend on `org.java-websocket:Java-WebSocket:1.4.0` instead of `com.pusher:java-websocket:1.4.1`.

## Version 1.4.0
* Update the dependency to use pusher/java-websocket fork and remove dependency on clojars.org repository.

## Version 1.3.0
* Add retry logic when the connection is lost
* Accept 201 status code from auth endpoints

## Version 1.2.2

 * Improve resillience of pong timeout disconnections

## Version 1.2.0

2016-07-05 jpatel531

  * Expose Client interface

## Version 1.1.3

2016-05-12 mdpye, plackemacher, jpatel531

  * Fix exceptions where tasks were being pushed onto a shutting-down event queue.
  * Reduce construction of GSON objects and keep one centralized instance.

## Version 1.1.2

2016-03-23 jpatel531

  * Fix cases where the library would throw an error due to a connection state change
    from `CONNECTING` to `CONNECTING`. More information [here](https://github.com/pusher/pusher-websocket-java/pull/102)
  * Fix `ConcurrentModificationException` on event listeners.

## Version 1.1.1

2016-03-22 jpatel531

  * Removes the dependency on slf4j-log4j

## Version 1.1.0

2016-03-09 jpatel531, jameshfisher

  * Allow specifying a proxy via which to connect to Pusher.

## Version 1.0.2

2015-11-06 leggetter, siggijons
  * Use @SerializedName in PresenceChannelImpl for better serialization support across languages e.g. Turkish

## Version 1.0.1

2015-11-06 hamchapman, jpatel531
  * Resolves issues where Gson would cast numeric user ids as doubles before converting them to a string, leading to inconsistencies

## Version 1.0.0

2015-10-7
  * Use generic `Map` for `HttpAuthorizer` - trevorrjohn
  * Add to `Pusher` `isSubscribed` `getChannel`, `getPrivateChannel` and `getPresenceChannel` methods - jpatel531
  * Library unsubscribes asynchronously - jpatel531
  * Synchronize access to channel event listeners - trevorrjohn, jpatel531, mdpye

## Version 0.3.3
2015-02-09 mdpye
 * REALLY remove the JavaWebsockets submodule

## Version 0.3.2
2015-02-05 mdpye
 * Resolve dependency embedding issues, Java-Websockets is now published to clojars.org
2014-12-11 roccozanni
 * Allow unsubscribing while disconnected (will not re-subscribe when connection is restored)
2014-02-04 mdpye
 * Make ChannelImpl.state volatile - it is potentially read from any thread in pre-send checks triggering client messages

## Version 0.3.1
2013-12-13 mdpye
 * Add a generic `<V> V User.getInfo(Class<V>)` which parses the user info into an instance of `V`.

## Version 0.3.0
2013-12-13 mdpye
 * BREAKING CHANGE `User.getInfo()` returns valid JSON encoded String rather than the encoding provided by `java.util.AbstractMap.toString()`

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
