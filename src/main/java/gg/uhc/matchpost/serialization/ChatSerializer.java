package gg.uhc.matchpost.serialization;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.pegdown.Printer;
import org.pegdown.ast.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Stack;

public class ChatSerializer extends Serializer {

    protected Printer printer = new Printer();

    protected Stack<BaseComponent> stack;

    public BaseComponent readFromRoot(RootNode astRoot) {
        // empty root element
        TextComponent root = new TextComponent("");

        // create the stack for nested elements
        stack = new Stack<>();
        stack.push(root);

        // start visiting
        astRoot.accept(this);

        // return the root node
        return root;
    }

    @Override
    public void visit(RootNode node) {
        // visit each child node of the root
        visitChildren(node);
    }

    @Override
    public void visit(AbbreviationNode abbreviationNode) {
        visitChildren(abbreviationNode);
    }

    @Override
    public void visit(AnchorLinkNode anchorLinkNode) {
        TextComponent component = renderText(anchorLinkNode.getText());
        component.setColor(ChatColor.BLUE);
    }

    @Override
    public void visit(AutoLinkNode autoLinkNode) {
        renderLink(autoLinkNode.getText(), autoLinkNode.getText());
    }

    @Override
    public void visit(BlockQuoteNode blockQuoteNode) {
        TextComponent component = makeNewLevel(blockQuoteNode);
        component.setColor(ChatColor.DARK_GRAY);
    }

    @Override
    public void visit(BulletListNode bulletListNode) {
        visitChildren(bulletListNode);
    }

    @Override
    public void visit(CodeNode codeNode) {
        TextComponent component = renderText(codeNode.getText());
        component.setColor(ChatColor.GRAY);
        component.setItalic(true);
    }

    @Override
    public void visit(DefinitionListNode definitionListNode) {
        visitChildren(definitionListNode);
    }

    @Override
    public void visit(DefinitionNode definitionNode) {
        visitChildren(definitionNode);
    }

    @Override
    public void visit(DefinitionTermNode definitionTermNode) {
        visitChildren(definitionTermNode);
    }

    @Override
    public void visit(ExpImageNode expImageNode) {
        renderLink(expImageNode.title, expImageNode.url);
    }

    @Override
    public void visit(ExpLinkNode expLinkNode) {
        String contents = getTextFromNode(expLinkNode);
        renderLink(contents, expLinkNode.url);
    }

    @Override
    public void visit(HeaderNode headerNode) {
        TextComponent component = makeNewLevel(headerNode);
        component.setBold(true);
        component.setColor(ChatColor.GOLD);
    }

    @Override
    public void visit(HtmlBlockNode htmlBlockNode) {
        renderText(htmlBlockNode.getText());
    }

    @Override
    public void visit(InlineHtmlNode inlineHtmlNode) {
        renderText(inlineHtmlNode.getText());
    }

    @Override
    public void visit(ListItemNode listItemNode) {
        TextComponent component = makeNewLevel(listItemNode);
        component.setText("- ");
    }

    @Override
    public void visit(MailLinkNode mailLinkNode) {
        renderLink(mailLinkNode.getText(), "mailto:" + mailLinkNode);
    }

    @Override
    public void visit(OrderedListNode orderedListNode) {
        TextComponent component = makeNewLevel(orderedListNode);
        component.setText("- ");
    }

    @Override
    public void visit(ParaNode paraNode) {
        renderText("\n");
        visitChildren(paraNode);
        renderText("\n");
    }

    @Override
    public void visit(QuotedNode quotedNode) {
        String start = "";
        String end = "";

        switch (quotedNode.getType()) {
            case DoubleAngle:
                start = "«";
                end = "»";
                break;
            case Double:
                start = "“";
                end = "”";
                break;
            case Single:
                start = "'";
                end = "'";
                break;
        }

        renderText(start);
        visitChildren(quotedNode);
        renderText(end);
    }

    @Override
    public void visit(ReferenceNode referenceNode) {}

    @Override
    public void visit(RefImageNode refImageNode) {
        // no clue how to handle this shit
    }

    @Override
    public void visit(RefLinkNode refLinkNode) {
        // no clue how to handle this shit
    }

