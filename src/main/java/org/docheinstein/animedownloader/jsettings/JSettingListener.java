package org.docheinstein.animedownloader.jsettings;

/**
 * Interface used to listen to changes of the setting's value.
 * @param <T> the type
 */
public interface JSettingListener<T> {
    /**
     * Called when the setting value changes.
     * @param setting the setting
     * @param value the new setting's value
     */
    void onSettingValueChanged(JSetting setting, T value);
}
