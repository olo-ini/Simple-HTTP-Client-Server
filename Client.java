/*Program name: client.java
 * Author: Ini Olorunnishola
 * 
 * Description: This is the client process of the client/proxyServer/webServer assignment.  
 * You would set up the client last of the three processes. Only after the webServer and the proxyServer
 * have been set up.
 * 
 * Expected input: It asks the user to connect to the proxyServer by providing the port number the proxyServer is located on.
 * The proxyServer will require the client user to enter a request starting with either
 * GET or HEAD requests only and expects http/https/ftp protocols only. 
 * 
 * Expected output: requested web page or valid error.
*/


import java.io.*;
import java.net.*;

public class Client {
	protected static Socket proxyConn;
	
	public static void main (String args[]) throws UnknownHostException, IOException {
		// if the arguments are less than 2
		if (args.length < 1) {
			System.out.println("Usage: java Client [port]");
			System.exit(1);
		}
		int port = 3000;
		try {
			port = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e) {}
		
		try {
			// create a new socket connection
			proxyConn = new Socket("localhost", port);
		}
		catch(ConnectException e) {
			System.out.println("Failed to connect to proxy " + e);
			System.exit(1);;
		}
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(proxyConn.getInputStream())); // stream for reading characters from the server
			PrintWriter out = new PrintWriter(proxyConn.getOutputStream(), true); // stream for writing characters to the server
			var webIn = proxyConn.getInputStream();
			System.out.println("Server response: " + in.readLine());
			
			var stdin = new BufferedReader(new InputStreamReader(System.in));
			String userInput = stdin.readLine();
			
			out.println(userInput);
			int i;
			
			while((i = webIn.read()) != -1) {
				System.out.print((char)i);
			}
			System.out.println();
			System.out.println("Server response: " + in.readLine());
			
		}
		catch (IOException e) {
			System.out.println("input error " + e);
		}
	}
}
