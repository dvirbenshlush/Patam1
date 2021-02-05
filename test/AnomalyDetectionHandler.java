package test;


import test.Commands.DefaultIO;
import test.Server.ClientHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler{


	@Override
	public void handleClient(InputStream inFromClient, OutputStream outToClient) {
		SocketIO sio=new SocketIO(inFromClient, outToClient);
		CLI cli=new CLI(sio);
		cli.start();
		sio.close();

	}

	public class SocketIO implements DefaultIO{
		Scanner in;
		PrintWriter out;

		public  SocketIO(InputStream inFromClient, OutputStream outToClient){
			in=new Scanner(inFromClient);
			out = new PrintWriter(outToClient);
		}

		@Override
		public String readText() {
			return in.nextLine();
		}

		@Override
		public void write(String text) {
			out.println(text);
			out.flush();
		}

		@Override
		public float readVal() {
			return in.nextFloat();
		}

		@Override
		public void write(float val) {
			out.println(val);
			out.flush();
		}

		@Override
		public void close() {
			in.close();
			out.close();

		}
	}


}
