package com.codefactoryglobal.emojitotext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EmojiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.editText);
                TextView output = findViewById(R.id.resultText);
                String input_text = input.getText().toString();
                String translated_text = EmojiToText.translateEmoji(EmojiActivity.this, input_text);
                output.setText(translated_text);
            }
        });

    }
}
