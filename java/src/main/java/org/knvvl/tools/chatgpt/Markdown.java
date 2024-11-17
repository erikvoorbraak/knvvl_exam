package org.knvvl.tools.chatgpt;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

/**
 * Markdown helper class
 */
public final class Markdown
{
    private Markdown()
    {
    }

    /**
     * Convert markdown to html
     * 
     * @param markdown Markdown text
     * @return html 
     */
    public static String toHtml(String markdown)
    {
        // Create the Markdown parser
        Parser parser = Parser.builder().build();
        
        // Parse the Markdown content to a Node
        Node document = parser.parse(markdown);
        
        // Create the HTML renderer
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        
        // Render the HTML from the parsed Node
        return renderer.render(document).strip();
    }

    /**
     * Convert markdown to plain text
     *
     * @param markdown Markdown text
     * @return plain text
     */
    public static String toPlain(String markdown)
    {
       // Create the Markdown parser
       Parser parser = Parser.builder().build();
        
       // Parse the Markdown content to a Node
       Node document = parser.parse(markdown);
       
       return extractPlainText(document);
    }

    // Recursive function to extract plain text from nodes, preserving line breaks
    private static String extractPlainText(Node node)
    {
        StringBuilder sb = new StringBuilder();

        // Traverse through the document nodes
        for (Node child = node.getFirstChild(); child != null; child = child.getNext())
        {
            // Handle block elements with line breaks
            if (child instanceof Paragraph || child instanceof Heading || child instanceof ListItem)
            {
                sb.append(extractPlainText(child));
                sb.append("\n"); // Append a newline after paragraphs, headings, and list items
            }
            else if (child instanceof BulletList)
            {
                sb.append(extractPlainText(child));
                sb.append("\n"); // Append a newline after the entire list
            }
            else if (child instanceof Text)
            {
                // For text nodes, simply append the text
                sb.append(child.getChars());
            }
            else
            {
                // Recursively process child nodes
                sb.append(extractPlainText(child));
            }
        }

        return sb.toString().trim();
    }
}
