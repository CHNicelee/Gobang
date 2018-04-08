package csu.edu.ice.gobang;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import csu.edu.ice.gobang.widget.ChessBoard;

/**
 * Created by ice on 2018/4/3.
 */

public class GobangActivitiy extends AppCompatActivity implements SocketUtil.MessageHandler {
    private static final String TAG = "GobangActivity";
    private static String ip = "www.ice97.cn";
    private static int port = 8885;
    private SocketUtil socketUtil;
    private int userId;
    private Integer friendId;
    private int chessColor;
    private ChessBoard chessBoard;
    private TextView tvFriend,tvUserId;
    private TextView tvRoom,tvRoomTip;
    private ImageView ivFriendPic,ivMyChess,ivFriendChess;
    private TextView tvMyTime,tvFriendTime;

    private boolean isEnd = false;
    private AlertDialog alertDialog;
    private int totalTime = 3 * 10000;//倒计时
    private boolean isWin = false;

    private String[] lostImageUrls = {"http://img5.imgtn.bdimg.com/it/u=824535348,1848358469&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3506604075,4147732860&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=1815751955,4088443349&fm=214&gp=0.jpg",
            "http://easyread.ph.126.net/SJeUgZ_yqf9U8NbYcWWQAA==/7917045570429865351.jpg"};
    private String[] winImageUrls = {"http://news.cnhubei.com/xw/yl/201609/W020160923555134408141.jpg",
            "http://img.mp.sohu.com/upload/20170629/98c68c5902294b718d5ba0f572f6fe85_th.png",
    "http://img.mp.sohu.com/upload/20170629/5d5ac4d21e1d4aceafc98beb09a991c6_th.png",
    "http://img.mp.sohu.com/upload/20170629/7e233d0b54eb4b5eac0b58d085491449_th.png"};
    //定时器
    CountDownTimer countDownTimer = new CountDownTimer(totalTime,1000) {
        @Override
        public void onTick(long millisUntilFinished) {

            if(myTurn){
                if(millisUntilFinished/1000<=10){
                    tvMyTime.setTextColor(Color.RED);
                }
                tvMyTime.setText("00:"+millisUntilFinished/1000);
            }
            else{
                tvFriendTime.setText("00:"+millisUntilFinished/1000);
            }
        }

        @Override
        public void onFinish() {
            onTimeFinished();
        }

    };

