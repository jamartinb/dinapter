package jsearchdemo;
import java.awt.event.*;
import java.awt.*;

class NormalState extends State{

    protected Renderer selection;

    NormalState (GraphRenderer gp) {
	super (gp);
    }

    public void mousePressed(MouseEvent m) {
	Renderer source = boss.getThing(new Point(m.getX(),m.getY()));
	if (source != null) {
	    boss.setSelection(source);
	    if (source instanceof NodeRenderer) {
		if (m.getModifiers() == MouseEvent.BUTTON3_MASK) 
		    boss.popup(m.getPoint());
		else boss.drag();
	    }
	}
	else {
	    if (m.getModifiers() == MouseEvent.BUTTON3_MASK) 
		boss.popupEmpty(m);
	}
    }

    public String toString() {
	return "Drag node to move it, click node/edge to change its properties, click a button or right-click on node. ";
    }
}





