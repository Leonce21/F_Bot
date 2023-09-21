package com.example.f_bot;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.viewHolder>{

    List<MessageModel> modelList;
    private Context context; // Add a context variable

    public MessageAdapter(Context context,List<MessageModel> messageList) {
        this.modelList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat,null);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        MessageModel model = modelList.get(position);

        if(model.getSentBy().equals(MessageModel.SENT_BY_ME)){
            holder.leftchat.setVisibility(View.GONE);
            holder.rightchat.setVisibility(View.VISIBLE);
            holder.right_text.setText(model.getMessage());
        }else{
            holder.rightchat.setVisibility(View.GONE);
            holder.leftchat.setVisibility(View.VISIBLE);
            holder.left_text.setText(model.getMessage());
        }

        // Show or hide the typing indicator based on the sender
//        if (model.getSentBy().equals(MessageModel.SENT_BY_BOT)) {
//            holder.typingIndicator.setVisibility(View.VISIBLE);
//            try {
//                holder.typingIndicator.setAnimation("typing_animation.json"); // Set the animation file
//                holder.typingIndicator.playAnimation(); // Start the Lottie animation
//            } catch (Exception e) {
//                Log.e("LottieError", "Error loading animation: " + e.getMessage());
//            }
//        } else {
//            holder.typingIndicator.setVisibility(View.GONE);
//            holder.typingIndicator.cancelAnimation(); // Stop and hide the Lottie animation
//        }

        // Store the current position for context menu handling
        holder.position = position;

        // Set long-press listener to show the popup menu
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopupMenu(v, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout rightchat, leftchat;
        TextView left_text, right_text;
        LottieAnimationView typingIndicator;

        private int position; // Store the current position

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            rightchat = itemView.findViewById(R.id.right_chat);
            right_text = itemView.findViewById(R.id.right_text);
            leftchat = itemView.findViewById(R.id.left_chat);
            left_text = itemView.findViewById(R.id.left_text);
//            typingIndicator = itemView.findViewById(R.id.typing_indicator);// Initialize the typing indicator view

        }

    }

    // Show the popup menu when a message is long-pressed
    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.message_context_menu, popupMenu.getMenu());

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MessageModel model = modelList.get(position);
                switch (item.getItemId()) {
                    case R.id.menu_copy:
                        copyToClipboard(model.getMessage());
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_paste:
                        String clipboardText = pasteFromClipboard();
                        // Do something with the pasted text
                        Toast.makeText(context, "Pasted text: " + clipboardText, Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popupMenu.setForceShowIcon(true);
        }

        popupMenu.show();
    }

    private void copyToClipboard(String textToCopy) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Text", textToCopy);
        clipboardManager.setPrimaryClip(clipData);
    }

    private String pasteFromClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            return item.getText().toString();
        }
        return "";
    }

}
