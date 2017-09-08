package com.ogma.rocketchatsdkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SimpleListener;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.callback.GetSubscriptionListener;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.callback.MessageListener;
import com.rocketchat.core.callback.RoomListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RoomObject;
import com.rocketchat.core.model.SubscriptionObject;
import com.rocketchat.core.model.TokenObject;
import com.rocketchat.core.model.attachment.TAttachment;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("This class is not used in the app. Just for testing purpose")
public class TestActivity extends AppCompatActivity implements ConnectListener, RoomListener.GetRoomListener, LoginListener, GetSubscriptionListener, MessageListener.SubscriptionListener {

    private RocketChatAPI api;
    //    private LiveChatAPI.ChatRoom room; //This is required to provide abstraction over further communication
    private final String serverurl = "http://www.smartlywallet.de:3000";
    private int maxAttempts = 5;    //Number of attemps are 10
    private int timeInterval = 3000; // in milliseconds, reconnection will be called after 5 seconds
    private RocketChatAPI.ChatRoom chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        connect();
    }

    public void connect() {
        api = new RocketChatAPI(serverurl);
        api.setReconnectionStrategy(new ReconnectionStrategy(maxAttempts, timeInterval));
        api.connect(this);
    }

    @Override
    public void onConnect(String sessionID) {
        Log.e("onConnect: ", "Connected to server");
        api.login("Koenigstein", "E10ADC3949BA59ABBE56E057F20F883E", this);
    }

    @Override
    public void onDisconnect(boolean closedByServer) {
//        api.reconnect();
        Log.e("onDisconnect: ", "Disconnected from server");
    }

    @Override
    public void onConnectError(Exception websocketException) {
        websocketException.printStackTrace();
        Log.e("onConnectError: ", "Got connect error with the server");
    }

    @Override
    public void onLogin(TokenObject token, ErrorObject error) {
        if (error == null) {
            Log.e("onLogin: ", "Logged in successfully, returned token " + token.getAuthToken());
//            api.getRooms(this);
            api.getSubscriptions(this);
        } else {
            Log.e("onLogin: ", "Got error " + error.getMessage());
        }
    }

    @Override
    public void onGetRooms(List<RoomObject> rooms, ErrorObject error) {
        if (error == null) {
            for (RoomObject room : rooms) {
                Log.e("onGetRooms: ", "Room name is " + room.getRoomName());
                Log.e("onGetRooms: ", "Room id is " + room.getRoomId());
                Log.e("onGetRooms: ", "Room topic is " + room.getTopic());
                Log.e("onGetRooms: ", "Room type is " + room.getRoomType());
//                if (room.getRoomId().equals("mEx8HmhPoAWFkeNqQ")) {
//                    this.roomObject = room;
//                }
            }
        } else {
            Log.e("onGetRooms: ", "Got error " + error.getMessage());
        }
    }

    @Override
    public void onGetSubscriptions(List<SubscriptionObject> subscriptions, ErrorObject error) {
        if (error == null) {
            for (SubscriptionObject subscription : subscriptions) {
                System.out.println("subscription name is " + subscription.getRoomName());
                System.out.println("subscription id is " + subscription.getRoomId());
                System.out.println("subscription created at " + subscription.getRoomCreated());
                System.out.println("subscription type is " + subscription.getRoomType());
            }

            //Creating Logical ChatRooms using factory class
            ChatRoomFactory factory = api.getChatRoomFactory();
            ArrayList<RocketChatAPI.ChatRoom> rooms = factory.createChatRooms(subscriptions).getChatRooms();
            chatRoom = factory.getChatRoomById("mEx8HmhPoAWFkeNqQ");
            if (chatRoom != null) {
                Log.e("onGetSubscriptions: ", chatRoom.getRoomData().getRoomName());
                subscribeMessage();
                //Without acknowledgement
                chatRoom.sendMessage("This is some random message");
            }

            for (RocketChatAPI.ChatRoom room : rooms) {
                System.out.println("Room id is " + room.getRoomData().getRoomId());
                System.out.println("Room name is " + room.getRoomData().getRoomName());
                System.out.println("Room type is " + room.getRoomData().getRoomType());
            }

        } else {
            System.out.println("Got error " + error.getMessage());
        }
    }

    void subscribeMessage() {
        chatRoom.subscribeRoomMessageEvent(new SubscribeListener() {
            @Override
            public void onSubscribe(Boolean isSubscribed, String subId) {
                if (isSubscribed) {
                    System.out.println("subscribed to room successfully");
                }
            }
        }, this);
    }

    @Override
    public void onMessage(String roomId, RocketChatMessage message) {
        System.out.println("Got message " + message.getMessage());
        switch (message.getMsgType()) {
            case TEXT:
                System.out.println("This is a text message");
                break;
            case ATTACHMENT:
                List<TAttachment> attachments = message.getAttachments();
                for (TAttachment attachment : attachments) {
                    switch (attachment.getAttachmentType()) {
                        case TEXT_ATTACHMENT:
                            System.out.println("This is a reply or quote to a message");
                            break;
                        case IMAGE:
                            System.out.println("There is a image attachment");
                            break;
                        case AUDIO:
                            System.out.println("There is a audio attachment");
                            break;
                        case VIDEO:
                            System.out.println("There is a video attachment");
                            break;
                    }
                }
                break;
            case MESSAGE_EDITED:
                System.out.println("Message has been edited");
                break;
            case MESSAGE_STARRED:
                System.out.println("Message is starred now");
                break;
            case MESSAGE_REACTION:
                System.out.println("Got message reaction");
                break;
            case MESSAGE_REMOVED:
                System.out.println("Message is deleted");
                break;
            case ROOM_NAME_CHANGED:
                System.out.println("Room name changed");
                break;
            case ROOM_ARCHIVED:
                System.out.println("Room is archived");
                break;
            case ROOM_UNARCHIVED:
                System.out.println("Room is unarchieved");
                break;
            case USER_ADDED:
                System.out.println("User added to the room");
                break;
            case USER_REMOVED:
                System.out.println("User removed from the room");
                break;
            case USER_JOINED:
                System.out.println("User joined the room");
                break;
            case USER_LEFT:
                System.out.println("User left the room");
                break;
            case USER_MUTED:
                System.out.println("User muted now");
                break;
            case USER_UNMUTED:
                System.out.println("User un-muted now");
                break;
            case WELCOME:
                System.out.println("User welcomed");
                break;
            case SUBSCRIPTION_ROLE_ADDED:
                System.out.println("Subscription role added");
                break;
            case SUBSCRIPTION_ROLE_REMOVED:
                System.out.println("Subscription role removed");
                break;
            case OTHER:
                break;
        }
    }

    private void logout() {
        api.logout(new SimpleListener() {
            @Override
            public void callback(Boolean success, ErrorObject error) {
                if (success) {
                    Log.e("callback: ", "Logged out successfully");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        logout();
        api.disconnect();
        super.onDestroy();
    }


}
