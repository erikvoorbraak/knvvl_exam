package org.knvvl.exam.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Utils
{
    public static String getAsString(JsonObject form, String key)
    {
        JsonElement valueJson = form.get(key);
        return valueJson == null ? "" : valueJson.getAsString();
    }

}
