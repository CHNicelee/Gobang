package csu.edu.ice.gobang;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LongSparseArray;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ice on 2018/three/30.
 */

public class SocketUtil {

    private static final String TAG = "SocketUtil";

    public interface MessageHandler {
        void handleMessage(EasyMessage easyMessage);
    }

    public interface  OnDisconnectListener{
        void onDisconnect();
    }

    public interface Callback {
        void onSuccess();

        void onFailed(String errorMsg);
    }
    private OnDisconnectListener onDisconnectListener;
    private Class dataClass;
    private Socket socket;
    private Converter converter = new GsonConverter();

    String UNCONNECTED = "TO_UNCONNECTED";

    public MessageHandler mMessageHandler;

    public SocketUtil(MessageHandler messageHandler) {
        this.mMessageHandler = messageHandler;
    }



    public void connect(String host, int port, EasyMessage easyMessage, Callback callback) {

        Observable.create((ObservableOnSubscribe<String>) e -> {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            startListening();//开始监听服务器的消息
            OutputStream os = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, "UTF-8");
            writer.write(converter.toJson(easyMessage) + "\n");
            writer.flush();
            e.onNext("SUCCESS");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e -> callback.onSuccess(), throwable -> {
                    callback.onFailed(throwable.getMessage());
                    throwable.printStackTrace();
                });
    }

    /**
     * 开始监听服务器发来的消息
     */
    private void startListening() {
        try {
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            Observable.create((ObservableOnSubscribe<String>) e -> {
                while (!socket.isClosed()) {
                    //获取一行消息
                    String msg = br.readLine();
                    e.onNext(msg);
                }
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(s -> {
                        Log.d(TAG, "receiveMsg: "+s);
                        s = s.replaceAll("\n", EasyMessage.replaceStr);
                        return converter.toEasyMessage(s);
                    })
                    .filter(easyMessage -> easyMessage!=null)
                    .subscribe(s -> handleMessage(s), throwable -> {
                        throwable.printStackTrace();
                        if(onDisconnectListener!=null)onDisconnectListener.onDisconnect();
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "startListening: 执行完毕");
    }

    LongSparseArray<Callback> sendMessageCallbacks = new LongSparseArray<>();

    /**
     * 发送消息
     * @param easyMessage  待发送的消息
     * @param sendMessageCallback  发送消息之后的回调函数
     */
    public void sendMessage(@NonNull EasyMessage easyMessage, Callback sendMessageCallback) {
        if(socket == null)return;
        if (socket.isClosed() && sendMessageCallback!=null)sendMessageCallback.onFailed("socket is closed");
        if (sendMessageCallback != null)
            sendMessageCallbacks.put(easyMessage.getTime(), sendMessageCallback);
        if(easyMessage.getType() ==null)easyMessage.setType(EasyMessage.type_user_message);
        Observable.create((ObservableOnSubscribe<String>) e -> {
            String json = converter.toJson(easyMessage);
            json = json.replaceAll("\n", EasyMessage.replaceStr);
            OutputStream os = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, "UTF-8");
            Log.d(TAG, "sendMessage: "+json);
            writer.write(json + "\n");
            writer.flush();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e -> {
                }, throwable -> {
                    if (sendMessageCallback != null)
                        sendMessageCallback.onFailed(throwable.getMessage());
                });

    }

    //处理结果和服务器发来的消息
    public void handleMessage(EasyMessage easyMessage) {

        Log.d(TAG, "handleMessage: " + easyMessage);
        Callback callback = getSendMessageCallback(easyMessage);
        if (easyMessage.getType().equalsIgnoreCase(EasyMessage.type_send_success)) {
            //消息发送成功
            if (callback != null) callback.onSuccess();

        } else if (easyMessage.getType().equalsIgnoreCase(EasyMessage.type_send_failed)) {
            //消息发送失败了 对方关闭了socket
            if (callback != null) callback.onFailed(UNCONNECTED);

        } else if(easyMessage.getType().equalsIgnoreCase(EasyMessage.type_connect_refused)){
            //服务器的onConnect方法返回了false
            if(callback!=null) callback.onFailed("服务器拒绝连接，请调用closeSocket方法关闭连接");

        } else if(easyMessage.getType().equalsIgnoreCase(EasyMessage.type_keep_alive)){
            //心跳包
        }else if (mMessageHandler != null) {
            if(dataClass!=null && easyMessage.getMessage()!=null)
            easyMessage.setMessage( gson.fromJson(easyMessage.getMessage().toString(),dataClass));
            //用户自定义的消息  让用户处理
            mMessageHandler.handleMessage(easyMessage);
        }

    }

    Gson gson = new Gson();

    //关闭socket
    public void closeSocket() {
        try {
            if(socket!=null)socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从map中根据消息的time获取callback
    private Callback getSendMessageCallback(EasyMessage easyMessage){
        try {
            if(easyMessage.getTime()==null)return null;

            Callback callback = sendMessageCallbacks.get(easyMessage.getTime());
            sendMessageCallbacks.remove(easyMessage.getTime());
            return callback;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //-----------------SETTER-----------

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setDataClass(Class dataClass) {
        this.dataClass = dataClass;
    }

    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }
}
