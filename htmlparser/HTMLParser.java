package htmlparser;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import htmlparser.tokens.*;
import htmlparser.tagnodes.TagNode;
import htmlparser.tagnodes.HTMLParseTree;
import htmlparser.tagnodes.TagNodeMaker;

/**
 * This class actually parses the html and will give a parse tree
 */

public class HTMLParser {

    private HTMLScanner sc;

    /**
     * The constructor
     * @param s the string to make the parser of
     */
    public HTMLParser(String s) {
        sc = new HTMLScanner(s);
    }

    /**
     * Makes a parse tree
     * @return
     * @throws Exception
     */
    public HTMLParseTree parse() throws Exception {
        try {
            return new HTMLParseTree(parse2());
        } finally {
            sc.close();
        }
    }

    /**
     * Helper method for parsing
     * @return A TagNode that represents the root of a parse tree
     * @throws Exception If something is weird with the tags (unmatched stuff)
     */
    private TagNode parse2() throws Exception {
        // if just put opening tag on
        // next node will be nested to the left unless closing
        //
        // closing tag means remove
        // then go next thing is to the right of what was closed
        //
        // if standalone tag
        // go right unless just opened
        //
        // ContentNode
        // treater same as standalone

        Queue<Tag> unmatchedTags = verifyTagsMatch();
        // System.out.println(unmatchedTags.size());
        Stack<TagNode> stack = new Stack<>();
        Token token;
        TagNode node, root = null;
        Tag tag;
        boolean goingLeft = false;

        while ((token = sc.nextToken()) != null) {
            boolean temp;

            if (token instanceof OpeningTag) {
                tag = (Tag) token;
                node = TagNodeMaker.makeNode(tag.getTagName());
                node.setAttributes(tag.getAttributes());
                if (token.equals(unmatchedTags.peek())) {
                    // System.out.println("An unmatched tag was found " + unmatchedTags.remove());
                    unmatchedTags.remove();
                    temp = false;
                } else
                    temp = true;
            } else if (token instanceof ClosingTag) {
                TagNode nested;
                // System.out.println(nested + " " + token + (nested.equals(token)));
                while(!stack.isEmpty()) {
                    nested = stack.peek();
                    if (nested.equals(token)) break;
                    stack.pop();
                }
                goingLeft = false;
                continue;
            } else if (token instanceof StandaloneTag) {
                Tag tmp = (Tag) token;
                node = TagNodeMaker.makeNode(tmp.getTagName());
                node.setAttributes(tmp.getAttributes());
                temp = false;
            } else {
                node = TagNodeMaker.makeNode("content", token.toString());
                temp = false;
            }
            if (!stack.isEmpty()) {
                if (goingLeft)
                    stack.peek().setLeft(node);
                else
                    stack.peek().setRight(node);
            }
            stack.push(node);
            goingLeft = temp;
            if (root == null)
                root = node; // idk
        }
        return root;
    }

    /**
     * Makes sure that the tags are lined up properly
     * @return A queue of unmatched tags which can be considered "empty tags" e.g. <br>
     * @throws Exception If something is weird with the tags
     */
    private Queue<Tag> verifyTagsMatch() throws Exception {
        HTMLScanner sc = this.sc.clone();
        Deque<Tag> stack = new LinkedList<>();
        Queue<Tag> queue = new LinkedList<>();
        Token t;
        while ((t = sc.nextToken()) != null) {
            if (t instanceof OpeningTag) {
                stack.push((Tag) t);
            } else if (t instanceof ClosingTag) {
                Tag s = stack.pop();
                while (!s.equals(t) && !stack.isEmpty()) {
                    queue.add(s);
                    s = stack.pop();
                }
                if (!s.equals(t))
                    throw new Exception(String.format("Could not find LinkNode matching opening tag for %s", t));
            } else {
                // no need to match
            }
        }
        while (!stack.isEmpty())
            queue.add(stack.removeLast()); // stack is actually LinkNode deque
        return queue;
    }

}
