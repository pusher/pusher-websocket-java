# Pusher Channels Java Client

[![Build Status](https://travis-ci.org/pusher/pusher-websocket-java.svg?branch=master)](https://travis-ci.org/pusher/pusher-websocket-java)
[![codecov](https://codecov.io/gh/pusher/pusher-websocket-java/branch/master/graph/badge.svg)](https://codecov.io/gh/pusher/pusher-websocket-java)
[![Maven Central](https://img.shields.io/maven-central/v/com.pusher/pusher-java-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.pusher%22%20AND%20a:%22pusher-java-client%22)

Pusher Channels client library for Java targeting **Android** and general Java.

## Supported platforms

* Java SE - supports versions 8, 11 and 17
* Oracle JDK
* OpenJDK
* Android 7 and above. 5 and 6 will require [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring).

## TOC

This README covers the following topics:

- [Pusher Channels Java Client](#pusher-channels-java-client)
  - [Supported platforms](#supported-platforms)
  - [TOC](#toc)
  - [Installation](#installation)
    - [Maven](#maven)
    - [Gradle](#gradle)
    - [Download](#download)
    - [Source](#source)
  - [API Overview](#api-overview)
  - [The Pusher constructor](#the-pusher-constructor)
  - [Connecting](#connecting)
  - [The PusherOptions object](#the-pusheroptions-object)
  - [Reconnecting](#reconnecting)
  - [Disconnecting](#disconnecting)
  - [Listening to connection events](#listening-to-connection-events)
  - [Subscribing to channels](#subscribing-to-channels)
    - [Public channels](#public-channels)
    - [Private channels](#private-channels)
    - [Private encrypted channels](#private-encrypted-channels)
    - [Presence channels](#presence-channels)
      - [The User object](#the-user-object)
      - [Client event authenticity](#client-event-authenticity)
  - [Binding and handling events](#binding-and-handling-events)
    - [ChannelEventListener](#channeleventlistener)
    - [SubscriptionEventListener](#subscriptioneventlistener)
    - [Unbinding event listeners](#unbinding-event-listeners)
    - [Example](#example)
  - [Triggering events](#triggering-events)
  - [Accessing the connection socket ID](#accessing-the-connection-socket-id)
  - [Helper Methods](#helper-methods)
    - [Getting a channel from string](#getting-a-channel-from-string)
      - [Basic channels](#basic-channels)
      - [Private channels](#private-channels-1)
      - [Presence channels](#presence-channels-1)
    - [Check if a channel has subscribed](#check-if-a-channel-has-subscribed)
  - [JavaDocs](#javadocs)
  - [Library Development Environment](#library-development-environment)
    - [Prerequisites](#prerequisites)
    - [Cloning the project](#cloning-the-project)
    - [Android Studio](#android-studio)
    - [Eclipse Project](#eclipse-project)
    - [Build](#build)
    - [Run the Example Application](#run-the-example-application)

## Installation

The compiled library is available in two ways:

### Maven

The pusher-java-client is available in Maven Central.

```xml
<dependencies>
    <dependency>
      <groupId>com.pusher</groupId>
      <artifactId>pusher-java-client</artifactId>
      <version>2.4.4</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
  implementation 'com.pusher:pusher-java-client:2.4.4'
}
```

### Download

You can download a version of the `.jar` directly from <https://repo1.maven.org/maven2/com/pusher/pusher-java-client/>

### Source

You can build the project from the source in this repository. See **Library development environment** for more information on build environment.

## API Overview

Here's the API in a nutshell.

```java
// Create a new Pusher instance
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);

pusher.connect(new ConnectionEventListener() {
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
        System.out.println("State changed to " + change.getCurrentState() +
                           " from " + change.getPreviousState());
    }

    @Override
    public void onError(String message, String code, Exception e) {
        System.out.println("There was a problem connecting!");
    }
}, ConnectionState.ALL);

// Subscribe to a channel
Channel channel = pusher.subscribe("my-channel");

// Bind to listen for events called "my-event" sent to "my-channel"
channel.bind("my-event", new SubscriptionEventListener() {
    @Override
    public void onEvent(PusherEvent event) {
        System.out.println("Received event with data: " + event.toString());
    }
});

// Disconnect from the service
pusher.disconnect();

// Reconnect, with all channel subscriptions and event bindings automatically recreated
pusher.connect();
// The state change listener is notified when the connection has been re-established,
// the subscription to "my-channel" and binding on "my-event" still exist.
```

More information in reference format can be found below.

## The Pusher constructor

The standard constructor take an application key which you can get from the app's API Access section in the Pusher Channels dashboard.

```java
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);
```

If you are going to use [private](https://pusher.com/docs/channels/using_channels/private-channels) or [presence](https://pusher.com/docs/channels/using_channels/presence-channels) channels then you will need to provide an `ChannelAuthorizer` to be used when authenticating subscriptions. In order to do this you need to pass in a `PusherOptions` object which has had an `ChannelAuthorizer` set.

```java
HttpChannelAuthorizer channelAuthorizer = new HttpChannelAuthorizer("http://example.com/some_auth_endpoint");
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER).setChannelAuthorizer(channelAuthorizer);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);
```

See the documentation on [Authorizing Users](https://pusher.com/docs/channels/server_api/authorizing-users) for more information.

If you need finer control over the endpoint then the setHost, setWsPort and setWssPort methods can be employed.
## Connecting

In order to send and receive messages you need to connect to Channels.

```java
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);
pusher.connect();
```

## The PusherOptions object

Most of the functionality of this library is configured through the PusherOptions object. You configure it by calling
methods with parameters on the object before passing it to the Pusher object. Below is a table containing all of the
methods you can call.

| Method                      | Parameter         | Description                                                                                                                                   |
|-----------------------------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| setEncrypted                | Boolean           | Sets whether the connection should be made with TLS or not.                                                                                   |
| setChannelAuthorizer        | ChannelAuthorizer | Sets the channel authorizer to be used when authorizing private and presence channels.                                                        |
| setHost                     | String            | The host to which connections will be made.                                                                                                   |
| setWsPort                   | int               | The port to which unencrypted connections will be made. Automatically set correctly.                                                          |
| setWssPort                  | int               | The port to which encrypted connections will be made. Automatically set correctly.                                                            |
| setCluster                  | String            | Sets the cluster the client will connect to, thereby setting the Host and Port correctly.                                                     |
| setActivityTimeout          | long              | The number of milliseconds of inactivity at which a "ping" will be triggered to check the connection. The default value is 120,000.           |
| setPongTimeout              | long              | The number of milliseconds the client waits to receive a "pong" response from the server before disconnecting. The default value is 30,000.   |
| setMaxReconnectionAttempts  | int               | Number of reconnection attempts that will be made when pusher.connect() is called, after which the client will give up.                       |
| setMaxReconnectGapInSeconds | int               | The delay in two reconnection extends exponentially (1, 2, 4, .. seconds) This property sets the maximum inbetween two reconnection attempts. |
| setProxy                    | Proxy             | Specify a proxy, e.g. ```options.setProxy( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "proxyaddress", 80 ) ) )```                     |

## Reconnecting

The `connect` method is also used to re-connect in case the connection has been lost, for example if an Android
device loses reception. Note that the state of channel subscriptions and event bindings will be preserved while
disconnected and re-negotiated with the server once a connection is re-established.

## Disconnecting

```java
pusher.disconnect();
```

After disconnection the Pusher instance will release any internally allocated resources (threads and network connections)

## Listening to connection events

Implement the `ConnectionEventListener` interface to receive connection state change events:

```java
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);
pusher.connect(new ConnectionEventListener() {
    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
        System.out.println("State changed to " + change.getCurrentState() +
                           " from " + change.getPreviousState());
    }

    @Override
    public void onError(String message, String code, Exception e) {
        System.out.println("There was a problem connecting!");
    }
});
```

A series of `ConnectionState` members can be passed after the listener in this call to filter the states which will receive notification, e.g.

```java
// MyConnectionEventListener is notified only of transitions to the disconnected state
pusher.connect(new MyConnectionEventListener(), ConnectionState.DISCONNECTED);
```

For more information see [connection states](https://pusher.com/docs/channels/using_channels/connection#connection-states).

## Subscribing to channels

Channels uses the concept of [channels](https://pusher.com/docs/channels/using_channels/channels) as a way of subscribing to data. They are identified and subscribed to by a simple name. Events are bound to on a channels and are also identified by name. To listen to an event you need to implemented the `ChannelEventListener` interface (see **Binding and handling events**).

As mentioned above, channel subscriptions need only be registered once per `Pusher` instance. They are preserved across disconnection and re-established with the server on reconnect. They should NOT be re-registered. They may, however, be registered with a `Pusher` instance before the first call to `connect` - they will be completed with the server as soon as a connection becomes available.

### Public channels

```java
Channel channel = pusher.subscribe("my-channel");
```

If you wish to be informed when the subscription succeeds, pass an implementation of the `ChannelEventListener` interface:

```java
Channel channel = pusher.subscribe("my-channel", new ChannelEventListener() {
    @Override
    public void onSubscriptionSucceeded(String channelName) {
        System.out.println("Subscribed to channel: " + channelName);
    }

    // Other ChannelEventListener methods
});
```
If you wish to be informed for subscription count events, use the `bind` function to listen to event type `pusher:subscription_count`:

```java
Channel channel = pusher.subscribe("my-channel");
channel.bind("pusher:subscription_count", new SubscriptionEventListener() {
    @Override
    public void onEvent(PusherEvent event) {
        System.out.println("Received event with data: " + event.toString());
        System.out.println("Subscription Count is: " + channel.getCount());
    }
});

```

### Private channels

It's possible to subscribe to [private channels](https://pusher.com/docs/channels/using_channels/private-channels) that provide a mechanism for [authorizing channel subscriptions](https://pusher.com/docs/channels/server_api/authorizing-users). In order to do this you need to provide a `ChannelAuthorizer` when creating the `Pusher` instance (see **The Pusher constructor** above).

The library provides a `HttpChannelAuthorizer` implementation of `ChannelAuthorizer` which makes an HTTP `POST` request to an authorization endpoint. However, you can implement your own authorization mechanism if required.

Private channels are subscribed to as follows:

```java
PrivateChannel privateChannel = pusher.subscribePrivate( "private-channel" );
```

In addition to the events that are possible on public channels a private channel exposes an `onAuthenticationFailure`. This is called if the `ChannelAuthorizer` does not successfully authorize the subscription:

```java
PrivateChannel channel = pusher.subscribePrivate("private-channel",
    new PrivateChannelEventListener() {
        @Override
        public void onAuthenticationFailure(String message, Exception e) {
            System.out.println(
                String.format("Authentication failure due to [%s], exception was [%s]", message, e)
            );
        }

        // Other ChannelEventListener methods
    });
```

### Private encrypted channels

Similar to Private channels, you can also subscribe to a
[private encrypted channel](https://pusher.com/docs/channels/using_channels/encrypted-channels).
This library now fully supports end-to-end encryption. This means that only you and your connected clients will be able to read your messages. Pusher cannot decrypt them.

Like the private channel, you must provide your own authorization endpoint,
with your own encryption master key. There is a
[demonstration endpoint to look at using nodejs](https://github.com/pusher/pusher-channels-auth-example#using-e2e-encryption).

To get started you need to subscribe to your channel, provide a `PrivateEncryptedChannelEventListener`, and a list of the events you are
interested in, for example:

```java
PrivateEncryptedChannel privateEncryptedChannel =
	pusher.subscribePrivateEncrypted("private-encrypted-channel", listener, "my-event");
```

In addition to the events that are possible on public channels the
`PrivateEncryptedChannelEventListener` also has the following methods:
* `onAuthenticationFailure(String message, Exception e)` - This is called if
the `ChannelAuthorizer` does not successfully authorize the subscription:
* `onDecryptionFailure(String event, String reason);` - This is called if the message cannot be
decrypted. The decryption will attempt to refresh the shared secret key once
from the `ChannelAuthorizer`.

There is a
[working example in the repo](https://github.com/pusher/pusher-websocket-java/blob/master/src/main/java/com/pusher/client/example/PrivateEncryptedChannelExampleApp.java)
which you can use with the
[demonstration authorization endpoint](https://github.com/pusher/pusher-channels-auth-example#using-e2e-encryption)

### Presence channels

[Presence channels](https://pusher.com/docs/channels/using_channels/presence-channels) are private channels which provide additional events exposing who is currently subscribed to the channel. Since they extend private channels they also need to be authorized (see [authorizing channel subscriptions](https://pusher.com/docs/channels/server_api/authorizing-users)).

Presence channels can be subscribed to as follows:

```java
PresenceChannel presenceChannel = pusher.subscribePresence( "presence-channel" );
```

Presence channels provide additional events relating to users joining (subscribing) and leaving (unsubscribing) the presence channel. It is possible to listen to these events by implementing the `PresenceChannelEventListener`.

```java
PresenceChannel channel = pusher.subscribePresence("presence-channel",
    new PresenceChannelEventListener() {
        @Override
        public void onUsersInformationReceived(String channelName, Set<User> users) {
            for (User user : users) {
                userSubscribed(channelName, user);
            }
        }

        @Override
        public void userSubscribed(String channelName, User user) {
            System.out.println(
                String.format("A new user joined channel [%s]: %s, %s",
                              channelName, user.getId(), user.getInfo())
            );

            if (user.equals(channel.getMe())) {
                System.out.println("me");
            }
        }

        @Override
        public void userUnsubscribed(String channelName, User user) {
            System.out.println(
                String.format("A user left channel [%s]: %s %s",
                              channelName, user.getId(), user.getInfo())
            );
        }

        // Other ChannelEventListener methods
    });
```

#### The User object

*Note: In the [Pusher Channels documentation](http://pusher.com/docs/channels) a User may be referred to as a `Member`.*

The `User` object has two main methods.

`getId` fetches a unique identifier for the user on the presence channel.

`getInfo` fetches a string representing arbitrary additional information about the user in the form of a JSON hash, e.g.

```
{"user_name":"Mr. User","user_score":1357}
```

The following example using the [Gson library](https://sites.google.com/site/gson/gson-user-guide) to handle deserialization:

```java
String jsonInfo = user.getInfo();
Gson gson = new Gson();
UserInfo info = gson.fromJson(jsonInfo, UserInfo.class);
```

For more information on defining the user id and user info on the server see [Implementing the authorization endpoint for a presence channel](https://pusher.com/docs/channels/server_api/authorizing-users#implementing-the-authorization-endpoint-for-a-presence-channel) documentation.

#### Client event authenticity

Channels now provides a 'user-id' with client events sent from the server. With presence channels, your authorization endpoint provides your user with a user-id. Previously, it was up to you to include this user-id in every client-event triggered by your clients. Now, when a client of yours triggers a client event, Channels will append their user-id to their triggered message, so that the other clients in the channel receive it. This allows you to trust that a given user really did trigger a given payload.

If you’d like to make use of this feature, you’ll need to extract the user-id from the message delivered by Channels. To do this, call getUserId() on the event payload your event handler gets called with, like so:

```java
channel.bind("client-my-event", new SubscriptionEventListener() {
    @Override
    public void onEvent(PusherEvent event) {
        System.out.println("Received event with userId: " + event.getUserId());
    }
});
```

## Binding and handling events

There are two types of events that occur on channel subscriptions.

1. Protocol related events such as those triggered when a subscription succeeds
2. Application events that have been triggered by code within your application

### ChannelEventListener

The `ChannelEventListener` is an interface that is informed of both protocol related events and application data events. A `ChannelEventListener` can be used when initially subscribing to a channel.

```java
Channel channel = pusher.subscribe("my-channel", new ChannelEventListener() {
    @Override
    public void onSubscriptionSucceeded(String channelName) {
        System.out.println("Subscribed!");
    }

    @Override
    public void onEvent(PusherEvent event) {
        // Called for incoming events names "foo", "bar" or "baz"
    }
}, "foo", "bar", "baz");
```

The `ChannelEventListener` interface extends the `SubscriptionEventListener` interface.

### SubscriptionEventListener

Events triggered by your application are received by the `onEvent` method on the `SubscriptionEventListener` interface implementation. If you are only related to application events you can bind to events on `Channel` objects.

```java
Channel channel = pusher.subscribe("my-channel");
channel.bind("my-event", new ChannelEventListener() {
    @Override
    public void onEvent(PusherEvent event) {
        // Called for incoming events named "my-event"
    }
});
```

The event data is accessible by calling the `getData()` method on the event. From there you can handle the data as you like. Since we encourage data to be in JSON here's an example that uses [Gson object deserialization](https://sites.google.com/site/gson/gson-user-guide#TOC-Object-Examples):

```java
public class Example implements ChannelEventListener {
    public Example() {
        Pusher pusher = new Pusher(YOUR_APP_KEY);
        pusher.subscribe("my-channel", this);
        pusher.connect();
    }

    @Override
    public void onEvent(PusherEvent event) {
        Gson gson = new Gson();
        EventExample exampleEvent = gson.fromJson(event.getData(), EventExample.class);
    }
}

class EventExample {
    private int value1 = 1;
    private String value2 = "abc";
    private transient int value3 = 3;

    EventExample() { }
}
```

### Unbinding event listeners

You can unbind from an event:

```java
channel.unbind("my_event", listener);
```

### Example

```java
public class Example implements ChannelEventListener {
    private final Pusher pusher;
    private final Channel channel;

    public Example() {
        pusher = new Pusher(YOUR_APP_KEY);
        channel = pusher.subscribe("my-channel", this, "my_event");

        pusher.connect();
    }

    public void listenToOtherEvent() {
        channel.bind("my_other_event", this);
    }

    public void stopListeningToOtherEvent() {
        channel.unbind("my_other_event", this);
    }
}
```

## Triggering events

Once a [private](https://pusher.com/docs/channels/using_channels/private-channels) or [presence](https://pusher.com/docs/channels/using_channels/presence-channels) subscription has been authorized (see [authorizing users](https://pusher.com/docs/channels/server_api/authorizing-users)) and the subscription has succeeded, it is possible to trigger events on those channels.

```java
channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
```

Events triggered by clients are called [client events](https://pusher.com/docs/channels/using_channels/events#triggering-client-events). Because they are being triggered from a client which may not be trusted there are a number of enforced rules when using them. Some of these rules include:

* Event names must have a `client-` prefix
* Rate limits
* You can only trigger an event when the subscription has succeeded

For full details see the [client events documentation](https://pusher.com/docs/channels/using_channels/events#triggering-client-events).

```java
PrivateChannel channel = pusher.subscribePrivate("private-channel",
    new PrivateChannelEventListener() {
        @Override
        public void onSubscriptionSucceeded(String channelName) {
            channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
        }

        // Other PrivateChannelEventListener methods
    });
```

## Accessing the connection socket ID

Once connected you can access a unique identifier for the current client's connection. This is known as the `socket_id`.

You can access the value **once the connection has been established** as follows:

```java
String socketId = pusher.getConnection().getSocketId();
```

For more information on how and why there is a `socket_id` see the documentation on [authorizing users](ttps://pusher.com/docs/channels/server_api/authorizing-users) and [excluding recipients](https://pusher.com/docs/channels/server_api/excluding-event-recipients).

## Helper Methods

### Getting a channel from string

#### Basic channels

```java
Channel channel = pusher.getChannel("my-channel");
```

The library will raise an exception if the parameter to `Pusher#getChannel` is prefixed with `"private-"` or `"presence-"`.

#### Private channels

```java
PrivateChannel channel = pusher.getPrivateChannel("private-channel");
```

The library will raise an exception if the parameter to `Pusher#getPrivateChannel` is not prefixed with `"private-"`.

#### Presence channels

```java
PresenceChannel channel = pusher.getPresenceChannel("presence-channel");
```

The library will raise an exception if the parameter to `Pusher#getPresenceChannel` is not prefixed with `"presence-"`.

### Check if a channel has subscribed

```java
Channel channel = pusher.getChannel("my-channel");
channel.isSubscribed(); // => `true`/`false`
```

## JavaDocs

The JavaDocs can be found here: <http://pusher.github.com/pusher-websocket-java/>

## Library Development Environment
If you'd like to tweak this library there are ways to use your local code rather than the official. This is a rough guide on how to do so.

### Prerequisites

* A Java Virtual Machine.
* Gradle, the build system used for the project, is downloaded by the Gradle Wrapper (`gradlew`) which is included in the repo.
* On Windows `./gradlew.bat` should be used, on Linux `./gradle`.

### Cloning the project

* Clone the project: `git clone https://github.com/pusher/pusher-websocket-java`
* Change to the top level directory for the project: `cd pusher-websocket-java`

### Android Studio
* In your app project's `settings.gradle` add the following lines:

```
include ':pusher-websocket-java'
project(':pusher-websocket-java').projectDir = new File('<PATH_TO_THIS_PROJECT>/pusher-websocket-java')
```
* Add the following line to your application's `build.gradle` where you would normally add the actual `pusher-websocket-java` SDK:

```
dependencies {
    implementation project(':pusher-websocket-java')
}
```

### Eclipse Project

Assuming you are using Eclipse, follow these steps:

* Run `gradlew eclipse`. This will generate the `.classpath` and `.project` files
* You can now load the project in Eclipse by navigating to `Import project` and pointing it to the root directory of the existing project.

### Build

From the top level directory execute:

* `gradlew test` to execute the tests.
* `gradlew javadoc` to generate the JavaDoc. The docs will be output to the `build/docs/javadoc/` directory.
* `gradlew assemble` assemble all artifacts but does not run any tests.
* `gradlew build` to build all jars and execute all tests & verification. The jars will be output to the `build/libs` directory.

### Run the Example Application

After running `gradlew clean assemble` change to the `build/libs` directory and run `java -jar pusher-websocket-java-with-dependencies-<version>-jar-with-dependencies.jar`. This will run the example application.

By default the example will connect to a sample application and subscribe to the channel `my-channel`, listening to events on `my-event`. If you want to change these defaults, they can be specified on the command line:

`java -jar pusher-websocket-java-with-dependencies-<version>-jar-with-dependencies.jar [appKey] [channelName] [eventName]`
