package jsearchdemo;
import java.awt.*;
import java.util.*;

class EdgeRenderer extends Renderer implements ThingWithProperties {

    static final int CLUMSY_USER_TOLERANCE = 10;

    WeighedEdge model;

    GraphRenderer parent;

    EdgeRenderer(WeighedEdge we, GraphRenderer p) {
	model = we;
	parent = p;
    }
    
    public void paintComponent(Graphics g) {
	g.setColor(getColor());
	if (parent.inFirstPath(this))
	    g.setColor(Color.yellow);	
	else if (parent.inQueue(this))
	    g.setColor(Color.orange);
	try {
	    NodeRenderer source = parent.getNodeRenderer(model.getBeginNode());
	    NodeRenderer destination = parent.getNodeRenderer(model.getEndNode());
	    int x1,x2,y1,y2;
	    x1 = (int) Math.round(source.getPosition().getX());
	    y1 = (int) Math.round(source.getPosition().getY());
	    x2 = (int) Math.round(destination.getPosition().getX());
	    y2 = (int) Math.round(destination.getPosition().getY()); 
	    g.drawLine(x1, y1, x2, y2);
	    if (!parent.getHideCost())
		g.drawString(model.getWeight()+"", x1 - (x1 - x2) / 2, y1 - (y1 - y2) /2);
	}
	catch (NullPointerException n) {
	    model = null;
	    parent.kill(this);
	}
	
    }

    void setCost(int c) {
	model.setWeight(c);
    }


    /* David says:
       All the points on the line between (x,y) and (u,v) can be written as
       (x + n(u - x), y + n(v - y)) for n between 0 and 1
       Therfore, to check whether there is a point of the line 
       with horizontal distance to point (a,b) < H, we check whether
       for m = (b - y) / (v - y) it holds that x + m (u - x) - b < H.
       Let's hope he's right ;-).
    */
    boolean containsPoint(Point p) {
	double x, y, u, v, a, b;
	Point beg = parent.getNodeRenderer(model.getBeginNode()).getPosition();
	Point end = parent.getNodeRenderer(model.getEndNode()).getPosition();
	x = beg.getX();
	y = beg.getY();
	u = end.getX();
	v = end.getY();
	a = p.getX();
	b = p.getY();
	double m = (b - y) / (v - y);
	if (Math.abs(x + m * (u - x) - a) <= CLUMSY_USER_TOLERANCE)
	    return true;
	m = (a - x) / (u - x);
	if (Math.abs(y + m * (u - y) - b) <= CLUMSY_USER_TOLERANCE)
	    return true;
	return false;
    }
    
    public Vector getProperties() {
	Vector result = new Vector();
	Property name = new Property() {
		public String get() {
		    return model.toString();
		}
		
		public void set(String s) {
		}
		public boolean getEditable() { return false; }
		public String toString() {
		    return "Name";
		}
	    };
	Property cost = new Property() {
		public String get() {
		    return (new Integer(model.getWeight())).toString();
		}
		public void set(String s) {
		    model.setWeight(Integer.valueOf(s).intValue());
		    repaint();
		}

		public String toString() {
		    return "Cost";
		}
		public boolean getEditable() { return true; }
	    };
	result.add(name);
	result.add(cost);
	return result;
    }

    public WeighedEdge getModel() {
	return model;
    }

    public String toString() {
	return getModel().toString();
    }

    // TEMPORARY SOLUTION!
    public void repaint() {
	parent.repaint();
    }
}







