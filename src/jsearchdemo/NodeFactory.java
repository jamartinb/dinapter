package jsearchdemo;
import java.text.*;

class NodeFactory {

    private static final int DEFAULT_HEURISTIC = 0;

    private NodeRenderer toAdd;

    private String front = "";

    private String name;

    private boolean first = true;

    private final CharacterIterator seq = new StringCharacterIterator("SABCDEFHIJKLMNOPQRTUVWXYZ");

    private GraphRenderer graph;

    NodeFactory(GraphRenderer gr) {
	graph = gr;
    }

    NodeRenderer newNode() {
	NamedHeuristicNode nhn = new NamedHeuristicNode(getNewName(), DEFAULT_HEURISTIC);
	NodeRenderer nr = new NodeRenderer(nhn, graph);
	return nr;
    }
    
    private String getNewName() {
	if (first) {
	    first = false;
	    return ""+ seq.first();
	}
	char next = seq.next();
	if (next == CharacterIterator.DONE) {
	    front += seq.last();
	    seq.first();
	    return getNewName();
	}
	return front + next;
    }



}








