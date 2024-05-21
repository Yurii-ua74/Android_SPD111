package step.learning.android_spd111;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import step.learning.android_spd111.orm.ChatMessage;
import step.learning.android_spd111.orm.MessageAdapter;

public class MyChatActivity extends AppCompatActivity {

    private final List<ChatMessage> chatMessages = new ArrayList<>();  // список повідомлень чату
    private MessageAdapter adapter;        // адаптер для відображення повідомлень чату
    private LinearLayout chatContainer;    // контейнер  для відображення повідомлень
    private ScrollView chatScroller;       // для прокрутки повідомлень

    // Метод onCreate викликається, коли активність створюється.
    // В ньому ініціалізуються компоненти інтерфейсу і логіка.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Викликає метод onCreate суперкласу AppCompatActivity,
        // щоб забезпечити належне створення активності.
        super.onCreate(savedInstanceState);
        // Викликає метод для налаштування режиму "edge-to-edge",
        // що дозволяє інтерфейсу використовувати всю площу екрана,
        // включаючи області під системними панелями.
        EdgeToEdge.enable(this);
        // Встановлює макет активності, використовуючи файл activity_my_chat.xml
        setContentView(R.layout.activity_my_chat);
        // Встановлює слухач для обробки зміщень системних панелей.
        // Метод setPadding налаштовує відступи вмісту відповідно до системних панелей.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Ініціалізація змінних
        chatContainer = findViewById(R.id.chat_container);
        chatScroller = findViewById(R.id.chat_scroller);
        // Знаходить елемент введення повідомлень і зберігає його в змінну editTextMessage
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        // Знаходить кнопку відправки повідомлень і зберігає її в змінну buttonSend
        Button buttonSend = findViewById(R.id.buttonSend);
        // Ініціалізує адаптер MessageAdapter, передаючи контекст і список повідомлень
        adapter = new MessageAdapter(this, chatMessages);

        //Встановлює слухач натискань на кнопку buttonSend,
        // що викликає метод для обробки введеного повідомлення.
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // Перевизначає метод onClick для обробки події натискання на кнопку buttonSend
                String messageText = editTextMessage.getText().toString(); // Отримує текст введеного повідомлення.
                // Перевірка, чи текст повідомлення не є порожнім
                if (!messageText.isEmpty()) {
                    // Визначає автора повідомлення.
                    // Якщо кількість повідомлень парна, автор - "Me", інакше - "Other"
                    String author = (chatMessages.size() % 2 == 0) ? "Me:" : "Other:";
                    ChatMessage message = new ChatMessage(); // новий об'єкт ChatMessage
                    message.setText(messageText);      // Встановлює текст повідомлення.
                    message.setAuthor(author);         // Встановлює автора повідомлення
                    message.setMoment(new Date());     // Встановлює час створення повідомлення
                    chatMessages.add(message);         // Додає нове повідомлення до списку chatMessages
                    addMessageToView(message);         // Викликає метод для додавання нового повідомлення до інтерфейсу
                    editTextMessage.setText("");       // Очищує поле вводу після відправки повідомлення
                    // Прокручує ScrollView до кінця, щоб показати нове повідомлення
                    chatScroller.post(() -> chatScroller.fullScroll(View.FOCUS_DOWN));
                }
            }
        });
    }
    private void addMessageToView(ChatMessage message) {  //  додавання нового повідомлення до контейнера
        // адаптер для створення представлення нового повідомлення
        View view = adapter.getView(chatMessages.size() - 1, null, chatContainer);
        // Додає представлення повідомлення до LinearLayout, який містить всі повідомлення
        chatContainer.addView(view);
    }
}