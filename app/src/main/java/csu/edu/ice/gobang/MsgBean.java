package csu.edu.ice.gobang;

/**
 * Created by ice on 2018/3/29.
 */
public class MsgBean {
    public final static String type_connect = "connected";
    public final static String type_newChess = "newChess";
    public static String type_disconnect = "disconnect";
    public static String type_timeout = "timeout";

    public MsgBean() {
    }

    public MsgBean(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }
    private Integer y;
    private Integer x;
    private Integer color;
    private String message;
    private Integer room;
    private Boolean moveFirst;

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Integer getRoom() {
        return room;
    }

    public void setRoom(Integer room) {
        this.room = room;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Boolean getMoveFirst() {
        return moveFirst;
    }

    public void setMoveFirst(Boolean moveFirst) {
        this.moveFirst = moveFirst;
    }
}