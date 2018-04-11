
package csu.edu.ice.gobang;

import com.google.gson.Gson;

/**
 * Created by ice on 2018/4/1.
 */
public class GsonConverter implements Converter {
    public static Gson gson = new Gson();
    @Override
    public String toJson(EasyMessage easyMessage) {
        return gson.toJson(easyMessage);
    }

    @Override
    public EasyMessage toEasyMessage(String message) {
        int index = 0;
        for (; index < message.length()&&message.charAt(index)!='{'; index++) {}
        message = message.substring(index);
        try{
            return gson.fromJson(message,EasyMessage.class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}