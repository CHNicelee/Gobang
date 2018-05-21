package csu.edu.ice.gobang.algorithm;

import android.util.Log;

/**
 * Created by ice on 2018/5/21.
 * 策略来自：http://www.iteye.com/topic/765944
 */

public class SimpleStrategy implements MoveStrategy {

    private static final String TAG = "SimpleStrategy";
    private int i, j, k, m, n, icount;
    private int[][] board = new int [16][16];
    private boolean[][][] ptable = new boolean[16][16][672];
    private boolean[][][] ctable = new boolean[16][16][672];
    private int[][] cgrades = new int[16][16];
    private int[][] pgrades = new int[16][16];
    private int cgrade,pgrade;
    private int[][] win = new int[2][672];
    private int oldx,oldy;
    private int bout=1;
    private int pcount,ccount;
    private boolean player,computer,over,pwin,cwin,tie,start;
    private int mat,nat,mde,nde;

    @Override
    public void init() {
//初始化棋盘
        for(i=0;i<15;i++)
            for(j=0;j<15;j++)
            {
                this.pgrades[i][j] = 0;
                this.cgrades[i][j] = 0;
                this.board[i][j] = 2;
            }
        //遍历所有的五连子可能情况的权值
        //横
        for(i=0;i<15;i++)
            for(j=0;j<11;j++){
                for(k=0;k<5;k++){
                    this.ptable[j+k][i][icount] = true;
                    this.ctable[j+k][i][icount] = true;
                }
                icount++;
            }
        //竖
        for(i=0;i<15;i++)
            for(j=0;j<11;j++){
                for(k=0;k<5;k++){
                    this.ptable[i][j+k][icount] = true;
                    this.ctable[i][j+k][icount] = true;
                }
                icount++;
            }
        //右斜
        for(i=0;i<11;i++)
            for(j=0;j<11;j++){
                for(k=0;k<5;k++){
                    this.ptable[j+k][i+k][icount] = true;
                    this.ctable[j+k][i+k][icount] = true;
                }
                icount++;
            }
        //左斜
        for(i=0;i<11;i++)
            for(j=14;j>=4;j--){
                for(k=0;k<5;k++){
                    this.ptable[j-k][i+k][icount] = true;
                    this.ctable[j-k][i+k][icount] = true;
                }
                icount++;
            }
        for(i=0;i<=1;i++)  //初始化黑子白子上的每个权值上的连子数
            for(j=0;j<672;j++)
                this.win[i][j] = 0;
        this.player = true;
        this.icount = 0;
        this.ccount = 0;
        this.pcount = 0;
        this.start = true;
        this.over = false;
        this.pwin = false;
        this.cwin = false;
        this.tie = false;
        this.bout=1;
    }

    @Override
    public void playerDown(int x, int y) {
        int m1=x,n1=y;
        m = x;
        n = y;
        if(this.board[m][n] == 2){
            this.bout++;
            this.board[m][n] = 0;
            pcount++;
            if((ccount == 50) && (pcount == 50)){
                this.tie = true;
                this.over = true;
            }
            for(i=0;i<icount;i++){
                if(this.ptable[m][n][i] && this.win[0][i] != 7)
                    this.win[0][i]++;     //给黑子的所有五连子可能的加载当前连子数
                if(this.ctable[m][n][i]){
                    this.ctable[m][n][i] = false;
                    this.win[1][i]=7;
                }
            }
            this.player = false;
            this.computer = true;
        }else{
            m=m1;n=n1;
        }
    }

