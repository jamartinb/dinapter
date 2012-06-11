package jsearchdemo;
import javax.swing.*;
import java.awt.*;

class QueuePanel extends JPanel implements StateListener {

    private JLabel title;

    //    private JTextField q;

    private JEditorPane q;

    private AlgorithmChoicePanel source;

    private static final Dimension dim = new Dimension(650,40);

    private static final Dimension epSize = new Dimension(1500,30);

    QueuePanel() {
	title = new JLabel("Queue:");
	q = new JEditorPane();
	q.setContentType( "text/html");
	q.setEditable(false);
	q.setSize(epSize);
	add(title);
	JScrollPane jsp = new JScrollPane(q);
	jsp.setPreferredSize(dim);
	add(jsp);
	//	jsp.setHorizontalScrollBarPolicy(jsp.HORIZONTAL_SCROLLBAR_ALWAYS);
	//jsp.setVerticalScrollBarPolicy(jsp.VERTICAL_SCROLLBAR_NEVER);
	// q.setPreferredSize(dim);
	// q.setMinimumSize(dim);
	// q.setMaximumSize(dim);
	//add(q);
    }

    public void stateChanged() {
	// Don't care.
    }

    public void queueChanged(Algorithm q) {
	update(q);
    }

    private void update(Algorithm newQ) {
	if (newQ == null)
	    q.setText("");
	else q.setText(newQ.getStateString());
	repaint();
    }
}
