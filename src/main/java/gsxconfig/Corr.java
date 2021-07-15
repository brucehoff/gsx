package gsxconfig;

import java.util.Random;

// this program is meant to answer the question:
// for a given vector length, what percentage of pairs of random vectors have cc>0.01?
public class Corr {
	
	private static final double ccThresh = 1-.99833;
	private static final int numVectors = 1000;
//	private static final int vectorLength = 10;
	private static final int vectorLength = 1000;
	
	public static void main(String[] args) throws Exception {	
		// generate random data
		o("Generating random data...");
		double[][] data = new double[numVectors][vectorLength];
		Random r = new Random();
		for (int i=0; i<numVectors; i++) {
			for (int j=0; j<vectorLength; j++) {
				data[i][j] = r.nextGaussian();
			}
		}
		o("...done.  Computing and counting correlations bet. "+(numVectors*(numVectors-1)/2)+" pairs...");
		double[][] normed = new double[numVectors][vectorLength];
		o("Vector length\tfrac w/ cc>"+ccThresh);
//		for (int i=2; i<vectorLength; i++) { // the vector length
		for (int i : new int[] {10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000}) { // the vector length
			// normalize the truncated vector of length i
			for (int j=0; j<numVectors; j++) normalizeInto(data[j], normed[j], i);
			int count = 0;
			int highCC = 0;
			for (int j=0; j<numVectors-1; j++) { // the first vector
				//if (j % 10000 == 0) o("\t"+(j+1)+" of "+(numVectors-1));
				for (int k=j+1; k<numVectors; k++) { // the second vector
					double corr = dot(normed[j], normed[k], i); // correlation is just the dot product of the normalized vectors
					count++;
					if (Math.abs(corr)>ccThresh) highCC++;
				} 
			}
			double frac = (double)highCC / (double)count;
			o(i+"\t"+frac);
		}
		o("...done.");
	}
			
	// make the mean zero and length 1
	private static void normalizeInto(double[] x, double[] ans, int n) {
		if (x==null || x.length<n || ans.length<n) throw new IllegalArgumentException();
		double mean = mean(x, n);
		double normSq = 0;
		for (int i=0; i<n; i++) {
			normSq += (x[i]-mean)*(x[i]-mean);
		}
		for (int i=0; i<n; i++) ans[i] = (x[i]-mean)/Math.sqrt(normSq);
	}
	

	
	private static double corr(double[] x, double[]y, int n) {
		double[] xn = normalize(x, n);
		double[] yn = normalize(y, n);
		return dot(xn,yn,n);
	}
	
	
	// make the mean zero and length 1
	private static double[] normalize(double[] x, int n) {
		if (x==null || x.length<n) throw new IllegalArgumentException();
		double[] demeaned = new double[n];
		double mean = mean(x, n);
		for (int i=0; i<n; i++) demeaned[i] = x[i]-mean;
		double[] ans = new double[n];
		double norm = Math.sqrt(dot(demeaned, demeaned, n));
		for (int i=0; i<n; i++) ans[i] = demeaned[i]/norm;
		return ans;
	}
	
	private static double mean(double[] x, int n) {
		if (x==null || x.length<n) throw new IllegalArgumentException();
		double sum = 0;
		for (int i=0; i<n; i++) sum += x[i];
		return sum/n;
	}
	 
	private static double dot(double[] x, double[] y, int n) {
		if (x==null || y==null) throw new IllegalArgumentException();
		if (x.length<n || y.length<n) throw new IllegalArgumentException();
//		if (x.length!=y.length) throw new IllegalArgumentException();
		double ans=0; 
		for (int i=0; i<n; i++) ans += x[i]*y[i];
		return ans;
	}
	
	private static void o(Object s) {System.out.println(s);}
}
