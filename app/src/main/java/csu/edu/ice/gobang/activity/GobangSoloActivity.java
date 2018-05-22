package csu.edu.ice.gobang.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import csu.edu.ice.gobang.R;
import csu.edu.ice.gobang.algorithm.AiStrategy;
import csu.edu.ice.gobang.algorithm.MoveStrategy;
import csu.edu.ice.gobang.widget.ChessBoard;

public class GobangSoloActivity extends BaseActivity {
    private static final String TAG = "GobangSoloActivity";
    private int chessColor=1;
    private ChessBoard chessBoard;
    private ImageView ivMyChess;
    private boolean robotPlay = false;
    private boolean robotTurn = false;
    int[] result = new int[3];//落子的x y坐标和color
    private MoveStrategy strategy;
    private boolean isWin;
    private Button btnsolo;
    private Button btnsoloHuiqi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gobangsolo);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        //根据传过来的参数判断是否是自己对战还是和机器人对战
        robotPlay = getIntent().getBooleanExtra("robot",false);
        if(robotPlay){
            strategy = new AiStrategy();
            strategy.init();
        }

        chessBoard.setCanLuoZi(true);
        chessBoard.setNeedShowSequence(false);
        chessBoard.setOnChessDownListener((x, y) -> btnsolo.setEnabled(true));

        //有一方赢了
        chessBoard.setOnWinListener(chessColor -> onWin());
        //下棋
        btnsolo.setOnClickListener(e-> chessDown());
        //悔棋
        btnsoloHuiqi.setOnClickListener(e-> huiqi());

    }

    private void onWin() {
        isWin = true;
        chessBoard.setNeedShowSequence(true);
        chessBoard.setCanLuoZi(false);
        showWinDialog();
    }

    //玩家点击了下棋
    private void chessDown() {
        if(chessBoard.confirmPosition(result)){
            chessColor = 1-chessColor;
            chessBoard.setChessColor(chessColor);
            if(robotPlay && !isWin){

                //机器人对战  玩家下了后机器人下
                strategy.playerDown(result[0],result[1]);
                strategy.getNextPosition(result);
                chessBoard.addChess(result[0],result[1],chessColor);
                chessColor = 1- chessColor;
                chessBoard.setChessColor(chessColor);
            }
        } else{
            showToast("请落子后再点击确定按钮");
        }
    }

    //悔棋操作
    private void huiqi() {
        if(robotPlay){
            showToast("人机对战，无法悔棋！敬请期待新版本");
            return;
        }
        if(chessBoard.ismHasChess()){
            chessBoard.Huiqi();
            chessColor = 1-chessColor;
            showToast("悔棋成功");
        }
        else{
            showToast("棋盘上无棋子，不能进行悔棋");
        }
    }

    //显示结束之后的对话框
    private void showWinDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GobangSoloActivity.this);
        dialog.setTitle("胜负已分");
        if(chessColor==1){
            dialog.setMessage("黑棋胜");
        }
        else{
            dialog.setMessage("白棋胜");
        }
        dialog.setCancelable(false);
        dialog.setPositiveButton("查看棋局",null);
        dialog.setNegativeButton("退出", (dialogInterface, i) -> GobangSoloActivity.this.finish());
        dialog.show();

    }

    private void initView() {
        chessBoard = findViewById(R.id.chessBoard);
        ivMyChess = findViewById(R.id.ivMyChess);
        btnsolo = findViewById(R.id.btnsoloConfirm);
        btnsoloHuiqi = findViewById(R.id.btnsoloHuiqi);
    }

}
