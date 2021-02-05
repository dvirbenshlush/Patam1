package test;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Commands {

	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);
		public default void uploadFile(String exit, String outputPath) {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
				String line;
				while (  (line = readText()) != null) {
					if(line.equals(exit))
						break;
					if(line.equals(""))
						continue;
					writer.println(line);
				}
				writer.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public default String[] downloadFile(String outputFileName) {
			try {
				Scanner scanner=new Scanner(new BufferedReader(new FileReader(outputFileName)));
				String str="";
				while (scanner.hasNext()){
					str+=scanner.nextLine()+"split";
				}
				scanner.close();
				return str.split("split");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return "doesnt work".split(" ");
		}


		void close();

	}

	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}




	private class SharedState{
		float threshold= (float) 0.9;
		String anomaliesFile = "anomaliesFile.txt";
		String inputPath = "input.txt";
		String resultPath = "result.txt";
		String trainPath = "trainFile.csv";
		String testPath = "testFile.csv";
		SimpleAnomalyDetector sad = new SimpleAnomalyDetector();
		public void EnterClick(Command c) {
			c.execute();
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

	public class mainCommand extends Command{

		public mainCommand() {
			super("Welcome to the Anomaly Detection Server.\n" +
					"Please choose an option:\n");
		}

		@Override
		public void execute() {
			CLI cli= new CLI(dio);
			cli.commands.forEach(command -> dio.write(command.description));
			int clientNumber = (int) dio.readVal();
			cli.commands.get(clientNumber).execute();
		}
	}

	// Command class for example:
	public class uploadCommand extends Command{

		public uploadCommand() {
			super("1. upload a time series csv file\n");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			dio.uploadFile("done",sharedState.trainPath);
			dio.write("Upload complete.\n");
			dio.write("Please upload your local test CSV file.\n");
			dio.uploadFile("done",sharedState.testPath);
			dio.write("Upload complete.\n");
			sharedState.EnterClick(new mainCommand());
		}
	}

	public class algorithmCommand extends Command {

		public algorithmCommand() {
			super("2. algorithm settings\n");
		}

		@Override
		public void execute() {

			dio.write("The current correlation threshold is " + sharedState.threshold + "\n");
			dio.write("Type a new threshold or just press enter to exit without changing\n");
			float val = dio.readVal();
			sharedState.threshold = val;
			if (sharedState.threshold < 1 && sharedState.threshold > 0) {
				sharedState.EnterClick(new mainCommand());
			} else {
				dio.write("please choose a value between 0 and 1.\n");
				sharedState.EnterClick(new algorithmCommand());
			}
		}
	}

	public class detectCommand extends Command {

		public detectCommand() {
			super("3. detect anomalies\n");
		}

		@Override
		public void execute() {
			TimeSeries ts = new TimeSeries(sharedState.trainPath);
			sharedState.sad.learnNormal(ts);
//			sharedState.sad.getNormalModel().forEach(t->t.threshold=sharedState.threshold);
			TimeSeries ts2 = new TimeSeries(sharedState.testPath);
			sharedState.sad.detect(ts2);
			dio.write("anomaly detection complete.\n");
			sharedState.EnterClick(new mainCommand());

		}
	}

	public class displayCommand extends Command {

		public displayCommand() {
			super("4. display results\n");
		}

		@Override
		public void execute() {
			TimeSeries ts = new TimeSeries(sharedState.trainPath);
			sharedState.sad.learnNormal(ts);
			TimeSeries ts2 = new TimeSeries(sharedState.testPath);
			sharedState.sad.detect(ts2).forEach(d->dio.write(d.timeStep+" "+d.description+"\n"));
			dio.write("Done\n");
			sharedState.EnterClick(new mainCommand());
		}
	}

	public class downloadCommand extends Command {

		public downloadCommand() {
			super("5. download results\n");
		}

		@Override
		public void execute() {
			FileIO fio = new FileIO(sharedState.trainPath,sharedState.resultPath);
			TimeSeries ts = new TimeSeries(sharedState.trainPath);
			sharedState.sad.learnNormal(ts);
			TimeSeries ts2 = new TimeSeries(sharedState.testPath);
			sharedState.sad.detect(ts2).forEach(d->fio.write(d.timeStep+" "+d.description+"\n"));
			fio.close();
			sharedState.EnterClick(new mainCommand());
		}
	}

	public class analyzeCommand extends Command {

		public analyzeCommand() {
			super("5. upload anomalies and analyze results\n");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file.\n");
			dio.uploadFile("done",sharedState.anomaliesFile);
			dio.write("Upload complete.\n");
			TimeSeries ts = new TimeSeries(sharedState.trainPath);
			sharedState.sad.learnNormal(ts);
			TimeSeries ts2 = new TimeSeries(sharedState.testPath);
			List<AnomalyReport> an=sharedState.sad.detect(ts2);
			int N = ts.getColumn("A").length-an.size();
			List<List<AnomalyReport>> anomalyArray = new ArrayList<>();
			List<AnomalyReport> newAnomaly = new ArrayList<>();
			int i=0;
			while (i<an.size()-1){
				while (an.get(i).description.equals(an.get(i+1).description) && an.get(i).timeStep+1==an.get(i+1).timeStep){
					newAnomaly.add(an.get(i));
					i++;
					if(i==an.size()-2)
						break;
				}
				newAnomaly.add(an.get(i));
				anomalyArray.add(newAnomaly);
				newAnomaly = new ArrayList<>();
				i++;
			}
			if (!an.get(i).description.equals(an.get(i - 1).description) || an.get(i).timeStep - 1 != an.get(i - 1).timeStep) {
				newAnomaly = new ArrayList<>();
				anomalyArray.add(newAnomaly);
			}
			anomalyArray.get(anomalyArray.size()-1).add(an.get(i));

			List<List<Long>> ranges = new ArrayList<>();
			List<Long> r = new ArrayList<>();
			String[] anomalyMachine = new String[anomalyArray.size()];
			int inx=0;
			for (List<AnomalyReport> a:anomalyArray){
				r.add((a.get(0).timeStep));
				r.add((a.get(a.size()-1).timeStep));
				anomalyMachine[inx++]=r.get(0)+","+r.get(1);
				ranges.add(r);
				r = new ArrayList<>();
			}
			String[] anomalyInputFile = dio.downloadFile(sharedState.anomaliesFile);
			int T = anomalyInputFile.length;
			int TF =0;
			int FP =0;
			int count = 0;
			for (String am:anomalyMachine) {
					for (String s : anomalyInputFile) {

					long firstIn = Long.parseLong(s.split(",")[0]);
					long lastIn = Long.parseLong(s.split(",")[1]);
					long firstOut = Long.parseLong(am.split(",")[0]);
					long lastOut = Long.parseLong(am.split(",")[1]);
					if (isTP(firstIn, lastIn, firstOut, lastOut))
						TF++;
					else
						count++;
				}
				if(count==anomalyInputFile.length-1)
					FP++;
				count=0;
			}
			FP= isFP(anomalyMachine,anomalyInputFile);
			float resTP =  ((int)((TF+0.00000)/T*1000.0))/1000.0f;
			float resFP = ((int)((FP+0.00000)/N*1000.0))/1000.0f;
			dio.write("True Positive Rate: "+resTP+"\n");
			dio.write("False Positive Rate: "+resFP+"\n");
			sharedState.EnterClick(new mainCommand());
		}

		public boolean isTP(long firstFromDetect,long lastFromDetect,long firstFromFile,long lastFromFile){
			for (long i=firstFromDetect;i<=lastFromDetect;i++){
				for (long j = firstFromFile; j <=lastFromFile; j++) {
					if(i==j)
						return  true;
				}
			}
			return false;
		}

		public int isFP(String[] anomalyMachine,String[] anomalyInputFile){
			int counter=anomalyMachine.length;
			int i = 0;
			for (String aif:anomalyInputFile){
				long firstFromFile = Long.parseLong(aif.split(",")[0]);
				long lastFromFile = Long.parseLong(aif.split(",")[1]);
				long firstFromMachine = Long.parseLong(anomalyMachine[i].split(",")[0]);
				long lastFromMachine = Long.parseLong(anomalyMachine[i].split(",")[1]);
				boolean flag=true;
				for (int j = 0; j <anomalyMachine.length ; j++) {
					flag = isTP(Long.parseLong(anomalyMachine[j].split(",")[0]), Long.parseLong(anomalyMachine[j].split(",")[1]), firstFromFile, lastFromFile);
					if(flag)
						counter--;
				}
			}
			return counter;
		}
	}

	public class exitCommand extends Command {

		public exitCommand() {
			super("6. exit\n");
		}

		@Override
		public void execute() {
			dio.write("bye");
			dio.close();
		}
	}


}