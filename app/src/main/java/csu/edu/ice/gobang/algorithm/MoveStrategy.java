package csu.edu.ice.gobang.algorithm;

/**
 * Created by ice on 2018/5/21.
 */

public interface MoveStrategy {
    void init();
    void playerDown(int x,int y);
    void getNextPosition(int a[]);
}
