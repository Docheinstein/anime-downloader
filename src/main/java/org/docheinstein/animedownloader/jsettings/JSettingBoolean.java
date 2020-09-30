package org.docheinstein.animedownloader.jsettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Represents a boolean setting.
 */
public class JSettingBoolean extends JSettingImpl<Boolean> {

    public JSettingBoolean(String settingName, Boolean defaultValue) {
        super(settingName, defaultValue);
    }

    @Override
    public JsonElement jsonifiedValue(Boolean value) {
        return value != null ?
            new JsonPrimitive(value) :
            JsonNull.INSTANCE;
    }

    @Override
    public Boolean unjsonifiedValue(JsonElement json) {
        return json != null && !json.isJsonNull() ? json.getAsBoolean() : null;
    }
}
