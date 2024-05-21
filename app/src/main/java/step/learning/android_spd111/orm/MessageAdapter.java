package step.learning.android_spd111.orm;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import step.learning.android_spd111.R;


public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public MessageAdapter(Context context, List<ChatMessage> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        TextView textViewAuthor = convertView.findViewById(R.id.textViewAuthor);
        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        LinearLayout messageContainer = convertView.findViewById(R.id.messageContainer);

        textViewDate.setText(dateFormat.format(message.getMoment()));
        textViewAuthor.setText(message.getAuthor());
        textViewMessage.setText(message.getText());

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();
        if (position % 2 == 0) {
            // Своє повідомлення (парні)
            textViewAuthor.setVisibility(View.VISIBLE);   // приховати  -  View.GONE
            messageContainer.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.chat_msg_other));
            layoutParams.gravity = Gravity.END; // переміщення до правого краю
        } else {
            // Не своє повідомлення (непарні)
            textViewAuthor.setVisibility(View.VISIBLE);
            messageContainer.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.chat_msg_my));
            layoutParams.gravity = Gravity.START; // переміщення до лівого краю
        }
        messageContainer.setLayoutParams(layoutParams);

        return convertView;
    }
}
