package org.docheinstein.animedownloader.jsettings;

/**
 * Represents a generic key - value setting, meant to be used within a json file.
 * <p>
 * This setting is observable via  {@link #addListener(JSettingListener)}
 * @param <T> the type of the setting
 */
public interface JSetting<T> {
    /**
     * Returns the setting name, which should be the setting key.
     * @return the setting name
     */
    String getSettingName();

    /**
     * Returns the current setting value.
     * @return the current value
     */
    T getValue();

    /**
     * Updates the value of the setting.
     * @param value the new value
     */
    void updateValue(T value);

    /**
     * Adds a listener that will listen to any setting's change.
     * @param listener the listener to add
     */
    void addListener(JSettingListener<T> listener);

    /**
     * Removes a previously added listener from the listeners set.
     * @param listener the listener to remove
     */
    void removeListener(JSettingListener<T> listener);
}
