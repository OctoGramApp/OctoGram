/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android

import android.app.Activity
import org.telegram.messenger.ApplicationLoader

/**
 * Represents a configuration property that can be stored and retrieved.
 *
 * It supports storing and retrieving values of various types, including String, Int, Boolean, Float, and Long.
 *
 * @param T The type of the configuration property value.
 * @param key The key used to identify the property in shared preferences. Can be null if not stored persistently.
 * @param value The initial value of the property.
 */
class ConfigProperty<T>(
    private val key: String?,
    private var value: T
) {
    private val octoPreferences = ApplicationLoader.applicationContext.getSharedPreferences(
        "octoconfig",
        Activity.MODE_PRIVATE
    )

    fun getKey(): String? = key

    fun getValue(): T = value

    fun setValue(newValue: T) {
        value = newValue
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
        if (value != newValue) {
            if (key != null) {
                val editor = octoPreferences.edit()
                when (newValue) {
                    is String? -> editor.putString(key, newValue)
                    is Int -> editor.putInt(key, newValue)
                    is Boolean -> editor.putBoolean(key, newValue)
                    is Float -> editor.putFloat(key, newValue)
                    is Long -> editor.putLong(key, newValue)
                    else -> throw IllegalArgumentException("Unsupported type")
                }
                editor.apply()
            }
            value = newValue
        }
    }

    /**
     * Updates the value of a given configuration property.
     *
     * This function takes a [ConfigProperty] and a new value, and updates the property's value to the new value.
     *
     * @param T The type of the configuration property.
     * @param property The configuration property to update.
     * @param value The new value to set for the property.
     */
    fun <T> updateValue(property: ConfigProperty<T>, value: T) {
        property.updateValue(value)
    }
}
