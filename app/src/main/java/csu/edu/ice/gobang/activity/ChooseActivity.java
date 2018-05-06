package csu.edu.ice.gobang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

        friendplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        oneplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseActivity.this,GobangSoloActivity.class);
                startActivity(intent);
            }
        });

    }
}
