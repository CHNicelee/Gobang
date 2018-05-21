package csu.edu.ice.gobang.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.zhouwei.library.CustomPopWindow;
import com.squareup.picasso.Picasso;

import junit.framework.Assert;

import java.util.Arrays;

import csu.edu.ice.gobang.EasyMessage;
import csu.edu.ice.gobang.MsgBean;
import csu.edu.ice.gobang.R;
import csu.edu.ice.gobang.ResultDialog;
import csu.edu.ice.gobang.SocketUtil;
import csu.edu.ice.gobang.adapter.MessageAdapter;
import csu.edu.ice.gobang.widget.ChessBoard;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ice on 2018/4/three.
 */

public class GobangActivitiy extends BaseActivity implements SocketUtil.MessageHandler {
    private static final String TAG = "GobangActivity";
    private static final long MESSAGE_TIME = 3000;
    private static String ip = "www.ice97.cn";
//    private static String ip = "192.168.191.1";
//    private static String ip = "192.168.31.47";

    private static int port = 8885;
    private SocketUtil socketUtil;
    private int userId;
    private Integer friendId;
    private int chessColor;
    private ChessBoard chessBoard;
    private TextView tvFriend,tvUserId;
    private TextView tvRoom,tvRoomTip,tvMyMessage,tvFriendMessage;
    private ImageView ivFriendPic,ivMyChess,ivFriendChess,ivTop,ivBottom,ivMessage;
    private TextView tvMyTime,tvFriendTime,tvWhoLuozi;

    private boolean isEnd = false;
    private AlertDialog alertDialog;
    private int totalTime = 30 * 1000;//倒计时
    private boolean isWin = false;

    private Animation myTurnAnim;

