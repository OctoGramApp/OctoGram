package it.octogram.android.magic;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public abstract class MagicHashVector<T> extends MagicBaseObject implements Iterable<T> {
    private final HashSet<T> vector = new HashSet<>();

    @SafeVarargs
    public final void readParams(byte[] stream, boolean exception, T... defaultsObject) {
        readParams(stream, exception);
        if (stream == null && defaultsObject != null) {
            vector.addAll(Arrays.asList(defaultsObject));
        }
    }

    public void add(T object) {
        vector.add(object);
    }

    public void remove(T object) {
        vector.remove(object);
    }

    public int size() {
        return vector.size();
    }

    public void clear() {
        vector.clear();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return vector.iterator();
    }
}
