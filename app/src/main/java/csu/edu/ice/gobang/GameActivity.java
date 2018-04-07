package csu.edu.ice.gobang;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;

/**
 * Created by ice on 2018/3/30.
 */

public class GameActivity extends AppCompatActivity  implements SocketUtil.MessageHandler{

    private static final int FRIEND_CONNECT = 0;
    private static final int FRIEND_MESSAGE = 1;
    private static final int SERVER_RESPONSE = 2;
    private Integer roomNumber;
    private int userId;
    private Integer friendId;
    private Socket socket;
    private TextView tvMessage;
    private TextView tvFriend;
    private TextView tvRoomNumber;
    private EditText etInput;
    private Button btnSend;
    private String TAG = "GameActivity";

    private String message;
    private SocketUtil socketUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
         roomNumber = getIntent().getIntExtra("roomNumber",0);
         userId = getIntent().getIntExtra("userId",0);

         tvMessage = findViewById(R.id.tvMessage);
         tvFriend = findViewById(R.id.tvFriend);
         tvRoomNumber = findViewById(R.id.tvRoomNumber);
         etInput = findViewById(R.id.etInput);
         btnSend = findViewById(R.id.btnSend);
        btnSend.setClickable(false);
         tvRoomNumber.setText(roomNumber+"");
//         new SokectThread().start();

        MsgBean msgBean = new MsgBean();
        msgBean.setRoom(roomNumber);
        socketUtil = new SocketUtil(this);
        socketUtil.setDataClass(MsgBean.class);
        socketUtil.connect("192.168.191.1", 8885, new EasyMessage(userId,null,msgBean), new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(GameActivity.this, "服务器连接成功", Toast.LENGTH_SHORT).show();
                btnSend.setClickable(true);
            }

            @Override
            public void onFailed(String errorMsg) {
                Toast.makeText(GameActivity.this, "服务器连接失败"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

         btnSend.setOnClickListener(v -> {
             message = etInput.getText().toString();

             MsgBean messageBean = new MsgBean(0,0);

             socketUtil.sendMessage(new EasyMessage(userId,friendId,messageBean), new SocketUtil.Callback() {
                 @Override
                 public void onSuccess() {
                     Toast.makeText(GameActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                     tvMessage.append(message +"\n");
                 }

                 @Override
                 public void onFailed(String errorMsg) {
                     Toast.makeText(GameActivity.this, "发送失败，对方可能已经离线", Toast.LENGTH_SHORT).show();
                 }
             });
         });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketUtil.sendMessage(new EasyMessage("DISCONNECT",userId,friendId,null), new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                if(socketUtil!=null)
                    socketUtil.closeSocket();
            }

            @Override
            public void onFailed(String errorMsg) {
                if(socketUtil!=null)
                    socketUtil.closeSocket();
            }
        });

    }

    @Override
    public void handleMessage(EasyMessage easyMessage) {
        if ("connected".equalsIgnoreCase(easyMessage.getType())){
            tvFriend.setText(easyMessage.getFromKey().toString());
            friendId = Integer.valueOf(easyMessage.getFromKey().toString());
            Toast.makeText(this, "朋友"+tvFriend+"已经加入了房间", Toast.LENGTH_SHORT).show();
        }else{
            if(easyMessage.getType().equalsIgnoreCase("DISCONNECT")){
                //断开连接了
                Toast.makeText(this, "对方已经退出了房间", Toast.LENGTH_SHORT).show();
                tvFriend.setText("好友已退出");
            }else {
                tvMessage.append(easyMessage.getFromKey() + "对你说:" + ((MsgBean)easyMessage.getMessage()).getMessage() + "\n");
//                tvMessage.append(msgBean.getFrom() + "对你说:" + msgBean.getMessage() + "\n");
            }
        }
    }
}
