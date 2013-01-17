# Pusher Java Client

Pusher client library for Java targeting **Android** and general Java.

## The Pusher constructor

The standard constructor take an application key which you can get from the app's API Access section in the Pusher dashboard.

```java
Pusher pusher = new Pusher( YOUR_APP_KEY );
```

If you are going to use [private](http://pusher.com/docs/private_channels) or [presence](http://pusher.com/docs/presence_channels) channels then you will need to provide an `Authorizer` to be used when authenticating subscriptions. In order to do this you need to pass in a `PusherOptions` object which has had an `Authorizer` set.

```java
HttpAuthorizer authorizer = new HttpAuthorizer("http://example.com/some_auth_endpoint");
PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
Pusher pusher = new Pusher( YOUR_APP_KEY, options );
```

See the documentation on [Authenticating Users](http://pusher.com/docs/authenticating_users) for more information.

## Connecting

In order to send and receive messages you need to connect to Pusher.

```java
Pusher pusher = new Pusher( YOUR_APP_KEY );
pusher.connect();
```

## Listening to connection events

It is possible to receive connection state change events by implementing the `ConnectionEventListener` interface.

```java
public class Example implements ConnectionEventListener {
  
  public Example() {
    Pusher pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
  }

  @Override
  public void onConnectionStateChange(ConnectionStateChange change) {
    System.out.println( String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(), change.getCurrentState()) );
  }

  @Override
  public void onError(String message, String code, Exception e) {
    System.out.println( "Error: " + message );
  }

}
```

For more information see [connection states](http://pusher.com/docs/connection_states).

## Subscribing to channels

### Public channels

```java
Channel channel = pusher.subscribe( "my-channel" );
```

Sometimes you may want to be informed when the subscription succeeds. You can do this by implementing the `ChannelEventListener` interface:

```java
public class Example implements ChannelEventListener {
  
  public Example() {
    Pusher pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    Channel channel = pusher.subscribe( "my-channel", this );
  }
  
  @Override
  public void onSubscriptionSucceeded(String channelName) {
    System.out.println( "Subscribed!" );
  }

  @Override
  public void onEvent(String channelName, String eventName, String data){
  }

}
```

### Private channels

It's possible to subscribe to [private channels](http://pusher.com/docs/private_channels) that provide a mechanism for [authenticating channel subscriptions](http://pusher.com/docs/authenticating_users). In order to do this you need to provide an `Authorizer` when creating the `Pusher` instance (see **The Pusher constructor** above).

The library provides a `HttpAuthorizer` implementation of `Authorizer` which makes an HTTP `POST` request to an authenticating endpoint. However, you an implement your own authentication mechanism if required.

Private channels are subscribed to as follows:

```java
PrivateChannel privateChannel = pusher.subscribePrivate( "private-channel" );
```

In addition to the events that are possible on public channels a private channel exposes an `onAuthenticationFailure`. This is called if the `Authorizer` does not successfully authenticate the subscription:

```java
public class Example implements PrivateChannelEventListener {
  
  public Example() {
    Pusher pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    PrivateChannel channel = pusher.subscribePrivate( "private-channel", this );
  }

  @Override
  public void onAuthenticationFailure(String message, Exception e) {
	System.out.println(String.format("Authentication failure due to [%s], exception was [%s]", message, e));
  }

  /* ChannelEventListener methods would follow */

}
```

### Presence channels

[Presence channels](http://pusher.com/docs/presence_channels) can be subscribed to as follows:

```java
PresenceChannel presenceChannel = pusher.subscribePrivate( "presence-channel" );
```

Presence channels provide additional events relating to users joining and leaving the presence channel. It is possible to listen to these events by implementing the `PresenceChannelEventListener`.

```java
public class Example implements PrivateChannelEventListener {
  
  public Example() {
    Pusher pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    PresenceChannel channel = pusher.subscribePresence( "presence-channel", this );
  }

  @Override
  public void onUserInformationReceived(String channelName, Set<User> users) {
	System.out.println("Received user information");
  }

  @Override
  public void userSubscribed(String channelName, User user) {
	System.out.println(String.format("A new user has joined channel [%s]: %s", channelName, user.toString()));
  }

  @Override
  public void userUnsubscribed(String channelName, User user) {
    System.out.println(String.format("A user has left channel [%s]: %s", channelName, user));
  }

  /* PrivateChannelEventListener methods would follow */

}
```

### Binding to events

Events triggered by your application are recieved by the `onEvent` method on the `ChannelEventListener` interface implementation. These events can be bound to at two different stages/

At subscription:

```java
Channel channel = pusher.subscribe( "my-channel", new MyEventListener(), "my_event", "my_other_event" );
```
 
Or, by binding to the event on the `Channel`.

```java
channel.bind( "my_event", new MyEventListener() );
```

### Handling events

The event listener interfaces for all channel types have an `onEvent` method which is called whenever an event triggered by your application is received.

The event data will be passed as the third parameter to the `onEvent` method. From there you can handle the data as you like. Since we encourage data to be in JSON here's an example that uses [Gson object deserialization](https://sites.google.com/site/gson/gson-user-guide#TOC-Object-Examples):

```java
public class Example implements ChannelEventListener {
  
  public Example() {
    Pusher pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    pusher.subscribe( "my-channel", this );
  }

  @Override
  public void onEvent(String channelName, String eventName, String data){
    Gson gson = new Gson();
    EventExample exampleEvent = gson.fromJson(data, EventExample.class); 
  }

  @Override
  public void onSubscriptionSucceeded(String channelName) {
  }

}

class EventExample {
  private int value1 = 1;
  private String value2 = "abc";
  private transient int value3 = 3;
  EventExample() {
  }
}
```

### Unbinding events

You can unbind from an event:

```java
channel.bind( "my_event", listener );
```

### Binding/Unbinding example


```java
public class Example implements ChannelEventListener {
  
  private final Pusher pusher;
  private final Channel channel;

  public Example() {
    pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    channel = pusher.subscribe( "my-channel", this, "my_event" );    
  }

  public void listenToOtherEvent() {
    channel.bind( "my_other_event", this );
  }

  public void stopListeningToOtherEvent() {
    channel.unbind( "my_other_event", this );
  }

  /* ChannelEventListener methods would follow */}

}
```

### Triggering events

Once a subscription [private](http://pusher.com/docs/private_channels) or [presence](http://pusher.com/docs/presence_channels) has been authorized (see [authenticating users](http://pusher.com/docs/authenticating_users)) and the subscription has succeeded, it is possible to trigger events on those channels.

```java
channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
```

Events triggered by clients are called [client events](http://pusher.com/docs/client_events). Because they are being triggered from a client which may not be trusted there are a number of enforced rules when using them. Some of these rules include:

* Event names must have a `client-` prefix
* Rate limits
* You can only trigger an event when the subscription has succeeded

For full details see the [client events documentation](http://pusher.com/docs/client_events).

```java
public class Example implements PrivateChannelEventListener {
  
  private final Pusher pusher;
  private final PrivateChannel channel;

  public Example() {
    pusher = new Pusher( YOUR_APP_KEY );
    pusher.connect( this );
    
    channel = pusher.subscribePrivate( "private-channel", this );
  }

  @Override
  public void onSubscriptionSucceeded(String channelName) {
	channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
  }

  /* Other PrivateChannelEventListener methods would follow */

}
```

### Accessing the connection socket ID

If you are triggering events via your own server you may wish to exclude the originator of the event - the current client. You can do this by supplying a unique identifier for the current client's connection. This is known as the **socket ID**.

You can access the value as follows:

**TODO: How to access the socket ID**

## Library Development Environment

### Cloning the project

* If you don't have it already, [download Maven](http://maven.apache.org/download.cgi) and add the `mvn` executable to your path.
* Clone the project: `git clone git@github.com:leggetter/pusher-java-client.git`
* Change to the top level directory for the project: `cd pusher-java-client`
* Switch to the development branch: `git checkout public-channel`
* Retrieve the Java-WebSocket library: `git submodule update --init`

### Eclipse Project

Assuming you are using Eclipse, execute `mvn eclipse:clean eclipse:eclipse` to generate the `.project` and `.classpath` files. Then open Eclipse and import the project as normal.

### Build

From the top level directory execute `mvn clean test` to compile and run the unit tests or `mvn clean install` to build the jar. The jar will be output to the `target` directory.

### Run the Example Application

After running `mvn clean install` change to the `target` directory and run `java -jar pusher-java-client-<version>-jar-with-dependencies.jar`. This will run the example application. 

By default the example will connect to a sample application and subscribe to the channel `my-channel`, listening to events on `my-event`. If you want to change these defaults, they can be specified on the command line:

`java -jar pusher-java-client-<version>-jar-with-dependencies.jar [appKey] [channelName] [eventName]` 