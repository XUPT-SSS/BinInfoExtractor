package utils.ida;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import utils.zylib.io.SystemHelpers;

/**
 * Import into DataBase the parsed essential information by
 * reading IDB files with BinExport plugin
 * @author Administrator
 *
 */
public class DBImporter {

	private static final String BINEXPORTER_VERSION="zynamics_binexport_8";
	private static final String BINEXPORTER_IDC_FILE_CONTENT=
			"#include <idc.idc> \n"
			+"static main() {\n"
			+"	Batch(0);\n"
			+"	Wait();\n"
			+"	RunPlugin(\""+BINEXPORTER_VERSION+"\",1);\n"
			+"	Exit(0);\n"
			+"}";
	private static final String BINEXPORTER_IDC_FILE="binexport.idc";
	
	
	/**
	 * Creates the IDA Pro process that is used to export the data from the IDB file to the database.
	 * @param idaEXEPath: The location of the IDA Pro executable.
	 * @param idbFileName: The location of the IDB file to import.
	 * @param host: Host of the database;
	 * @param port: Port of the database;
	 * @param user:	Name of the user used to connect to the database.
	 * @param password: Password of the user.
	 * @param name: Name of the database to connect to.
	 * @return
	 * @throws Exception
	 */
	public static Process createIDAProcess(final String idaEXEPath,
			final String idbFileName,
			final String host,
			final int port,
			final String user,
			final String password,
			final String name) throws Exception
	{
		final String tempPath=SystemHelpers.getTempDirectory();
		final File idcFile=new File(tempPath+BINEXPORTER_IDC_FILE);
		
		//[start] Create the binexport.idc file in the system temporary dir
		try {
			if (!idcFile.exists()) {
				idcFile.createNewFile();
			}
			FileWriter fw=new FileWriter(idcFile);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(BINEXPORTER_IDC_FILE_CONTENT);
			bw.flush();
			fw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//[end]
		
		//[start] Setup the invocation of the IDA to SQL exporter
		final ProcessBuilder pBuilder = new ProcessBuilder(idaEXEPath,
				"-A",
				"-OExporterHost:" + host,
		        "-OExporterPort:" + port,
		        "-OExporterUser:" + user,
		        "-OExporterPassword:" + password,
		        "-OExporterDatabase:" + name,
		        "-OExporterSchema:public",
		        IDAHelpers.getSArgument(idcFile.getAbsolutePath(), SystemHelpers.isRunningWindows()),
		        idbFileName);
		//[end]
		
		//[start] Launch the exporter to export the IDB to the database
		try {
			Process processInfo = null;
			pBuilder.redirectErrorStream(true);
			processInfo = pBuilder.start();
			
			// Java manages the streams internally - if they are full, the process blocks, i.e. IDA
		    // hangs, so we need to consume them.
			final BufferedReader bReader=new BufferedReader(new InputStreamReader(processInfo.getInputStream()));
			@SuppressWarnings("unused")
			String line;
			try {
				while((line=bReader.readLine())!=null){
					System.out.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
				bReader.close();
			}
			bReader.close();			
			return processInfo;			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("E00210: Failed attempting to launch the importer with IDA: ");
		}
		//[end]
	}
}
