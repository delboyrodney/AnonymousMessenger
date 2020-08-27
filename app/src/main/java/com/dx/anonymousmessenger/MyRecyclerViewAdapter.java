package com.dx.anonymousmessenger;

import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dx.anonymousmessenger.util.Utils;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_READ = 1;
    private static final int VIEW_TYPE_UNREAD = 2;
    private List<String[]> mData;
    private LayoutInflater mInflater;
    private View.OnClickListener mClickListener;
    private DxApplication app;
    private AppFragment appFragment;

    // data is passed into the constructor
    MyRecyclerViewAdapter(DxApplication app, List<String[]> data, AppFragment appFragment) {
        this.mInflater = LayoutInflater.from(app);
        this.app = app;
        this.mData = data;
        this.appFragment = appFragment;
    }

    @Override
    public int getItemViewType(int position) {
        String[] contact = mData.get(position);

        if (contact[2].equals("unread")) {
            return VIEW_TYPE_UNREAD;
        } else {
            return VIEW_TYPE_READ;
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_READ) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);
            view.setOnClickListener(mClickListener);
            return new MyRecyclerViewAdapter.ReadContactHolder(view);
        } else if (viewType == VIEW_TYPE_UNREAD) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);
            view.setOnClickListener(mClickListener);
            return new MyRecyclerViewAdapter.UnreadContactHolder(view);
        }
        Log.e("finding contact type","something went wrong");
        return null;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String[] contact = mData.get(position);
        long createdAt = 0;
        try {
            if(contact[5].length()>0){
                createdAt = Long.parseLong(contact[5]);
            }
        }catch (Exception ignored){}
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_READ://String msg, String send_to, long createdAt, boolean received
                ((MyRecyclerViewAdapter.ReadContactHolder) holder).bind(contact[0].equals("")?contact[1]:contact[0],contact[3],contact[4],createdAt,contact[6].equals("true"));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appFragment.stopCheckingMessages();
                        int position = holder.getAdapterPosition();
                        Intent intent = new Intent(v.getContext(), MessageListActivity.class);
                        intent.putExtra("nickname",mData.get(position)[0]);
                        intent.putExtra("address",mData.get(position)[1]);
                        v.getContext().startActivity(intent);
                    }
                });
                break;
            case VIEW_TYPE_UNREAD:
                ((MyRecyclerViewAdapter.UnreadContactHolder) holder).bind(contact[0].equals("")?contact[1]:contact[0],contact[3],contact[4],createdAt,contact[6].equals("true"));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appFragment.stopCheckingMessages();
                        int position = holder.getAdapterPosition();
                        Intent intent = new Intent(v.getContext(), MessageListActivity.class);
                        intent.putExtra("nickname",mData.get(position)[0]);
                        intent.putExtra("address",mData.get(position)[1]);
                        v.getContext().startActivity(intent);
                    }
                });
                break;
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if(mData==null){
            return 0;
        }
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        TextView myTextView;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            myTextView = itemView.findViewById(R.id.contact_name);
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//        }
//    }

    // convenience method for getting data at click position
    public String[] getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(View.OnClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private class ReadContactHolder extends RecyclerView.ViewHolder {
        TextView contactName,msgText,timeText;
        ImageView imageView,seen;

        ReadContactHolder(View itemView) {
            super(itemView);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            imageView = (ImageView) itemView.findViewById(R.id.contact_unread_circle);
            timeText = itemView.findViewById(R.id.time_text);
            msgText = itemView.findViewById(R.id.message_text);
            seen = itemView.findViewById(R.id.seen);
        }

        void bind(String title, String msg, String send_to, long createdAt, boolean received) {
            contactName.setText(title);
            contactName.setTypeface(null, Typeface.NORMAL);
            imageView.setVisibility(View.INVISIBLE);
            msgText.setText(msg);
            timeText.setText(createdAt>0?Utils.formatDateTime(createdAt):"");
            seen.setVisibility(send_to.equals(app.getHostname())?View.GONE:received?View.VISIBLE:View.GONE);
        }
    }

    private class UnreadContactHolder extends ReadContactHolder {

        UnreadContactHolder(View itemView) {
            super(itemView);
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.dx_night_700,itemView.getResources().newTheme()));
        }

        void bind(String title, String msg, String send_to, long createdAt, boolean received) {
            imageView.setVisibility(View.VISIBLE);
            super.bind(title,msg,send_to,createdAt,received);
        }
    }
}
