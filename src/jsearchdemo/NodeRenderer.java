package jsearchdemo;
import java.awt.*;
import java.util.*;
import javax.swing.*;

class NodeRenderer extends Renderer implements ThingWithProperties {

    private static final int STRING_OFFSET = 10;

    private static final int HEURISTIC_SPACE = 10;

    static final int RADIUS = 20;

    /* Since CVS-1.4 this gives the CENTER of the node 
       (used to be top-left corner).
       If there's anything wrong with the position of UI-things,
       it's probably because of this change.
    */
    private Point position;

    private NamedHeuristicNode model;

    private GraphRenderer parent;

    void setModel(NamedHeuristicNode nn) {
	model = nn;
    }

    Point getPosition() {
	return position;
    }

    NodeRenderer(NamedHeuristicNode m, GraphRenderer g){
	model = m;
	parent = g;
    }

    /*    NodeRenderer(Point p, GraphRenderer g) {
	parent = g;
	position = p;
	}*/

    void setPosition(Point newP) {
	position = newP;
    }

    boolean containsPoint(Point p) {
	if (position == null)
	    return false;
	return  Math.pow(p.getX() - position.getX(),2) + Math.pow(p.getY() - position.getY(), 2) <= Math.pow(RADIUS,2);
    }

    public void paintComponent(Graphics g) {
	if (position == null)
	    return;
	int x = (int) position.getX();
	int y = (int) position.getY();
	if (parent.inFirstPath(this)) {
	    g.setColor(Color.yellow);
	    g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
	}
	else if (parent.inQueue(this)) {
	    g.setColor(Color.orange);
	    g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
	}
	g.setColor(getColor());
	g.drawOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
	if (parent.isGoalNode(this))
	    g.setColor(Color.green);
	else if (parent.isStartNode(this))
	    g.setColor(Color.blue);
	g.drawString(toString(),x, y);
	if (!parent.getHideHeur())
	    g.drawString(model.getHeuristic() + "", x + RADIUS,y + RADIUS);
    }

    public String toString() {
	return model.toString();
    }

    Node getModel() {
	return model;
    }

    void setName(String s) {
	model.setName(s);
    }

    void setHeuristic(int h) {
	model.setHeuristic(h);
    }

    public Vector getProperties() {
	Vector result = new Vector();
	Property name = new Property() {
		public String get() {
		    return model.toString();
		}
		
		public void set(String s) {
		    model.setName(s);
		    repaint();
		}
		public boolean getEditable() { return true; }
		public String toString() {
		    return "Name";
		}
	    };
	result.add(name);
	Property heur = new Property() {
		public String get() {
		    return (new Integer(model.getHeuristic())).toString();
		}
		
		public void set(String s) {
		    model.setHeuristic(Integer.valueOf(s).intValue());
		    repaint();
		}

		public String toString() {
		    return "Heuristic";
		}
		public boolean getEditable() { return true; }
	    };
	result.add(heur);
	return result;
    }


    // TEMPORARY SOLUTION!
    public void repaint() {
	parent.repaint();
    }
	
}

