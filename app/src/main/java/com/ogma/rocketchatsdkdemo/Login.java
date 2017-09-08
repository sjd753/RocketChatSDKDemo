package com.ogma.rocketchatsdkdemo;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.hanks.htextview.HTextView;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SimpleListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.model.TokenObject;

import static com.ogma.rocketchatsdkdemo.RocketChatAPIHelper.Cred.EMAIL;
import static com.ogma.rocketchatsdkdemo.RocketChatAPIHelper.Cred.PASSWORD;

public class Login extends AppCompatActivity implements LoginListener, ConnectListener {

    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private TextInputLayout tilPassword;
    private EditText etPassword;

    private RocketChatAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        api = RocketChatAPIHelper.getInstance().getApi();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        etEmail = (EditText) findViewById(R.id.et_email);

        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        etPassword = (EditText) findViewById(R.id.et_password);

        etEmail.setText(EMAIL);
        etPassword.setText(PASSWORD);
    }

    private boolean validateSignIn() {
        if (etEmail.getText().toString().trim().length() == 0) {
            tilEmail.setError("Please enter your email");
            Snackbar.make(coordinatorLayout, "Please enter your email", Snackbar.LENGTH_SHORT).show();
            return false;
        }
//        else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
//            tilEmail.setError("Please enter valid email");
//            Snackbar.make(coordinatorLayout, "Please enter valid email", Snackbar.LENGTH_SHORT).show();
//            return false;
//        }
        else {
            tilEmail.setErrorEnabled(false);
        }

        if (etPassword.getText().toString().trim().length() == 0) {
            tilPassword.setError("Please enter a password");
            Snackbar.make(coordinatorLayout, "Please enter a password", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilPassword.setErrorEnabled(false);
        }

        return true;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_login) {
            api = RocketChatAPIHelper.getInstance().getApi();
            if (validateSignIn()) {
                api.connect(this);
                Snackbar.make(coordinatorLayout, "Connecting...", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnect(String sessionID) {
        Log.e("onConnect: ", "Connected to server. Session Id: " + sessionID);
        Snackbar.make(coordinatorLayout, "Connected", Snackbar.LENGTH_SHORT).show();
        api.login(etEmail.getText().toString().trim(), etPassword.getText().toString(), this);
        Snackbar.make(coordinatorLayout, "Authenticating...", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect(boolean closedByServer) {
        Log.e("onDisconnect: ", "Disconnected from server");
        Snackbar.make(coordinatorLayout, "Disconnected", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(Exception websocketException) {
        Log.e("onConnectError: ", "Got connect error with the server");
        Snackbar.make(coordinatorLayout, "Connection Error", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onLogin(TokenObject token, ErrorObject error) {
        if (error == null) {
            Log.e("onLogin: ", "Logged in successfully, returned token " + token.getAuthToken());
            Snackbar.make(coordinatorLayout, "Authenticated", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(this, Subscriptions.class));
        } else {
            Log.e("onLogin: ", "Got error " + error.getMessage());
            Snackbar.make(coordinatorLayout, "Authenticate Error", Snackbar.LENGTH_SHORT).show();
        }
    }
}
