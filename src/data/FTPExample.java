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
/**
 * 
 */
package data;

import static dinapter.behavior.BehaviorNode.BehaviorNodeType.EXIT;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.PICK;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.RECEIVE;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.SEND;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.IF;
import net.sourceforge.jpowergraph.Edge;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * This class provides several example behaviors using {@link JPowerBehaviorGraph}.
 * In particular these examples represent different variations of an <i>FTP service</i>.
 * There are four different server behaviors (one of them is badly generated for testing
 * purposes) and a single client behavior.
 * <p>
 * <h3>The FTP client:</h3>
 * <p align=center>
 * <img src="doc-files/FTPExample-1.png" alt="FTP client behavior graph image."></p>
 * It a simple client which always connects, downloads a single file and ends.
 * <p>
 * <h3>The <i>very simple</i> FTP server:</h3>
 * <p align=center>
 * <img src="doc-files/FTPExample-2.png" alt="Very simple FTP server image."></p>
 * This server is almost the complementary of the ftp client but it may quit without
 * delivering any file and, even when a file is requested a quit confirmation is required.
 * <p>
 * <h3>The <i>simple</i> FTP server:</h3>
 * <p align=center>
 * <img src="doc-files/FTPExample-3.png" alt="Simple FTP server behavior graph image."></p>
 * This server extends the previous one by notifying the accepted connection and, once
 * the file is requested, it notifies whether this file is available or not.
 * <p>
 * <h3>The FTP server:</h3>
 * <p align=center>
 * <img src="doc-files/FTPExample-4.png" alt="Full FTP server behavior graph image."></p>
 * This server makes more sense than all the others and it includes the posibility that
 * the client may be rejected during the connection.
 * <p>
 * <h3>The <i>bad</i> FTP server:</h3>
 * <p align=center>
 * <img src="doc-files/FTPExample-5.png" alt="Bad structured FTP server behavior graph image."></p>
 * This behavior graph is <u>badly constructed</u>. It's a graph but the connections
 * between behavior nodes do not make sense or omit the necesary intermediary connection nodes.
 * @author José Antonio Martín Baena
 * @version $Revision: 466 $ - $Date: 2007-02-13 18:33:17 +0100 (mar, 13 feb 2007) $
 */
public class FTPExample {
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> ftpClient, ftpServer, simpleFtpServer, verySimpleFtpServer, badFtpServer;
	private JPowerBehaviorGraphBuilder<Object> builder = new JPowerBehaviorGraphBuilder<Object>();
    
    /**
     * It instantiates this class.
     */
    public FTPExample() {
    }

	/**
     * It returns the ftp client.
	 * @return The ftp client
	 */
	public JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> getFtpClient() {
		if (ftpClient == null)
			ftpClient = createFtpClient();
		return ftpClient;
	}

	/**
     * It returns the ftp server.
	 * @return The ftp server
	 */
	public JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> getFtpServer() {
		if (ftpServer == null)
			ftpServer = createFtpServer();
		return ftpServer;
	}
	
	/**
     * It returns the <i>simple</i> ftp server.
	 * @return The simple ftp server.
	 */
	public JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> getSimpleFtpServer() {
		if (simpleFtpServer == null)
			simpleFtpServer = createSimpleFtpServer();
		return simpleFtpServer;
	}

	/**
     * It returns the <i>very simple</i> ftp server.
	 * @return The very simple ftp server.
	 */
	public JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> getVerySimpleFtpServer() {
		if (verySimpleFtpServer == null)
			verySimpleFtpServer = createVerySimpleFtpServer();
		return verySimpleFtpServer;
	}
	
	/**
     * It retruns the badly constructed server.
	 * @return The bad ftp server.
	 */
	public JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> getBadFtpServer() {
		if (badFtpServer == null)
			badFtpServer = createBadFtpServer();
		return badFtpServer;
	}	
	
