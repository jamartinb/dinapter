/*
 * This file is part of Dinapter.
 *
 *  Dinapter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Dinapter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  (C) Copyright 2007 José Antonio Martín Baena
 *  
 *  José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
 *  Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
 */
package dinapter;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import jess.Console;
import net.sourceforge.jpowergraph.Edge;

import org.apache.log4j.Logger;

import data.FTPExample;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.bpelj.BPELJBehaviorProvider;
import dinapter.specificator.BridgeSpecificatorGraph;
import dinapter.specificator.DefaultRule;
import dinapter.specificator.JSearchSpecificator;
import dinapter.specificator.MapSpecificatorBuilder;
import dinapter.specificator.SimpleSpecification;
import dinapter.specificator.util.SpecificationGraphToJPowerGraph;
import dinapter.specificator.util.SpecificationTrace;

import jess.Value;
import jess.ValueVector;
import jess.Fact;
import jess.RU;
import jess.JessException;

public class Dinapter {
	
	private static final String SHOW_COMPONENT_BEHAVIORS_FLAG = "-v";

	private static final String SUPPORTED_EXTENSION = ".*\\.bpelj?";

	public static final String ENABLE_SPECIFICATION_GRAPH_DISPLAY = "ENABLE_SPECIFICATION_GRAPH_DISPLAY";

	public static final String DINAPTER_PROPERTIES = "dinapter.properties";
    
    private static final Logger log = Logger.getLogger(Dinapter.class);
    
    private static final Logger statistics = Logger.getLogger("dinapter.Dinapter.statistics");
    
    private static final Logger solutions = Logger.getLogger("dinapter.Dinapter.solutions");
    
    public static final Properties properties = new Properties();
    
    private static final String MODIFIED_PROPERTIES;
    
    private static String directoryLoaded = null;
    
    static {
    	// ****** Load the configuration properties *******
    	try {
    		properties.loadFromXML(Dinapter.class.getResourceAsStream("DinapterDefaultProperties.xml"));
    	} catch (IOException e) {
    		log.error("Default properties/parameters couldn't be loaded. \n" +
    				  "Dinapter won't work if the properties/parameters are not given manually.",e);
    	}
    	String propertiesResource = System.getProperties().getProperty(DINAPTER_PROPERTIES);
    	String modifiedProperties = "";
    	boolean customized = false;
    	if (propertiesResource != null) {
    		Properties customizedValues = new Properties();
    		try {
    			try {
    				InputStream propertiesStream = ClassLoader.getSystemResourceAsStream(propertiesResource);
    				if (propertiesStream == null) {
    					throw new FileNotFoundException("The given properties file ("+propertiesResource+") doesn't exist.");
    				}
    				customizedValues.loadFromXML(propertiesStream);
    				for (Map.Entry<Object, Object> entry:customizedValues.entrySet()) {
    					String defaultValue = properties.getProperty((String)entry.getKey());
    					if (!(((entry.getValue() == null) && (entry.getValue() == defaultValue))
    							|| entry.getValue().equals(defaultValue))) {
    						// If it's not the same as the default value.
    						modifiedProperties += entry.getKey()+" = "+entry.getValue()+"\n";
    					}
    				}
    				customizedValues = null;
    				properties.loadFromXML(ClassLoader.getSystemResourceAsStream(propertiesResource));
    			} catch (InvalidPropertiesFormatException e) {
    				properties.load(ClassLoader.getSystemResourceAsStream(propertiesResource));
    			}
    			customized = true;
    		} catch (IOException e) {
    			log.error("The customized properties/parameters couldn't be retreived",e);
    		}
    	}
    	if (properties.isEmpty()) {
    		String message = "No properties/parameters were loaded. The execution cannot continue.";
    		log.error(message);
    		throw new RuntimeException(message);
    	} else {
    		if (customized) {
    			log.info("Customized properties/parameters loaded.");
    		} else {
    			log.info("Default properties/parameters loaded.");
    		}
    	}
    	MODIFIED_PROPERTIES = modifiedProperties;
    }
    
    private Dinapter() {
    }

