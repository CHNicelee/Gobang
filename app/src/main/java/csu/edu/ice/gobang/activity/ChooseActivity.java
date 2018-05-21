package csu.edu.ice.gobang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import csu.edu.ice.gobang.R;

public class ChooseActivity extends AppCompatActivity {
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        Button friendplay = (Button)findViewById(R.id.friendplaybutton);
        Button oneplay = (Button)findViewById(R.id.oneplaybutton);
        Button robotPlay = findViewById(R.id.robotPlay);
        friendplay.setOnClickListener(view -> {
            Intent intent = new Intent(ChooseActivity.this,MainActivity.class);
            startActivity(intent);
        });
        oneplay.setOnClickListener(view -> {
            Intent intent = new Intent(ChooseActivity.this,GobangSoloActivity.class);
            startActivity(intent);
        });

        robotPlay.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseActivity.this,GobangSoloActivity.class);
            intent.putExtra("robot",true);
            startActivity(intent);
        });


    }
}
