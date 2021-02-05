package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	public interface ClientHandler{
		void handleClient(InputStream inFromClient, OutputStream outToClient);
	}

	volatile boolean stop;
	public Server() {
		stop=false;
	}


	private void startServer(int port, ClientHandler ch) {
		ServerSocket server= null;
		try {
			server = new ServerSocket(port);
			server.setSoTimeout(1000);
			Socket aClient=server.accept(); // blocking call

			while(!stop){
				try {
					ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
					aClient.getInputStream().close();
					aClient.getOutputStream().close();
					aClient.close();
				} catch (IOException e) {/*...*/}
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(()->startServer(port,ch)).start();
	}

	public void stop() {
		stop=true;
	}
}
