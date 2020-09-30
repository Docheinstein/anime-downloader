package org.docheinstein.animedownloader.jsettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Represents a string setting.
 */
public class JSettingString extends JSettingImpl<String> {
    public JSettingString(String settingName, String defaultValue) {
        super(settingName, defaultValue);
    }

    @Override
    public JsonElement jsonifiedValue(String value) {
        return value != null ?
            new JsonPrimitive(value) :
            JsonNull.INSTANCE;
    }

    @Override
    public String unjsonifiedValue(JsonElement json) {
        return json != null && !json.isJsonNull() ? json.getAsString() : null;
    }
}