    @Override
    public void getNextPosition(int[] a) {
        for(i=0;i<15;i++)     //遍历棋盘上的所有坐标
            for(j=0;j<15;j++){
                this.pgrades[i][j]=0;  //该坐标的黑子奖励积分清零
                if(this.board[i][j] == 2)  //在还没下棋子的地方遍历
                    for(k=0;k<672;k++)    //遍历该棋盘可落子点上的黑子所有权值的连子情况，并给该落子点加上相应奖励分
                        if(this.ptable[i][j][k]){
                            switch(this.win[0][k]){
                                case 1: //一连子
                                    this.pgrades[i][j]+=5;
                                    break;
                                case 2: //两连子
                                    this.pgrades[i][j]+=50;
                                    break;
                                case 3: //三连子
                                    this.pgrades[i][j]+=180;
                                    break;
                                case 4: //四连子
                                    this.pgrades[i][j]+=400;
                                    break;
                            }
                        }
                this.cgrades[i][j]=0;//该坐标的白子的奖励积分清零
                if(this.board[i][j] == 2)  //在还没下棋子的地方遍历
                    for(k=0;k<icount;k++)     //遍历该棋盘可落子点上的白子所有权值的连子情况，并给该落子点加上相应奖励分
                        if(this.ctable[i][j][k]){
                            switch(this.win[1][k]){
                                case 1:  //一连子
                                    this.cgrades[i][j]+=5;
                                    break;
                                case 2:  //两连子
                                    this.cgrades[i][j]+=52;
                                    break;
                                case 3: //三连子
                                    this.cgrades[i][j]+=100;
                                    break;
                                case 4:  //四连子
                                    this.cgrades[i][j]+=400;
                                    break;
                            }
                        }
            }
        if(this.start){      //开始时白子落子坐标
            int tempX,tempY;
            int t = (int) (System.currentTimeMillis()%3);
            Log.d(TAG, "getNextPosition: t1:"+t);
            if(m<7){
                tempX = m+1;
            }else if(m>7){
                tempX = m-1;
            }else{
                tempX = m+1-t;
            }
            t = (int) (System.currentTimeMillis()%3);
            Log.d(TAG, "getNextPosition: t2:"+t);

            if(n<7){
                tempY = n+1;
            }else if(n>7){
                tempY = n-1;
            }else{
                tempY = n+1-t;
            }
            if(tempX == m && tempY == n){
                tempY = n-1;
            }

            m = tempX;
            n = tempY;

            this.start = false;
        }else{
            for(i=0;i<15;i++)
                for(j=0;j<15;j++)
                    if(this.board[i][j] == 2){  //找出棋盘上可落子点的黑子白子的各自最大权值，找出各自的最佳落子点
                        if(this.cgrades[i][j]>=this.cgrade){
                            this.cgrade = this.cgrades[i][j];
                            this.mat = i;
                            this.nat = j;
                        }
                        if(this.pgrades[i][j]>=this.pgrade){
                            this.pgrade = this.pgrades[i][j];
                            this.mde = i;
                            this.nde = j;
                        }
                    }
            if(this.cgrade>=this.pgrade){   //如果白子的最佳落子点的权值比黑子的最佳落子点权值大，则电脑的最佳落子点为白子的最佳落子点，否则相反
                m = mat;
                n = nat;
            }else{
                m = mde;
                n = nde;
            }
        }
        this.cgrade = 0;
        this.pgrade = 0;
        this.board[m][n] = 1;  //电脑下子位置
        a[0] = m;
        a[1] = n;
        ccount++;
        if((ccount == 50) && (pcount == 50))  //平局判断
        {
            this.tie = true;
            this.over = true;
        }
        for(i=0;i<icount;i++){
            if(this.ctable[m][n][i] && this.win[1][i] != 7)
                this.win[1][i]++;     //给白子的所有五连子可能的加载当前连子数
            if(this.ptable[m][n][i]){
                this.ptable[m][n][i] = false;
                this.win[0][i]=7;
            }
        }

    }

    public static void main(String[] args) {
        SimpleStrategy simpleStrategy = new SimpleStrategy();
        simpleStrategy.init();
        simpleStrategy.playerDown(2,3);
        int[] a = new int[2];
        simpleStrategy.getNextPosition(a);
        System.out.println(a[0]+" "+a[1]);
    }
}
