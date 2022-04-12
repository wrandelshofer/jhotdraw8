/*
 * @(#)GraphvizReaderTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.io;

import org.jhotdraw8.graph.MutableDirectedGraph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class GraphvizReaderTest {

    @Test
    @Disabled
    void read() throws Exception {
        Path file = Path.of("….dot");
        final MutableDirectedGraph<String, String> g = GraphvizReader.newInstance().read(file);
        System.out.println("#vertex:" + g.getVertexCount());
        System.out.println("#arrow:" + g.getArrowCount());
        /*
        final StronglyConnectedComponentsAlgo sccAlgo = new StronglyConnectedComponentsAlgo();
        final List<List<String>> sccList = sccAlgo.findStronglyConnectedComponents(g);
        System.out.println("#scc:"+sccList.size());
        for (List<String> scc : sccList) {
            if (scc.size()>1||g.isNext(scc.get(0),scc.get(0))){
                System.out.println(" scc.size:"+scc.size());
                System.out.println("  "+scc.stream().collect(Collectors.joining("\",\n  \"","\"","\"")));
            }
        }*/

        /*
        List<String> list = new ArrayList<>(g.getVertices());
        final ToIntFunction<String> extractGroup = str -> Integer.parseInt(str.substring(1 + str.lastIndexOf(" ")));
        list.sort(Comparator.comparingInt(extractGroup));
        int prev = -1;
        for (String str : list) {
            final int group = extractGroup.applyAsInt(str);
            if (group != prev) {
                if (prev != -1) System.out.println("}");
                System.out.println("rank=same{");
            }
            System.out.println(str);
            prev = group;
        }
        System.out.println("}");

         */

    }
}