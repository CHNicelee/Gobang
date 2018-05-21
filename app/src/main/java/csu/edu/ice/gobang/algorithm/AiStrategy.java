package csu.edu.ice.gobang.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ice on 2018/5/21.
 * 算法来源:http://www.jb51.net/article/137158.htm
 */

public class AiStrategy implements MoveStrategy {

    BaseComputerAi baseComputerAi = new BaseComputerAi();

    @Override
    public void init() {
        baseComputerAi.setChessboard(new IChessboard() {
            List<Point> freePoints = new ArrayList<>(225);
            {
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 15; j++) {
                        freePoints.add(new Point(i,j));
                    }
                }
            }
            @Override
            public int getMaxX() {
                return 14;
            }

            @Override
            public int getMaxY() {
                return 14;
            }

            @Override
            public List<Point> getFreePoints() {
                return freePoints;
            }
        });

    }
    Point point = new Point(0,0);
    List<Point> humuns = new ArrayList<>();
    @Override
    public void playerDown(int x, int y) {
        humuns.add(new Point(x,y));
        baseComputerAi.run(humuns,point);
    }

    @Override
    public void getNextPosition(int[] a) {
        a[0] = point.getX();
        a[1] = point.getY();
    }
}
