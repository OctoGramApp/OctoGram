package it.octogram.android.magic;


import org.telegram.tgnet.AbstractSerializedData;

import java.util.NoSuchElementException;
import java.util.Objects;

public class OptionalMagic<T> extends MagicBaseObject {
    private T value;

    public static <T> OptionalMagic<T> of(T value) {
        OptionalMagic<T> optional = new OptionalMagic<>();
        optional.value = value;
        return optional;
    }

    public static <T> OptionalMagic<T> of(byte[] stream, boolean exception) {
        OptionalMagic<T> optional = new OptionalMagic<>();
        optional.readParams(stream, exception);
        return optional;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readParams(AbstractSerializedData stream, int constructor, boolean exception) {
        if (stream.readBool(exception)) {
            try {
                value = (T) readObject(stream, exception);
            } catch (SkipException ignored) {}
        } else {
            value = null;
        }
    }

    @Override
    protected void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(getConstructor());
        stream.writeBool(isPresent());
        if (isPresent()) {
            serializeObject(value, stream);
        }
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalMagic)) {
            return false;
        }

        OptionalMagic<?> other = (OptionalMagic<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int getConstructor() {
        return OPTIONAL_CONSTRUCTOR;
    }
}