    //private String[] lostImageUrls = {"http://img5.imgtn.bdimg.com/it/u=824535348,1848358469&fm=27&gp=0.jpg",
     //       "http://img5.imgtn.bdimg.com/it/u=3506604075,4147732860&fm=27&gp=0.jpg",
    //        "http://img0.imgtn.bdimg.com/it/u=1815751955,4088443349&fm=214&gp=0.jpg",
    //        "http://easyread.ph.126.net/SJeUgZ_yqf9U8NbYcWWQAA==/7917045570429865351.jpg"};
    private String[] winImageUrls = {"http://news.cnhubei.com/xw/yl/201609/W020160923555134408141.jpg",
            "http://img.mp.sohu.com/upload/20170629/98c68c5902294b718d5ba0f572f6fe85_th.png",
    "http://img.mp.sohu.com/upload/20170629/5d5ac4d21e1d4aceafc98beb09a991c6_th.png",
    "http://img.mp.sohu.com/upload/20170629/7e233d0b54eb4b5eac0b58d085491449_th.png"};
    private String[] lostImageUrls = winImageUrls;
    //定时器
    CountDownTimer countDownTimer = new CountDownTimer(totalTime,1000) {
        @Override
        public void onTick(long millisUntilFinished) {

            if(myTurn){
                if(millisUntilFinished/1000<=10){
                    tvMyTime.setTextColor(Color.RED);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1,3,1,3, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                    scaleAnimation.setDuration(1000);
                    scaleAnimation.setFillAfter(false);
                    tvMyTime.startAnimation(scaleAnimation);
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
    private CustomPopWindow mListPopWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gobang);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        Button btnConfirm = findViewById(R.id.btnConfirm);
        chessBoard.setNeedShowNewestCircle(true);//显示最新的棋子上面的圆点
        chessBoard.setNeedShowSequence(false);//不显示序号
        chessBoard.setOnChessDownListener((x, y) -> btnConfirm.setEnabled(true));

        btnConfirm.setOnClickListener(e->{
            resetTime(tvMyTime);
            countDownTimer.cancel();
            countDownTimer.start();
            chessBoard.confirmPosition(result);
            btnConfirm.setEnabled(false);
            chessBoard.setCanLuoZi(false);
            sendMoveMessage(result[0],result[1]);
            myTurn = false;
            tvWhoLuozi.clearAnimation();
            tvWhoLuozi.setText("对方落子");
        });
        chessBoard.setOnWinListener(winColor -> {
            countDownTimer.cancel();
            chessBoard.setNeedShowNewestCircle(false);
            chessBoard.setNeedShowSequence(true);
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
            chessBoard.invalidate();
        });

        ivMessage.setOnClickListener(v -> showMessageView());
        initSocket();

    }


    private void initView() {
        ivMessage = findViewById(R.id.ivMessage);
        tvMyMessage = findViewById(R.id.tvMyMessage);
        tvFriendMessage = findViewById(R.id.tvFriendMessage);
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
        ivTop = findViewById(R.id.ivTop);
        ivBottom = findViewById(R.id.ivBottom);
        tvWhoLuozi = findViewById(R.id.tvWhoLuoZi);
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
        EasyMessage easyMessage = new EasyMessage(EasyMessage.type_connect,userId,null,msgBean);
        socketUtil.connect(ip, port, easyMessage, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                showToast("官人请邀请好友前来一决雌雄");
            }

            @Override
            public void onFailed(String errorMsg) {
                showToast("不好意思，程序开了小差，请返回重试");
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
        if(msg==null || msg.getType()==null)return;
        switch (msg.getType()){
            case MsgBean.type_connect:
                onFriendComeIn(easyMessage,msg);
                break;
            case MsgBean.type_disconnect:
                onFriendExit();
                break;
            case MsgBean.type_friendMessage:
                showFromFriendMessage(msg.getMessage());
                break;
            case MsgBean.type_newChess:
                onFriendLuoZi(msg);
                break;
            case MsgBean.type_timeout:
                isEnd =true;
                isWin = true;
                showResult(true);//对方超时了
                break;
        }

    }

    /**
     * 显示好友发来的消息
     * @param message
     */
    private void showFromFriendMessage(String message) {
        tvMyMessage.setVisibility(View.GONE);
        tvFriendMessage.setText(message);
        tvFriendMessage.setVisibility(View.VISIBLE);
        tvFriendMessage.postDelayed(()-> tvFriendMessage.setVisibility(View.GONE),MESSAGE_TIME);
    }

    /**
     * 当好友退出了游戏
     */
    private void onFriendExit() {
        if(isEnd)return;
        showToast("您的对手已经退出游戏了！");
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
        countDownTimer.cancel();
        resetTime(tvFriendTime);
        countDownTimer.start();
        chessBoard.addChess(msg.getX(),msg.getY(),msg.getColor());
        if(!isEnd) {
//            showToast("轮到客官您了");
            onMyTurn();
        }
    }



    private void onMyTurn() {
        if (myTurnAnim == null) {
            myTurnAnim = new ScaleAnimation(1f,1.2f,1f,1.2f,ScaleAnimation.RELATIVE_TO_SELF,0.5f,ScaleAnimation.RELATIVE_TO_SELF,0.5f);
            myTurnAnim.setRepeatCount(-1);
            myTurnAnim.setRepeatMode(Animation.REVERSE);
            myTurnAnim.setDuration(1000);
        }
        tvWhoLuozi.setText("我方落子");
        tvWhoLuozi.startAnimation(myTurnAnim);
        myTurn = true;

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
//        String tip = "您的好友已经加入了房间，可以开始对战了";
//        if(moveFirst)tip+="您是先手";
//        else tip+="您是后手";
        myTurn = moveFirst;
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

        ivTop.setVisibility(View.VISIBLE);
        ivBottom.setVisibility(View.VISIBLE);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1.5f,0,1.5f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(false);
        int res[] = new int[]{R.mipmap.three,R.mipmap.two,R.mipmap.one,R.mipmap.begin};

        Observable.create((ObservableEmitter<Integer> e) -> {
            for (int i = 0; i <= 3; i++) {
                e.onNext(i);
                Thread.sleep(1000);
            }
            Thread.sleep(500);
            e.onNext(4);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer index) -> {
                    ivTop.clearAnimation();
                    if(index == 4) {
                        //开始比赛
                        //开始比赛
                        tvWhoLuozi.setVisibility(View.VISIBLE);
                        if(moveFirst){
                            chessBoard.setCanLuoZi(true);
                            onMyTurn();
                        }else{
                            tvWhoLuozi.setText("对方落子");
                        }
                        ivBottom.setVisibility(View.GONE);
                        ivTop.setVisibility(View.GONE);
                        countDownTimer.start();
                        return;
                    }
                    Picasso.with(GobangActivitiy.this).load(res[index]).into(ivTop);
                    if(index == 3){
                        //显示我方先手 或 后手
                        if(moveFirst){
                            Picasso.with(GobangActivitiy.this).load(R.mipmap.first_move).into(ivBottom);
                        }else{
                            Picasso.with(GobangActivitiy.this).load(R.mipmap.second_move).into(ivBottom);
                        }
                    }else{
                        ivTop.startAnimation(scaleAnimation);
                    }
                });

    }

    public void sendMoveMessage(int x,int y){
        MsgBean msgBean = new MsgBean(x,y);
        msgBean.setColor(chessColor);
        msgBean.setType(MsgBean.type_newChess);
        EasyMessage easyMsg = new EasyMessage(userId, friendId, msgBean);
        socketUtil.sendMessage(easyMsg, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                myTurn = false;
            }

            @Override
            public void onFailed(String errorMsg) {
                showToast("对不起，无法连接到对方，请重试");
            }
        });

    }

    //重置倒计时
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
            msgBean.setType(MsgBean.type_timeout);
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
        if(isShowed)return;
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
        isShowed = true;
    }

