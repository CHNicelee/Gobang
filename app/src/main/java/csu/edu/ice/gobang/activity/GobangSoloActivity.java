package csu.edu.ice.gobang.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import csu.edu.ice.gobang.R;
import csu.edu.ice.gobang.widget.ChessBoard;

public class GobangSoloActivity extends BaseActivity {
    private static final String TAG = "GobangSoloActivity";
    private int chessColor=1;
    private ChessBoard chessBoard;
    private ImageView ivMyChess;

    int[] result = new int[3];//落子的x y坐标和color


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gobangsolo);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        chessBoard.setCanLuoZi(true);
        chessBoard.setNeedShowSequence(false);
        Button btnsolo = (Button)findViewById(R.id.btnsoloConfirm);
        Button btnsoloHuiqi = (Button)findViewById(R.id.btnsoloHuiqi);
        chessBoard.setOnChessDownListener((x, y) -> btnsolo.setEnabled(true));
        chessBoard.setOnWinListener(new ChessBoard.OnWinListener() {
            @Override
            public void onWin(int chessColor) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GobangSoloActivity.this);
                dialog.setTitle("胜负已分");
                if(chessColor==1){
                    dialog.setMessage("黑棋胜");
                }
                else{
                    dialog.setMessage("白棋胜");
                }
                dialog.setCancelable(false);
                dialog.setPositiveButton("重开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GobangSoloActivity.this.recreate();
                    }
                });
                dialog.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          GobangSoloActivity.this.finish();
                    }
                });
                dialog.show();

            }
        });
        btnsolo.setOnClickListener(e->{
            if(chessBoard.confirmPosition(result)){
                chessColor = 1-chessColor;
                chessBoard.setChessColor(chessColor);
            }
            else{
                showToast("请落子后再点击确定按钮");
            }
        });
        btnsoloHuiqi.setOnClickListener(e->{
            if(chessBoard.ismHasChess()){
                chessBoard.Huiqi();
                chessColor = 1-chessColor;
                showToast("悔棋成功");
            }
            else{
                showToast("棋盘上无棋子，不能进行悔棋");
            }
        });

    }
    private void initView() {
        chessBoard = findViewById(R.id.chessBoard);
        ivMyChess = findViewById(R.id.ivMyChess);
    }

}
