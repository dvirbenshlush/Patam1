package test;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.*;

public class TimeSeries {
	Map<String, List<Float>> map = new HashMap<>();


	public TimeSeries(String csvFileName) {
		try {
			String str;
			Scanner scanner = new Scanner(
					new BufferedReader(
							new FileReader(csvFileName)));

			str = scanner.nextLine();
			String[] titles = str.split(",");
			for (String s : titles)
				map.put(s, new ArrayList<>());
			while (scanner.hasNextLine()) {
				str = scanner.nextLine();
				String[] lines = str.split(",");
				for (int i = 0; i < titles.length; i++) {
					map.get(titles[i]).add(Float.parseFloat(lines[i]));
				}
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
		public float[] getColumn(String column){
			List<Float> floatList = map.get(column);
			float[] floatArray = new float[floatList.size()];
			for (int i = 0; i < map.get(column).size(); i++) {
				floatArray[i] = floatList.get(i);
			}

			return floatArray;
		}

	public ArrayList<Float> getAttributeData(String column) {
		return (ArrayList<Float>) map.get(column);
	}
}
