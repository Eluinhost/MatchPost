package gg.uhc.matchpost.serialization;

import net.md_5.bungee.api.chat.BaseComponent;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.Visitor;

public abstract class Serializer implements Visitor {
    public abstract BaseComponent readFromRoot(RootNode astRoot);
}
