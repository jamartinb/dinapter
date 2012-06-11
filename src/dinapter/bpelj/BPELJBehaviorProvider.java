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
/*
 * BPELJBehaviorProvider.java
 *
 * Created on 6 de marzo de 2007, 17:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.bpelj;

import static dinapter.behavior.BehaviorNode.BehaviorNodeType.EXIT;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.IF;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.PICK;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.RECEIVE;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.SEND;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.START;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.jpowergraph.Edge;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dinapter.behavior.BehaviorGraph;
import dinapter.behavior.BehaviorGraphBuilder;
import dinapter.behavior.BehaviorNode;
import dinapter.behavior.BehaviorProvider;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

// @todo Refactorize in order to avoid implementation dependence.

/**
 *
 * @author arkangel
 */
public class BPELJBehaviorProvider<B extends BehaviorNode<String>,BG extends BehaviorGraph<B>> implements BehaviorProvider<BG> {
    
    private static enum LevelType {SEQUENCE,FLOW,IF,PICK};
    
    protected class BPELJHandler extends DefaultHandler {
        private static final String NO_TWO_WAYS_INVOKE = "Dinapter doesn't support two-ways INVOKE activities. Use equivalent INVOKE & RECEIVE instead.";

		protected class Level {
            public final Stack<B> nodes = new Stack<B>();
            public final LinkedList<B> afterLink = new LinkedList<B>();
            public final LinkedList<B> toLink = new LinkedList<B>();
            public final LevelType type;
            public final B previousNode; // @todo Remove this.
            public boolean ignoreEndSequence = false;
            
            public Level(LevelType type) {
                this(type,levels.peek().nodes.peek()); // @toreview Does this work?
            }
            
            public Level(LevelType type, B previous) {
                this.type = type;
                previousNode = previous;
                nodes.push(previousNode);
            }
            
            public void pushNode(B node) {
                for (B from:toLink) {
                    // We link all afterlinks from closed levels.
                    graphBuilder.link(from, node);
                }
                if ((type != LevelType.SEQUENCE) && (
                        (node.getType() == SEND) ||
                        (node.getType() == RECEIVE))) {
                    // If we are in a FLOW, PICK, SWITCH we must afterlink.
                    afterLink.add(node);
                }
                if (toLink.isEmpty()) {
                	if (!closedStructures.contains(nodes.peek())) {
	                	/* We link this node after the previous one if
                		 * it isn't a closed IF/PICK. */
                		if ((nodes.peek() != endNode) || (node != endNode)) {
                			graphBuilder.link(nodes.peek(), node);
                		} else {
                			log.debug("Not linked and EXIT -> EXIT.");
                		}
                	} else if (node != endNode) {
                		log.error("The node ("+node+") has no parent.");
                	}
                }
                toLink.clear();
                if (type == LevelType.SEQUENCE) {
                    // If this is a SEQUENCE we link each after another.
                    nodes.push(node);
                }
            }
        }
        
        private final Stack<Level> levels = new Stack<Level>();
        
        private String operation = null;
        
        private final List<String> arguments = new LinkedList<String>();
        
        private B endNode = null;
        
        private final Map<String,B> links = new HashMap<String, B>();
        
        /**
         * It keeps track of all the source nodes of links.
         */
        private final Set<B> linkSources = new HashSet<B>();
        
        /**
         * The links that have not finished yet.
         */
        private final Set<String> remainingLinks = new HashSet<String>();
        
        private final Map<String,Boolean> directions = new HashMap<String, Boolean>();
        
        private boolean linkWarn = false;
        
        private void addLink(String linkName, boolean isSource) {
        	log.trace("Adding link: operation= "+operation+" ; linkName= "+linkName+" ; isSource= "+isSource);
        	if (!linkWarn) {
        		linkWarn = true;
        		log.warn("Links have a special meaning in Dinapter. " +
        				"They're not control dependencies but alternative control flows for loops. " +
        				"Use with caution.");
        	}
        	if (linkName == null) {
        		log.error("There is a link without name which is going to be ignored.");
        		return;
        	}
            // Loops in the same node are not allowed.
            if (operation != null) {
                // It's an action so we must wait until it finishes.
                directions.put(linkName, isSource);
            } else {
                // We're within an special node. We can link immediately.
                link(linkName,isSource,levels.peek().previousNode);
            }
        }
        
