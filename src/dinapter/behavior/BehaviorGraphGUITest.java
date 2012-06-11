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
package dinapter.behavior;

import static org.junit.Assert.assertTrue;

import javax.swing.JFrame;

import org.junit.Test;

import data.FTPExample;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * This class tests a couple of graph libraries: <a href="http://www.jgraph.com/">JGraph</a> 
 * and <a href="https://sourceforge.net/projects/jpowergraph/">JPowergraph</a>.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class BehaviorGraphGUITest {

    /**
     * It shows several behavior graphs.
     * @param args No argument being used so far.
     */
	public static void main(String[] args) {
		BehaviorGraphGUITest tests = new BehaviorGraphGUITest();
		tests.ftpSystemTest();
		tests.otherFtpSystemTest();
	}
	
    /**
     * Custom behavior graph example using jpowergraph library.
     * @see <a href="https://sourceforge.net/projects/jpowergraph/">JPowerGraph site</a>
     */
	@Test
	public void jPowerTest() {
		// Graph model creation /
		JPowerBehaviorGraphBuilder<String> builder = new JPowerBehaviorGraphBuilder<String>();
		builder.setWrappingEnabled(true);
		JPowerBehaviorNode<String> a, b;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("login", BehaviorNodeType.SEND, "username",
				"password");
		builder.link(a, b);
		a = builder.createNode("get", BehaviorNodeType.SEND, "filename");
		builder.link(b, a);
		b = builder.createNode("data", BehaviorNodeType.RECEIVE, "data");
		builder.link(a, b);
		a = builder.createNode(BehaviorNodeType.EXIT);
		builder.link(b, a);
		// ---------------------
		       
		JFrame frame = new JFrame("JPowerBehaviorGraph");
		frame.getContentPane().add(builder.getGraphView());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.pack();
		frame.setVisible(true);
		assertTrue(true);
	}
	
    /**
     * A whole client-server behavior in jpowergraphs.
     * @see <a href="https://sourceforge.net/projects/jpowergraph/">JPowerGraph site</a>
     * @see FTPExample
     */
	@Test
	public void ftpSystemTest() {
		FTPExample example = new FTPExample();
		example.getBuilder().setWrappingEnabled(true);
		example.getFtpClient();
		JFrame client = new JFrame("FTP Client");
		client.getContentPane().add(example.getBuilder().getGraphView());
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setSize(800, 500);
		client.pack();
		example.getFtpServer();
		JFrame server = new JFrame("FTP Server");
		server.getContentPane().add(example.getBuilder().getGraphView());
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setSize(800, 500);
		server.pack();
		server.setVisible(true);
		client.setVisible(true);
		assertTrue(true);
	}

    /**
     * Another client-server example using jpowergraph.
     * @see <a href="https://sourceforge.net/projects/jpowergraph/">JPowerGraph site</a>
     * @see FTPExample
     */
	@Test
	public void otherFtpSystemTest() {
		FTPExample example = new FTPExample();
		example.getBuilder().setWrappingEnabled(true);
		example.getSimpleFtpServer();
		JFrame simpleClient = new JFrame("Simple FTP Server");
		simpleClient.getContentPane().add(example.getBuilder().getGraphView());
		simpleClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		simpleClient.setSize(800, 500);
		simpleClient.pack();
		example.getVerySimpleFtpServer();
		JFrame verySimpleServer = new JFrame("Very Simple FTP Server");
		verySimpleServer.getContentPane().add(example.getBuilder().getGraphView());
		verySimpleServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		verySimpleServer.setSize(800, 500);
		verySimpleServer.pack();
		simpleClient.setVisible(true);
		example.getBadFtpServer();
		JFrame badServer = new JFrame("Bad FTP Server");
		badServer.getContentPane().add(example.getBuilder().getGraphView());
		badServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		badServer.setSize(800, 500);
		badServer.pack();
		badServer.setVisible(true);
		simpleClient.setVisible(true);
		verySimpleServer.setVisible(true);
		assertTrue(true);
	}
}
