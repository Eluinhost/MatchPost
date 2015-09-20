package gg.uhc.matchpost.reddit;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import gg.uhc.matchpost.serialization.Serializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

public class MatchPostParser implements Function<String, BaseComponent> {

    protected final Supplier<Serializer> serializerSupplier;

    public MatchPostParser(Supplier<Serializer> serailizerSupplier) {
        this.serializerSupplier = serailizerSupplier;
    }

    public BaseComponent parseChat(String input) {
        RootNode root = new PegDownProcessor(Extensions.ALL).parseMarkdown(input.toCharArray());
        return serializerSupplier.get().readFromRoot(root);
    }

    @Override
    public BaseComponent apply(String input) {
        return parseChat(input);
    }
}
