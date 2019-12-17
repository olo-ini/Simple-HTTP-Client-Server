/*Program name: webServer.java
 * Author: Ini Olorunnishola
 * 
 * Description: This is the webServer process of the client/proxyServer/webServer assignment.  
 * You would set up the webServer first of the three processes. 
 * 
 * Expected input: It asks the user to set up the webServer by 
 * inputting the port number for the webServer to be set up on.
 * 
 * 
 * Expected output: valid progress reports.
*/

import java.io.*;
import java.util.*;
import java.net.*;


@SuppressWarnings("unused")

public class WebServer {
	
	ServerSocket serverSocket;
	int port = 3000;
	File request = new File(".", "request.html");
	
	
	public WebServer(int port) throws IOException{
		System.out.println("Connecting server socket to port..");
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e) {
			System.out.println("Failed to connect to port.." + port);
			System.exit(1);
		}
	}
	
	public static void main(String argv[]) throws IOException {
		int port = 3000;
		if (argv.length > 0) {
			int tmp = port;
			try {
				tmp = Integer.parseInt(argv[0]);
			}
			catch (NumberFormatException e){}
			port = tmp;
		}
		
		WebServer webServer = new WebServer(port);
		webServer.listen();
	}
	
	public void listen() {
		try {
			System.out.println("Waiting for proxy request...");
			while(true) {
				Socket proxyConn = serverSocket.accept();
				System.out.println("Found proxy server..");
				serviceProxy(proxyConn);
			}
		}
		catch (IOException e) {
			System.out.println("IO exception while listening for clients.");
			System.exit(1);
		}
	}
	
	public void serviceProxy(Socket proxyConn) throws IOException {
		try {
			var out = new PrintWriter(proxyConn.getOutputStream(), true);
			var in = new BufferedReader(new InputStreamReader(proxyConn.getInputStream()));
			var requestOut = proxyConn.getOutputStream();
			
			out.println("Now we're ready to take requests");
			
			String inputLine = in.readLine(); 
			//System.out.println("printing string server received " + inputLine);
			if(in != null) {
				StringTokenizer parse = new StringTokenizer(inputLine); //break up the input received from client
				String method = parse.nextToken().toUpperCase(); // get the first entry which should be the method
				String requestFile = parse.nextToken().toLowerCase();
				System.out.println(method);
				System.out.println(requestFile);
				System.out.println();
				
				boolean bool = request.createNewFile();
				FileOutputStream requestStream = new FileOutputStream(request);
				
				if(method.equals("GET")) {
					URL requestURL = new URL(requestFile);
					var download = new BufferedReader(new InputStreamReader(requestURL.openStream()));
					String input;
					
					while ((input = download.readLine()) != null){
						requestStream.write(input.getBytes());
					}
					download.close();
					requestStream.close();
				}
				
				int fileLength = (int) request.length();
				byte[] fileData = readFileData(request, fileLength);
				System.out.println("FileLength " + fileLength);
				requestOut.write(fileData, 0, fileLength);
				requestOut.flush();
				
			}
			else {
				System.out.println("Server received no input");
			}
		}
		catch (Exception e) {
			System.err.println("403 - bad request" + e);
		}
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
		

}
