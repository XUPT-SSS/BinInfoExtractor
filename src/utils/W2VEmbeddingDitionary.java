package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;


public class W2VEmbeddingDitionary {

	public static HashMap<String, double[]> embeddingDictionary = new HashMap<>();
	
	/**
	 * parse the input dictionary file to a embedding dictionary object 
	 * @param dicFilePath
	 */
	public static void generateEmbeddingDictionary(String dicFilePath) {
		try {
			File dicFile = new File(dicFilePath);
			if (!dicFile.exists()) {
				throw new Exception("The specified dictionary file does not exit!");
			}
			BufferedReader bReader = new BufferedReader(new FileReader(dicFile));
			String line = bReader.readLine();
			String[] eles = null;
			double[] vals = null;
			while (line!=null) {
				eles = line.split(",");
				vals = new double[eles.length-1];
				for (int i = 0; i < vals.length; i++) {
					vals[i] = Double.parseDouble(eles[i+1]);
				}
				embeddingDictionary.put(eles[0], vals);
				line = bReader.readLine();
			}
			bReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
