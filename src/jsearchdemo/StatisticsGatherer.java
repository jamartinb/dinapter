package jsearchdemo;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class StatisticsGatherer {

    private int maxQFill;

    private int nbSteps;

    private JDialog dialog;

    private AlgorithmChoicePanel agp;

    public StatisticsGatherer (AlgorithmChoicePanel a) {
	agp = a;
	nbSteps = 0;
	maxQFill = 0;
    }

    public void log(Queue q) {
	int newSize = q.getSize();
	if (newSize > maxQFill)
	    maxQFill = newSize;
	nbSteps++;
    }
	
    public void showLog() {/*
	//	if (dialog != null)
	//    dialog.dispose();
	final JDialog jd = new JDialog();
	Container content = jd.getContentPane();
	content.setLayout(new GridLayout(2,2));
	content.add(new JLabel("Maximum elements in Q:"));
	content.add(new JLabel(""+maxQFill));
	content.add(new JLabel("Number of steps:"));
	content.add(new JLabel(""+nbSteps));
	jd.setSize(500,150);
	jd.setVisible(true);
	jd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	maxQFill = 0;
	nbSteps = 0;
	//	dialog = jd;
	JButton ok = new JButton("OK");
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    jd.dispose();
		}});
		jd.setTitle(agp.toString());*/
    }
	

}
