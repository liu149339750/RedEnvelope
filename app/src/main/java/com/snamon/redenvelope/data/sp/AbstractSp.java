package com.snamon.redenvelope.data.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * sp数据操作基类.
 */
@SuppressWarnings("unused")
public abstract class AbstractSp {
    @NonNull
    private final Context mContext;
    public AbstractSp(@NonNull Context context) {
        mContext = context;
    }
    /**
     * 异步保存键值对.
     *
     * @param key   健
     * @param value 值
     */
    protected void asyncPut(@NonNull String key, @NonNull Object value) {
        SharedPreferences.Editor editor = putIntoEditor(key, value);
        editor.apply();
    }
    /**
     * 同步保存键值对.
     *
     * @param key   健
     * @param value 值
     * @return true - 保存成功, false - 保存失败
     */
    protected boolean syncPut(@NonNull String key, @NonNull Object value) {
        SharedPreferences.Editor editor = putIntoEditor(key, value);
        return editor.commit();
    }
    /**
     * 获取健对应的值.
     *
     * @param key      健
     * @param defValue 默认值
     * @return 健对应的值
     */
    protected Object get(@NonNull String key, @NonNull Object defValue) {
        SharedPreferences sp = getSp();
        if (defValue instanceof String) {
            return sp.getString(key, (String) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Float) {
            return sp.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Long) {
            return sp.getLong(key, (Long) defValue);
        } else {
            return defValue;
        }
    }
    /**
     * 异步移除键值对.
     *
     * @param key 健
     */
    protected void asyncRemove(String key) {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(key);
        editor.apply();
    }
    /**
     * 同步移除键值对.
     *
     * @param key 健
     * @return true - 移除成功, false - 移除失败
     */
    protected boolean syncRemove(String key) {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(key);
        return editor.commit();
    }
    /**
     * 异步清除全部键值对.
     */
    protected void asyncClear() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        editor.apply();
    }
    /**
     * 同步清除全部键值对.
     *
     * @return true - 清除成功, false - 清除失败
     */
    protected boolean syncClear() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        return editor.commit();
    }
    /**
     * 获取SharedPreferences的文件名.
     *
     * @return SharedPreferences的文件名.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    protected abstract String getSpName();
    private SharedPreferences.Editor getEditor() {
        return getSp().edit();
    }
    private SharedPreferences.Editor putIntoEditor(@NonNull String key, @NonNull Object value) {
        SharedPreferences.Editor editor = getEditor();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            editor.putString(key, value.toString());
        }
        return editor;
    }
    private SharedPreferences getSp() {
        return mContext.getSharedPreferences(getSpName(), Context.MODE_PRIVATE);
    }
}