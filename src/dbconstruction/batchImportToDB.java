package dbconstruction;

import java.io.File;
import java.util.ArrayList;

import Config.Config;
import Config.IDAConfig;
import utils.ida.DBImporter;
import utils.ida.IDBGain;

/**
 * A script to batch import binary files into your database
 * @author tianz
 *
 */
public class batchImportToDB {
		
	public static void main(String[] args) {
		IDAConfig.isIA32 = true; //default is true, set it to false if you are processing 64 bit binaries
		
		String idaEXEPath= "";
		//it's your responsibility to set isIA32 to true or false according to the binaries you gonna to analyze
		if (IDAConfig.isIA32) {	//idaq to process 32 bit binaries
			idaEXEPath = IDAConfig.idaBaseDir+"/idaq.exe";
		}else{	//idaq64 to process 64 bit binaries
			idaEXEPath = IDAConfig.idaBaseDir+"/idaq64.exe";
		}
		
		
		String host=Config.host;
		int port=Config.port;
		String name=Config.dbName;
		String user=Config.usrName;
		String password=Config.pssWord;

		//the directory containing your binaries to be processed
		String binFileDirPath="F:/tmp/test/ia32";
		//the directory for storing the IDB files produced by IDA Pro
		String dstIDBDirPath="F:/tmp/test/ia32/IDBs";
		
		String suffix = "";
		if (IDAConfig.isIA32) {
			suffix = ".idb";
		}else{
			suffix = ".i64";
		}
		
		try {
			ArrayList<File> allFiles = getFilesUnderDir(new File(binFileDirPath));
			
			File dstIDBDir=new File(dstIDBDirPath);
			if (!dstIDBDir.exists()) {
				dstIDBDir.mkdirs();
			}
			
			for (File binFile : allFiles) {
				if (binFile.isFile()) {
					System.out.println("Processing "+binFile.getName());
					String dstIDBName="";
//					if (binFile.getName().lastIndexOf(".")==-1) {
//						dstIDBName=binFile.getName()+suffix;
//					}else{
//						dstIDBName=binFile.getName().substring(0, binFile.getName().lastIndexOf("."))+suffix;
//					}
					dstIDBName = binFile.getName()+suffix;
					String binFilePath=binFile.getPath();
					String dstIDBPath=dstIDBDirPath+"/"+dstIDBName;	//idc 脚本里边的路径必须是/分隔
					IDBGain.parseBin(idaEXEPath, binFilePath,dstIDBPath);
					DBImporter.createIDAProcess(idaEXEPath, dstIDBPath, host, port, user, password, name);
				}

			}	
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// For test
	public static void main1(String[] args) {
		IDAConfig.isIA32 = false; //default is true, set it to false if you are processing 64 bit binaries
		
		String idaEXEPath= "";
		//it's your responsibility to set isIA32 to true or false according to the binaries you gonna to analyze
		if (IDAConfig.isIA32) {	//idaq to process 32 bit binaries
			idaEXEPath = IDAConfig.idaBaseDir+"/idaq.exe";
		}else{	//idaq64 to process 64 bit binaries
			idaEXEPath = IDAConfig.idaBaseDir+"/idaq64.exe";
		}
		
		String host="localhost";
		int port=5432;
		String user="zztian";
		String password="openit1987";
		String name="BinaryStore";

		
		String binFileDirPath="D:/DataSpace/ExperDirs/RSPB/DataSet/AHBins";
		String dstIDBDirPath="D:/DataSpace/ExperDirs/RSPB/DataSet/AHIDBs";
		binFileDirPath = "F:/tmp/stu/xie/bins";
		dstIDBDirPath = "F:/tmp/stu/xie/idbs";
		
		String suffix = "";
		if (IDAConfig.isIA32) {
			suffix = ".idb";
		}else{
			suffix = ".i64";
		}
		
		
		try {
			ArrayList<File> allFiles = getFilesUnderDir(new File(binFileDirPath));
			
			File dstIDBDir=new File(dstIDBDirPath);
			if (!dstIDBDir.exists()) {
				dstIDBDir.mkdirs();
			}
			
			for (File binFile : allFiles) {
				if (binFile.isFile()) {
					System.out.println("Processing "+binFile.getName());
					String dstIDBName="";
//					if (binFile.getName().lastIndexOf(".")==-1) {
//						dstIDBName=binFile.getName()+suffix;
//					}else{
//						dstIDBName=binFile.getName().substring(0, binFile.getName().lastIndexOf("."))+suffix;
//					}
					dstIDBName = binFile.getName()+suffix;
					String binFilePath=binFile.getPath();
					String dstIDBPath=dstIDBDirPath+"/"+dstIDBName;	//idc 脚本里边的路径必须是/分隔
					IDBGain.parseBin(idaEXEPath, binFilePath,dstIDBPath);
					DBImporter.createIDAProcess(idaEXEPath, dstIDBPath, host, port, user, password, name);
				}
			}	
			
		}catch(Exception e){
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
