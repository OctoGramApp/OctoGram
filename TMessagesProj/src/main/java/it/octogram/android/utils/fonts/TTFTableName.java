/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.fonts;

import androidx.annotation.NonNull;

public final class TTFTableName {

    public static final TTFTableName TABLE_DIRECTORY = new TTFTableName("tableDirectory");

    public static final TTFTableName NAME = new TTFTableName("name");
    private final String name;

    private TTFTableName(String name) {
        this.name = name;
    }

    public static TTFTableName getValue(String tableName) {
        if (tableName != null) {
            return new TTFTableName(tableName);
        }
        throw new IllegalArgumentException("A TrueType font table name must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TTFTableName)) {
            return false;
        }
        TTFTableName to = (TTFTableName) o;
        return name.equals(to.getName());
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