    /**
     * This is a single entry method for generating specifications. It blocks the execution while the specification process is performed and,
     * once it ends, it returns the specificator engine to operate with it.
     * 
     * @param componentA The behavioral description of the left-hand side component.
     * @param componentB The behavioral description of the right-hand side component.
     * @param compatibilityFacts Compatibility measures between the operations of both components. This argument is optionally empty (<code>Object[0][0]</code>).
     * @param timeout Timeout in seconds that, when reached, it will stop and return no specification. Dinapter will be reset as well. If <code>timeout <= 0</code>, it will wait till Dinapter ends. 
     * @return An specificator engine where the specifications can be obtained, skipped, restarted, and so on.
     */
    public static JSearchSpecificator
    <Object
    ,JPowerBehaviorNode<Object>
    ,JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge>
    ,DefaultRule<JPowerBehaviorNode<Object>>
    ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
    ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>> 
    generateSpecifications(
    		JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge> componentA,
    		JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge> componentB,
    		Object[][] compatibilityFacts,
    		long timeout) {
    	JSearchSpecificator
    	<Object
    	,JPowerBehaviorNode<Object>
    	,JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge>
    	,DefaultRule<JPowerBehaviorNode<Object>>
    	,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
    	,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>>
    	engine = new JSearchSpecificator
    	<Object
        ,JPowerBehaviorNode<Object>
        ,JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge>
        ,DefaultRule<JPowerBehaviorNode<Object>>
        ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
        ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>
        ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>>(new MapSpecificatorBuilder<Object>());
    	engine.setComponents(componentA, componentB,2000);
    	try {
    		for (int i=0;i<compatibilityFacts.length;i++) {
	    		Fact fact = new Fact("compatibility",engine.engine);
		    	ValueVector vv = new ValueVector();
		    	for (int j=0;j<2;j++) {
		    		vv.add(new Value(compatibilityFacts[i][j]));
	    		}
		    	vv.add(new Value(Math.round(Math.round(
		    			((Double)compatibilityFacts[i][2]).doubleValue()*100.)),RU.INTEGER));
		    	fact.setSlotValue("__data", new Value(vv, RU.LIST));
		    	engine.engine.assertFact(fact);
	    	}
    	} catch (JessException e) {
    		log.error("Compatibility values couldn't be used.",e);
    	}
    	if (timeout <= 0) { 
    		engine.run();
    	} else {
    		// -- Cancel and reset Dinapter is the timeout is reached. --
    		long time;
    		if (timeout < 0) {
    			time = 0;
    		} else {
    			time = timeout*1000;
    		}
    		Thread thread = new Thread(engine,"JSearchSpecificator");
    		thread.start();
    		try {
    			thread.join(time);
    		} catch (InterruptedException e) {
    			// @TODO: Log something.
    		}
    		if (!engine.isSuccessfullyCompleted()) {
    			try {
    				engine.reset();
    			} catch (JessException e) {
    				// @TODO: Log something
    			}
    			thread.interrupt();
    			try {
    				thread.join(3000);
    			} catch (InterruptedException e) {
    				// @TODO: Log something.
    			}
    		}
    		// ========================================================
    	}
    	return engine;
    }
    
