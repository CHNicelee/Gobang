package csu.edu.ice.gobang;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import csu.edu.ice.gobang.widget.ChessBoard;

public class GobangsoloActivity extends AppCompatActivity {
    private static final String TAG = "GobangsoloActivity";
    private int chessColor=1;
    private ChessBoard chessBoard;
    private ImageView ivMyChess;

    private boolean isEnd = false;
    private AlertDialog alertDialog;

    int[] result = new int[3];//落子的x y坐标和color


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gobangsolo);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        chessBoard.setCanLuoZi(true);

        Button btnsolo = (Button)findViewById(R.id.btnsoloConfirm);
        chessBoard.setOnChessDownListener((x, y) -> btnsolo.setEnabled(true));
        chessBoard.setOnWinListener(new ChessBoard.OnWinListener() {
            @Override
            public void onWin(int chessColor) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GobangsoloActivity.this);
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
                        GobangsoloActivity.this.recreate();
                    }
                });
                dialog.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          GobangsoloActivity.this.finish();
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
                Toast.makeText(GobangsoloActivity.this,"请落子后再点击确定按钮",Toast.LENGTH_SHORT).show();
            }




        });

    }
    private void initView() {
        chessBoard = findViewById(R.id.chessBoard);
        ivMyChess = findViewById(R.id.ivMyChess);
    }

}
