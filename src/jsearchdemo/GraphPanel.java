package jsearchdemo;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.*;
import java.net.URL;

public class GraphPanel extends JPanel implements StateListener {

    private GraphRenderer renderer;

    private JButton newNode;

    private JButton link;

    private JLabel status;

    private JButton del;

    private JButton goal;

    private JButton def;

    private JButton nieuw;

    private PropertyPanel prop;

    private AlgorithmChoicePanel agp;

    public GraphPanel(UndirectedGraph g) {
	renderer = new GraphRenderer(g);
	agp = new AlgorithmChoicePanel(renderer);
	newNode = new JButton("Add node");
	newNode.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.add();
		}});
	link = new JButton("Add edge");
	link.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.link();
		}});
	del = new JButton("Delete");
	del.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.delete();
		}});
	goal = new JButton("Make goal");
	goal.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.makeGoal();
		}});
	JPanel graphbuttons = new JPanel();
	JPanel metabuttons = new JPanel();
	def = new JButton("Default graph");
	def.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.gotoDefault();
		    agp.reset();
		}});
	metabuttons.add(def);
	nieuw = new JButton("New graph");
		nieuw.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    renderer.init(new UndirectedGraph());
		    agp.reset();
		}});
	metabuttons.add(nieuw);
	graphbuttons.add(newNode);
	graphbuttons.add(link);
	graphbuttons.add(del);
	graphbuttons.add(goal);
	JPanel buttons = new JPanel();
	buttons.setLayout(new BorderLayout());
	buttons.add(metabuttons, BorderLayout.SOUTH);
	buttons.add(graphbuttons, BorderLayout.NORTH);
	//	buttons.setBorder(new LineBorder(Color.green));
	JPanel center = new JPanel();
	center.setLayout(new BorderLayout());
	center.add(buttons, BorderLayout.SOUTH);
	//	center.add(metabuttons, BorderLayout.SOUTH);
	center.add(renderer, BorderLayout.CENTER);
	status = new JLabel();
	updateStatus();
	renderer.addStateListener(this);
	prop = new PropertyPanel(renderer);
	QueuePanel qp = new QueuePanel();
	agp.addStateListener(qp);
	agp.addStateListener(renderer);
	JPanel leftWing = new JPanel();
	leftWing.setLayout(new BorderLayout());
	//	prop.setBorder(new LineBorder(Color.red));
	JPanel bottom = new JPanel();
	//	buttons.setBorder(new LineBorder(Color.blue));
	bottom.setLayout(new BorderLayout());
	//bottom.add(qp, BorderLayout.CENTER);
	bottom.add(status, BorderLayout.SOUTH);
	setLayout(new BorderLayout());
	leftWing.add(prop, BorderLayout.NORTH);
	leftWing.add(agp, BorderLayout.SOUTH);
	leftWing.add(new Legend(), BorderLayout.CENTER);
	add(qp, BorderLayout.NORTH);
	add(center, BorderLayout.CENTER);
	add(bottom, BorderLayout.SOUTH);
	add(leftWing, BorderLayout.WEST);
    }


    public void stateChanged() {
	updateStatus();
	prop.update();
    }

    void updateStatus() {
	status.setText(renderer.getStatus());
    }

    public void queueChanged(Algorithm q) {
	// Sorry, don't care.
    }

}









