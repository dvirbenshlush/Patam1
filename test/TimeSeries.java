package test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TimeSeries {

	public ArrayList table;
	public int tableSize = 0;


	public TimeSeries(String csvFileName) {
		String file = csvFileName;
		String delimiter = ",";
		String line;
		ArrayList lines = new ArrayList();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			line = br.readLine();
			String [] headers = (line.split(delimiter));
			lines.add(headers);
			tableSize++;

			while(((line = br.readLine()) != null) && (line) != "done"){
				tableSize++;
				String [] values = (line.split(delimiter));
				Float[] floats = Arrays.stream(values).map(Float::valueOf).toArray(Float[]::new);
				lines.add(floats);
			}
			this.table = lines;
			//lines.forEach(l -> l.toString());
		//	lines.forEach(l -> System.out.println(l));
		} catch (Exception e){
			System.out.println(e);
		}
	}


	public TimeSeries(Scanner in)  {
		//String file = csvFileName;
		String delimiter = ",";
		String line;
		ArrayList lines = new ArrayList();
		//try (BufferedReader br = new BufferedReader((in))) {
			line = in.nextLine();
			String [] headers = (line.split(delimiter));
			lines.add(headers);
			tableSize++;

			boolean cont = true;
			//if (in.nextLine().equals("done") ) {cont = false;}
//nextLine().equals("done")
			while(!(line = in.nextLine()).equals("done")){
				tableSize++;
				String [] values = (line.split(delimiter));
				Float[] floats = Arrays.stream(values).map(Float::valueOf).toArray(Float[]::new);
				lines.add(floats);
				//if (in.nextLine().equals("done") ) {cont = false;}
			}
			this.table = lines;
			//lines.forEach(l -> l.toString());
			//	lines.forEach(l -> System.out.println(l));
		}

	}
