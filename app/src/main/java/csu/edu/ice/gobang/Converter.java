package csu.edu.ice.gobang;

/**
 * Created by ice on 2018/4/1.
 */
public interface Converter {
    String toJson(EasyMessage easyMessage);
    EasyMessage toEasyMessage(String message);
}