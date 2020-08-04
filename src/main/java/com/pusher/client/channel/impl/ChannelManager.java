package com.pusher.client.channel.impl;

import com.github.davidmoten.rx2.RetryWhen;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.pusher.client.AuthRequestHandler;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.AuthorizationMissingException;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChannelManager implements ConnectionEventListener {

    private static final Gson GSON = new Gson();
    private final Map<String, InternalChannel> channelNameToChannelMap = new ConcurrentHashMap<String, InternalChannel>();

    private final Factory factory;
    private InternalConnection connection;
    private PusherOptions options;
    private AuthRequestHandler pusherAuthRequestHandler;

    private Disposable authDisposable = null;
    private Map<String, Disposable> criticalChannelDisposable = new ConcurrentHashMap<String, Disposable>();
    private Scheduler scheduler = Schedulers.io();

    public ChannelManager(final Factory factory) {
        this.factory = factory;
    }

    public Channel getChannel(String channelName){
        if (channelName.startsWith("private-")){
            throw new IllegalArgumentException("Please use the getPrivateChannel method");
        } else if (channelName.startsWith("presence-")){
            throw new IllegalArgumentException("Please use the getPresenceChannel method");
        }
        return (Channel) findChannelInChannelMap(channelName);
    }

    public PrivateChannel getPrivateChannel(String channelName) throws IllegalArgumentException{
        if (!channelName.startsWith("private-")) {
            throw new IllegalArgumentException("Private channels must begin with 'private-'");
        } else {
            return (PrivateChannel) findChannelInChannelMap(channelName);
        }
    }

    public PrivateEncryptedChannel getPrivateEncryptedChannel(String channelName) throws IllegalArgumentException{
        if (!channelName.startsWith("private-encrypted-")) {
            throw new IllegalArgumentException("Encrypted private channels must begin with 'private-encrypted-'");
        } else {
            return (PrivateEncryptedChannel) findChannelInChannelMap(channelName);
        }
    }

    public PresenceChannel getPresenceChannel(String channelName) throws IllegalArgumentException{
        if (!channelName.startsWith("presence-")) {
            throw new IllegalArgumentException("Presence channels must begin with 'presence-'");
        } else {
            return (PresenceChannel) findChannelInChannelMap(channelName);
        }
    }

    private InternalChannel findChannelInChannelMap(String channelName){
        return channelNameToChannelMap.get(channelName);
    }

    public Collection<InternalChannel> getChannelList() {
        return channelNameToChannelMap.values();
    }

    public void setPusherOptions(PusherOptions options) {
        this.options = options;
    }

    public void setConnection(final InternalConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Cannot construct ChannelManager with a null connection");
        }

        if (this.connection != null) {
            this.connection.unbind(ConnectionState.CONNECTED, this);
        }

        this.connection = connection;
        connection.bind(ConnectionState.CONNECTED, this);
    }

    public void setPusherAuthRequestHandler(@NonNull AuthRequestHandler handler) {
        pusherAuthRequestHandler = handler;
    }

    public AuthRequestHandler  getPusherAuthRequestHandler() {
        return pusherAuthRequestHandler;
    }

    public void subscribeTo(final InternalChannel channel, final ChannelEventListener listener, final String... eventNames) {

        validateArgumentsAndBindEvents(channel, listener, eventNames);
        channelNameToChannelMap.put(channel.getName(), channel);
        sendOrQueueSubscribeMessage(channel);
    }

    public void unsubscribeFrom(final String channelName) {

        if (channelName == null) {
            throw new IllegalArgumentException("Cannot unsubscribe from null channel");
        }

        final InternalChannel channel = channelNameToChannelMap.remove(channelName);
        if (channel == null) {
            return;
        }
        if (connection.getState() == ConnectionState.CONNECTED) {
            sendUnsubscribeMessage(channel);
        }
        Disposable d = criticalChannelDisposable.remove(channelName);
        if (d != null) {
            d.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    public void onMessage(final String event, final String wholeMessage) {

        final Map<Object, Object> json = GSON.fromJson(wholeMessage, Map.class);
        final Object channelNameObject = json.get("channel");

        if (channelNameObject != null) {
            final String channelName = (String)channelNameObject;
            final InternalChannel channel = channelNameToChannelMap.get(channelName);

            if (channel != null) {
                channel.onMessage(event, wholeMessage);
            }
        }
    }

    /* ConnectionEventListener implementation */

    @Override
    public void onConnectionStateChange(final ConnectionStateChange change) {

        if (change.getCurrentState() == ConnectionState.CONNECTED) {
            for(final InternalChannel channel : channelNameToChannelMap.values()){
                sendOrQueueSubscribeMessage(channel);
            }
        }
    }

    @Override
    public void onError(final String message, final String code, final Exception e) {
        // this event listener is here for connection resume, so no duplicated error logs here
    }

    /* implementation detail */

    private Flowable<Long> retryDelays(final int retryCounts) {
        ArrayList<Long> list = new ArrayList();
        for (int i=0; i<=retryCounts; i++) {
            Double d = new Double(i);
            Long delay =  new Double(Math.pow(  new Double(2).doubleValue(), new Double(i).doubleValue())).longValue();
            if (delay >= 30) {
                list.add(i, 30L);
            } else {
                list.add(i, delay);
            }
        }

        return Flowable.fromArray(list.toArray(new Long[]{}));
    }
    /**
     * implementation detail
     * 1. Critical channel should subscibe right away
     * 2. Other Channels should have a batch request delay depends on getAuthDelay()
     * 3. batch delay wont reset timer if request is more than MAX_REQUESTS_PER_BATCH
     * */
    private void sendOrQueueSubscribeMessage(final InternalChannel channel) {
        if (connection.getState() != ConnectionState.CONNECTED) return;
        //critical channel should subscribe immediately
        if (isCritical(channel.getName())) {
            Disposable d = authCritical(channel)
                    .retryWhen(RetryWhen.delays(retryDelays(99), TimeUnit.SECONDS).build())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            response -> sendOrQueueSubscribeMessage(channel, response),
                            e -> {
                                if (e instanceof AuthorizationFailureException) {
                                    handleAuthenticationFailure(channel, (AuthorizationFailureException) e);
                                } else {
                                    handleAuthenticationFailure(channel, new AuthorizationFailureException(e));
                                }
                            }, () -> {});
            Disposable preDisposable = criticalChannelDisposable.remove(channel.getName());
            if (preDisposable != null) {
                preDisposable.dispose();
            }
            criticalChannelDisposable.put(channel.getName(), d);
        } else {
            if (getNonCriticalAndNonSubscribedCount() <= getMaxReqeustPerBatch()) {
                if (authDisposable != null && !authDisposable.isDisposed()) {
                    authDisposable.dispose();
                }
                authDisposable = Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                    try {
                        Thread.sleep(getAuthDelay());
                        emitter.onSuccess(true);
                    } catch (InterruptedException e) {
                    }
                })
                        .flatMapCompletable(list -> Completable.defer(() -> authNonCritical()))
                        .repeatUntil(() -> getNonCriticalAndNonSubscribedCount() == 0)
                        .subscribeOn(scheduler)
                        .subscribe();
            } else {
                //Do nothing, just wait for the batch to be done
            }
        }

    }

    @NonNull
    private Long getAuthDelay() {
        if (null != options && null != options.getAuthDelay()) {
            return options.getAuthDelay();
        } else {
            return 0L;
        }
    }

    private int getMaxReqeustPerBatch() {
        if (null != options) {
            return options.getMaxRequestPerBatch();
        } else {
            return 1;
        }
    }

    @NonNull
    private ArrayList<String> getCriticalChannelPrefixList() {
        if (null !=options && options.getCriticalChannelPrefixList() !=null) {
            return options.getCriticalChannelPrefixList();
        } else {
            return new ArrayList<String>();
        }
    }

    private boolean isCritical(String channelName) {
        if (channelName == null) return false;
        boolean b = false;
        for (String s: getCriticalChannelPrefixList()) {
            if (channelName.startsWith(s)) {
                b = true;
                break;
            }
        }
        return b;
    }

    synchronized private int getNonCriticalAndNonSubscribedCount() {
        int count = 0;
        for (InternalChannel channel : channelNameToChannelMap.values()) {
            if (channel.isInitial() && !isCritical(channel.getName())) {
                count++;
            }
        }
        return count;
    }

    private Maybe<String> authCritical(InternalChannel channel) {
        if (channel == null) return Maybe.create(MaybeEmitter::onComplete);
        return Maybe.create(emitter -> {
            synchronized (this) {
                ArrayList<String> list = new ArrayList<>();
                String channelName = channel.getName();
                String socketId = connection.getSocketId();
                list.add(channelName);
                try {
                    AuthResponseData responseData = authHttpRequest(socketId, list);
                    Map<String, PusherAuthResponse> response = responseData.getResponse();
                    String responseForChannel = GSON.toJson(response.get(channelName));
                    if (responseForChannel != null && !responseForChannel.isEmpty()) {
                        InternalChannel queuedChannel = channelNameToChannelMap.get(channelName);
                        if (queuedChannel == null) {
                            System.out
                                    .println(channelName + "channel removed before auth, auth(" + socketId + ") success");
                            emitter.onComplete();
                        } else {
                            System.out.println(channelName + "@(" + socketId + ") auth success");
                            emitter.onSuccess(responseForChannel);
                        }
                    } else {
                        if (channelNameToChannelMap.get(channelName) == null) {
                            System.out
                                    .println(channelName + "channel removed before auth, auth(" + socketId + ") failed too");
                            emitter.onComplete();
                        } else {
                            System.out
                                    .println(channelName + "@(" + socketId + ") auth failed, no response for it ");
                            emitter.onError(new AuthorizationFailureException(
                                    new AuthorizationMissingException(
                                            "unable to get response for this channel " + channelName + ", log: " + responseData
                                                    .toString())));
                        }
                    }

                } catch (AuthorizationFailureException e) {
                    emitter.onError(e);
                } catch (Exception e) {
                    emitter.onError(new AuthorizationFailureException(e));
                }
            }
        });
    }

    private Completable authNonCritical() {
        if (connection == null || connection.getSocketId() == null || connection.getState() != ConnectionState.CONNECTED) {
            System.out.println("no connection, keep channels for resume");
            return Completable.complete();
        }
        final String socketId = connection.getSocketId();
        final ArrayList<String> listCopy = new ArrayList<>();
        synchronized (this) {
            for (InternalChannel channel : channelNameToChannelMap.values()) {
                if (channel.isInitial() && !isCritical(channel.getName())) {
                    listCopy.add(channel.getName());
                    if (listCopy.size() < getMaxReqeustPerBatch()) {
                        System.out.println("Authing channel[" + channel.getName() + "](" + socketId + ")");
                    } else {
                        break;
                    }
                }
            }
        }
        Completable ob = Completable.create(emitter -> {
            if (socketId == null || socketId.isEmpty()) {
                emitter.onError(new AuthorizationFailureException("socket id is not available"));
            } else if (getNonCriticalAndNonSubscribedCount() == 0) {
                System.out.println("no channel to subscribe");
                emitter.onComplete();
            } else {
                synchronized (this) {
                    AuthResponseData responseData = authHttpRequest(socketId, listCopy);
                    Map<String, PusherAuthResponse> responseMap =  responseData.getResponse();
                    System.out.println("Auth responsed: " + GSON.toJson(responseMap));
                    for (String channelName : listCopy) {
                        String responseForChannel = GSON.toJson(responseMap.get(channelName));
                        if (responseForChannel != null && !responseForChannel.isEmpty()) {
                            InternalChannel channel = channelNameToChannelMap.get(channelName);
                            if (channel == null) {
                                System.out.println(channelName + " removed before auth, auth(" + socketId + ") success, " + responseForChannel);
                            } else {
                                if (channel.getState() == ChannelState.INITIAL) {
                                    sendOrQueueSubscribeMessage(channel, responseForChannel);
                                } else {
                                    System.out.println(channelName + "[" + channel.getState() + "] auth(" + socketId + ") success but ignored, the state is not initial");
                                }
                            }
                        } else {
                            InternalChannel channel = channelNameToChannelMap.get(channelName);
                            if (channel == null) {
                                System.out.println(channelName + " removed before auth, auth(" + socketId + ") failed too");
                            } else {
                                System.out.println(channelName + " auth(" + socketId + ") failed, no response for it, take " + responseData.toString());
                                handleAuthenticationFailure(channel, new AuthorizationFailureException(
                                        new AuthorizationMissingException("unable to get response for this channel " + channelName + ", takes " + responseData.toString())
                                ));
                            }
                        }
                    }
                    emitter.onComplete();
                }
            }
        });

        return ob
                .retryWhen(RetryWhen.delays(retryDelays(999), TimeUnit.SECONDS).build())
                ;
    }

    private String composeAuthBody(String socketId, List<String> list) {
        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("socket_id", socketId);

        JsonArray channelNameArray = new JsonArray();
        for (String s : list) {
            channelNameArray.add(new JsonPrimitive(s));
        }
        bodyJson.add("channels", channelNameArray);
        return bodyJson.toString();
    }

    private AuthResponseData authHttpRequestWithoutToken(String socketId, List<String> list) {
        return pusherAuthRequestHandler.authRequest(socketId, list, false);
    }

    private AuthResponseData authHttpRequest(String socketId, List<String> list) {
        return pusherAuthRequestHandler.authRequest(socketId, list, true);
    }

    private String parseAuthResponseForChannel(String channelName, String s) {
        if (null != s && !s.isEmpty()) {
            JsonObject response = (new JsonParser()).parse(s).getAsJsonObject();
            if (response.has(channelName)) {
                String str = response.get(channelName).toString();
                return str;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isSSL() {
        if (null != options) {
            URL endPoint = options.getAuthEndPoint();
            if (null != endPoint) {
                return endPoint.getProtocol().equals("https");
            }
        }
        return false;
    }

    private void sendOrQueueSubscribeMessage(final InternalChannel channel, String authResponse) {
        factory.queueOnEventThread(new Runnable() {

            @Override
            public void run() {
                if (connection.getState() == ConnectionState.CONNECTED) {
                    try {
                        final String message = channel.toSubscribeMessage(authResponse);
                        connection.sendMessage(message);
                        channel.updateState(ChannelState.SUBSCRIBE_SENT);
                    } catch (final AuthorizationFailureException e) {
                        handleAuthenticationFailure(channel, e);
                    }
                }
            }
        });
    }

    private void sendUnsubscribeMessage(final InternalChannel channel) {
        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                connection.sendMessage(channel.toUnsubscribeMessage());
                channel.updateState(ChannelState.UNSUBSCRIBED);
            }
        });
    }

    private void handleAuthenticationFailure(final InternalChannel channel, final Exception e) {

        channelNameToChannelMap.remove(channel.getName());
        channel.updateState(ChannelState.FAILED);

        if (channel.getEventListener() != null) {
            factory.queueOnEventThread(new Runnable() {

                @Override
                public void run() {
                    // Note: this cast is safe because an
                    // AuthorizationFailureException will never be thrown
                    // when subscribing to a non-private channel
                    final ChannelEventListener eventListener = channel.getEventListener();
                    final PrivateChannelEventListener privateChannelListener = (PrivateChannelEventListener)eventListener;
                    privateChannelListener.onAuthenticationFailure(e.getMessage(), e);
                }
            });
        }
    }

    private void validateArgumentsAndBindEvents(final InternalChannel channel, final ChannelEventListener listener, final String... eventNames) {

        if (channel == null) {
            throw new IllegalArgumentException("Cannot subscribe to a null channel");
        }

        if (channelNameToChannelMap.containsKey(channel.getName())) {
            throw new IllegalArgumentException("Already subscribed to a channel with name " + channel.getName());
        }

        for (final String eventName : eventNames) {
            channel.bind(eventName, listener);
        }

        channel.setEventListener(listener);
    }
}
