# Pusher Channels Java Client

[![Build Status](https://travis-ci.org/pusher/pusher-websocket-java.svg?branch=master)](https://travis-ci.org/pusher/pusher-websocket-java)

Pusher Channels client library for Java targeting **Android** and general Java.

## Supported platforms

* Java 8+
* [Android, via this wrapper SDK](https://github.com/pusher/pusher-websocket-java)

## TOC

This README covers the following topics:

- [Installation](#installation)
	- [Maven](#maven)
	- [Gradle](#gradle)
	- [Download](#download)
	- [Source](#source)
- [API Overview](#api-overview)
- [The Pusher constructor](#the-pusher-constructor)
- [Connecting](#connecting)
- [Reconnecting](#reconnecting)
- [Disconnecting](#disconnecting)
- [Listening to connection events](#listening-to-connection-events)
- [Subscribing to channels](#subscribing-to-channels)
	- [Public channels](#public-channels)
	- [Private channels](#private-channels)
	- [Presence channels](#presence-channels)
		- [The User object](#the-user-object)
- [Binding and handling events](#binding-and-handling-events)
- [Triggering events](#triggering-events)
- [Accessing the connection socket ID](#accessing-the-connection-socket-id)
- [Helper Methods](#helper-methods)
	- [Getting a channel from string](#getting-a-channel-from-string)
	- [Check if a channel has subscribed](#check-if-a-channel-has-subscribed)
- [JavaDocs](#javadocs)
- [Library Development Environment](#library-development-environment)
	- [Prerequisites](#prerequisites)
	- [Cloning the project](#cloning-the-project)
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
      <version>1.8.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
  compile 'com.pusher:pusher-java-client:1.8.0'
}
```

### Download

You can download a version of the `.jar` directly from <http://repo1.maven.org/maven2/com/pusher/pusher-java-client/>

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
    public void onEvent(String channel, String event, String data) {
        System.out.println("Received event with data: " + data);
    }
});

// Disconnect from the service (or become disconnected my network conditions)
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

If you are going to use [private](https://pusher.com/docs/channels/using_channels/private-channels) or [presence](https://pusher.com/docs/channels/using_channels/presence-channels) channels then you will need to provide an `Authorizer` to be used when authenticating subscriptions. In order to do this you need to pass in a `PusherOptions` object which has had an `Authorizer` set.

```java
HttpAuthorizer authorizer = new HttpAuthorizer("http://example.com/some_auth_endpoint");
PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER).setAuthorizer(authorizer);
Pusher pusher = new Pusher(YOUR_APP_KEY, options);
```

See the documentation on [Authenticating Users](https://pusher.com/docs/channels/server_api/authenticating-users) for more information.

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
| setAuthorizer               | Pusher Authorizer | Sets the authorizer to be used when authenticating private and presence channels.                                                             |
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

### Private channels

It's possible to subscribe to [private channels](https://pusher.com/docs/channels/using_channels/private-channels) that provide a mechanism for [authenticating channel subscriptions](https://pusher.com/docs/channels/server_api/authenticating-users). In order to do this you need to provide an `Authorizer` when creating the `Pusher` instance (see **The Pusher constructor** above).

The library provides a `HttpAuthorizer` implementation of `Authorizer` which makes an HTTP `POST` request to an authenticating endpoint. However, you can implement your own authentication mechanism if required.

Private channels are subscribed to as follows:

```java
PrivateChannel privateChannel = pusher.subscribePrivate( "private-channel" );
```

In addition to the events that are possible on public channels a private channel exposes an `onAuthenticationFailure`. This is called if the `Authorizer` does not successfully authenticate the subscription:

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

### Presence channels

[Presence channels](https://pusher.com/docs/channels/using_channels/presence-channels) are private channels which provide additional events exposing who is currently subscribed to the channel. Since they extend private channels they also need to be authenticated (see [authenticating channel subscriptions](https://pusher.com/docs/channels/server_api/authenticating-users)).

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

For more information on defining the user id and user info on the server see [Implementing the auth endpoint for a presence channel](https://pusher.com/docs/channels/server_api/authenticating-users#implementing-the-auth-endpoint-for-a-presence-channel) documentation.

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
    public void onEvent(String channelName, String eventName, String data) {
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
    public void onEvent(String channelName, String eventName, String data) {
        // Called for incoming events named "my-event"
    }
});
```

The event data will be passed as the third parameter to the `onEvent` method. From there you can handle the data as you like. Since we encourage data to be in JSON here's an example that uses [Gson object deserialization](https://sites.google.com/site/gson/gson-user-guide#TOC-Object-Examples):

```java
public class Example implements ChannelEventListener {
    public Example() {
        Pusher pusher = new Pusher(YOUR_APP_KEY);
        pusher.subscribe("my-channel", this);
        pusher.connect();
    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        Gson gson = new Gson();
        EventExample exampleEvent = gson.fromJson(data, EventExample.class);
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

Once a [private](https://pusher.com/docs/channels/using_channels/private-channels) or [presence](https://pusher.com/docs/channels/using_channels/presence-channels) subscription has been authorized (see [authenticating users](https://pusher.com/docs/channels/server_api/authenticating-users)) and the subscription has succeeded, it is possible to trigger events on those channels.

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

For more information on how and why there is a `socket_id` see the documentation on [authenticating users](ttps://pusher.com/docs/channels/server_api/authenticating-users) and [excluding recipients](https://pusher.com/docs/channels/server_api/excluding-event-recipients).

## Helper Methods

### Getting a channel from string

#### Basic channels

```java
Channel channel = pusher.getChannel("my-channel");
```

The library will raise an exception if the parameter to `Pusher#getPrivateChannel` is prefixed with `"private-"` or `"presence-"`.

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
    compile project(':pusher-websocket-java')
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
* `gradlew createPublishTarget uploadArchives` to upload all artifacts. **This task requires some properties to be set, see below.**
* `gradlew publishGhPages` to upload JavaDocs to `gh-pages`. **This task requires some properties to be set, see below.**

#### Build Properties

There are several build properties used for authentication. These should be set either in `~/.gradle/gradle.properties` using the format `property=value` or can be passed via command line as `-Pprop=val`.

The properties used for the build are:

* `maven.username` - the username used for Maven deployment authentication
* `maven.password` - the password used for Maven deployment authentication
* `github.username` - the username used for GitHub authentication
* `github.password` - the password used for GitHub authentication

### Run the Example Application

After running `gradlew clean assemble` change to the `build/libs` directory and run `java -jar pusher-java-client-<version>-jar-with-dependencies.jar`. This will run the example application.

By default the example will connect to a sample application and subscribe to the channel `my-channel`, listening to events on `my-event`. If you want to change these defaults, they can be specified on the command line:

`java -jar pusher-java-client-<version>-jar-with-dependencies.jar [appKey] [channelName] [eventName]`
