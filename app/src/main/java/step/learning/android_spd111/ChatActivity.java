package step.learning.android_spd111;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.android_spd111.orm.ChatMessage;
import step.learning.android_spd111.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private final byte[] buffer = new byte[8096];  // буфер для зчитування даних

    // паралельні запити до кількох ресурсів не працюють, виконується лише один
    // це обмежує вибір виконавчого сервісу.
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EditText etNik;
    private EditText etMessage;
    private ScrollView chatScroller;
    private LinearLayout container;
    private MediaPlayer newMessageSound;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final Handler handler = new Handler();
    private boolean isAuthorLocked = false; // для блоування зміни Ніка
    private boolean isMuted = false; // для вимикача звука

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Заважає адаптуватись під екранну клавіатуру
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updateChat();
        // використати фото з інтернет-ресурса
        urlToImageView(
                "https://cdn-icons-png.flaticon.com/512/5962/5962463.png",
                findViewById(R.id.chat_iv_logo));
        // робота з ресурсами
        etNik = findViewById( R.id.chat_et_nik ) ;
        etMessage = findViewById( R.id.chat_et_message ) ;
        chatScroller = findViewById( R.id.chat_scroller ) ;
        container = findViewById( R.id.chat_container );
        newMessageSound = MediaPlayer.create(this, R.raw.pickup);

        findViewById( R.id.chat_btn_send ).setOnClickListener( this::onSendClick );

        // Mute Switch
        Switch muteSwitch = findViewById(R.id.chat_switch_mute);
        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isMuted = isChecked);


        container.setOnClickListener( (v) ->  hideSoftInput() );
    }


    private void hideSoftInput() {
        // клавіатура з'являється автоматично через фокус введення,
        // прибрати її - прибрати фокус
        // шукаємо елемент, що має фокус введення
        View focusedView = getCurrentFocus();
        if(focusedView != null) {
            // Запитуємо систему щодо засобів управління клавіатурою
            InputMethodManager manager = (InputMethodManager)
                    getSystemService( Context.INPUT_METHOD_SERVICE );
            // Прибираємо клавіатуру з фокусованого елемента
            manager.hideSoftInputFromWindow( focusedView.getWindowToken(), 0);
            // прибираємо фокус з елемента
            focusedView.clearFocus();
        }
    }
    private void updateChat() {
        if( executorService.isShutdown() )  { return; }
            CompletableFuture
                    .supplyAsync(this::loadChat, executorService)  // supply - тільки повертає
                    .thenApplyAsync(this::processChatResponse)     // Apply - перетворювач (який і приймає і повертає)
                    .thenAcceptAsync(this::displayChatMessages);   // Accept - тільки приймає

            handler.postDelayed(this::updateChat, 3000);
    }
    private void onSendClick(View v) {
        String author = etNik.getText().toString();
        String message = etMessage.getText().toString();

        if(author.isEmpty()) {
            Toast.makeText(this, "Введіть свій 'nickname'", Toast.LENGTH_SHORT).show();
            return;
        }
        if(message.isEmpty()) {
            Toast.makeText(this, "Введіть повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor( author );
        chatMessage.setText( message );

        if (!isAuthorLocked) {
            etNik.setEnabled(false);
            isAuthorLocked = true;
        }

        //etMessage.setText("");  // очищення поля вводу

        CompletableFuture
                .runAsync( () -> sendChatMessage( chatMessage ), executorService)
                .thenRun(() -> runOnUiThread(() -> {
                    etMessage.setText("");  // очищення поля вводу
                    updateChat();           // оновлення чату
                }));

    }
    private void sendChatMessage( ChatMessage chatMessage) {
    /*
        Необхідно сформувати POST-запит на URL чату та передати дані форми
        з полями author та msg з відповідними значеннями з chatMessage
        дані форми:
        - заголовок Content-Type: application/x-www-form-urlencoded
        - тіло у вигляді: author=TheAuthor&msg=The%20Message
    */
        try {

            // 1, готуємо підключення та налаштовуємо його
            URL url = new URL( CHAT_URL ) ;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );  // не ділити на чанки (фрагменти)
            connection.setDoOutput( true );  // запис у підключення -- передача тіла
            connection.setDoInput( true );   // читання -- одержання тіла відповіді від сервера

            connection.setRequestMethod( "POST" );
            // заголовки у connection задаються через setRequestProperty
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            // 2. Запис тіла (DoOutput) в буфер
            OutputStream connectionOutput = connection.getOutputStream() ;
            String body = String.format(  // author=TheAuthor&msg=The%20Message
                    "author=%s&msg=%s",
                    URLEncoder.encode( chatMessage.getAuthor(), StandardCharsets.UTF_8.name() ),
                    URLEncoder.encode( chatMessage.getText(), StandardCharsets.UTF_8.name() )
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ));
            // 3, Надсилаємо - "ВИШТОВХУЄМО" буфер
            connectionOutput.flush();
            // 3.1 Звільняємо ресурс (якщо не використовуємо форму try(){})
            connectionOutput.close();

            //4. Отримуємо відповідь
            int statusCode = connection.getResponseCode();
            // у разі успіху сервер передає код 201 і не передає тіло
            // якщо помилка, то код інший та є тіло з описом помилки
            if( statusCode == 201 ) {
                // якщо потрібно тіло відповіді, то воно у потоці .getInputStream()
                // запуск оновлення чату
                updateChat();
            }
            else{
                InputStream connectionInput = connection.getErrorStream();
                //body = readString( connectionInput );////////////////////
                String responseBody = readString(connectionInput);
                connectionInput.close();
                //Log.e( "sendChatMessage", body ); ///////////////////
                Log.e("sendChatMessage", responseBody);
            }

            // 5. закриваємо підключення
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e( "sendChatMessage", ex.getMessage());
        }
    }
    private String loadChat() {
        //URL chatUrl = new URL( CHAT_URL );
        try( InputStream chatStream = new URL( CHAT_URL ).openStream() ) {
            return readString( chatStream );
        }
        catch (Exception ex) {
            // логування помилки
            Log.e("ChatActivity::loadChat()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() ) ;
        }
        return null;
    }
    private boolean processChatResponse(String  response) {
        boolean wasNewMessage = false;
        boolean isFirstProcess = this.chatMessages.isEmpty();
        try {
            ChatResponse chatResponse = ChatResponse.fromJsonString( response ) ;
            String currentUser = etNik.getText().toString(); // додано для вимикача звуку
            for( ChatMessage message : chatResponse.getData() ) {
                if( this.chatMessages.stream().noneMatch(
                        m -> m.getId().equals( message.getId() ) ) ) {
                    // немає жодного повідомлення з таким id, як у message -- це нове повідомлення
                    this.chatMessages.add( message ) ;
                    // якщо автор інший то увімкнути звук
                    if (!message.getAuthor().equals(currentUser)) {  //////////
                        wasNewMessage = true;
                    }
                }
            }
            if(isFirstProcess) {
                this.chatMessages.sort( Comparator.comparing( ChatMessage::getMoment ) ) ;
            }
            else if(wasNewMessage) {
                newMessageSound.start();
            }
        }
        catch (IllegalArgumentException ex) {
            Log.e("ChatActivity::processChatResponse",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() );
        }
        return wasNewMessage;
    }
    private void displayChatMessages( boolean wasNewMessage ) {
        if( ! wasNewMessage ) return;

        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my );
        Drawable otherBackground = AppCompatResources.getDrawable( //// додано для інших повідомлень
                getApplicationContext(),
                R.drawable.chat_msg_other);
        // для своїх повідомлень
        LinearLayout.LayoutParams myMsgParams = new LinearLayout.LayoutParams( /////////////
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        myMsgParams.setMargins(0, 10, 8, 10); /////////////
        myMsgParams.gravity = Gravity.END;  //////////////
        // для інших повідомлень
        LinearLayout.LayoutParams otherMsgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        otherMsgParams.setMargins(8, 10, 0, 10);
        otherMsgParams.gravity = Gravity.START;

        String currentUser = etNik.getText().toString();  /////////////

        runOnUiThread( () -> {
            for( ChatMessage message : this.chatMessages ) {
                if(message.getView() != null) {   // вже показане
                    continue;
                }
                TextView tv = new TextView(this);
                tv.setText(String.format(
                        "[%s] %s: \n%s",
                        message.getMoment(),
                        message.getAuthor(),
                        message.getText()
                ));
                //  розділю свої повідомлення та інші
                if (message.getAuthor().equals(currentUser)) {
                    tv.setBackground(myBackground);
                    tv.setGravity(Gravity.END);
                    tv.setLayoutParams(myMsgParams);
                } else {
                    tv.setBackground(otherBackground);
                    tv.setGravity(Gravity.START);
                    tv.setLayoutParams(otherMsgParams);
                }
                tv.setPadding(15, 5, 15, 5);
                container.addView(tv);
                message.setView(tv);
            }
            /*
            chatScroller.fullScroll( View.FOCUS_DOWN ) ;
            Асинхронність Android призводить до того, що на момент подачі команди
            не всі представлення, додані до контейнера, вже сформовані.
            Прокрутка діятиме лише на поточне наповнення контейнера.
             */
            chatScroller.post(   // передача дії, яка виконається після поточної черги
                    () -> chatScroller.fullScroll( View.FOCUS_DOWN )
            );
        } ) ;
    }
    private String readString( InputStream stream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while( ( len = stream.read(buffer) ) != -1 ) {
            byteBuilder.write( buffer, 0, len );
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }
    @Override
    protected void onDestroy() {
        executorService.shutdownNow();  // зупинка всіх потоків
        super.onDestroy();
    }
    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture.supplyAsync(
                () -> {
                    try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
                        return BitmapFactory.decodeStream( is );
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, executorService )
                .thenAccept( imageView::setImageBitmap );
    }

}


/*
Робота з мережею Інтернет
основу складає клас java.net.URL
традиційно для Java створення об'єкту не призводить до якоїсь активності,
лише створюється програмний об'єкт.
Підключення та передача даних здійснюється при певних командах, зокрема,
відкриття потоку.
Читання даних з потоку має особливості
 - мульти-байтове кодування: різні символи мають різну байтову довжину. Це
     формує пораду спочатку одержати всі дані у бінарному вигляді і потім
     декодувати як рядок (замість одержання фрагментів даних і їх перетворення)
 - запити до мережі не можуть виконуватись з основного (UI) потоку. Це
     спричинює виняток (android.os.NetworkOnMainThreadException).
     Варіанти рішень
     = запустити в окремому потоці
        + простіше і наочніше
        - складність завершення різних потоків, особливо, якщо їх багато.
     = запустити у фоновому виконавці
        + централізоване завершення
        - не забути завершення
 - Для того щоб застосунок міг звертатись до мережі йому потрібні
      відповідні дозволи. Без них виняток (Permission denied (missing INTERNET permission?))
      Дозволи зазначаються у маніфесті
       <uses-permission android:name="android.permission.INTERNET"/>
 - Необхідність запуску мережних запитів у окремих потоках часто призводить до
     того, що з них обмежено доступ до елементів UI
     (Only the original thread that created a view hierarchy can touch its views.)
     Перехід до UI потоку здійснюється або викликом runOnUiThread або переходом
     до синхронного режиму.
 */
