package org.jhotdraw8.icollection.alt.impl.bmtrie;

public class GraphvizBitMappedTrie {
    public <T> String toGraphviz(BitMappedTrie<T> trie) {
        var b = new StringBuilder();
        b.append("strict digraph G {\n");
        b.append(toNodeId("root")).append("[").append(toBoxLabel("root")).append("]\n");
        toGraphviz("root", trie, b);
        b.append("}\n");
        return b.toString();
    }

    private <T> void toGraphviz(Object parent, BitMappedTrie<T> node, StringBuilder b) {
        b.append(toNodeId(node)).append("[").append(toBoxLabel(node)).append("]\n");
        b.append(toNodeId(parent)).append(" -> ").append(toNodeId(node)).append("\n");
        toArray(node, node.array, b);
    }

    private <T> void toArray(Object parent, Object node, StringBuilder b) {
        if (node instanceof Object[] a && a.length > 0 && a[0] instanceof Object[]) {
            for (int i = 0; i < a.length; i++) {
                Object ai = a[i];
                b.append(toNodeId(ai)).append("[").append(toBoxLabel(ai)).append("]\n");
                b.append(toNodeId(parent)).append(" -> ").append(toNodeId(ai)).append("\n");
            }
        } else if (node instanceof Object[] a) {
            b.append(toNodeId(node)).append("[").append(toBoxLabel(node)).append("]\n");
            b.append(toNodeId(parent)).append(" -> ").append(toNodeId(node)).append("\n");
        } else {
            b.append(toNodeId(node)).append("[").append(toBoxLabel(node)).append("]\n");
            b.append(toNodeId(parent)).append(" -> ").append(toNodeId(node)).append("\n");
        }

    }

    private <T> void toArrayElement(Object parent, T node, StringBuilder b) {
        b.append(toNodeId(node)).append("[").append(toBoxLabel(node)).append("]\n");
        b.append(toNodeId(parent)).append(" -> ").append(toNodeId(node)).append("\n");

    }


    private static String toNodeId(Object o) {
        String s = o == null ? "root" : o.getClass().getSimpleName() + "_" + Integer.toHexString(System.identityHashCode(o));
        return s
                .replaceAll("\\[]", "Array");
    }

    private static String toBoxLabel(Object o) {

        String label = toLabel(o);
        //label = label.replaceAll("[\\[\\]]", "&lt;");
        return "shape=\"box\",label=\"" + label + "\"";
    }

    private static String toLabel(Object o) {
        if (o == null) return "null";
        String label = switch (o) {
            case Object[] i when i.length == 0 -> "[]";
            case Object[] i -> "[" + toLabel(i[0]) + ".." + toLabel(i[i.length - 1]) + "]";
            case Integer i -> i.toString();
            case String s -> s;
            case BitMappedTrie<?> t ->
                    "BitMappedTrie offset=" + t.offset + " length=" + t.length + " shift=" + t.depthShift;
            default -> o.getClass().getSimpleName();
        };
        //label = label.replaceAll("[\\[\\]]", "&lt;");
        return label;
    }

}
