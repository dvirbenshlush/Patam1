package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	class usefulFeatures {
		public int indexFeature1,indexFeature2;
		public final Line lin_reg;
		public final float maxNormalDiff;

		public usefulFeatures(int indexFeature1, int indexFeature2, Line lin_reg, float maxNormalDiff) {
			this.indexFeature1 = indexFeature1;
			this.indexFeature2 = indexFeature2;
			this.lin_reg = lin_reg;
			this.maxNormalDiff = maxNormalDiff;
		}
	}

	public List<CorrelatedFeatures> cf = new  ArrayList();
	public List<AnomalyReport> ar = new  ArrayList();
	public List<usefulFeatures> usef = new  ArrayList();
	public float rightThreshold = (float)0.9;

	@Override
	public void learnNormal(TimeSeries ts) {
		String[] headers = (String[]) ts.table.get(0);
		ArrayList<Number[]> lines = ts.table;
		lines.remove(0);
		int tableSize = ts.tableSize;
		//System.out.println("tableSize " +tableSize);
		int numFeatures = ( lines.get(1)).length;
		//System.out.println("numFeatures " +numFeatures);

		float[][] matrix = new float[numFeatures][tableSize-1];

		for (int i = 0; i < numFeatures; i++) {
			float [] featureI = new float[tableSize-1];
			for (int j = 0; j < tableSize-1; j++) {
				matrix[i][j] = (lines.get(j))[i].floatValue();
			}
		}

		for (int i = 0; i < numFeatures; i++) {
			for (int j = i; j < numFeatures; j++) {
				if (i!=j)
				{
					String fI = (headers[i]);
					String fJ = (headers[j]);
					if (StatLib.pearson(matrix[i],matrix[j]) > rightThreshold)
					{
						Point pointIJ[] = new Point[tableSize-1];
						for (int k = 0; k < tableSize-1; k++) {
							pointIJ[k] = new Point ((matrix[i][k]),(matrix[j][k]));
						}
						CorrelatedFeatures cfIJ = new CorrelatedFeatures(fI,fJ,StatLib.pearson(matrix[i],matrix[j]), StatLib.linear_reg(pointIJ), (float) 0.2);
						cf.add(cfIJ);
						//System.out.println("pearson for features "+ fI + " and " + fJ + " is: " + StatLib.pearson(matrix[i],matrix[j]));

						float maxNormDiff = 0;
						for (int l = 0; l <  ts.tableSize-1; l++) {
							float a = pointIJ[l].x;
							float b = pointIJ[l].y;
							float regY = StatLib.linear_reg(pointIJ).f(a);
							float currDiff = Math.abs(regY-b);

							if (currDiff>maxNormDiff)
								maxNormDiff = currDiff;
						}
						usefulFeatures usefull = new usefulFeatures(i,j, StatLib.linear_reg(pointIJ),maxNormDiff);
						this.usef.add(usefull);
					}
				}
			}

		}


	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		String[] headers = (String[]) ts.table.get(0);
		ArrayList<Number[]> lines = ts.table;
		lines.remove(0);
		int tableSize = ts.tableSize;
		int numFeatures = (lines.get(1)).length;

		float[][] matrix2 = new float[numFeatures][tableSize-1];
		for (int i = 0; i < numFeatures; i++) {
			float [] featureI = new float[tableSize-1];
			for (int j = 0; j < tableSize-1; j++) {
				matrix2[i][j] = (lines.get(j))[i].floatValue();
			}
		}

		int numCorll = this.usef.size();
		for (int l = 0; l < numCorll; l++) {
			int i = this.usef.get(l).indexFeature1;
			int j = this.usef.get(l).indexFeature2;
			Line regLine = this.usef.get(l).lin_reg;
			float maxNormDiff = this.usef.get(l).maxNormalDiff;

			for (int k = 0; k < tableSize-1; k++) {
				float a = matrix2[i][k];
				float b = matrix2[j][k];
				float regY = regLine.f(a);
				float currDiff = Math.abs(regY-b);

				float gap = currDiff-maxNormDiff;

				if (gap>0.1) {
					//maxNormDiff = currDiff;
					String f1 = headers[i];
					String f2 = headers[j];
					String desc = f1 + "-" + f2;
					long timeStepush = (long)(k+1); //(long)Math.abs(maxNormDiff - currDiff);
					AnomalyReport anomalyR = new AnomalyReport(desc,timeStepush);
					//System.out.println("Anomaly Detected! - for " +desc + " diff is "+ currDiff + " vs " + maxNormDiff + " at " + timeStepush);
					//System.out.println("desc: "+ desc + " , timeStep : " + timeStepush+"\n");
					ar.add(anomalyR);
				}

			}
		}
		return ar;
	}
	
	public List<CorrelatedFeatures> getNormalModel(){
		return this.cf;
	}
}