    @Override
    public void visit(SimpleNode simpleNode) {
        switch (simpleNode.getType()) {
            case Apostrophe:
                renderText("'");
                break;
            case Ellipsis:
                renderText("…");
                break;
            case Emdash:
            case Endash:
                renderText("-");
                break;
            case HRule:
                TextComponent component = renderText("\n----------\n");
                component.setColor(ChatColor.GRAY);
                component.setStrikethrough(true);
                break;
            case Linebreak:
                renderText("\n");
                break;
            case Nbsp:
                renderText(" ");
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void visit(SpecialTextNode specialTextNode) {
        printer.clear();
        printer.printEncoded(specialTextNode.getText());
        renderText(printer.getString());
        printer.clear();
    }

    @Override
    public void visit(StrikeNode strikeNode) {
        TextComponent component = makeNewLevel(strikeNode);
        component.setStrikethrough(true);
    }

    @Override
    public void visit(StrongEmphSuperNode node) {
        if (node.isClosed()) {
            TextComponent component = makeNewLevel(node);

            if (node.isStrong()) {
                component.setBold(true);
                component.setColor(ChatColor.GOLD);
            } else {
                component.setItalic(true);
            }
        } else {
            // sequence was not closed, treat open chars as ordinary chars
            renderText(node.getChars());
            visitChildren(node);
        }
    }

    @Override
    public void visit(TableBodyNode tableBodyNode) {
        // todo
    }

    @Override
    public void visit(TableCaptionNode tableCaptionNode) {
        // todo
    }

    @Override
    public void visit(TableCellNode tableCellNode) {
        // todo
    }

    @Override
    public void visit(TableColumnNode tableColumnNode) {
        // todo
    }

    @Override
    public void visit(TableHeaderNode tableHeaderNode) {
        // todo
    }

    @Override
    public void visit(TableNode tableNode) {
        // todo
    }

    @Override
    public void visit(TableRowNode tableRowNode) {
        // todo
    }

    @Override
    public void visit(VerbatimNode verbatimNode) {
        // don't know what this is
    }

    @Override
    public void visit(WikiLinkNode node) {
        try {
            String text = node.getText();
            String url = text;
            int pos;
            if ((pos = text.indexOf("|")) >= 0) {
                url = text.substring(0, pos);
                text = text.substring(pos+1);
            }

            url = "./" + URLEncoder.encode(url.replace(' ', '-'), "UTF-8") + ".html";

            renderLink(text, url);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void visit(TextNode textNode) {
        renderText(textNode.getText());
    }

    @Override
    public void visit(SuperNode superNode) {
        visitChildren(superNode);
    }

    @Override
    public void visit(Node node) {
        // override this method for processing custom Node implementations
        throw new RuntimeException("Don't know how to handle node " + node);
    }

    // helpers
    protected void visitChildren(SuperNode node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    // helpers
    protected void renderLink(String name, String link) {
        TextComponent component = renderText(name);

        component.setColor(ChatColor.BLUE);
        component.setUnderlined(true);

        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
    }

    // helpers
    protected TextComponent renderText(String text) {
        TextComponent component = new TextComponent(text);
        stack.peek().addExtra(component);
        return component;
    }

    // helpers
    protected TextComponent makeNewLevel(SuperNode node) {
        TextComponent component = new TextComponent("");

        stack.push(component);
        visitChildren(node);
        stack.pop();

        stack.peek().addExtra(component);

        return component;
    }

    protected String getTextFromNode(SuperNode node) {
        // backup old stack
        Stack<BaseComponent> backup = stack;

        // make a fresh stack
        stack = new Stack<>();
        TextComponent base = new TextComponent("");
        stack.push(base);

        // visit each child
        visitChildren(node);

        // restore original stack
        stack = backup;

        // get the textual contents of the components
        StringBuilder builder = new StringBuilder();
        getTextContents(base, builder);
        return builder.toString();
    }

    protected StringBuilder getTextContents(BaseComponent component, StringBuilder builder) {
        if (component instanceof TextComponent) {
            builder.append(((TextComponent) component).getText());
        }

        List<BaseComponent> extra = component.getExtra();

        if (extra != null) {
            for (BaseComponent c : component.getExtra()) {
                getTextContents(c, builder);
            }
        }

        return builder;
    }
}
