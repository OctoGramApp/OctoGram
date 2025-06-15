/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.telegram.messenger.ApplicationLoader

/**
 * Represents a configuration property that can be stored and retrieved.
 *
 * It supports storing and retrieving values of various types, including String, Int, Boolean, Float, and Long.
 *
 * @param T The type of the configuration property value.
 * @param key The key used to identify the property in shared preferences. Can be null if not stored persistently.
 * @param initialValue The initial value of the property.
 */
class ConfigProperty<T>(
    var key: String?,
    initialValue: T
) {
    private val octoPreferences: SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences(
            "octoconfig",
            Context.MODE_PRIVATE
        )
    private var _value: T = initialValue
    private val defaultValue: T = initialValue

    var value: T
        get() = _value
        set(newValue) {
            updateValue(newValue)
        }

    /**
     * Updates the value of the preference and persists it to SharedPreferences.
     *
     * This function updates the internal value and, if a key is associated with this preference,
     * it also updates the value in the SharedPreferences using the appropriate put method
     * based on the type of the new value.
     *
     * @param newValue The new value to set for the preference.
     * @throws IllegalArgumentException If the type of the new value is not supported.
     * Supported types are: String, Int, Boolean, Float, Long.
     */
    @Throws(IllegalArgumentException::class)
    fun updateValue(newValue: T) {
        if (_value != newValue) {
            _value = newValue
            key?.let { k ->
                octoPreferences.edit {
                    when (newValue) {
                        is String? -> putString(k, newValue)
                        is Int -> putInt(k, newValue)
                        is Boolean -> putBoolean(k, newValue)
                        is Float -> putFloat(k, newValue)
                        is Long -> putLong(k, newValue)
                        else -> throw IllegalArgumentException("Unsupported type")
                    }
                }
            }
        }
    }

    /**
     * Removes the key value from preferences and resets to default value.
     */
    fun clear() {
        key?.let { k ->
            octoPreferences.edit {
                remove(k)
            }
            _value = defaultValue
        }
    }

    companion object {
        /**
         * Utility function to update the value of a given configuration property.
         *
         * This function takes a [ConfigProperty] and a new value, and updates the property's value to the new value.
         *
         * @param property The configuration property to update.
         * @param value The new value to set for the property.
         * @param <T> The type of the configuration property.
         */
        @JvmStatic
        fun <T> updateValue(property: ConfigProperty<T>, value: T) {
            property.updateValue(value)
        }
    }
}
