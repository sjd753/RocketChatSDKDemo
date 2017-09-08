package com.ogma.rocketchatsdkdemo;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cpiz.android.bubbleview.BubbleStyle;
import com.cpiz.android.bubbleview.BubbleTextView;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.callback.HistoryListener;
import com.rocketchat.core.callback.MessageListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.attachment.TAttachment;
import com.rocketchat.livechat.callback.LoadHistoryListener;
import com.rocketchat.livechat.model.LiveChatMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends AppCompatActivity implements View.OnClickListener, MessageListener.SubscriptionListener, SubscribeListener, HistoryListener {

    public static final String EXTRA_ROOM_ID = "extra_room_id";

    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    private EditText etMessage;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private String roomId = "";
    private RocketChatAPI.ChatRoom chatRoom;
    private List<RocketChatMessage> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChatHistory();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        etMessage = (EditText) findViewById(R.id.et_message);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        if (getIntent().hasExtra(EXTRA_ROOM_ID)) {
            roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
            RocketChatAPI api = RocketChatAPIHelper.getInstance().getApi();
            //Creating Logical ChatRooms using factory class
            ChatRoomFactory factory = api.getChatRoomFactory();
            chatRoom = factory.getChatRoomById(roomId);
            subscribeMessage();
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private void getChatHistory() {
        /****
         * @param oldestMessageTimestamp Used to do pagination (null means latest timestamp)
         * @param count The message quantity, messages are loaded having timestamp older than @param oldestMessageTimestamp
         * @param lastTimestamp Date of the last time when client got data (Used to calculate unread)[unread count suggests number of unread messages having timestamp above @param lastTimestamp]
         **/
//        Hint: pass count=20, oldestMessageTimestamp=new Date(),lastTimestamp=null for getting latest 20 messages

        chatRoom.getChatHistory(20, new Date(), null, this);
    }

    @Override
    public void onLoadHistory(List<RocketChatMessage> list, int unreadNotLoaded, ErrorObject error) {
        System.out.println("onLoadHistory size = " + list.size());
        Chat.this.list = list;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void subscribeMessage() {
        chatRoom.subscribeRoomMessageEvent(this, this);
    }

    @Override
    public void onSubscribe(Boolean isSubscribed, String subId) {
        if (isSubscribed) {
            System.out.println("subscribed to room successfully");
            getChatHistory();
        }
    }

    @Override
    public void onMessage(String roomId, RocketChatMessage message) {
        System.out.println("Got message " + message.getMessage());
        switch (message.getMsgType()) {
            case TEXT:
                System.out.println("This is a text message: " + message.getMessage());
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

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

//        private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
//                .cloneFrom(UniversalImageLoaderFactory.getDefaultOptions())
//                .displayer(new FadeInBitmapDisplayer(1400, true, false, false))
//                .build();

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new RecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
//            imageLoader.displayImage(jArr.optJSONObject(position).optString("sender_image"),
//                    holder.ivImage,
//                    displayImageOptions);
            String message = list.get(position).getMessage();
            holder.tvMsg.setText(message.trim());
            String dateTime = list.get(position).getUpdatedAt().toString();
            holder.tvDateTime.setText(dateTime);

            boolean myMessage = position % 2 == 0;
//            boolean myMessage = list.get(position).getSender().getUserId().equals(app.getAppSettings().__uId);
            holder.itemView.setLayoutDirection(!myMessage ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
            holder.tvMsg.setArrowDirection(!myMessage ? BubbleStyle.ArrowDirection.Left : BubbleStyle.ArrowDirection.Right);
            holder.tvMsg.setFillColor(!myMessage ? ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()) : ResourcesCompat.getColor(getResources(), android.R.color.darker_gray, getTheme()));
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView ivImage;
            BubbleTextView tvMsg;
            TextView tvDateTime;


            ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
                tvMsg = (BubbleTextView) itemView.findViewById(R.id.tv_message);
                tvDateTime = (TextView) itemView.findViewById(R.id.tv_time_stamp);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == itemView.getId()) {
                    Log.e("ViewHolder", "onClick at position: " + getAdapterPosition());
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == fab.getId()) {
            if (!TextUtils.isEmpty(etMessage.getText().toString())) {
                //Without acknowledgement
                chatRoom.sendMessage(etMessage.getText().toString());
                etMessage.setText("");
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
}
