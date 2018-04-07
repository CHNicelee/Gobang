
package csu.edu.ice.gobang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnEnter = findViewById(R.id.btnEnter);
        EditText etRoomNumber = findViewById(R.id.etRoomNumber);
        btnEnter.setOnClickListener(e->{
            Integer roomNumber = Integer.valueOf(etRoomNumber.getText().toString());
            Intent intent = new Intent(this,GobangActivitiy.class);
            intent.putExtra("roomNumber",roomNumber);
            intent.putExtra("userId",id);
            startActivity(intent);
        });
        int seconds = (int) (System.currentTimeMillis() / 1000);
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        if(sp.getInt("id",-1)!=-1){
            //不是第一次
             id = sp.getInt("id",-1);
        }else {
            id = seconds;
            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
            editor.putInt("id", id);
            editor.commit();
        }
    }


}
