package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Test5 {

	public void test() {
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connected!");
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				out.println("hi!");
				out.close();
				clientSocket.close();
				System.out.println("Done!");
			}
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port or listening for a connection");
			System.out.println(e.getMessage());
		}
	}

}
