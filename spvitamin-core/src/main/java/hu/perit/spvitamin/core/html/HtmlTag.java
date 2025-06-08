package hu.perit.spvitamin.core.html;

import lombok.Setter;

import java.util.Collections;

/**
 * A builder class for programmatically creating HTML documents with proper structure and formatting.
 * 
 * <p>This class provides a fluent API for constructing HTML documents by creating and nesting
 * HTML tags with attributes and content. It maintains the parent-child relationships between
 * tags and handles proper indentation and rendering of the final HTML output.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Fluent builder pattern for creating HTML documents</li>
 *   <li>Support for nested tags with proper parent-child relationships</li>
 *   <li>Convenience methods for common HTML tags (head, body, table, etc.)</li>
 *   <li>Proper indentation and formatting of the rendered HTML</li>
 *   <li>Support for self-closing tags</li>
 *   <li>Simple content and complex nested content handling</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * HtmlTag html = HtmlTag.newInstance("en");
 * HtmlTag head = html.head();
 * head.title("My Page");
 * HtmlTag body = html.body("class=\"main\"");
 * body.h1(null, "Welcome");
 * System.out.println(html.render());
 * </pre>
 */
public class HtmlTag
{
    @Setter
    private HtmlTag parent;
    private final String tag;
    private final String attr;
    private final HtmlTagContent content;
    private boolean selfClosing = false;


    public static HtmlTag newInstance(String lang)
    {
        return new HtmlTag("html", String.format("lang = \"%s\"", lang));
    }


    private HtmlTag(String tag, String attr)
    {
        this.parent = null;
        this.tag = tag;
        this.attr = attr;
        this.content = new HtmlTagContent();
    }


    private HtmlTag(String tag, String attr, String simpleContent)
    {
        this.parent = null;
        this.tag = tag;
        this.attr = attr;
        this.content = new HtmlTagContent(simpleContent);
    }


    public void append(HtmlTag tag)
    {
        tag.setParent(this);
        this.content.append(tag);
    }


    public String render()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        this.print(sb, 0);
        return sb.toString();
    }


    void print(StringBuilder sb, int level)
    {
        sb.append(String.join("", Collections.nCopies(level, "  ")));
        sb.append("<").append(this.tag);
        if (this.attr != null)
        {
            sb.append(" ").append(this.attr);
        }
        sb.append(">");
        this.content.print(sb, level);
        if (!this.selfClosing)
        {
            if (!this.content.isSimpleContent())
            {
                sb.append(System.lineSeparator());
                sb.append(String.join("", Collections.nCopies(level, "  ")));
            }
            sb.append("</").append(this.tag).append(">");
        }
    }


    public HtmlTag tag(String tag, String attr)
    {
        HtmlTag newTag = new HtmlTag(tag, attr);
        append(newTag);
        return newTag;
    }


    public HtmlTag tag(String tag, String attr, String simpleContent)
    {
        HtmlTag newTag = new HtmlTag(tag, attr, simpleContent);
        append(newTag);
        return newTag;
    }


    public HtmlTag selfClosingTag(String tag, String attr)
    {
        HtmlTag newTag = new HtmlTag(tag, attr);
        newTag.selfClosing = true;
        append(newTag);
        return newTag;
    }


    public HtmlTag head()
    {
        return tag("head", null);
    }


    public HtmlTag meta(String attr)
    {
        return selfClosingTag("meta", attr);
    }


    public HtmlTag title(String title)
    {
        return tag("title", null, title);
    }


    public HtmlTag body(String attr)
    {
        return tag("body", attr);
    }


    // <span style="color:red; font-weight: bold;">English</span>
    public HtmlTag span(String style, String attr)
    {
        return tag("span", "style=" + quoted(style), attr);
    }


    private static String quoted(String text)
    {
        return "\"" + text + "\"";
    }


    public HtmlTag table(String attr)
    {
        return tag("table", attr);
    }


    public HtmlTag thead(String attr)
    {
        return tag("thead", attr);
    }


    public HtmlTag tbody(String attr)
    {
        return tag("tbody", attr);
    }


    public HtmlTag th(String attr, String simpleContent)
    {
        return tag("th", attr, simpleContent);
    }


    public HtmlTag td(String attr, String simpleContent)
    {
        return tag("td", attr, simpleContent);
    }


    public HtmlTag tr(String attr)
    {
        return tag("tr", attr);
    }


    public HtmlTag h1(String attr, String text)
    {
        return tag("h1", attr, text);
    }


    public HtmlTag h2(String attr, String text)
    {
        return tag("h2", attr, text);
    }

    public HtmlTag p(String attr, String text)
    {
        return tag("p", attr, text);
    }

    public HtmlTag br()
    {
        return selfClosingTag("br", null);
    }


    public HtmlTag style(String attr, String css)
    {
        HtmlTag newTag = new HtmlTag("style", attr, css);
        append(newTag);
        return newTag;
    }


    public HtmlTag parent()
    {
        return this.parent;
    }


    public HtmlTag root()
    {
        if (this.parent == null)
        {
            return this;
        }
        return this.parent.root();
    }
}
