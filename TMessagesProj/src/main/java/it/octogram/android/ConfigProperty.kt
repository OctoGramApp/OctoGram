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
 * A class representing a configuration property.
 *
 * @param T the type of the property value.
 * @property key the key of the property, can be null.
 * @property value the value of the property.
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

    fun updateValue(newValue: T) {
        if (value != newValue) {
            if (key != null) {
                val editor = octoPreferences.edit()
                when (newValue) {
                    is String -> editor.putString(key, newValue)
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

    fun <T> updateValue(property: ConfigProperty<T>, value: T) {
        property.updateValue(value)
    }
}
