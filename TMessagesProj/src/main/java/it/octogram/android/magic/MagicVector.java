package it.octogram.android.magic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public abstract class MagicVector<T> extends MagicBaseObject implements Iterable<T> {
    private final ArrayList<T> vector = new ArrayList<>();

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

    public void add(int index, T object) {
        vector.add(index, object);
    }

    public T get(int index) {
        return vector.get(index);
    }

    public void move(int fromIndex, int toIndex) {
        T object = vector.remove(fromIndex);
        vector.add(toIndex, object);
    }

    public boolean contains(T object) {
        return vector.contains(object);
    }

    public void addAll(ArrayList<T> objects) {
        vector.addAll(objects);
    }

    public T remove(int index) {
        return vector.remove(index);
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
