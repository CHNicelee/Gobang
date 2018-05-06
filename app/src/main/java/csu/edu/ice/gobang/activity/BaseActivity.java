package csu.edu.ice.gobang.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by ice on 2018/5/6.
 */

public class BaseActivity extends AppCompatActivity{
    public static Toast toast;
    public void showToast(String text){
        if(toast==null){
            toast = Toast.makeText(this,text,Toast.LENGTH_SHORT);
        }else {
            toast.setText(text);
        }
        toast.show();
    }

}
