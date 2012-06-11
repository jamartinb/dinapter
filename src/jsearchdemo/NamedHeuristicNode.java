package jsearchdemo;
public class NamedHeuristicNode extends NamedNode implements HeuristicNode
{
    private int _heuristic;

    public NamedHeuristicNode(String name, int heuristic) {
        super(name);
        _heuristic = heuristic;
    }

    public int getHeuristic() {
        return _heuristic;
    }

    public void setHeuristic(int h) {
	_heuristic = h;
    }
}


