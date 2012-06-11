package jsearchdemo;
import java.awt.event.*;

abstract class State extends IgnoreAll {
    
    protected GraphRenderer boss;
    
    State (GraphRenderer gp) {
	boss = gp;
    }

    public void mouseClicked(MouseEvent m) {
	boss.gotoStartState();
    }
    
}






