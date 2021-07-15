package gsxconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class CRLF {
	public static void main(String[] args) throws Exception {		
		long start = System.currentTimeMillis();
		int j=0;
		for (int i=0; i<100000000; i++) j+=i;
		System.out.println("Elapsed: "+((System.currentTimeMillis()-start)/1000F)+" seconds.");
	}
	
	private static final int LF = 10;
	
	public static void modFile(String fname, String outFile) throws IOException {
		if ((new File(outFile)).exists()) throw new IOException(outFile+" exists.");
		InputStream is = new FileInputStream(fname);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		OutputStream os = new FileOutputStream(outFile);
//		StringBuffer sb = new StringBuffer();
		String s = br.readLine();
		while (s!=null) {
			os.write(s.getBytes());
			os.write(LF);
			s = br.readLine();
		}
		br.close();
		is.close();
		os.flush();
		os.close();
	}
	
	public static int[][] diag(int n) {
		int[][] ans = new int[n][n];
		for (int i=0; i<ans.length; i++) {
			for (int j=0; j<ans[i].length; j++) {
				ans[i][j] = (i==j?1:0);
			}
		}
		return ans;
	}
	
	public static int[][] blur(int[][] x, int n) {
		int shift = n/2;
		int[][] ans = new int[x.length][x[0].length];
		for (int i=0; i<ans.length; i++) {
			for (int j=0; j<ans[i].length; j++) {
				ans[i][j]=0;
				for (int k=0; k<n; k++) {
					int ii = i+k-shift;
					if (ii<0 || ii>=x.length) continue;
					for (int L=0; L<n; L++) {
						int jj = j+L-shift;
						if (jj<0 || jj>=x[i].length) continue;
						ans[i][j] += x[ii][jj];
					}
				}
			}
		}
		return ans;
	}
	
	public static void dump(int[][] x) {
		for (int i=0; i<x.length; i++) {
			for (int j=0; j<x[i].length; j++) {
				System.out.print(x[i][j]+"\t");
			}
			System.out.println();
		}
	}	
	
	public static void dumpToFile(int[][] x, File file) throws IOException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(file));
		for (int j=0; j<x[0].length; j++) {
			pw.print("C"+(j+1));
			if (j<x[0].length-1) pw.print("\t");
		}
		pw.println();
		for (int i=0; i<x.length; i++) {
			for (int j=0; j<x[i].length; j++) {
				pw.print(x[i][j]);
				if (j<x[0].length-1) pw.print("\t");
			}
			pw.println();
		}
		pw.close();
	}
}
