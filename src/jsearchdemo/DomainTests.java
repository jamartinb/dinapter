package jsearchdemo;
public class DomainTests
{
    public static void main(String argv[]) {
        //testFFGG();
        testAlgorithms();
        testGraph();
    }


    public static void testFFGG() {
        FFGGGraph g = new FFGGGraph();
        DepthFirstAlgorithm a = new DepthFirstAlgorithm(g);
        System.out.println(a.getStateString());
        while (!a.finished()) {
            a.step();
            System.out.println(a.getStateString());
        }
    }

    public static void testAlgorithms() {
        UndirectedGraph g = new UndirectedGraph();
        Node S = new NamedHeuristicNode("S",0);
        Node E = new NamedHeuristicNode("E",0);
        Node B = new NamedHeuristicNode("B",4);
        Node C = new NamedHeuristicNode("C",3);
        Node D = new NamedHeuristicNode("D",0);
        Node G = new NamedHeuristicNode("G",0);
        g.addEdge(new DefaultWeighedEdge(S,C,9));
        g.addEdge(new DefaultWeighedEdge(S,B,8));
        g.addEdge(new DefaultWeighedEdge(S,E,10));
        g.addEdge(new DefaultWeighedEdge(D,B,4));
        g.addEdge(new DefaultWeighedEdge(B,G,5));
        g.addEdge(new DefaultWeighedEdge(E,D,1));
        g.addEdge(new DefaultWeighedEdge(C,G,5));
        g.addEdge(new DefaultWeighedEdge(S,G,3));
        g.removeEdge(new DefaultEdge(S,G));
        g.removeEdge(new DefaultEdge(S,G));
        g.setStartNode(S);
        g.setGoalNode(G);
        System.out.println(g);
        
        System.out.println("Depth First:");
        DepthFirstAlgorithm df = new DepthFirstAlgorithm(g);
        System.out.println(df.getStateString());
        while (!df.finished()) {
            df.step();
            System.out.println(df.getStateString());
        }
        System.out.println();
        
        System.out.println("Breadth First:");
        BreadthFirstAlgorithm bf = new BreadthFirstAlgorithm(g);
        System.out.println(bf.getStateString());
        while (!bf.finished()) {
            bf.step();
            System.out.println(bf.getStateString());
        }
        System.out.println();
        
        System.out.println("HillClimbing1:");
        HillClimbing1Algorithm hc1 = new HillClimbing1Algorithm(g);
        System.out.println(hc1.getStateString());
        while (!hc1.finished()) {
            hc1.step();
            System.out.println(hc1.getStateString());
        }
        System.out.println();
        
        System.out.println("Beam Search:");
        BeamSearchAlgorithm bs = new BeamSearchAlgorithm(g,2);
        System.out.println(bs.getStateString());
        while (!bs.finished()) {
            bs.step();
            System.out.println(bs.getStateString());
        }
        System.out.println();
        
        System.out.println("Hill Climbing 2:");
        HillClimbing2Algorithm hc2 = new HillClimbing2Algorithm(g);
        System.out.println(hc2.getStateString());
        while (!hc2.finished()) {
            hc2.step();
            System.out.println(hc2.getStateString());
        }
        System.out.println();
        
        System.out.println("Greedy Search:");
        GreedySearchAlgorithm gs = new GreedySearchAlgorithm(g);
        System.out.println(gs.getStateString());
        while (!gs.finished()) {
            gs.step();
            System.out.println(gs.getStateString());
        }
        System.out.println();
        
        System.out.println("Uniform Cost:");
        UniformCostAlgorithm uc = new UniformCostAlgorithm(g);
        System.out.println(uc.getStateString());
        while (!uc.finished()) {
            uc.step();
            System.out.println(uc.getStateString());
        }
        System.out.println();
        
        System.out.println("A*:");
        AStarAlgorithm as = new AStarAlgorithm(g);
        System.out.println(as.getStateString());
        while (!as.finished()) {
            as.step();
            System.out.println(as.getStateString());
        }
        System.out.println();
        
        System.out.println("IDA*:");
        IDAStarAlgorithm idas = new IDAStarAlgorithm(g);
        System.out.println(idas.getStateString());
        while (!idas.finished()) {
            idas.step();
            System.out.println(idas.getStateString());
        }
        System.out.println();
    }

    public static void testGraph() {
        UndirectedGraph g = new UndirectedGraph();
        Node S = new NamedNode("S");        
        Node A = new NamedNode("A");        
        Node B = new NamedNode("B");        
        Node C = new NamedNode("C");        
        Node D = new NamedNode("D");        
        Node E = new NamedNode("E");        
        g.addEdge(new DefaultEdge(S,A));
        g.addEdge(new DefaultEdge(A,B));
        g.addEdge(new DefaultEdge(A,C));
        g.addEdge(new DefaultEdge(B,E));
        g.addEdge(new DefaultEdge(B,D));

        /* Test removeNode */
        g.removeNode(B);
        if (!g.contains(S) || !g.contains(A) || !g.contains(C) ||
            g.contains(B) || g.contains(E) || g.contains(D)) 
            System.out.println("removeNode() FAILED!");
    }

}
