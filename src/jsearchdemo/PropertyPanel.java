package jsearchdemo;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.text.*;

class PropertyPanel extends JPanel {

    private JTextField [] textfields = new JTextField[2];
    
    private JLabel [] lab = new JLabel [2];

    private Vector props;

    private JButton commit;

    private GraphRenderer graph;

    private static final Dimension TEXTFDIM = new Dimension(20,50);

    PropertyPanel(GraphRenderer gr) {
	graph = gr;
	setPreferredSize(new Dimension(50,50));
	// Sniff. My old code was prettier, but sometimes didn't work. Sniff.
	// new
	setLayout(new GridLayout(2,2));
	textfields[0] = new JTextField(5);
	textfields[1] = new JTextField(5);
	lab[0] = new JLabel();	
	lab[1] = new JLabel();	
	add(lab[0]);
	add(textfields[0]);
	add(lab[1]);
	add(textfields[1]);
	textfields[0].setPreferredSize(TEXTFDIM);	
	textfields[1].setPreferredSize(TEXTFDIM);
	textfields[0].setMaximumSize(TEXTFDIM);	
	textfields[1].setMaximumSize(TEXTFDIM);
	// new
	update();
    }

    private DocumentListener dl1;

    private DocumentListener dl2;

    void update() {
	ThingWithProperties show = graph.getSelection();
	if (show != null) {
	    Vector props = show.getProperties();
	    // new
	    lab[0].setText(((Property) props.elementAt(0)).toString());
	    lab[1].setText(((Property) props.elementAt(1)).toString());
	    if (dl1 != null)
		textfields[0].getDocument().removeDocumentListener(dl1);
	    if (dl2 != null)
		textfields[1].getDocument().removeDocumentListener(dl2);
	    dl1 = new PropertyListener((Property) props.elementAt(0));
	    dl2 = new PropertyListener((Property) props.elementAt(1));
	    textfields[0].getDocument().addDocumentListener(dl1);
	    textfields[1].getDocument().addDocumentListener(dl2);
	    textfields[0].setText(((Property) props.elementAt(0)).get());
	    textfields[0].setEditable(((Property) props.elementAt(0)).getEditable());
	    textfields[1].setText(((Property) props.elementAt(1)).get());
	    textfields[1].setEditable(((Property) props.elementAt(1)).getEditable());
	    /*	    setLayout(new GridLayout(2, props.size()));
	    JTextField tf;
	    Property p;
	    JLabel n;
	    for (int i = 0; i < props.size(); i++) {
		p = (Property) props.elementAt(i);
		System.out.println("Proprty " + p);
		n = new JLabel(p.toString());
		tf = new JTextField(3);
		add(n);
		add(tf);
		tf.setText(p.get());
		tf.setEditable(p.getEditable());
		tf.getDocument().addDocumentListener(new PropertyListener(p));
		}*/
	}
	repaint();
    }
	
    class PropertyListener implements DocumentListener {

	private Property prop;

	PropertyListener(Property p) {
	    prop = p;
	}

	public void changedUpdate(DocumentEvent e) {
	    // No idea what this is supposed to do.
	    // So let's ignore it :-)
	}

	public void insertUpdate(DocumentEvent e) {
	    Document doc = e.getDocument();
	    try {
		prop.set(doc.getText(0, doc.getLength()));
	    }
	    catch (BadLocationException b) {
		// Once again, no idea what this is supposed to be.
		// I don't think I like this interface much :-(.
		System.out.println(b);
	    }
	}

	public void removeUpdate(DocumentEvent e){
	}
    }
   
}
