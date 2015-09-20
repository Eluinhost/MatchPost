package gg.uhc.matchpost.serialization;

import com.google.common.base.Supplier;

public class DefaultSerializationProvider implements Supplier<Serializer> {
    @Override
    public Serializer get() {
        return new ChatSerializer();
    }
}
