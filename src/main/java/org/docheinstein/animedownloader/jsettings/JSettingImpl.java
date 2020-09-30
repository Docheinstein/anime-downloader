package org.docheinstein.animedownloader.jsettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.docheinstein.commons.logger.DocLogger;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implementation of {@link JSetting}.
 * <p>
 * Introduces the concept of current and future value.
 * The purpose is that {@link #updateValue(Object)} do not actually change
 * the value of the setting until {@link #flushValue()} is called (usually
 * by the settings manager).
 * <p>
 * After a flush is called, the observers are notified about the change.
 * @param <T> the type of the setting
 */
public abstract class JSettingImpl<T> implements JSetting<T> {

    protected String mSettingName;
    protected T mCurrentValue;
    protected T mFutureValue;

    protected final Set<JSettingListener<T>> mListeners = new CopyOnWriteArraySet<>();

    private static final DocLogger L =
        DocLogger.createForClass(JSettingImpl.class);


    public JSettingImpl(String settingName, T defaultValue) {
        mSettingName = settingName;
        mFutureValue = defaultValue;
    }

    @Override
    public String getSettingName() {
        return mSettingName;
    }

    @Override
    public T getValue() {
        return mCurrentValue;
    }

    @Override
    public void updateValue(T value) {
        mFutureValue = value;
    }

    @Override
    public void addListener(JSettingListener<T> listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeListener(JSettingListener<T> listener) {
        mListeners.remove(listener);
    }

    /**
     * Returns the json associated with the given setting value.
     * @param value the value to jsonoify
     * @return the json of the value
     */
    public abstract JsonElement jsonifiedValue(T value);

    /**
     * Returns the value associated with the given json element.
     * @param json the json to unjsonify
     * @return the value of the json
     */
    public abstract T unjsonifiedValue(JsonElement json);

    /**
     * Returns the future setting value (the last one set via {@link #updateValue(Object)}).
     * @return the future setting value
     */
    public T getFutureValue() {
        return mFutureValue;
    }

    /**
     * Flushes the future setting value into the current value and notifies
     * the listener about the change.
     */
    public void flushValue() {
        if (!Objects.equals(mCurrentValue, mFutureValue)) {
            mCurrentValue = mFutureValue;
            notifyListeners();
        }
    }

    /**
     * Returns the current setting value as a json element.
     * @return the current setting value as json
     */
    public JsonElement getValueJson() {
        return jsonifiedValue(mCurrentValue);
    }

    /**
     * Returns the future setting value (the last one set via {@link #updateValue(Object)})
     * as a json element.
     * @return the future setting value as json
     */
    public JsonElement getFutureValueJson() {
        try {
            return jsonifiedValue(mFutureValue);
        } catch (RuntimeException re) {
            L.warn("Failed to get property " + mSettingName + "; returning null");
            return JsonNull.INSTANCE;
        }
    }

    /**
     * Updates the (future) setting value using the given json element
     * @param json the json element from which grab the setting value
     */
    public void updateValueFromJson(JsonElement json) {
        try {
            mFutureValue = unjsonifiedValue(json);
        } catch (RuntimeException re) {
            L.warn("Failed to set property " + mSettingName);
            mFutureValue = null; // set null as fallback
        }
    }

    private void notifyListeners() {
        mListeners.forEach(l -> l.onSettingValueChanged(this, getValue()));
    }
}
