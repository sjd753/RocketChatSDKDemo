package com.ogma.rocketchatsdkdemo;

import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.core.RocketChatAPI;

/**
 * Created by User on 07-09-2017.
 */

public class RocketChatAPIHelper {

    private final String SERVER_URL = "YOUR SERVER URL HERE"; //Your server url where Rocket Chat is installed
    private final int MAX_RECONNECT_ATTEMPTS = 5;    //Number of attemps are 10
    private final int RECONNECT_INTERVAL = 3000; // in milliseconds, reconnection will be called after 5 seconds

    static class Cred {
        static final String EMAIL = "", PASSWORD = ""; //Set default email and password for quicker login while debugging.
    }

    private static RocketChatAPIHelper rocketChatAPIHelper;
    private RocketChatAPI api;

    private RocketChatAPIHelper() {
        api = new RocketChatAPI(SERVER_URL);
        api.setReconnectionStrategy(new ReconnectionStrategy(MAX_RECONNECT_ATTEMPTS, RECONNECT_INTERVAL));
    }

    public static RocketChatAPIHelper getInstance() {
        if (rocketChatAPIHelper == null) {
            rocketChatAPIHelper = new RocketChatAPIHelper();
        }
        return rocketChatAPIHelper;
    }

    public RocketChatAPI getApi() {
        return api;
    }

    public static void revoke() {
        rocketChatAPIHelper = null;
    }

}
