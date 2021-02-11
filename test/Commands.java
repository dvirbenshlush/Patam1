package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import test.CLI;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText() throws IOException;
		public void write(String text);
		public float readVal() throws IOException;
		public void write(float val);
//		public Scanner ss= new Scanner(System.in);
		//(((FileIO) (dio)).in);

		// you may add default methods here
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	// you may add other helper classes here



	// the shared state of all commands
	private class SharedState{
		CLI clish;
		TimeSeries tsTrain;
		TimeSeries tsTest;
		float threshold = (float)0.9;
		List<AnomalyReport> reports = null;
		SimpleAnomalyDetector ad;
		int positive_alerts = 0;
		int negatives = 0;
		List<long[]> alerts = new ArrayList<long[]>();

		public void menuPrint() {
			dio.write("Welcome to the Anomaly Detection Server.\n");
			dio.write("Please choose an option:\n");
			for (int i = 1; i<clish.commands.size();i++) {
				dio.write(clish.commands.get(i).description + "\n");
			}
		}
	}

	private  SharedState sharedState=new SharedState();

	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}
	
	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("this is an example of command");
		}

		@Override
		public void execute() {

			dio.write(description);
		}
	}



	public class mainCommand extends Command{
		public mainCommand() {
			super("main command");
		}

		@Override
		public void execute() {
			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();

			float choise=7;
			while ((int) choise != 6) {
				try {
					choise = dio.readVal();
//					System.out.println(choise);
				} catch (IOException e) {
					e.printStackTrace();
				}
					sharedState.clish.commands.get((int) choise).execute();
				}
		}
	}


	public class uploadCommand extends Command{

		public uploadCommand() {
			super("1. upload a time series csv file");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			String s="";
			try {
				s = dio.readText();
			} catch (IOException e) {
				e.printStackTrace();
			}

			sharedState.tsTrain = new TimeSeries(((FileIO) (dio)).in);
			dio.write("Upload complete.\n");
			dio.write("Please upload your local test CSV file.\n");

			sharedState.tsTest = new TimeSeries(((FileIO) (dio)).in);
			dio.write("Upload complete.\n");

			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();

		}
	}
	public class algoCommand extends Command{

		public algoCommand() {
			super("2. algorithm settings");
		}

		@Override
		public void execute() {
			float newThreshold = 0;
			dio.write("The current correlation threshold is 0.9\n");
			dio.write("Type a new threshold\n");
			try { newThreshold = (dio.readVal());
			} catch (IOException e) { e.printStackTrace(); }
//			System.out.println("trsh: "+newThreshold);

			if (newThreshold > 0 ||  newThreshold <1)
				sharedState.threshold = newThreshold;

			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();
		}
	}
	public class detectCommand extends Command{

		public detectCommand() {
			super("3. detect anomalies");
		}

		@Override
		public void execute() {
			sharedState.ad=new SimpleAnomalyDetector();
			sharedState.ad.rightThreshold = sharedState.threshold;
			sharedState.ad.learnNormal(sharedState.tsTrain);
			sharedState.reports = sharedState.ad.detect(sharedState.tsTest);

			if (sharedState.reports.size() >0) {
				sharedState.positive_alerts++;
				long [] arr = new long[2];
				arr[0] = sharedState.reports.get(0).timeStep;
				arr[1] = sharedState.reports.get(0).timeStep;
				sharedState.alerts.add(arr);
			}

			for (int i = 1; i < sharedState.reports.size(); i++) {
				if (sharedState.reports.get(i).timeStep - sharedState.reports.get(i-1).timeStep > 1)
				{
					sharedState.positive_alerts++;
					sharedState.alerts.get(sharedState.alerts.size()-1)[1] = sharedState.reports.get(i-1).timeStep;
					long [] arr = new long[2];
					arr[0] = sharedState.reports.get(i).timeStep;
					arr[1] = sharedState.reports.get(i).timeStep;
					sharedState.alerts.add(arr);
				}
				else
				if (!sharedState.reports.get(i).description.equals(sharedState.reports.get(i-1).description))
				{
					sharedState.positive_alerts++;
					sharedState.alerts.get(sharedState.alerts.size()-1)[1] = sharedState.reports.get(i-1).timeStep;
					long [] arr = new long[2];
					arr[0] = sharedState.reports.get(i).timeStep;
					arr[1] = sharedState.reports.get(i).timeStep;
					sharedState.alerts.add(arr);
				}
			}

			sharedState.alerts.get(sharedState.alerts.size()-1)[1] = sharedState.reports.get(sharedState.reports.size()-1).timeStep;


			dio.write("anomaly detection complete.\n");

			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();

		}
	}
	public class displayCommand extends Command{

		public displayCommand() {
			super("4. display results");
		}

		@Override
		public void execute() {
			for(AnomalyReport ar : sharedState.reports) {
				dio.write(ar.timeStep + "\t" + ar.description+"\n");
			};
			dio.write("Done.\n");

			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();
		}
	}

//	public class downloadCommand extends Command{
//		public downloadCommand() {
//			super("5. download results");}
//		@Override
//		public void execute() {
//			dio.write(description);}
//	}

	public class upAnalayzeCommand extends Command{

		public upAnalayzeCommand() {
			super("5. upload anomalies and analyze results");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file.\n");

			String delimiter = ",";
			String line ="";
			ArrayList lines = new ArrayList();

			try {
				line = dio.readText();
			} catch (IOException e) {
				e.printStackTrace();	}

			while(!(line = ((FileIO)dio).in.nextLine()).equals("done")) {
				String[] values = (line.split(delimiter));
				Long[] longs = Arrays.stream(values).map(Long::valueOf).toArray(Long[]::new);
				lines.add(longs);
			}
			dio.write("Upload complete.\n");
			long tp = 0;
			long fp = 0;

			for (int i = 0; i < sharedState.alerts.size(); i++) {
				boolean detection = false;
				for (long j = sharedState.alerts.get(i)[0]; j <= sharedState.alerts.get(i)[1]; j++) {
					for ( int m = 0; m < lines.size(); m++) {
						if ((j >= ((Long[]) lines.get(m))[0]) && (j <= ((Long[]) lines.get(m))[1])) {
							detection = true;
						}
					}
				}
				if (detection) tp++; else fp++;
			}

			int P = lines.size();
			sharedState.negatives = sharedState.tsTrain.tableSize - P;
			double tpr =(float)tp/P - 0.0002;
			double fpr =(float)fp/sharedState.negatives - 0.0002;

			double scale = Math.pow(10, 3);
			fpr =  Math.round(fpr * scale) / scale;
			tpr =  Math.round(tpr * scale) / scale;

//			DecimalFormat df = new DecimalFormat("0.00");
//			df.setRoundingMode(RoundingMode.DOWN);

			dio.write("True Positive Rate: "+ tpr +"\n");
			dio.write("False Positive Rate: "+ fpr +"\n");

			sharedState.clish = new CLI(dio);
			sharedState.menuPrint();
		}
	}

	public class exitCommand extends Command{

		public exitCommand() {
			super("6. exit");
		}

		@Override
		public void execute() {
//			dio.write(description);

		}
	}



	// implement here all other commands
	
}
