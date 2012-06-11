package jsearchdemo;
import java.applet.*;

public class LookMommyImAnApplet extends Applet {

    public LookMommyImAnApplet() {
    }

    public void init() {
	UndirectedGraph ug = new UndirectedGraph();
	GraphPanel gp = new GraphPanel(ug);
	add(gp);
    }

}
	
