package jsearchdemo;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.applet.*;
import java.net.URL;

class GraphRenderer extends JPanel implements StateListener {
    
    private static final int SPACE_FOR_NODE = 20;

    private static final int DEFAULT_WEIGHT = 1;

    private static final int DEFAULT_HEURISTIC = 0;

    private UndirectedGraph source;

    private Vector nodes = new Vector();

    private Vector edges = new Vector();

    private State state;

    private StateFactory sFactory;

    private Vector stateListeners = new Vector();

    private Renderer selection;

    private NodeFactory nodeFactory;

    private Vector stateTrail = new Vector();

    private Queue queue;

    /* If false this locks down everything until enable().
       Currently, this is only used to prevent nodes being added
       without a position being given to them.
    */
    private boolean enabled = true;

    GraphRenderer(UndirectedGraph g) {
	source = g;
	addMouseListener(new PassOnListener());
	addMouseMotionListener(new PassOnListener());
	sFactory = new StateFactory(this);
	gotoState(sFactory.getStartState());
	setBorder(new LineBorder(Color.blue));
	init(g);
    }
    
    /* g should be a new AbstractGraph!
     */
    void init(UndirectedGraph g) {
	source = g;
	nodes = new Vector();
	edges = new Vector();
	nodeFactory = new NodeFactory(this);
	NodeRenderer start = nodeFactory.newNode();
	source.setStartNode(start.getModel());
	addNode(start);
	setSelection(start);
	moveTo(new Point(25,25));
	source.setStartNode(start.getModel());
	repaint();
    }


    EdgeRenderer edge(NodeRenderer from, NodeRenderer to) {
	EdgeRenderer e;
	Node f = from.getModel();
	Node t = to.getModel();
	for (int i = 0; i < edges.size(); i ++) {
	    e = (EdgeRenderer) edges.elementAt(i);
	    if ( ( e.getModel().getBeginNode().equals(f) &&
		   e.getModel().getEndNode().equals(t)  ) ||
		 ( e.getModel().getBeginNode().equals(t) &&
		   e.getModel().getEndNode().equals(f)  ))
		return e;
	}
	return null;
    }

    void gotoDefault() {
	init(new UndirectedGraph());
	moveTo(new Point(25, 150));
	NodeRenderer s = (NodeRenderer) selection;
	NodeRenderer a = nodeFactory.newNode();
	addNode(a);
	setSelection(a);
	moveTo(new Point(100,50));
	NodeRenderer b = nodeFactory.newNode();
	addNode(b);
	setSelection(b);
	moveTo(new Point(100,250));
	NodeRenderer c = nodeFactory.newNode();
	addNode(c);
	setSelection(c);
	moveTo(new Point(250,50));
	NodeRenderer d = nodeFactory.newNode();
	addNode(d);
	setSelection(d);
	moveTo(new Point(250,250));
	NodeRenderer e = nodeFactory.newNode();
	addNode(e);
	setSelection(e);
	moveTo(new Point(325,75));
	NodeRenderer f = nodeFactory.newNode();
	addNode(f);
	setSelection(f);
	moveTo(new Point(400,250));
	NodeRenderer g = nodeFactory.newNode();
	addNode(g);
	g.setName("G");
	setSelection(g);
	moveTo(new Point(150,150));
	NodeRenderer h = nodeFactory.newNode();
	addNode(h);
	h.setName("H");
	setSelection(h);
	moveTo(new Point(325,225));
	s.setHeuristic(12);
	a.setHeuristic(5);
	b.setHeuristic(5);
	c.setHeuristic(5);
	d.setHeuristic(2);
	e.setHeuristic(2);
	f.setHeuristic(1);
	h.setHeuristic(1);
	g.setHeuristic(0);
	link(s, a);
	link(s, b);
	link(a, g);
	link(a, c);
	link(b, g);
	link(b, d);
	link(g, c);
	link(g, e);
	link(g, d);
	link(d, h);
	link(h, f);
	link(c, e);
	edge(s,a).setCost(10);
	edge(a,g).setCost(10);
	edge(s,b).setCost(8);
	edge(b,g).setCost(16);
	edge(b,d).setCost(8);
	edge(g,c).setCost(9);
	edge(a,c).setCost(2);
	edge(c,e).setCost(3);
	edge(e,g).setCost(2);
	edge(g,d).setCost(3);
	setSelection(g);
	makeGoal();
    }
    public void addStateListener(StateListener sl) {
	stateListeners.add(sl);
    }

    public void add() {
	if (!enabled)
	    return;
	NodeRenderer adding = nodeFactory.newNode();
	addNode(adding);
	setSelection(adding);
	gotoState(sFactory.getAddingNodeState());
	// Node has to be given a position before anything else can be done!
	// Otherwise we end up with ghost-nodes, which is baaaaaaaaad!
	enabled = false;
    }

