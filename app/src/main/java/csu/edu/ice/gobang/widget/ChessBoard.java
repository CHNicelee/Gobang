package csu.edu.ice.gobang.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import csu.edu.ice.gobang.R;

/**
 * Created by ice on 2018/4/three.
 */

public class ChessBoard extends View {
    public static final int COLOR_WHITE = 0;//白色棋子
    public static final int COLOR_BLACK = 1;//黑色棋子
    private static final String TAG = "ChessBoard";
    private int mLineCounts = 15;//网格线的数量
    private float padding =  35;//网格的内边距

    char mChessBoard[][] = new char[15][15];//棋盘矩阵  B代表黑色  W代表白色
    private int mChessColor = COLOR_BLACK;//自己的棋子的颜色



    private OnChessDownListener mOnChessDownListener;
    private OnWinListener mOnWinListener;
    private int mChessRadius;//棋子半径

    private int mNewPosX,mNewPosY;//新棋子的坐标  用于在该棋子上画点标注
    private int mWidth; //view的宽度
    private boolean canLuoZi = false;//能否落子

    public ChessBoard(Context context) {
        this(context,null);
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.RED);
        mCirclePaint.setStrokeWidth(10);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
/*
        mChessBoard[5][5] = 'B';
        mChessBoard[4][5] = 'W';
        mChessBoard[4][three] = 'B';
        mChessBoard[5][three] = 'W';
        mChessBoard[4][two] = 'B';*/

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = 0,height = 0;
        if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST){
            width = MeasureSpec.getSize(widthMeasureSpec);
        }else if(heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST){
            height = MeasureSpec.getSize(heightMeasureSpec);
        }

        int min = Math.min(width,height);
        if(min !=0 ) mWidth = min;
        else if(width!=0) mWidth = width;
        else if(height!=0) mWidth = height;
        else throw new IllegalStateException("Width and Height can't be zero!");
        setMeasuredDimension(mWidth, mWidth);//棋盘为正方形
    }

    Paint mLinePaint;//划线
    Paint mCirclePaint;//画移动棋子外面的环
    boolean mHasChess = false;//棋盘上面是否有棋

    @Override
    protected void onDraw(Canvas canvas) {

        //划线
        for (int i = 0; i < mLineCounts; i++) {
            float pos = (float) (padding+(i*(mWidth - 2*padding)*1.0/(mLineCounts-1)));//计算x和y的坐标
            canvas.drawLine(padding,pos,mWidth-padding,pos, mLinePaint);
            canvas.drawLine(pos,padding,pos,mWidth-padding, mLinePaint);
        }

        //画四个点

        float startX = padding;
        float startY = padding;
        float gap = (float) ((mWidth - 2*padding)*1.0/(mLineCounts-1));
        int pointRadius = 12;
        //左上
        canvas.drawCircle(startX+3*gap,startY+3*gap,pointRadius,mLinePaint);
        //右上
        canvas.drawCircle(startX+11*gap,startY+3*gap,pointRadius,mLinePaint);
        //左下
        canvas.drawCircle(startX+3*gap,startY+11*gap,pointRadius,mLinePaint);
        //右下
        canvas.drawCircle(startX+11*gap,startY+11*gap,pointRadius,mLinePaint);
        //中间
        canvas.drawCircle(startX+7*gap,startY+7*gap,pointRadius+3,mLinePaint);

        //有一方胜利了    标识出五颗棋子
        if(isWin){
            for (int i = 0; i < winPoints.length; i++) {
                int x = winPoints[i][0];
                int y = winPoints[i][1];
                int centerX = (int) (startX+(x*gap));
                int centerY = (int) (startY+(y*gap));
                canvas.drawCircle(centerX,centerY,mChessRadius+10, mCirclePaint);
            }
        }

        mChessRadius = (int) (gap*0.45);
        Bitmap blackChessBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black_chess_2);
        Bitmap whiteChessBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white_chess_2);
        //得到缩放之后的bitmap
        blackChessBitmap = scaleBitmap(blackChessBitmap,mChessRadius*2,mChessRadius*2);
        whiteChessBitmap = scaleBitmap(whiteChessBitmap,mChessRadius*2,mChessRadius*2);
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {

                //画黑棋
                if(getChessColor(col,row)==COLOR_BLACK){
                    int centerX = (int) (startX+(col*gap));
                    int centerY = (int) (startY+(row*gap));
                    canvas.drawBitmap(blackChessBitmap,centerX-mChessRadius,centerY-mChessRadius, mLinePaint);
                }else if(getChessColor(col,row) == COLOR_WHITE){
                    int centerX = (int) (startX+(col*gap));
                    int centerY = (int) (startY+(row*gap));
                    canvas.drawBitmap(whiteChessBitmap,centerX-mChessRadius,centerY-mChessRadius, mLinePaint);
                }else if(isMoving()){
                    int centerX = (int) (startX+(mMovingX*gap));
                    int centerY = (int) (startY+(mMovingY*gap));
                    canvas.drawCircle(centerX,centerY,mChessRadius+10, mCirclePaint);
                    if(mChessColor == COLOR_WHITE)
                        canvas.drawBitmap(whiteChessBitmap,centerX-mChessRadius,centerY-mChessRadius, mLinePaint);
                    else
                        canvas.drawBitmap(blackChessBitmap,centerX-mChessRadius,centerY-mChessRadius, mLinePaint);
                }
            }
        }

        //没有正在移动  那么绘制最新棋子上面的圆点
        if(!isMoving() && mHasChess) {
            int centerX = (int) (startX + (mNewPosX * gap));
            int centerY = (int) (startY + (mNewPosY * gap));
            canvas.drawCircle(centerX, centerY, 10, mCirclePaint);
        }


    }

    /**
     * 是否正在移动
     * @return
     */
    private boolean isMoving() {
        if(mMovingX==-1&&mMovingY==-1) return false;
        return true;
    }


    private Bitmap scaleBitmap(Bitmap bm,int newWidth,int newHeight){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = (float) ((newWidth*1.0) / width);
        float scaleHeight = (float) ((newHeight*1.0) / height);
        Log.d(TAG, "scaleBitmap: 缩放比例："+scaleHeight);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
    }


    int mMovingX = -1, mMovingY = -1;
    int mDownPosX, mDownPosY;
    float mDownX,mDownY;
    boolean moving = false;

    int maxUpTime = 200;//200ms
    long downTime;
    boolean isNewChess = true;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!canLuoZi)return false;
        float gap = (float) ((mWidth - 2*padding)*1.0/(mLineCounts-1));

        float x =  event.getX();
        float y = event.getY();

        int tempPosX;
        int tempPosY;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                    downTime = System.currentTimeMillis();
                    //当前点击不是落子  是准备滑动
                    mDownX = x;
                    mDownY = y;
                    mDownPosX  = mMovingX;
                    mDownPosY = mMovingY;
                    return true;

            case MotionEvent.ACTION_MOVE:
                if(!isMoving())return false;
                 tempPosX =  Math.round((x-mDownX)/gap);
                 tempPosY =  Math.round((y-mDownY)/gap);

                 if(mDownPosX + tempPosX<0){
                     tempPosX = 0;
                     mDownX = x;
                     mDownPosX = 0;
                 }
                 if(mDownPosY + tempPosY<0){
                     tempPosY = 0;
                     mDownY = y;
                     mDownPosY = 0;
                 }
                 //同一个位置
                 if(mDownPosX + tempPosX == mMovingX && mDownPosY + tempPosY== mMovingY)return true;
                 //位置不合法
                 if(!isValidPosition(mDownPosX + tempPosX,mDownPosY + tempPosY))return true;
                mMovingX = mDownPosX + tempPosX;
                mMovingY = mDownPosY + tempPosY;
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if(System.currentTimeMillis() - downTime >maxUpTime ){
                    return true;
                }

                if(Math.abs(x-mDownX)<mChessRadius && Math.abs(y-mDownY)<mChessRadius){
                    //是点击事件
                     tempPosX =  Math.round((x-padding)/gap);
                     tempPosY =  Math.round((y-padding)/gap);

                    //无法落子 此次点击作废
                    if(!isValidPosition(tempPosX,tempPosY))return false;
                    mMovingX = tempPosX;
                    mMovingY = tempPosY;
                    mDownPosX = tempPosX;
                    mDownPosY = tempPosY;
                    moving = true;
                    if(isNewChess && mOnChessDownListener!=null)mOnChessDownListener.onDown(mMovingX,mMovingY);
                    isNewChess = false;
                    invalidate();
                }

                break;
        }

        return super.onTouchEvent(event);
    }


    /**
     * 查看新棋子是否能下到这个位置
     * @param x
     * @param y
     * @return
     */
    public boolean isValidPosition(int x,int y){
        //超出了界限
        if(x<0||y<0||x>=mLineCounts||y>=mLineCounts)return false;
        //已经有棋子了
        if(mChessBoard[y][x]=='W'||mChessBoard[y][x]=='B')return false;
        return true;
    }



    /**
     * 移动之后 确定落子
     * @param a  用于带出参数
     */
    public void confirmPosition(@Nullable int[] a){
        if(a!=null && a.length>2){
            a[0] = mMovingX;
            a[1] = mMovingY;
            a[2] = mChessColor;
        }

        mHasChess = true;
        mChessBoard[mMovingY][mMovingX] = (mChessColor==COLOR_BLACK? 'B':'W');
        mNewPosX = mMovingX;
        mNewPosY = mMovingY;
        if(mOnWinListener!=null && (isWin = isWin(mMovingX,mMovingY,mChessColor))){
            mOnWinListener.onWin(mChessColor);
        }
        mMovingY = mMovingX = -1;
        mDownX = mDownY = -1;
        moving = false;
        isNewChess = true;
        invalidate();

    }

    /**
     * 添加棋子
     * @param x
     * @param y
     * @param color
     * @return
     */
    public boolean addChess(int x,int y,int color){
        if(!isValidPosition(x,y))return false;
        if(color == COLOR_WHITE){
            mChessBoard[y][x] = 'W';
        }else {
            mChessBoard[y][x] = 'B';
        }
        mNewPosX = x;
        mNewPosY = y;
        mHasChess = true;
        moving = false;
        if(mOnWinListener!=null && (isWin = isWin(x,y,color))){
            mOnWinListener.onWin(color);
        }
        invalidate();
        return true;
    }

    public void setOnWinListener(OnWinListener onWinListener){
        this.mOnWinListener = onWinListener;
    }
    public void setOnChessDownListener(OnChessDownListener onChessDownListener){
        this.mOnChessDownListener = onChessDownListener;
    }

    int winPoints[][] = new int[5][2];

    /**
     * 判断是否赢了
     * @param x
     * @param y
     * @param chessColor  棋子颜色
     * @return
     */
    public boolean isWin(int x,int y,int chessColor){
        //是否赢了

        int startY = y -4 <0?0:y-4;
        int endY =  y+4>=mLineCounts?mLineCounts-1:y+4;
        int startX = x-4<0?0:x-4;
        int endX = x+4>=mLineCounts?mLineCounts-1:x+4;

        //横着检查
        int count = 0;
            for(int col = startX;col<=endX;col++){
                if(chessColor == getChessColor(col,y)){
                    winPoints[count][0] = col;
                    winPoints[count][1] = y;
                    count++;
                    if(count==5){
                        return true;
                    }
                }else{
                    count = 0;
                }
            }

        //竖着检查
        count = 0;
        Log.d(TAG, "isWin: startY"+startY+" endY"+endY);
        for(int row = startY;row<=endY;row++){
                if(chessColor == getChessColor(x,row)){
                    winPoints[count][0] = x;
                    winPoints[count][1] = row;
                    count++;
                    if(count==5)return true;
                }else{
                    count=0;
                }
            }

        //右下斜检查
        count =0;
        int col = x-4;
        int row = y-4;
        while(col<=x+4 && row<=y+4){
            if(col<0||col>=mLineCounts ||row<0||row>=mLineCounts){
                col++;
                row++;
                continue;
            }
            if(chessColor == getChessColor(col,row)){
                winPoints[count][0] = col;
                winPoints[count][1] = row;
                count++;
                if(count==5)return true;
            }else{
                count=0;
            }
            col++;
            row++;
        }

        //左下斜检查
        count =0;
         col = x+4;
         row = y-4;
        while(col>=x-4 && row<=y+4){
            if(col<0||col>=mLineCounts ||row<0||row>=mLineCounts){
                col--;
                row++;
                continue;
            }
            if(chessColor == getChessColor(col,row)){
                winPoints[count][0] = col;
                winPoints[count][1] = row;
                count++;
                if(count==5)return true;
            }else{
                count=0;
            }
            col--;
            row++;
        }

        return false;
    }
    public boolean isWin = false;
    public int getChessColor(int x,int y){
        if(mChessBoard[y][x] == 'B')return COLOR_BLACK;
        else if(mChessBoard[y][x] == 'W')return COLOR_WHITE;
        return -1;
    }

    public void setCanLuoZi(boolean canLuoZi) {
        this.canLuoZi = canLuoZi;
    }

    public void setChessColor(int chessColor) {
        this.mChessColor = chessColor;
    }


    //------------interface--------------
    public interface OnChessDownListener{
        void onDown(int x,int y);
    }
    public interface OnWinListener{
        void onWin(int chessColor);
    }

    class Point{
        public int x;
        public int y;
        public int color;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
