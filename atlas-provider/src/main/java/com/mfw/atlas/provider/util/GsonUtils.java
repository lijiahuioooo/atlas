package com.mfw.atlas.provider.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author KL
 * @Time 2020/6/8 4:31 下午
 */
public class GsonUtils {

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private GsonUtils() {

    }

    /**
     * 返回json字符串
     *
     * @param obj
     * @return
     */
    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 解析json为对象
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     * @throws JsonSyntaxException
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return gson.fromJson(json, clazz);
    }


    /**
     * 转换为JsonObject
     *
     * @param jsonStr
     * @return
     */
    public static JsonObject toJsonObject(String jsonStr) throws JsonSyntaxException {
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }

    /**
     * 转换为JsonArray
     *
     * @param jsonArrayStr
     * @return
     */
    public static JsonArray toJsonArray(String jsonArrayStr) throws JsonSyntaxException {
        return JsonParser.parseString(jsonArrayStr).getAsJsonArray();
    }
}