    public void link(){
	gotoStartState();
	gotoState(sFactory.getLinkingState());
    }

    public void delete() {
	if (!enabled)
	    return;
	gotoStartState();
	if (selection instanceof NodeRenderer) 
	    removeNode((NodeRenderer) selection);
	else removeEdge((EdgeRenderer) selection);
    }

    void popupEmpty(final MouseEvent m) {
	JPopupMenu pop = new JPopupMenu();
	JMenuItem newN = new JMenuItem("New node");
	newN.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    add();
		    getState().mouseClicked(m);
		}});
	pop.add(newN);
	Point p = m.getPoint();
	pop.show(this, (int) p.getX(), (int) p.getY());
    }

    void popup(Point p) {
	JPopupMenu pop = new JPopupMenu();
	JMenuItem link = new JMenuItem("Link");
	link.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    link();
		}});
	JMenuItem delete = new JMenuItem("Delete");
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    delete();
		}});
	JMenuItem goal = new JMenuItem("Make goal");
	goal.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    makeGoal();
		}});
	pop.add(goal);
	pop.add(link);
	pop.add(delete);
	pop.show(this, (int) p.getX(), (int) p.getY());
    }

    void drag() {
	gotoStartState();
	gotoState(sFactory.getDraggingState());
    }

    /* Currently, only AddingNodeState is supposed to use this!
       (See "private boolean enabled" for explanation.)
       (BTW, the names enable() and setEnabled() are taken by JComponent.)
     */
    void reenable() {
	enabled = true;
    }

    void makeGoal() {
	if (selection instanceof NodeRenderer) {
	    source.setGoalNode(((NodeRenderer) selection).getModel());
	    repaint();
	}
    }
    boolean isStartNode(NodeRenderer n) {
	if (n.getModel() == null)
	    return false;
	return n.getModel().equals(source.getStart());
    }
	
    boolean isGoalNode(NodeRenderer n) {
	if (n.getModel() == null)
	    return false;
	return n.getModel().equals(source.getGoal());
    }

    boolean inQueue(NodeRenderer n) {
	if (queue == null)
	    return false;
	if (queue.contains(n.getModel()))
	    return true;
	return false;
    }


    boolean inQueue(EdgeRenderer e) {
	if (queue == null)
	    return false;
	if (queue.contains(e.getModel()))
	    return true;
	return false;
    }

    boolean inFirstPath(NodeRenderer n) {
	if (queue == null)
	    return false;
	if (queue.isEmpty())
	    return false;
	if (queue.getFirst().contains(n.getModel()))
	    return true;
	return false;
    }

    boolean inFirstPath(EdgeRenderer e) {
	if (queue == null)
	    return false;
	if (queue.isEmpty())
	    return false;
	if (queue.getFirst().getEdges().contains(e.getModel()))
	    return true;
	return false;
    }

    void setSelection(Renderer s){
	if (selection != null)
	    selection.setSelected(false);
	selection = s;
	if (selection != null)
	    selection.setSelected(true);
	repaint();
	notifyListeners();
    }

    UndirectedGraph getModel() {
	return source;
    }

    Renderer getSelection() {
	return selection;
    }

    void linkTo(NodeRenderer target) {
	if (selection != null && selection instanceof NodeRenderer &&
	    target != null && target instanceof NodeRenderer &&
	    selection != target)
	link((NodeRenderer)selection, target);
    }


    private void link(NodeRenderer from, NodeRenderer to) {
	WeighedEdge we = new DefaultWeighedEdge(from.getModel(), to.getModel(), DEFAULT_WEIGHT);
	edges.add(new EdgeRenderer(we, this));
	source.addEdge(we);
	repaint();
    }

    Vector getEdges() {
	return edges;
    }
    
    private void notifyListeners() {
	for (int i = 0; i < stateListeners.size(); i++) {
	    ((StateListener) stateListeners.elementAt(i)).stateChanged();
	}
    }

    void gotoState(State s) {
	if (!enabled)
	    return;
	stateTrail.add(s);
	notifyListeners();
	repaint();
    }

    State getState() {
	return (State) stateTrail.lastElement();
    }
    
    void moveTo(Point p) {
	if (getSelection() instanceof NodeRenderer) {
	    ((NodeRenderer) getSelection()).setPosition(p);
	    repaint();
	}
    }

    void gotoStartState() {
	if (!enabled)
	    return;
	State first = (State) stateTrail.firstElement();
	stateTrail.removeAllElements();
	gotoState(first);
    }

    void gotoPreviousState() {
	if (!enabled)
	    return;
	/*	stateTrail.removeElementAt(stateTrail.size() - 1);
		gotoState((State) stateTrail.lastElement());*/
	gotoStartState();
    }


    Renderer getThing(Point p) {
	NodeRenderer n = getNode(p);
	if (n != null)
	    return n;
	EdgeRenderer e = getEdge(p);
	if (e != null)
	    return e;
	return null;
    }

    
    EdgeRenderer getEdge(Point p){
	Vector edges = getEdges();
	EdgeRenderer current;
	for (int i = 0; i < edges.size(); i++) {
	    current = (EdgeRenderer) edges.elementAt(i);
	    if (current.containsPoint(p))
		return current;
	}
	return null;
    }

    NodeRenderer getNode(Point p) {
	Iterator i = nodes.iterator();
	double minDist = SPACE_FOR_NODE;
	double dist;
	NodeRenderer current;
	Point currentP;
	while (i.hasNext()) {
	    current = (NodeRenderer) i.next();
	    if (current.containsPoint(p))
		return current;
	}
	return null;
    }

    // ??????????????????????????
    Point getEmpty(Point p){
	NodeRenderer closestN = getNode(p);
	    if (closestN != null) {
		Point closest = closestN.getPosition();
		if (Math.abs(p.getX() - closest.getX()) < SPACE_FOR_NODE ||
		    Math.abs(p.getY() - closest.getY()) < SPACE_FOR_NODE)
		return null;
	    }
	return p;
    }

    void addNode(NodeRenderer n){
	nodes.add(n);
    }
    
    void startAddingNewNode() {
	setSelection(nodeFactory.newNode());
	gotoState(sFactory.getAddingNodeState());
    }

    void removeNode(NodeRenderer n) {
	if (!isStartNode(n)) {
	    source.removeNode(n.getModel());
	    nodes.removeElement(n);
	    repaint();
	}
    }

    void removeEdge(EdgeRenderer e) {
	edges.removeElement(e);
	source.removeEdge(e.getModel());
	repaint();
    }

    private boolean hideCost = false;
    private boolean hideHeur = false;

    void setHideCost(boolean h) {
	hideCost = h;
	repaint();
    }

    boolean getHideCost() {
	return hideCost;
    }

    void setHideHeur(boolean h) {
	hideHeur = h;
	repaint();
    }


    boolean getHideHeur() {
	return hideHeur;
    }

    NodeRenderer getNodeRenderer(Node n) {
	Iterator i = nodes.iterator();
	NodeRenderer checking;
	while (i.hasNext()) {
	    checking = (NodeRenderer) i.next();
	    if (checking.getModel().equals(n))
		return checking;
	}
	throw new NullPointerException("Joost says this cannot happen.");
    }
	    

    /* This is a self-destruct method for EdgeRenderers,
       in case they notice the edge they're drawing
       has been removed from the original model.
       No-one else should use it.
    */
    void kill(EdgeRenderer e) {
	edges.removeElement(e);
	// Edgerenderers call this method when trying to paint themselves.
	// Unfortunately, at that time the GraphRenderer is in the middle of a
	// "for (int j = 0; j < edges.size(); j++)"-loop, 
	// which obviously gets fucked up. Hence, another repaint is needed.
	// (Took me a while to figure this one out. Serves me right 
	// for not using that new Container stuff, I suppose.) 
	repaint();
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Iterator i = nodes.iterator();
	NodeRenderer painting;
	while (i.hasNext()) {
	    painting = (NodeRenderer) i.next();
	    painting.paintComponent(g);
	}
	Vector edges = getEdges();
	EdgeRenderer current;
	for (int j = 0; j < edges.size(); j++) {
	    current = (EdgeRenderer) edges.elementAt(j);
	    current.paintComponent(g);
	}

    }

    public String getStatus() {
	return getState().toString();
    }

    public void queueChanged(Algorithm a) {
	queue = a.getQueue();
	repaint();
    }

    public void stateChanged() {
	// Don't care.
    }

    public static void main (String argv[]) {
	JFrame jf = new JFrame();
	jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	GraphPanel gr = new GraphPanel(new UndirectedGraph());
	jf.getContentPane().add(gr);
	jf.setSize(800,800);
	jf.setVisible(true);
    }

    class PassOnListener implements MouseListener, MouseMotionListener, ActionListener {
	public void mouseClicked(MouseEvent e) {
	    getState().mouseClicked(e);
	}
	public void mousePressed(MouseEvent e) {
	    getState().mousePressed(e);
	}
	public void mouseEntered(MouseEvent e) {  
	    getState().mouseEntered(e);
	}    
	public void mouseExited(MouseEvent e) {   
	    getState().mouseExited(e);
	}
	public void mouseReleased(MouseEvent e) { 
	    getState().mouseReleased(e);
	}
	public void mouseMoved(MouseEvent e) {    
	    getState().mouseMoved(e);
	}    
	public void mouseDragged(MouseEvent e) {  
	    getState().mouseDragged(e);
	}
	public void actionPerformed(ActionEvent a) {
	    getState().actionPerformed(a);
	}
    }

}




