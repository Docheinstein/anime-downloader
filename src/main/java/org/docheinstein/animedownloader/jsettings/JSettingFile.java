package org.docheinstein.animedownloader.jsettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.io.File;

/**
 * Represents a file setting (stores the absolute path).
 */
public class JSettingFile extends JSettingImpl<File> {

    public JSettingFile(String settingName, File defaultValue) {
        super(settingName, defaultValue);
    }

    @Override
    public JsonElement jsonifiedValue(File value) {
        return value != null ?
            new JsonPrimitive(value.getAbsolutePath()) :
            JsonNull.INSTANCE;
    }

    @Override
    public File unjsonifiedValue(JsonElement json) {
        return json != null && !json.isJsonNull() ? new File(json.getAsString()) : null;
    }
}
