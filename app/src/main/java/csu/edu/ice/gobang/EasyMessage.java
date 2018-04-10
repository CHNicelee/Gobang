package csu.edu.ice.gobang;

/**
 * Created by ice on 2018/three/31.
 */
public class EasyMessage {
    public  static final String replaceStr = "/*a#z*/";
    public static final String type_connect_refused = "CONNECT_REFUSED";
    public static final String type_send_success = "SEND_SUCCESS";
    public static final String type_send_failed = "SEND_FAILED";
    public static final String type_user_message = "USER_MESSAGE";
    public static String type_reconnect = "RECONNECT";
    public static String type_keep_alive = "KEEP_ALIVE";
    private String type;
    private Integer fromKey;
    private Integer toKey;
    private Integer toGroup;
    private Object message;
    private Long time;
    public EasyMessage(){}

    public EasyMessage(Integer fromKey, Integer toKey, Object message) {
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.message = message;
        this.time = System.currentTimeMillis();
    }

    public EasyMessage(String type,Integer fromKey,Integer toKey,Object message){
        this(fromKey,toKey,message);
        this.type = type;
    }


    public Integer getFromKey() {
        return fromKey;
    }

    public void setFromKey(Integer fromKey) {
        this.fromKey = fromKey;
    }

    public Integer getToKey() {
        return toKey;
    }

    public void setToKey(Integer toKey) {
        this.toKey = toKey;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Integer getToGroup() {
        return toGroup;
    }

    public void setToGroup(Integer toGroup) {
        this.toGroup = toGroup;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "EasyMessage{" +
                "type='" + type + '\'' +
                ", fromKey=" + fromKey +
                ", toKey=" + toKey +
                ", toGroup=" + toGroup +
                ", message=" + message +
                ", time=" + time +
                '}';
    }
}