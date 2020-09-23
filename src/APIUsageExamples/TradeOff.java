package APIUsageExamples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.Buffer;
import java.util.ArrayList;

/**
 * Perform medium-level abstraction to the instructions
 * It processes on the basis of the files with fine-grained abstraction
 * @author Administrator
 *
 */
public class TradeOff {
	public static void main(String[] args) {
		try {
//			String dirpath = "D:/tmp/CompilerProvenance/PhaseI-PathExtraction";
//			String dirpath = "D:/tmp/CompilerProvenance/PhaseI-CFGExtraction";
			String dirpath = "D:/tmp/CompilerProvenance/PhaseI-OrderedPathExtraction";
			

			ArrayList<File> allFiles = getFilesUnderDir(new File(dirpath));
			for (File file : allFiles) {
				if (file.getName().endsWith("#fine.csv")) {
					String outFilePath = file.getAbsolutePath().replace("#fine.csv", "#medium.csv");
					BufferedWriter bWriter = new BufferedWriter(new FileWriter(outFilePath));
					BufferedReader bReader = new BufferedReader(new FileReader(file));
					String line = bReader.readLine();
					while (line!=null) {
						if (!line.startsWith(">>>")) {
							line = line.replaceAll("\\d+]", "IMM]");
						}
						bWriter.write(line);
						bWriter.newLine();
						line = bReader.readLine();
						
					}
					bReader.close();
					bWriter.flush();
					bWriter.close();
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static ArrayList<File> getFilesUnderDir(File dir){
		ArrayList<File> fileList=new ArrayList<>();
		if (dir.isFile()){
			fileList.add(dir);
		}
		else {
			File[] subFiles=dir.listFiles();
			for (File file : subFiles)
			{
				fileList.addAll(getFilesUnderDir(file));
			}
		}
		return fileList;
	}
}
