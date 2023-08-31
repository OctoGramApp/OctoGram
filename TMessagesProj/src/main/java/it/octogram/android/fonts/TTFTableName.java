package it.octogram.android.fonts;

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
