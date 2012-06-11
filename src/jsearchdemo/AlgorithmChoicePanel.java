package jsearchdemo;
import javax.swing.*; 
import javax.swing.border.*; 
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

class AlgorithmChoicePanel extends JPanel {

    private static final String [] algs = {"DepthFirstAlgorithm", "BreadthFirstAlgorithm", "HillClimbing1Algorithm", "HillClimbing2Algorithm", "AStarAlgorithm", "BeamSearchAlgorithm"};

    // In tenths of a second
    private static final int SLEEP_TIME = 10;

    private ButtonGroup optionsBG;

    private JPanel optionsJP;

    private JButton step;

    private JButton run;

    private JButton reset;

    private GraphRenderer graph;

    private Algorithm selection;

    private Vector stateListeners = new Vector();

    private boolean done;

    private Vector algds;

    // Represents the run-interval in amount of seconds.
    private JSlider interval;

    private StatisticsGatherer sg;

    private javax.swing.Timer timer;

    AlgorithmChoicePanel(GraphRenderer g) {
	sg = new StatisticsGatherer(this);
	graph = g;
	setLayout(new BorderLayout());
	makeAlgs();
	optionsJP = new JPanel();
	optionsJP.setLayout(new GridLayout(algds.size(), 1));
	optionsBG = new ButtonGroup();	
	for (int i = 0; i < algds.size(); i ++) {
	    optionsBG.add(((AlgorithmData) algds.elementAt(i)));
	    optionsJP.add((AlgorithmData) algds.elementAt(i));
	}
	ActionListener stepper = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    if (!done)
			doStep();
		    else {
			stopWorking();
		    }}};
	timer = new javax.swing.Timer(SLEEP_TIME, stepper);
	reset = new JButton("Reset");
	reset.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    reset();
		}});
	step = new JButton("Step");
	step.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    doStep();
		}});
	run = new JButton(" Run ");
	run.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    if (((JButton)a.getSource()).getText().equals("Run"))
			run();
		    else {
			timer.stop();
			run.setText("Run");
			step.setEnabled(true);
		    }
		}});
	interval = new JSlider(0,20,SLEEP_TIME);
	interval.setMajorTickSpacing(5);
	interval.setPaintTicks(true);
	interval.setPaintLabels(true);
	Hashtable labelTable = new Hashtable();
	labelTable.put( new Integer( interval.getMinimum() ), new JLabel("Slow") );
	labelTable.put( new Integer( interval.getMaximum() ), new JLabel("Fast") );
	interval.setLabelTable( labelTable );
	add(optionsJP, BorderLayout.NORTH);
	JPanel buttons = new JPanel();
	buttons.add(step);
	buttons.add(run);
	buttons.add(reset);
	JPanel bottom = new JPanel();
	bottom.setLayout(new BorderLayout());
	bottom.add(buttons, BorderLayout.NORTH);
	bottom.add(interval, BorderLayout.SOUTH);
	add(bottom,BorderLayout.SOUTH);
	((AlgorithmData) algds.elementAt(0)).setSelected(true);
	((AlgorithmData) algds.elementAt(0)).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST,"whatever"));
    }

    void reset() {
	stopWorking();
	findSelectedButton().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST,"only me"));
    }

    public String toString() {
	return selectedB.getText();
    }

    private AlgorithmData selectedB;

    private void setSelectedB(AlgorithmData sel) {
	selectedB = sel;
    }

    private AlgorithmData findSelectedButton() {
	return selectedB;
    }

    private void run() {
	timer.setDelay((interval.getMaximum()-interval.getValue())*100);
	interval.setEnabled(false);
	timer.start();
	run.setText("Stop");
	step.setEnabled(false);
    }

    private void notifyListeners() {

	for (int i = 0; i < stateListeners.size(); i++) {
	    ((StateListener) stateListeners.elementAt(i)).queueChanged(selection);
	}
    }

    private AlgorithmChoicePanel getMe(){
	return this;
    }

    public void addStateListener(StateListener sl) {
	stateListeners.add(sl);
    }

    private void done() {
	stopWorking();
	sg.showLog();
    }

    private void stopWorking() {
	done = true;
	timer.stop();
	interval.setEnabled(true);
	step.setEnabled(false);
	run.setEnabled(false);
    }

    private void startWorking() {
	
	done = false;
	run.setText("Run");
	step.setEnabled(true);
	run.setEnabled(true);
    }

    Queue getQueue() {
	if (selection == null)
	    return null;
	return selection.getQueue();
    }

    /*    private void initializeSelection() {
	String s = optionsBG.getSelection().getActionCommand();
	try {
	    // Such an elegant language, isn't it...
	    selection = (Algorithm) Class.forName(s).getConstructor(new Class[] {Class.forName("AbstractGraph")}).newInstance(new Object [] {graph.getModel()});
	}
	catch (Exception e) {System.out.println(e);}
	notifyListeners();
    }*/

    private void doStep() { 
	if (!done) {
	    if (!selection.finished()) {
		selection.step();
		sg.log(selection.getQueue());
		notifyListeners();
	    }
	    else done();
	    if (selection.finished())
		done();
	}
    }

    private static final int DEFAULT_BEAM =2 ;

    private void makeAlgs() {
	AlgorithmData depth = new AlgorithmData("Depth first", false, false) {
		Algorithm getInstance() {
		    return new DepthFirstAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData breadth = new AlgorithmData("Breadth first", false, false) {
		Algorithm getInstance() {
		    return new BreadthFirstAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData greedy = new AlgorithmData("Greedy search", true, false) {
		Algorithm getInstance() {
		    return new GreedySearchAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData hill1 = new AlgorithmData("Hill climbing 1", true, false) {
		Algorithm getInstance() {
		    return new HillClimbing1Algorithm(graph.getModel());
		}
	    };
	AlgorithmData hill2 = new AlgorithmData("Hill climbing 2", true, false) {
		Algorithm getInstance() {	
		    return new HillClimbing2Algorithm(graph.getModel());
		}
	    };
	AlgorithmData beam = new BeamData("Beam search", true, false);
	AlgorithmData unif = new AlgorithmData("Uniform cost", true, true) {
		Algorithm getInstance() {
		    return new UniformCostAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData eeunif = new AlgorithmData("E.E. uniform cost", true, true) {
		Algorithm getInstance() {
		    return new EEUniformCostAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData idastar = new AlgorithmData("IDA*", true, true) {
		Algorithm getInstance() {
		    return new IDAStarAlgorithm(graph.getModel());
		}
	    };
	AlgorithmData astar = new AlgorithmData("A*", true, true) {
		Algorithm getInstance() {
		    return new AStarAlgorithm(graph.getModel());
		}
	    };
	algds = new Vector();
	algds.add(depth); algds.add(breadth); algds.add(greedy);
	algds.add(hill1); algds.add(hill2); algds.add(beam); 
	algds.add(unif);  algds.add(eeunif);
	algds.add(idastar); algds.add(astar);
    }

    private abstract class AlgorithmData extends JRadioButton implements ActionListener {
	
	protected String name;

	protected boolean needsHeur;

	protected boolean needsCost;

	AlgorithmData (String s, boolean h, boolean c) {
	    name = s;
	    needsHeur = h;
	    needsCost = c;
	    setText(s);
	    addActionListener(this);
	}

	abstract Algorithm getInstance();

	void select() {
	    setSelectedB(this);
	    selection = getInstance();
	    if (needsHeur)
		graph.setHideHeur(false);
	    else graph.setHideHeur(true);
	    if (needsCost) 
		graph.setHideCost(false);
	    else graph.setHideCost(true);
	}


	public void actionPerformed(ActionEvent a) {
	    select();
	    notifyListeners();
	    startWorking();
	}

    }

    private class BeamData extends AlgorithmData {

	int size;

	BeamData(String s, boolean b, boolean c) {
	    super (s, b, c);
	    size = DEFAULT_BEAM;
	    setText(s + " (beam "+size+")");
	}
	
	private BeamWidthPop bp;

	public void actionPerformed(ActionEvent a) {
	    // If we're being called by Remko's damned reset button
	    // we shouldn't bother the user for another beam-width.
	    if (!a.getActionCommand().equals("only me")) {
		bp = new BeamWidthPop(this);
		bp.setInvoker(this);
		bp.setLabel("Beam");
		bp.show(this, 0, 0);
	    }
	    else {
		select();	    
		notifyListeners();
		startWorking();
	    }
	}

	Algorithm getInstance() {
	    return new BeamSearchAlgorithm(graph.getModel(), size);
	}

	void beam(int i) {
	    size = i;
	    setText(name + " (beam "+size+")");
	    select();
	    startWorking();
	}
    }
	    
    private static final int MAX_BEAM_SIZE = 5;

    private class BeamWidthPop extends JPopupMenu {
	
	BeamWidthPop(final BeamData callback) {
	    super ();
	    for (int i = 1; i <= MAX_BEAM_SIZE; i++) {
		JMenuItem jm = new JMenuItem(i+"");
		jm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
			    callback.beam(new Integer(((JMenuItem) a.getSource()).getText()).intValue());
			}});
		add(jm);
	    }
	}
    }
		    

}




