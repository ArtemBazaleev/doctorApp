package com.example.doctorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.doctorapp.R;
import com.example.doctorapp.model.BaseMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_PHOTO = 3;
    private static final int VIEW_TYPE_MESSAGE_SENT_PHOTO = 4;

    private Context mContext;
    private List<BaseMessage> mMessageList;
    private IOnReceivedImageClicked mListener;

    public MessageListAdapter(Context context, List<BaseMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    public void addMessage(BaseMessage message){
        mMessageList.add(message);
        notifyDataSetChanged();
    }

    public void addOnRecivedImageListener(IOnReceivedImageClicked listener){
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position).messageType == BaseMessage.MESSAGE_TYPE_SENDER) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else if ((mMessageList.get(position).messageType == BaseMessage.MESSAGE_TYPE_RECIVER)){
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }  else if ((mMessageList.get(position).messageType == BaseMessage.MESSAGE_TYPE_SENDER_IMAGE)){
            return VIEW_TYPE_MESSAGE_SENT_PHOTO;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED_PHOTO;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED_PHOTO){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_recived_image, parent, false);
            return new ReceivedImageMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent_image, parent, false);
            return new SentMessageImageHolder(view);
        }

    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        BaseMessage message = mMessageList.get(position);
        if (position == getItemCount()-1 && holder.getItemViewType()==VIEW_TYPE_MESSAGE_RECEIVED)
            animateLeftToRight(holder.itemView);
        if (position == getItemCount()-1 && holder.getItemViewType()==VIEW_TYPE_MESSAGE_SENT)
            animateRightToLeft(holder.itemView);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_PHOTO:
                ((ReceivedImageMessageHolder) holder).onBind(message);
                break;
            default:
                ((SentMessageImageHolder) holder).onBind(message);
        }
    }

    private void animateRightToLeft(View itemView) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.right_to_left_anim);
        itemView.startAnimation(animation);
    }

    private void animateLeftToRight(View holder) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.left_to_right_anim);
        holder.startAnimation(animation);
    }

    public void addMessages(List<BaseMessage> subList) {
        for (BaseMessage i:subList) {
            mMessageList.add(0, i);
            notifyItemInserted(0);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
        }

        void bind(BaseMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String strDate = mdformat.format(calendar.getTime());
            timeText.setText(strDate);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            nameText =  itemView.findViewById(R.id.text_message_name);
            profileImage =  itemView.findViewById(R.id.image_message_profile);
        }

        void bind(BaseMessage message) {
            messageText.setText(message.getMessage());
            // Format the stored timestamp into a readable String using method.
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String strDate = mdformat.format(calendar.getTime());
            timeText.setText(strDate);
            nameText.setText(message.getFrom());
        }
    }

    private class ReceivedImageMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private BaseMessage message;
        ReceivedImageMessageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageReceived);
            imageView.setOnClickListener(this);
        }
        void onBind(BaseMessage message){
            this.message = message;
            Glide.with(mContext)
                    .load(message.getUri())
                    .into(imageView);
        }

        @Override
        public void onClick(View v) {
            if (mListener!=null)
                mListener.onReceivedImage(message);
        }
    }

    private class SentMessageImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        SentMessageImageHolder(@NonNull View view) {
            super(view);
            imageView = itemView.findViewById(R.id.imageReceived);
        }

        void onBind(BaseMessage baseMessage){
            Glide.with(mContext)
                    .load(baseMessage.getUri())
                    .into(imageView);
        }
    }

    public interface IOnReceivedImageClicked{
        void onReceivedImage(BaseMessage baseMessage);
    }
}