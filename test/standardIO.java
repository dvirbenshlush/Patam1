package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

import test.Commands.DefaultIO;

public class standardIO implements DefaultIO{

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//	Scanner in=new Scanner(System.in);
//	PrintWriter out;
	public standardIO() {
	//	in=new Scanner(System.in);
//		out=new PrintWriter(System.out);
	}
	
	@Override
	public String readText() throws IOException {
		//return new BufferedReader(in).readLine();
		return br.readLine();
	}

	@Override
	public void write(String text) {
		System.out.print(text);
	}

	@Override
	public float readVal()  {
		//float f=Float.parseFloat(new BufferedReader(in).readLine());
		float f= 0;
		try {
			f = Float.parseFloat(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	@Override
	public void write(float val) {
		System.out.print(val);
	}

	public void close()  {
		//in.close();
		//out.close();
	}
}