    /**
     * 显示消息列表
     */
    public void showMessageView(){
        if(mListPopWindow!=null){
            mListPopWindow.showAsDropDown(ivMessage,0,-550);
            return;
        }
        View contentView = LayoutInflater.from(this).inflate(R.layout.popview_message,null);
        //处理popWindow 显示内容
        handleListView(contentView);
        //创建并显示popWindow
        mListPopWindow= new CustomPopWindow.PopupWindowBuilder(this)
                .setView(contentView)
                .size(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)//显示大小
                .create();
        PopupWindow popWindow = mListPopWindow.getPopupWindow();
        Log.d(TAG, "showMessageView: height:"+popWindow.getHeight());
        mListPopWindow.showAsDropDown(ivMessage,0,-550);
//        popWindow.showAtLocation(chessBoard.getRootView(), Gravity.NO_GRAVITY,0,chessBoard.getTop());
    }

    private void handleListView(View contentView){
        RecyclerView recyclerView = contentView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        String messages[] = {"一着不慎，满盘皆输！","少侠，能快点吗？？？","和阁下过招真是快哉！"};
        MessageAdapter adapter = new MessageAdapter(R.layout.item_message, Arrays.asList(messages));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener((adapter1, view, position) -> {
            mListPopWindow.dissmiss();
            MsgBean msgBean = new MsgBean();
            msgBean.setType(MsgBean.type_friendMessage);
            msgBean.setMessage(messages[position]);
            socketUtil.sendMessage(new EasyMessage(userId, friendId, msgBean),null);
            showSendToFriendMessage(msgBean.getMessage());
        });
    }

    /**
     * 显示我发给好友的消息
     * @param message
     */
    private void showSendToFriendMessage(String message) {
        tvFriendMessage.setVisibility(View.GONE);
        tvMyMessage.setText(message);
        tvMyMessage.setVisibility(View.VISIBLE);
        tvMyMessage.postDelayed(()-> tvMyMessage.setVisibility(View.GONE),MESSAGE_TIME);
    }

    //是否已经弹窗了
    private boolean isShowed = false;

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
        msgBean.setType(MsgBean.type_disconnect);
        socketUtil.sendMessage(new EasyMessage(userId, friendId, msgBean),null);
        socketUtil.closeSocket();
    }


}

