package ai.shizhongying.template.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Gson 工具类
 */
public class GsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * JSON 字符串转对象（支持泛型）
     */
    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        return GSON.fromJson(json, typeToken.getType());
    }
}
