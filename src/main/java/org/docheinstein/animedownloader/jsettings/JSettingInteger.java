package org.docheinstein.animedownloader.jsettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Represents an integer setting.
 */
public class JSettingInteger extends JSettingImpl<Integer> {

    public JSettingInteger(String settingName, Integer defaultValue) {
        super(settingName, defaultValue);
    }

    @Override
    public JsonElement jsonifiedValue(Integer value) {
        return value != null ?
            new JsonPrimitive(value) :
            JsonNull.INSTANCE;
    }

    @Override
    public Integer unjsonifiedValue(JsonElement json) {
        return json != null && !json.isJsonNull() ? json.getAsInt() : null;
    }
}