    int[] result = new int[3];//落子的x y坐标和color

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gobang);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        Button btnConfirm = findViewById(R.id.btnConfirm);
        chessBoard.setOnChessDownListener((x, y) -> btnConfirm.setEnabled(true));

        btnConfirm.setOnClickListener(e->{
            chessBoard.confirmPosition(result);
            btnConfirm.setEnabled(false);
            chessBoard.setCanLuoZi(false);
            sendMoveMessage(result[0],result[1]);
            myTurn = false;
            resetTime(tvMyTime);
            countDownTimer.cancel();
            countDownTimer.start();
        });
        chessBoard.setOnWinListener(winColor -> {
            chessBoard.setCanLuoZi(false);
            Log.d(TAG, "onCreate: "+winColor+"  myColor:"+chessColor);
            if(winColor == chessColor){
                showResultDelay(true);//胜利
                isWin = true;
            }else{
                showResultDelay(false);
                isWin = false;
            }
            isEnd = true;
        });

        initSocket();

    }


    private void initView() {
        tvRoom = findViewById(R.id.tvRoom);
        tvRoomTip = findViewById(R.id.tvRoomTip);
        chessBoard = findViewById(R.id.chessBoard);
        tvFriend = findViewById(R.id.tvFriend);
        tvMyTime = findViewById(R.id.tvMyTime);
        tvFriendTime = findViewById(R.id.tvFriendTime);
        tvMyTime = findViewById(R.id.tvMyTime);
        ivMyChess = findViewById(R.id.ivMyChess);
        ivFriendChess = findViewById(R.id.ivFriendChess);
        ivFriendPic = findViewById(R.id.ivFriendPic);
        tvUserId = findViewById(R.id.tvUserId);
    }

    private void initSocket() {
        Integer room = getIntent().getIntExtra("roomNumber",-1);
        userId  = getIntent().getIntExtra("userId",-1);
        Assert.assertNotSame(room,-1);
        tvRoom.setText(room+"");
        tvUserId.setText(userId+"");

        socketUtil = new SocketUtil(this);
        socketUtil.setDataClass(MsgBean.class);
        MsgBean msgBean = new MsgBean();
        msgBean.setRoom(room);
        EasyMessage easyMessage = new EasyMessage(userId,null,msgBean);
        socketUtil.connect(ip, port, easyMessage, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(GobangActivitiy.this, "官人请邀请好友前来一决雌雄", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errorMsg) {
                Toast.makeText(GobangActivitiy.this, "不好意思，程序开了小差，请返回重试", Toast.LENGTH_SHORT).show();
            }
        });


       /*socketUtil.setOnDisconnectListener(() -> {
            if(isEnd)return;
            if(alertDialog == null) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle("网络故障")
                        .setMessage("您已经断开连接了，请检查网络后重试")
                        .setPositiveButton("重连", (dialog, which) -> {
                            socketUtil.connect(ip, port, new EasyMessage(EasyMessage.type_reconnect, userId, null, null), new SocketUtil.Callback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(GobangActivitiy.this, "重连成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailed(String errorMsg) {
                                    Toast.makeText(GobangActivitiy.this, "连接失败", Toast.LENGTH_SHORT).show();
                                    alertDialog.show();
                                }
                            });
                        })
                        .setNegativeButton("退出", (dialog, which) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .create();
            }
            alertDialog.show();
        });*/
    }

    @Override
    public void handleMessage(EasyMessage easyMessage) {
        Log.d(TAG, "handleMessage: "+easyMessage);
        MsgBean msg = (MsgBean) easyMessage.getMessage();

        if(MsgBean.type_connect.equalsIgnoreCase(msg.getMessage())){
            onFriendComeIn(easyMessage,msg);
        }else if(MsgBean.type_newChess.equalsIgnoreCase(msg.getMessage())){
            onFriendLuoZi(msg);
        }else if(MsgBean.type_disconnect.equalsIgnoreCase(msg.getMessage())){
            onFriendExit();
        }else if(MsgBean.type_timeout.equalsIgnoreCase(msg.getMessage())){
            isEnd =true;
            isWin = true;
            showResult(true);//对方超时了
        }
    }

    /**
     * 当好友退出了游戏
     */
    private void onFriendExit() {
        Toast.makeText(this, "您的对手已经退出游戏了！", Toast.LENGTH_SHORT).show();
        countDownTimer.cancel();
        tvFriend.setText("已退出");
        isWin = true;
        isEnd = true;
        showResult(true);
//        socketUtil.closeSocket();
    }

    /**
     * 好友落子了
     * @param msg
     */
    private void onFriendLuoZi(MsgBean msg) {
        chessBoard.setCanLuoZi(true);
        chessBoard.addChess(msg.getX(),msg.getY(),msg.getColor());
        if(!isEnd)
            Toast.makeText(this, "轮到客官您了", Toast.LENGTH_SHORT).show();
        countDownTimer.cancel();
        myTurn = true;
        resetTime(tvFriendTime);
        countDownTimer.start();
    }

    /**
     * 好友加入了聊天室
     * @param easyMessage
     * @param msg
     */
    public void onFriendComeIn(EasyMessage easyMessage,MsgBean msg){
        tvRoomTip.setVisibility(View.GONE);
        tvRoom.setVisibility(View.GONE);
        friendId = easyMessage.getFromKey();
        tvFriend.setText(friendId+"");
        chessColor = msg.getColor();
        chessBoard.setChessColor(chessColor);
        Boolean moveFirst = msg.getMoveFirst();
        String tip = "您的好友已经加入了房间，可以开始对战了";
        if(moveFirst)tip+="您是先手";
        else tip+="您是后手";
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
        if(moveFirst){
            chessBoard.setCanLuoZi(true);
        }

        ivFriendPic.setImageResource(R.drawable.btn_player);
        if(chessColor == ChessBoard.COLOR_WHITE) {
            ivMyChess.setImageResource(R.drawable.white_chess_2);
            ivFriendChess.setImageResource(R.drawable.black_chess_2);
        }else{
            ivMyChess.setImageResource(R.drawable.black_chess_2);
            ivFriendChess.setImageResource(R.drawable.white_chess_2);
        }
        tvMyTime.setVisibility(View.VISIBLE);
        ivMyChess.setVisibility(View.VISIBLE);
        tvFriendTime.setVisibility(View.VISIBLE);
        ivFriendChess.setVisibility(View.VISIBLE);
        tvFriend.setVisibility(View.VISIBLE);
    }

    public void sendMoveMessage(int x,int y){
        MsgBean msgBean = new MsgBean(x,y);
        msgBean.setColor(chessColor);
        msgBean.setMessage(MsgBean.type_newChess);
        EasyMessage easyMsg = new EasyMessage(userId, friendId, msgBean);
        socketUtil.sendMessage(easyMsg, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                myTurn = false;
            }

            @Override
            public void onFailed(String errorMsg) {
                Toast.makeText(GobangActivitiy.this, "对不起，无法连接到对方，请重试", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void resetTime(TextView tvMyTime){
        tvMyTime.setText("00:30");
        tvMyTime.setTextColor(Color.BLACK);
    }
    /**
     * 超时了
     */
    private void onTimeFinished() {
        if(myTurn){
            MsgBean msgBean = new MsgBean();
            msgBean.setMessage("timeout");
            socketUtil.sendMessage(new EasyMessage(EasyMessage.type_user_message, userId, friendId, msgBean), null);
            isWin = false;
            isEnd = true;
            showResult(false);
        }
    }

    /**
     * 弹窗展示结果
     * @param isWin
     */
    public void showResult(boolean isWin){
        ResultDialog resultDialog = new ResultDialog();
        if(isWin){
            resultDialog.setCancelable(true);
        }else{
            resultDialog.setCancelable(false);
        }
        String url;
        if(isWin) url = winImageUrls[(int) (System.currentTimeMillis()%winImageUrls.length)];
        else url = lostImageUrls[(int) (System.currentTimeMillis()%winImageUrls.length)];
        resultDialog.setArguments(url, isWin);
        resultDialog.show(getSupportFragmentManager(), "resultDialog");
    }

    public void showResultDelay(boolean isWin){
        this.isWin = isWin;
        if(isEnd) return;
        isEnd = true;
        tvUserId.postDelayed(() -> showResult(isWin), 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(isWin || friendId==null){
            return super.onKeyDown(keyCode, event);
        }
        if(keyCode==KeyEvent.KEYCODE_BACK && !isEnd){

            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("游戏还没结束，现在退出会受到处罚，是否确认退出？")
                    .setNegativeButton("继续游戏",null)
                    .setPositiveButton("确认退出", (dialog, which) -> {
                        sendDisconnectMessage();
                        isEnd = true;
                        isWin = false;
                        showResult(false);
                    })
                    .create()
                    .show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    boolean myTurn = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isEnd = true;
        sendDisconnectMessage();
    }

    public void sendDisconnectMessage(){
        MsgBean msgBean = new MsgBean();
        msgBean.setMessage(MsgBean.type_disconnect);
        socketUtil.sendMessage(new EasyMessage(userId, friendId, msgBean), new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                socketUtil.closeSocket();
            }

            @Override
            public void onFailed(String errorMsg) {
                socketUtil.closeSocket();
            }
        });
    }

}
