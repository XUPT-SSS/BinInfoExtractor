package utils.ida;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import utils.zylib.io.SystemHelpers;

/**
 * Process the binary with IDA Pro.
 * Store and package the parsed information into an IDB format file.
 * @author ZZTian
 *
 */
public class IDBGain {

	private static final String IDC_FILE_CONTENT_Head=
			"#include <idc.idc> \n"
			+"static main() {\n"
			+"	SetShortPrm(INF_AF2, GetShortPrm(INF_AF2) | AF2_DODATA);\n"
			+"	Message(\"Waiting for the end of the auto analysis...\\n\");\n"
			+"	Wait();\n"
			+"	Message(\"\\n\\n------ Creating the output file.... --------\\n\");\n"
			//+"	auto file = GetIdbPath()[0:-4] + \".asm\";\n"
			//+"	WriteTxt(file, 0, BADADDR);	 // create the assembler file\n"
			;
	private static final String IDC_FILE_CONTENT_Tail=
			"	Message(\"All done, exiting...\\n\");\n"
			+"	Exit(0);\n"
			+"}";
	private static final String IDC_FILE="parse.idc";
	
	/**
	 * Generate the content of the idc script
	 * @param dstIDBPath
	 * @return
	 */
	private static String generateIDCScript(String dstIDBPath)
	{
		String idbSaveStr="	SaveBase(\""+dstIDBPath+"\",0);\n";
		String idcContent=IDC_FILE_CONTENT_Head+idbSaveStr+IDC_FILE_CONTENT_Tail;
		return idcContent;
	}
	
	
	/**
	 * Start an IDA process to process the binary file.
	 * It runs in backstage.
	 * @param idaEXEPath
	 * @param binFilePath
	 * @param dstIDBPath
	 * @return
	 * @throws Exception
	 */
	public static Process parseBin(final String idaEXEPath, 
			final String binFilePath,
			final String dstIDBPath) throws Exception {
		final String tempPath = SystemHelpers.getTempDirectory();
		final File idcFile = new File(tempPath + IDC_FILE);
		
		// [start] Create the parse.idc file in the system temporary dir
		try {
			if (!idcFile.exists()) {
				idcFile.createNewFile();
			}
			FileWriter fw = new FileWriter(idcFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(generateIDCScript(dstIDBPath));
			bw.flush();
			fw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// [end]
		
		//[start] Create dir if the dst IDB file is to Store in a directory
		try {
			File dstIDBFile=new File(dstIDBPath);
			File parentDir=dstIDBFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//[end]

		// [start] Setup the commandline arguments for invoking IDA Pro in the backstage
		final ProcessBuilder pBuilder = new ProcessBuilder(idaEXEPath, "-A", "-c",
				IDAHelpers.getSArgument(idcFile.getAbsolutePath(), SystemHelpers.isRunningWindows()), binFilePath);
		// [end]

		// [start] Launch IDA to analyze the binary input file
		try {
			Process processInfo = null;
			pBuilder.redirectErrorStream(true);
			processInfo = pBuilder.start();

			// Java manages the streams internally - if they are full, the
			// process blocks, i.e. IDA
			// hangs, so we need to consume them.
			final BufferedReader bReader = new BufferedReader(new InputStreamReader(processInfo.getInputStream()));
			@SuppressWarnings("unused")
			String line;
			try {
				while ((line = bReader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
				bReader.close();
			}
			bReader.close();
			
			//delete the default idb file
			
			try {
				File binFile=new File(binFilePath);
				String binFileName=binFile.getName();
				File dstIDBFile=new File(dstIDBPath);
				if (!binFile.getParent().equals(dstIDBFile.getParent())) {
					String defaultIDBName=binFileName;
					if (binFile.getName().lastIndexOf(".")==-1) {
						defaultIDBName+=".idb";
					}else{
						defaultIDBName=binFileName.substring(0, binFileName.lastIndexOf("."))+".idb";
					}
					
					String defaultIDBPath=binFile.getParent()+"/"+defaultIDBName;
					File defaultIDBFile=new File(defaultIDBPath);
					if (defaultIDBFile.exists()) {
						defaultIDBFile.delete();
					}
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			return processInfo;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("E00210: Failed attempting to launch the importer with IDA: ");
		}
		// [end]
		
	}
}
