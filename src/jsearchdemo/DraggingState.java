package jsearchdemo;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

class DraggingState extends NormalState {

    DraggingState (GraphRenderer gp) {
	super (gp);
    }

    public void mouseReleased(MouseEvent e) {
	boss.gotoPreviousState();
    }

    public void mouseDragged(MouseEvent e) {
	boss.moveTo(e.getPoint());
    }

    public String toString() {
	return "Release button to drop node.";
    }
}