    /**
     * <p>
     * It generate the specification for the given two components.
     * It displays a <b>Jess console</b> which provides some process feedback and
     * Jess interaction. Log events are printed in the
     * standard output.
     * </p>
     * <p>
     * It accepts two <it>bpel like</it> files which describe the behavior of two
     * components. If the third argument is "<code>-v</code>" the behavior graph 
     * of those component will be displayed.
     * Alternatively, it can accept just one argument which specifies a one
     * of the built-in examples. In all of the examples one of the 
     * components is the same ({@link FTPExample#getFtpClient()})
     * and only the other component is decided by this argument.
     * This unique argument must be one of:
     * <ul>
     * <li><code>tiny</code> - It corresponds to {@link FTPExample#getVerySimpleFtpServer()}.
     * <li><code>small</code> - It corresponds to {@link FTPExample#getSimpleFtpServer()}.
     * <li><code>full</code> - It corresponds to {@link FTPExample#getFtpServer()}.
     * </ul>
     * <b>WARNING:</b> <code>full</code> example may take much longer and it could not
     * work under every configuration.
     * <p>
     * The <b>Jess console</b> supports two particulary usefull commands:
     * <ul>
     * <li><code>(cancel)</code> - This cancels the process which may finish with the
     * solutions found so far.
     * <li><code>(exit)</code> - This forces an exit. <u>No results are displayed</u>.
     * </ul>
     * @param args Either the behaviors of the two components to adapt or one of the built-in examples ("tiny", "small" or "full").
     * @see data.FTPExample
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JSearchSpecificator
        	<String
        	,JPowerBehaviorNode<String>
        	,JPowerBehaviorGraph<String,JPowerBehaviorNode<String>,Edge>
        	,DefaultRule<JPowerBehaviorNode<String>>
        	,SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>
        	,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<String>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>>>
        	engine = new JSearchSpecificator
        	<String
            ,JPowerBehaviorNode<String>
            ,JPowerBehaviorGraph<String,JPowerBehaviorNode<String>,Edge>
            ,DefaultRule<JPowerBehaviorNode<String>>
            ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>
            ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<String>>
            ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>>>(new MapSpecificatorBuilder<String>());
        // Whether to display the component behavior graphs.
        boolean visual = false;
        if ((args.length > 0) && (args[args.length-1].equals(SHOW_COMPONENT_BEHAVIORS_FLAG))) {
        	visual = true;
        	args = Arrays.copyOf(args, args.length-1);
        }
        if (args.length > 1) {
        	
            JPowerBehaviorGraph<String,JPowerBehaviorNode<String>,Edge> [] components = new JPowerBehaviorGraph[2];
            String [] componentNames = new String[2];
            JPowerBehaviorGraphBuilder<String> graphBuilder = new JPowerBehaviorGraphBuilder<String>();
            BPELJBehaviorProvider<JPowerBehaviorNode<String>,JPowerBehaviorGraph<String, JPowerBehaviorNode<String>, Edge>>
            	provider = new BPELJBehaviorProvider<JPowerBehaviorNode<String>,JPowerBehaviorGraph<String, JPowerBehaviorNode<String>, Edge>>
            	(graphBuilder);
            try {
            	printLogHeaders();
            	// A builder with colors and shapes for visual display.
            	JPowerBehaviorGraphBuilder<String> visualBuilder = new JPowerBehaviorGraphBuilder<String>();
            	visualBuilder.setWrappingEnabled(true);
            	for (int i = 0; i < components.length; i++) {
                    File file = new File(args[i]);
                    if (!file.exists()) {
                        log.fatal("The file \""+file.getAbsolutePath()+"\" doesn't exists!");
                        System.exit(1);
                    }
                    components[i] = provider.getBehaviorGraph(file);
                    componentNames[i] = provider.getProcessName();
                    String message = "Loaded component: "+componentNames[i]; 
                    log.info(message);
                    solutions.debug(message);
                    statistics.debug(message);
                    if (visual) {
                    	visualBuilder.copyGraph(graphBuilder.getGraph());
                    	JFrame frame = new JFrame("Behavior of "+componentNames[i]);
                    	frame.getContentPane().add(visualBuilder.getGraphView());
                    	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    	frame.setPreferredSize(new Dimension(512, 600));
                    	frame.setLocation(512*i, 0);
                    	frame.pack();
                    	frame.setVisible(true);
                    }
                }
            	visualBuilder = null;
            } catch (Exception e) {
                log.fatal("Exception thrown while parsing BPELJ behavior files.",e);
                throw new RuntimeException(e);
            }
            engine.setComponents(components[0], components[1],2000);
        } else if (args.length == 1) {
        	/* If it is a directory we try to load the two bpelj files in it.
        	 * Otherwise we'll try to load a predefined example.
        	 */
        	File possibleDirectory = new File(args[0]);
        	if (possibleDirectory.exists() && possibleDirectory.isDirectory()) {
        		// There is a directory, let's try load it.
        		String [] newArgs = possibleDirectory.list(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						return name.matches(SUPPORTED_EXTENSION);
					}
        		});
        		if (newArgs.length == 2) {
        			directoryLoaded = args[0];
        			Arrays.sort(newArgs);
        			for (int i = 0; i < newArgs.length; i++) {
        				// Let's include the absolute direction.
        				newArgs[i] = possibleDirectory.getAbsolutePath()+File.separator+newArgs[i];
        			}
        			if (visual) {
        				newArgs = Arrays.copyOf(newArgs, newArgs.length+1);
        				newArgs[newArgs.length-1] = SHOW_COMPONENT_BEHAVIORS_FLAG;
        			}
        			Dinapter.main(newArgs);
        			return;
        		} else {
        			log.fatal("The given directory ("+args[0]+
        					") didn't contain two files with the supported extension ("+SUPPORTED_EXTENSION+").");
        			System.exit(1);
        		}
        	} else {
        		// There isn't any directory, let's try to load a predefined example.
	            log.warn("You didn't introduced two BPELJ files. Running predefined examples...");
	            JSearchSpecificator.main(args);
	            return;
        	}
        } else {
            String errorMessage = "Incorrect arguments: There must to be two BPELJ files with the components behavior.";
            log.fatal(errorMessage);
            System.exit(1);
        }
        // @todo This is duplicated from JSearchSpecificator
        final Console console = new Console("Jess engine console",engine.engine);
        console.addWindowListener(new WindowAdapter(){
        	public void windowClosing(WindowEvent we) {
        		console.dispose();
        	}
        	public void windowClosed(WindowEvent we) {
        		System.exit(0);
        	}
        });
        log.info("Running specification process.\n"
                +"\tCommands available:\n"
                +"\t\t(cancel) - To finish the process as it is so far.\n"
                +"\t\t  (exit) - To exit the JVM immediately.");
        new Thread() {
            public void run() {
                String [] arguments = {"-nologo"};
                console.execute(arguments);
            }
        }.start();
        // *** Now we find solutions until the user is happy with the results ***
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        boolean firstTime = true;
        try {
        	String response = null;
	        do {
	        	if (firstTime) {
	        		firstTime = false;
	        		engine.run();
	        	} else {
	        		engine.findDifferentSolutions(0);
	        	}
		        statistics.info(engine.getStatusMessage());
		        solutions.info(engine.getBestSolutionsMessage()
		        		+"\n---------------------------------------------------------");
		        if (!engine.isSuccessfullyCompleted()) {
		        	log.info("--- Press ENTER to quit ---");
		        	input.readLine();
		        	break;
		        }
		        /* Now we allow the user to see the specification graph of any solution if it is
		         * enabled in the properties.
		         */
		        if (getProperty(ENABLE_SPECIFICATION_GRAPH_DISPLAY).equalsIgnoreCase("true")) {
			        List<SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>> solutions = engine.getBestSolutions();
		            while (true) {
			        	// ESCA-JAVA0266:
						System.out.print("Enter the number of the solution to see its trace (press ENTER to continue): ");
			        	// ESCA-JAVA0266:
						System.out.flush();
			        	response = input.readLine();
			        	if (response.length() == 0) {
			        		break;
			        	} else {
			        		try {
			        			int index = Integer.parseInt(response);
			        			if ((index < 0) || (index >= solutions.size())) {
			        				log.error("The introduced number goes beyond the range [ 0, "+solutions.size()+"). Please try again");
			        			} else {
			        				JFrame frame = new JFrame("Trace of the solution #"+index);
			        				SpecificationTrace
			        					<DefaultRule<JPowerBehaviorNode<String>>
			        	        		, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>, BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<String>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>>> traceGenerator = new SpecificationTrace<DefaultRule<JPowerBehaviorNode<String>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>, BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<String>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>>>(
											engine.engine);
	
									SpecificationGraphToJPowerGraph converter = new SpecificationGraphToJPowerGraph();
									
									frame
											.getContentPane().add(
			        						converter.copyToJPowerSpecificatorGraph(
			        								traceGenerator.getSpecificationTrace(
			        										solutions.get(index)
			        										, new BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<String>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<String>>>>())
			        						).getGraphView());
									frame.setPreferredSize(new Dimension(600,600));
									frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									frame.pack();
									frame.setVisible(true);
			        			}
			        		} catch (NumberFormatException e) {
			        			log.error("Error parsing the number. Please try again.");
			        		}
			        	}
			        }
		        }
		        // ESCA-JAVA0266:
				System.out.print("Do you want to search for different solutions (y/N)? ");
		        // ESCA-JAVA0266:
				System.out.flush();
		        response = input.readLine();
		    } while ((response != null) && response.toLowerCase().equals("y"));
	        log.info("Bye bye!");
	        System.exit(0);
        } catch (IOException e) {
			throw new RuntimeException("Exception thrown interacting with the user.",e);
		}
    }

	private static void printLogHeaders() {
		Date now = new Date();
		String header = "\n####################### "+now+" ########################";
		if (MODIFIED_PROPERTIES.length() > 0) {
				header += "\n"+MODIFIED_PROPERTIES;
		}
		if (directoryLoaded != null) {
				header += "\nLoading directory: "+directoryLoaded;
		}
		statistics.debug(header);
		solutions.debug(header);
	}
    
    /**
     * It returns the configuration properties of Dinapter.
     * @param propertyName The name of the property to retrieve.
     * @return the value of the property or null if it doesn't exist.
     */
    public static String getProperty(String propertyName) {
    	return properties.getProperty(propertyName);
    }
}
