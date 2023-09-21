package com.example.f_bot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    LottieAnimationView typingIndicator;
    RecyclerView recyclerView;
    TextView left_text;
    EditText message;
    ImageView send;
    List<MessageModel> list;
    MessageAdapter adapter;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerview);
        message = findViewById(R.id.message);
        send = findViewById(R.id.sendBtn);

        typingIndicator = findViewById(R.id.typing_indicator);
        typingIndicator.setVisibility(View.INVISIBLE);

        list = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(this,list);
        recyclerView.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = message.getText().toString();
                if(question.isEmpty()){
                    Toast.makeText(MainActivity.this, "Type something", Toast.LENGTH_SHORT).show();
                }else{
                    addToChat(question,MessageModel.SENT_BY_ME);
                    message.setText("");
                    callAPI(question);
                }
            }
        });
    }


    private void addToChat(String question, String sentByMe) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list.add(new MessageModel(question,sentByMe));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });
    }

    private void addResponse(String response) {
        list.remove(list.size() - 1);
        addToChat(response, MessageModel.SENT_BY_BOT);
    }

    //okHttp API
    void callAPI(String question) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Show the typing indicator before making the API call
                typingIndicator.setVisibility(View.VISIBLE);

            }
        });
        list.add(new MessageModel("",MessageModel.SENT_BY_BOT));
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "text-davinci-003");
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle API call failure
                addResponse("Failed to load due to " + e.getMessage());

                // Hide the typing indicator when done
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        typingIndicator.setVisibility(View.INVISIBLE);

                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle API response
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    addResponse("Failed to load due to " + response.body().toString());
                }
                // Hide the typing indicator when done
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        typingIndicator.setVisibility(View.INVISIBLE);

                    }
                });
            }
        });

    }

}