        // @todo: Several occurrences of a link will fail.
        private void link(String linkName, boolean isSource, B node) {
            if (links.containsKey(linkName)) {
            	// The other node is available, lets link!
                B other = links.get(linkName);
                log.trace("Linking "+node+" and "+other);
                graphBuilder.link(isSource?node:other, isSource?other:node);
                if (linkName != null) {
                	remainingLinks.remove(linkName);
                }
            } else {
            	log.trace("To link the node ("+node+"; isSource:"+isSource+")");
                // We store this node till the next one is found.
                links.put(linkName, node);
                if (linkName != null) {
                	remainingLinks.add(linkName);
                }
            }
            if (isSource) {
            	linkSources.add(node);
            }
        }
        
        @Override
        public void startDocument() {
            graphBuilder.createNewGraph();
            closedStructures.clear();
            levels.clear();
            linkSources.clear();
            links.clear();
            pushLevel(new Level(LevelType.SEQUENCE,graphBuilder.createNode(START)));
            remainingLinks.clear();
            endNode = graphBuilder.createNode(EXIT);
        }
        
        private boolean isInvoke = false;
        private boolean onMessage = false;
        
        @Override
        @SuppressWarnings("unchecked")
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            log.trace("Start: "+qName+" (isInvoke:"+isInvoke+")");
            boolean source = false;
            if ("input".equalsIgnoreCase(qName) ||
	                "output".equalsIgnoreCase(qName)) {
	            arguments.add(attributes.getValue("part"));
	    	} else if (isInvoke && "toPart".equalsIgnoreCase(qName)) {
	    		arguments.add(attributes.getValue("part"));
	    	} else if (isInvoke && "fromPart".equalsIgnoreCase(qName)) {
	    		log.warn(NO_TWO_WAYS_INVOKE);
	    	} else if (!isInvoke && "fromPart".equalsIgnoreCase(qName)) {
	    		arguments.add(attributes.getValue("part"));
	    	} else if ((source = "source".equalsIgnoreCase(qName))  
                    || "target".equalsIgnoreCase(qName)) {
                addLink(attributes.getValue("linkName"), source);
	    	} else {
	    		if (onMessage) {
	    			createNode(false);
	    			onMessage = false;
	    		}
	 	    	if ("sequence".equalsIgnoreCase(qName)) {
	 	    		if (levels.peek().type == LevelType.SEQUENCE) {
	 	    			levels.peek().ignoreEndSequence = true;
	 	    		} else {
	 	    			pushLevel(LevelType.SEQUENCE);
	 	    		}
	            } else if ("if".equalsIgnoreCase(qName)) {
	                addNode(graphBuilder.createNode(IF));
	                pushLevel(LevelType.IF);
	            } else if ("pick".equalsIgnoreCase(qName)) {
	                addNode(graphBuilder.createNode(PICK));
	                pushLevel(LevelType.PICK);
	            } else if ("flow".equalsIgnoreCase(qName)) {
	            	log.error("Dinapter doesn't support FLOWs. Use several processes instead.");
	                /*
	                addNode(graphBuilder.createNode(FLOW));
	                pushLevel(LevelType.FLOW);
	                */
	            } else if ("invoke".equalsIgnoreCase(qName) 
	            		|| "receive".equalsIgnoreCase(qName) 
	            		|| "reply".equalsIgnoreCase(qName)
	            		|| (onMessage = "onMessage".equalsIgnoreCase(qName))) {
	            	isInvoke = "invoke".equalsIgnoreCase(qName) || "reply".equalsIgnoreCase(qName);
	                operation = attributes.getValue("operation");
	                arguments.clear();
	                handleAttributes(isInvoke?SEND:RECEIVE, attributes);
	                if (onMessage) {
	                	pushLevel(LevelType.SEQUENCE);
	                }
	        	} else if ("exit".equalsIgnoreCase(qName)) {
	                addNode(endNode);
	        	} else if ("process".equalsIgnoreCase(qName)) {
	                processName = attributes.getValue("name");
	        	}
	    	}
        }

		private void handleAttributes(BehaviorNodeType type, Attributes attributes) {
			if (type == SEND) {
				int index = attributes.getIndex("inputVariable"); 
				if (index != -1) {
					arguments.add(attributes.getValue(index));
				}
				index = attributes.getIndex("outputVariable");
				if (index != -1) {
					log.warn(NO_TWO_WAYS_INVOKE);
				}
			} else if (type == RECEIVE) {
				int index = attributes.getIndex("variable"); 
				if (index != -1) {
					arguments.add(attributes.getValue(index));
				}
			} else {
				throw new IllegalArgumentException("The type argument must be either SEND or RECEIVE.");
			}
		}
        
    private void pushLevel(LevelType type) {
            pushLevel(new Level(type));
        }
        
        private void pushLevel(Level level) {
            levels.push(level);
        }
        
