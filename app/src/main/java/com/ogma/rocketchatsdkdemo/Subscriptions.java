package com.ogma.rocketchatsdkdemo;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.SimpleListener;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.callback.GetSubscriptionListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.SubscriptionObject;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Subscriptions extends AppCompatActivity implements GetSubscriptionListener {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerAdapter recyclerAdapter;
    private RocketChatAPI api;
    private ArrayList<RocketChatAPI.ChatRoom> chatRooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Subscriptions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        api = RocketChatAPIHelper.getInstance().getApi();
        api.getSubscriptions(this);
    }

    @Override
    public void onGetSubscriptions(List<SubscriptionObject> subscriptions, ErrorObject error) {
        if (error == null) {
            //Creating Logical ChatRooms using factory class
            ChatRoomFactory factory = api.getChatRoomFactory();
            chatRooms = factory.createChatRooms(subscriptions).getChatRooms();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerAdapter.notifyDataSetChanged();
                }
            });
        } else {
            System.out.println("Got error " + error.getMessage());
            Snackbar.make(coordinatorLayout, "Error getting subscriptions", Snackbar.LENGTH_SHORT).show();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(chatRooms.get(position).getRoomData().getRoomName());
        }


        @Override
        public int getItemCount() {
            return chatRooms.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
                textView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == textView.getId()) {
                    Log.e("ViewHolder", "onClick at position: " + getAdapterPosition());
                    String roomId = chatRooms.get(getAdapterPosition()).getRoomData().getRoomId();
                    startActivity(new Intent(Subscriptions.this, Chat.class).putExtra(Chat.EXTRA_ROOM_ID, roomId));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        api.logout(new SimpleListener() {
            @Override
            public void callback(Boolean success, ErrorObject error) {
                if (success) {
                    Log.e("callback: ", "Logged out successfully");
                    api.disconnect();
                    RocketChatAPIHelper.revoke();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        logout();
        super.onDestroy();
    }
}