    /**
     * It returns the builder used for example construction.
     * @return The example builder.
     */
	public JPowerBehaviorGraphBuilder<Object> getBuilder() {
		return builder;
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createFtpClient() {
		builder.createNewGraph();
		JPowerBehaviorNode<Object> a, b;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("login", BehaviorNodeType.SEND, "username",
				"password");
		builder.link(a, b);
		a = builder.createNode("download", BehaviorNodeType.SEND, "filename");
		builder.link(b, a);
		b = builder.createNode("data", BehaviorNodeType.RECEIVE, "data");
		builder.link(a, b);
		a = builder.createNode(BehaviorNodeType.EXIT);
		builder.link(b, a);
		return builder.getGraph();
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createFtpServer() {
		builder.createNewGraph();
		JPowerBehaviorNode<Object> a, b,c,d;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("user", RECEIVE, "username", "password");
		builder.link(a, b);
		a = builder.createNode(IF);
		builder.link(b, a);
		d = builder.createNode("rejected", SEND);
		builder.link(a,d);
		b = builder.createNode("connected", SEND);
		builder.link(a,b);
		a = builder.createNode(PICK);
		builder.link(b, a);
		b = builder.createNode("quit", RECEIVE);
		builder.link(a,b);
		c = builder.createNode(EXIT);
		builder.link(b,c);
		builder.link(d, c);
		b = builder.createNode("getFile",RECEIVE, "filename");
		builder.link(a,b);
		d = builder.createNode(IF);
		builder.link(b,d);
		c = builder.createNode("noSuchFile",SEND);
		builder.link(d, c);
		builder.link(c, a);
		c = builder.createNode("result", SEND, "data");
		builder.link(d, c);
		builder.link(c, a);
		return builder.getGraph();
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createSimpleFtpServer() {
		builder.createNewGraph();
		JPowerBehaviorNode<Object> a, b,c,d;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("user", RECEIVE, "username", "password");
		builder.link(a, b);
		a = b;
		b = builder.createNode("connected", SEND);
		builder.link(a,b);
		a = builder.createNode(PICK);
		builder.link(b, a);
		b = builder.createNode("quit", RECEIVE);
		builder.link(a,b);
		c = builder.createNode(EXIT);
		builder.link(b,c);
		b = builder.createNode("getFile",RECEIVE, "filename");
		builder.link(a,b);
		d = builder.createNode(IF);
		builder.link(b, d);
		c = builder.createNode("noSuchFile",SEND);
		builder.link(d, c);
		builder.link(c, a);
		c = builder.createNode("result", SEND, "data");
		builder.link(d, c);
		builder.link(c, a);
		return builder.getGraph();
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createVerySimpleFtpServer() {
		builder.createNewGraph();
		JPowerBehaviorNode<Object> a, b,c;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("user", RECEIVE, "username", "password");
		builder.link(a, b);
		a = builder.createNode(PICK);
		builder.link(b, a);
		b = builder.createNode("quit", RECEIVE);
		builder.link(a,b);
		c = builder.createNode(EXIT);
		builder.link(b,c);
		c = builder.createNode("getFile",RECEIVE, "filename");
		builder.link(a,c);
		a = builder.createNode("result", SEND, "data");
		builder.link(c, a);
		builder.link(a, b);
		return builder.getGraph();
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createBadFtpServer() {
		builder.createNewGraph();
		JPowerBehaviorNode<Object> a, b,c,d;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("user", RECEIVE, "username", "password");
		builder.link(a, b);
		a = builder.createNode(PICK);
		builder.link(b, a);
		b = builder.createNode("quit", RECEIVE);
		builder.link(a,b);
		c = builder.createNode("bad-1", SEND);
		builder.link(a, c);
		builder.link(c, b);
		d = builder.createNode("bad-2", SEND);
		builder.link(c,d);
		c = builder.createNode(EXIT);
		builder.link(b,c);
		c = builder.createNode("getFile",RECEIVE, "filename");
		builder.link(a,c);
		d = builder.createNode(IF);
		builder.link(c, d);
		builder.link(d, c);
		c = d;
		a = builder.createNode("result", SEND, "data");
		builder.link(c, a);
		builder.link(a, b);
		return builder.getGraph();
	}
}
