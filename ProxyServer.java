/*Program name: proxyServer.java
 * Author: Ini Olorunnishola
 * 
 * Description: This is the proxyServer process of the client/proxyServer/webServer assignment.  
 * You would set up the proxy second of the three processes. Only after the webServer has been set up.
 * 
 * Expected input: It asks the user to set up the proxyServer and connect to the webServer by 
 * inputting first the port number for the proxyServer to be set up on and then the port number
 * the webServer is set up on.
 * 
 * The proxyServer will require the client user to enter a request starting with either
 * GET or HEAD requests only and expects http/https/ftp protocols only. 
 * 
 * Expected output: valid progress reports.
*/

import java.io.*;
import java.util.*;
import java.net.*;


public class ProxyServer {
	ServerSocket toClientSocket;
	Socket toServerSocket;
	Socket clientConn;
	
	PrintWriter clientOut; // writes characters to client
	BufferedReader clientIn; // reads characters from clients 
	PrintWriter webOut; // writes characters to web
	BufferedReader webIn; // reads characters from web
	OutputStream requestOut; // writes binary to client 
	InputStream binaryIn; // reads binary from web
	
	String inputLine;
	
	int port = 3000;
	int sPort;
	
	File ROOT = new File(".");
	String DEFAULT = "index.html";
	String NOT_FOUND = "404.html";
	String NOT_SUPPORTED = "not_supported.html";
	
	File index = new File(ROOT, DEFAULT);
	File not_found = new File(ROOT, NOT_FOUND);
	File not_supported = new File(ROOT, NOT_SUPPORTED);
	
	public final Runnable fromClientStream;
	public final Runnable toServerStream;
	public final Runnable serverToClientStream;

	
	public ProxyServer(int port, int serverPort) throws IOException{
		sPort = serverPort;
		System.out.println("Binding proxy socket to port..");
		
		try {
			toClientSocket = new ServerSocket(port);
		}
		catch (IOException e) {
			System.out.println("Failed to Bind proxy to port.." + port);
			System.exit(1);
		}
		
		fromClientStream = new Runnable() {
			public void run() {
				try {
					ProxyServer.this.readClient(clientConn);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		toServerStream = new Runnable() {
			public void run() {
				ProxyServer.this.sendToServer(inputLine);
			}
		};
		
		serverToClientStream = new Runnable() {
			public void run() {
				try {
					ProxyServer.this.sendToClient();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
		
	}
	
	public static void main(String argv[]) throws IOException {
		int port = 3000;
		int serverPort = 3001;
		if (argv.length == 2) {
			int tmp = port;
			try {
				tmp = Integer.parseInt(argv[0]); // get the port for client socket to bind to 
			}
			catch (NumberFormatException e){}
			port = tmp;
			
			try {
				tmp = Integer.parseInt(argv[1]); // get the port where connection to webServer socket
			}
			catch (NumberFormatException e){}
			serverPort = tmp;
			
			ProxyServer proxy = new ProxyServer(port, serverPort);
			proxy.listen();
			
		}
		
		else {
			System.out.println("Usage: java ProxyServer [port][serverPort]");
			System.exit(1);
			
		}
		
		
	}
	
	public void listen() {
		try {
			System.out.println("Proxy is waiting for client request...");
			while(true) {
				clientConn = toClientSocket.accept();
				System.out.println("Proxy found a client..");
				new Thread(fromClientStream).start(); // start up the thread to read from client
			}
		}
		catch (IOException e) {
			System.out.println("IO exception while listening for clients in proxy.");
			System.exit(1);
		}
	}
	
	public void readClient(Socket clientConn) throws IOException {
		try {
			clientOut = new PrintWriter(clientConn.getOutputStream(), true); // out stream to the client
			clientIn = new BufferedReader(new InputStreamReader(clientConn.getInputStream()));// in stream from client
			requestOut = clientConn.getOutputStream();
			
			clientOut.println("Please enter an https:// http:// or ftp:// address only");
			
			inputLine = clientIn.readLine(); 
			if(clientIn != null) {
				StringTokenizer parse = new StringTokenizer(inputLine); //break up the input received from client
				String method = parse.nextToken().toUpperCase(); // get the first entry which should be the method
				String requestFile = parse.nextToken().toLowerCase();
				System.out.println(method);
				System.out.println(requestFile);
				System.out.println();
				
				if(!method.equals("GET") && !method.contentEquals("HEAD")){
					clientOut.println("403 - Bad request, This server only accepts GET and HEAD methods ");
					System.exit(1);
				}
				else {
					System.out.println("Sending this to the webserver");
					new Thread(toServerStream).start();	
				}
			}
			else {
				System.out.println("Server received no input");
			}
		}
		catch (Exception e) {
			System.err.println("Server Error: " + e);
		}
	}
	
	public void sendToServer(String requestFile) {
		try {
			toServerSocket = new Socket("localhost", sPort);
			
			webOut = new PrintWriter(toServerSocket.getOutputStream(), true); // out stream to the web server
			webIn = new BufferedReader(new InputStreamReader(toServerSocket.getInputStream()));// in stream from web server
			binaryIn = toServerSocket.getInputStream();
			
			System.out.println("Server response: " + webIn.readLine());
			webOut.println(requestFile);
			
			
		}
		catch (IOException e) {
			System.out.println("Failed to connect proxy to server port.." + port);
			System.exit(1);
		}
		
		new Thread(serverToClientStream).start();
	}
	
	public void sendToClient() throws IOException {	
		int i; 
		while((i = binaryIn.read()) != -1) {
			requestOut.write(i);
		}
		requestOut.flush();
		clientOut.println("Done");
	}
}
