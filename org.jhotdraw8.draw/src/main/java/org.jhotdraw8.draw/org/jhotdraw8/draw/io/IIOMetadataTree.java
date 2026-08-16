package org.jhotdraw8.draw.io;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.fxbase.tree.PreorderSpliterator;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadataNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IIOMetadataTree {
    public Enumerator<Node> preorderSpliterator(Node root) {
        return new PreorderSpliterator<>(n -> (Iterable<Node>) () -> new Iterator<Node>() {
            Node next = n.getFirstChild();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Node next() {
                Node result = next;
                next = next.getNextSibling();
                return result;
            }
        }, root);

    }

    public int getDepth(Node node) {
        int depth = 0;
        for (Node p = node.getParentNode(); p != null; p = p.getParentNode()) {
            depth++;
        }
        return depth;
    }

    public Node findFirstNodeByName(Node root, String name) {
        Enumerator<Node> it = preorderSpliterator(root);
        while (it.moveNext()) {
            Node n = it.current();
            if (Objects.equals(name, n.getNodeName())) {
                return n;
            }
        }
        return null;
    }

    public List<Node> findAllNodesByName(Node root, String name) {
        var result = new ArrayList<Node>();
        Enumerator<Node> it = preorderSpliterator(root);
        while (it.moveNext()) {
            Node n = it.current();
            if (Objects.equals(name, n.getNodeName())) {
                result.add(n);
            }
        }
        return result;
    }

    public String printTree(Node root) {
        StringBuilder buf = new StringBuilder();
        Enumerator<Node> it = preorderSpliterator(root);
        while (it.moveNext()) {
            Node n = it.current();
            int depth = getDepth(n);
            buf.repeat('.', depth);
            buf.append("<").append(n.getNodeName()).append(" ").append("\n");
            NamedNodeMap attr = n.getAttributes();
            for (int i = 0, attrLength = attr.getLength(); i < attrLength; i++) {
                Node item = attr.item(i);
                buf.repeat(' ', depth);
                buf.append("  ").append(item.getNodeName()).append("=").append(item.getNodeValue());
                buf.append('\n');
            }
            if (n instanceof IIOMetadataNode iitem) {
                buf.repeat(' ', depth);
                buf.append("  userObject=").append(iitem.getUserObject()).append('\n');
            }
            buf.repeat(' ', depth);
            buf.append(">\n");
        }
        return buf.toString();
    }


}

