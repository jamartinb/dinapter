package jsearchdemo;
import java.awt.Color;

abstract class Renderer implements ThingWithProperties {

    private boolean selected;

    void setSelected(boolean s) {
	selected = s;
    }

    boolean getSelected() {
	return selected;
    }

    Color getColor() {
	if (selected)
	    return Color.red;
	else return Color.black;
    }
}



