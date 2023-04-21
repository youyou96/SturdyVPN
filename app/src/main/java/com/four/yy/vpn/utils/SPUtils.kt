package com.four.yy.vpn.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences

class SPUtils private constructor() {

    private val NAME = "wifi_data_config"

    private var mWrapper: ContextWrapper? = null
    private var mPreferences: SharedPreferences? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: SPUtils? = null
            get() {
                if (field == null) {
                    field = SPUtils()
                }
                return field
            }

        fun get(): SPUtils {
            return instance!!
        }
    }

    fun init(context: Context?) {
        if (mWrapper == null) {
            mWrapper = ContextWrapper(context)
        }
        if (mPreferences == null) {
            mPreferences = mWrapper!!.getSharedPreferences(NAME, ContextWrapper.MODE_PRIVATE)
        }
    }

    /**
     * putInt
     * @param key 键
     * @param value 缓存值
     */
    fun putInt(key: String?, value: Int) {
        mPreferences!!.edit().putInt(key, value).apply()
    }

    /**
     * getInt
     * @param key 键
     * @param defValue 默认值
     * @return 结果
     */
    fun getInt(key: String?, defValue: Int): Int {
        return mPreferences!!.getInt(key, defValue)
    }

    /**
     * putString
     * @param key 键
     * @param value 缓存值
     */
    fun putString(key: String?, value: String?) {
        mPreferences!!.edit().putString(key, value).apply()
    }

    /**
     * getString
     * @param key 键
     * @param defValue 默认值
     * @return 结果
     */
    fun getString(key: String?, defValue: String): String? {
        if (mPreferences != null) {
            return mPreferences!!.getString(key, defValue)
        }
        return defValue
    }

    /**
     * putBoolean
     * @param key 键
     * @param value 缓存值
     */
    fun putBoolean(key: String?, value: Boolean) {
        mPreferences!!.edit().putBoolean(key, value).apply()
    }

    /**
     * getBoolean
     * @param key 键
     * @param defValue 默认值
     * @return 结果
     */
    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mPreferences!!.getBoolean(key, defValue)
    }

    /**
     * putFloat
     * @param key 键
     * @param value 缓存值
     */
    fun putFloat(key: String?, value: Float) {
        mPreferences!!.edit().putFloat(key, value).apply()
    }

    /**
     * getFloat
     * @param key 键
     * @param defValue 默认值
     * @return 结果
     */
    fun getFloat(key: String?, defValue: Float): Float {
        return mPreferences!!.getFloat(key, defValue)
    }

    /**
     * putLong
     * @param key 键
     * @param value 缓存值
     */
    fun putLong(key: String?, value: Long) {
        mPreferences!!.edit().putLong(key, value).apply()
    }

    /**
     * getLong
     * @param key 值
     * @param defValue 默认值
     * @return 结果
     */
    fun getLong(key: String?, defValue: Long): Long {
        return mPreferences!!.getLong(key, defValue)
    }

    /**
     * putStringSet
     * @param key 键
     * @param value 缓存值
     */
    fun putStringSet(key: String?, value: Set<String?>?) {
        mPreferences!!.edit().putStringSet(key, value).apply()
    }

    /**
     * getStringSet
     * @param key 值
     * @param defValue 默认值
     * @return 结果
     */
    fun getStringSet(key: String?, defValue: Set<String?>?): Set<String?>? {
        return mPreferences!!.getStringSet(key, defValue)
    }

    /**
     * 清除缓存
     * @param key 键
     */
    fun remove(key: String?) {
        mPreferences!!.edit().remove(key).apply()
    }
}