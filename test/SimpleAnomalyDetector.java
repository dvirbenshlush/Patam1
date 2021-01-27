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

		i = 0;
		List<Line> linesList = new ArrayList<>();
		float per = 0;
		float max = 0;
		String f1 = "";
		String f2 = "";
		int l = 0;
		Point[] listPoints = new Point[ts.map.get(names[0]).size()];
		String[] fetcher1 = new String[names.length];
		String[] fetcher2 = new String[names.length];
		Point p;

		for (int k = 0; k < ts.map.size(); k++) {
			for (int j = 0; j < ts.map.size(); j++) {
				if (names[j] != names[k]) {
					per = Math.abs(StatLib.pearson(ts.getColumn(names[k]), ts.getColumn(names[j])));
					if(per>0.9) {
						for (int m = 0; m < ts.map.get(names[0]).size(); m++) {
							p = new Point(ts.map.get(names[k]).get(m), ts.map.get(names[j]).get(m));
							listPoints[i++] = p;
						}
						i = 0;
						if (per > max) {
							max = per;
							f1 = names[k];
							f2 = names[j];
						}
					}
				}
			}
			if(cf.size()<names.length/2) {
				Line line = linear_reg(listPoints);
				listPoints = new Point[ts.map.get(names[0]).size()];
				listFL.add(max);
				fetcher1[l] = f1;
				fetcher2[l] = f2;

				CorrelatedFeatures c = new CorrelatedFeatures(f1, f2, max, line, (float) 0.2);
				cf.add(c);
				l++;
				max = 0;
			}
		}
//		List<CorrelatedFeatures> cf2 = new ArrayList<>();
//		for (int j = 0; j < cf.size() / 2; j++) {
//			cf2.add(cf.get(j));
//		}
//		cf = cf2;
	}




	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		List<AnomalyReport> AnomalyList = new ArrayList<>();
		for (CorrelatedFeatures c:cf){
			float[] x=ts.getColumn(c.feature1);
			float[] y=ts.getColumn(c.feature2);
			for (int i = 0; i < x.length; i++) {
				if(Math.abs(y[i] - c.lin_reg.f(x[i]))>c.threshold*1.3){
					long time =(long) ts.getColumn(names[0])[i];
					String rep = c.feature1+"-"+c.feature2;
					AnomalyList.add(new AnomalyReport(rep,time));
				}
			}
		}
		return AnomalyList;
	}

	public List<CorrelatedFeatures> getNormalModel(){
		return cf;
	}
}
