package test;

import java.util.ArrayList;
import java.util.List;

import static test.StatLib.linear_reg;

public class SimpleAnomalyDetector  implements TimeSeriesAnomalyDetector {

	List<CorrelatedFeatures> cf = new ArrayList<>();
	String[] names;

	@Override
	public void learnNormal(TimeSeries ts) {
		List<Float> listFL = new ArrayList<>();
		names = new String[ts.map.size()];
		int i = 0;
		for (String title : ts.map.keySet())
			names[i++] = title;

		float per = 0;
		Point[] listPoints = new Point[ts.map.get(names[0]).size()];
		Point p;

		for (int k = 0; k < ts.map.size(); k++) {
			for (int j = k+1; j < ts.map.size(); j++) {
				per = Math.abs(StatLib.pearson(ts.getColumn(names[k]), ts.getColumn(names[j])));
				if(per>0.9) {
					i=0;
					for (int m = 0; m < ts.map.get(names[0]).size(); m++) {
						p = new Point(ts.map.get(names[k]).get(m), ts.map.get(names[j]).get(m));
						listPoints[i++] = p;
					}
					Line line = linear_reg(listPoints);
					float threshold = findThreshold(listPoints,line);
					CorrelatedFeatures c = new CorrelatedFeatures(names[k], names[j], per, line, threshold);
					cf.add(c);
				}
			}
			listPoints = new Point[ts.map.get(names[0]).size()];
		}
	}

	private float findThreshold(Point points[],Line line){
		float max=0;
		for(Point p:points){
			float d=Math.abs(p.y - line.f(p.x));
			if(d>max)
				max=d;
		}
		return max;
	}


	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		List<AnomalyReport> AnomalyList = new ArrayList<>();
		for (CorrelatedFeatures c:cf){
			float[] x=ts.getColumn(c.feature1);
			float[] y=ts.getColumn(c.feature2);
			for (int i = 0; i < x.length; i++) {
				if(Math.abs(y[i] - c.lin_reg.f(x[i]))>c.threshold*1.1){
					long time =(long) ts.getColumn(names[0])[i];
					String rep = c.feature1+"-"+c.feature2;
					AnomalyList.add(new AnomalyReport(rep,i+1));
				}
			}
		}
		return AnomalyList;
	}

	public List<CorrelatedFeatures> getNormalModel(){
		return cf;
	}
}