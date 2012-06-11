package jsearchdemo;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

class Legend extends JPanel {

    Legend() {
	JTextField start = new JTextField("Start node");
	JTextField goal = new JTextField("Goal node");
	JTextField queue = new JTextField("Path in Q");
	JTextField first = new JTextField("First path in Q");
	JTextField sel = new JTextField("Selected node/edge");
	start.setEnabled(false);
	goal.setEnabled(false);
	sel.setEnabled(false);
	first.setEnabled(false);
	queue.setEnabled(false);
	sel.setDisabledTextColor(Color.red);
	start.setDisabledTextColor(Color.blue);
	goal.setDisabledTextColor(Color.green);
	queue.setDisabledTextColor(Color.orange);
	first.setDisabledTextColor(Color.yellow);
	sel.setBackground(Color.gray);
	start.setBackground(Color.gray);
	goal.setBackground(Color.gray);
	queue.setBackground(Color.gray);
	first.setBackground(Color.gray);
	setLayout(new GridLayout(5,1));
	add(sel);
	add(start);
	add(goal);
	add(queue);
	add(first);
	setBorder(new TitledBorder("Legend"));
    }
}
