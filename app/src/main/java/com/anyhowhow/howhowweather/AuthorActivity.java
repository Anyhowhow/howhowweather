package com.anyhowhow.howhowweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        Button backButton = (Button)findViewById(R.id.authorBack_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AuthorActivity.this,WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        });
        TextView authorTextInfo = (TextView)findViewById(R.id.author_Info);
        StringBuilder authorInfo = new StringBuilder();
        authorInfo.append("武汉大学 汪礼浩").append("\n");
        authorInfo.append("2018.夏");
        authorTextInfo.setText(authorInfo);
    }
}