        private void popLevel() {
        	log.trace("Popping level afterLink:"+levels.peek().afterLink+" ; toLink:"+levels.peek().toLink+" ; node:"+levels.peek().nodes.peek());
            LinkedList<B> toLink = levels.peek().afterLink;
            toLink.addAll(levels.peek().toLink);
            if (levels.peek().type == LevelType.SEQUENCE) {
                toLink.add(levels.peek().nodes.peek());
            } else {
            	closedStructures.add(levels.peek().previousNode);
            }	
            levels.pop();
            for (Iterator<B> i=toLink.iterator();i.hasNext();) {
            	B node = i.next();
            	BehaviorNodeType type = node.getType();
            	// The linkSources check removes those nodes that have been source of a link.
            	if ((node == endNode) || (type == PICK) || (type == IF) || linkSources.contains(node)) {
            		i.remove();
            	}
            }
            levels.peek().toLink.addAll(toLink);
        }
        
        public void addNode(B node) {
            for (Map.Entry<String, Boolean> link:directions.entrySet()) {
                link(link.getKey(),link.getValue(),node);
            }
            directions.clear();
            levels.peek().pushNode(node);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            log.trace("End: "+qName);
            boolean invoke = false;
            boolean resetOperation = true;
            boolean sequence = false;
            if ((invoke = ("invoke".equalsIgnoreCase(qName) || "reply".equalsIgnoreCase(qName))) 
            		|| "receive".equalsIgnoreCase(qName)) {
                createNode(invoke);
            } else if ((sequence = "sequence".equalsIgnoreCase(qName)) ||
                    "if".equalsIgnoreCase(qName) ||
                    "pick".equalsIgnoreCase(qName) ||
                    "flow".equalsIgnoreCase(qName) ||
                    "onMessage".equalsIgnoreCase(qName)) {
            	if (onMessage && "onMessage".equalsIgnoreCase(qName)) {
            		createNode(false);
            		onMessage = false;
            	}
            	if (sequence && levels.peek().ignoreEndSequence) {
            		levels.peek().ignoreEndSequence = false;
            	} else {
            		popLevel();
            	}
            } else if ("process".equalsIgnoreCase(qName)) {
            	addNode(endNode);
            } else
                resetOperation = false;
            if (resetOperation)
                operation = null;
        }

		private void createNode(boolean invoke) {
			B node = graphBuilder.createNode(
			        operation
			        ,invoke?SEND:RECEIVE
			        ,new ArrayList<String>(arguments));
			addNode(node);
		}
        
        @Override
        public void endDocument() throws SAXException {
        	if (!remainingLinks.isEmpty()) {
        		log.error("There are uncomplete links: "+remainingLinks);
        	}
            /* DISABLED BY LINKS
             * for (JPowerBehaviorNode<Object> node:levels.peek().toLink)
                graphBuilder.link(node, endNode);*/
        }
    }
    
    protected final BehaviorGraphBuilder<B,BG,String> graphBuilder;
    protected SAXParser parser = null;
    protected DefaultHandler parserHandler = new BPELJHandler();
    private static final Logger log = Logger.getLogger(BPELJBehaviorProvider.class);
    protected String processName = null;
    
    private Set<B> closedStructures = new HashSet<B>();
    
    /** Creates a new instance of BPELJBehaviorProvider */
    public BPELJBehaviorProvider(BehaviorGraphBuilder<B, BG, String> builder) {
    	graphBuilder = builder;
    	builder.createNewGraph();
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch(Exception e) {
            String msg = "Error parsing BPELJ files";
            log.fatal(msg,e);
            throw new RuntimeException(msg,e);
        }
    }
    
    public String getProcessName() {
        return processName;
    }
    
    public BG getBehaviorGraph() {
        return graphBuilder.getGraph();
    }
    
    public BG getBehaviorGraph(File file)
    throws SAXException, IOException {
        parser.parse(file,parserHandler);
        return graphBuilder.getGraph();
    }
    
    public static void main(String... args) throws Exception {
        File file = new File("input/server-full.bpelj");
        if (!file.exists())
            throw new RuntimeException("File "+file.getAbsolutePath()+" doesn't exists!");
        BPELJBehaviorProvider<JPowerBehaviorNode<String>,JPowerBehaviorGraph<String, JPowerBehaviorNode<String>, Edge>>
    	provider = new BPELJBehaviorProvider<JPowerBehaviorNode<String>,JPowerBehaviorGraph<String, JPowerBehaviorNode<String>, Edge>>
    	(new JPowerBehaviorGraphBuilder<String>());
        provider.getBehaviorGraph(file);
        JFrame frame = new JFrame("Prueba");
        frame.getContentPane().add(((JPowerBehaviorGraphBuilder)provider.graphBuilder).getGraphView());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.pack();
        frame.setVisible(true);
    }
}
