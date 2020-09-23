package APIUsageExamples;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Function;
import utils.Module;
import utils.MyPGSql;

public class CheckThis {
	
	
	public static void main(String[] args) {
		String baseDirPath = "F:/tmp/test/usage";

		// you can retrieve all modules within the database
		MyPGSql sql = new MyPGSql();
		sql.estConn();
		ArrayList<Module> modules = sql.retriModules();
		sql.closeConn();
		
		// or specify the module with its name and ID in the database
		Module module = new Module("zlib#gcc-7-O3-g-x64#minigzip", 3);
		
		// three high-level APIs can be used to query binary information
		module.doExtract();
		module.doExtract_BasicInfoOnly();
		module.doExtract_BasicInfos();
		
		HashMap<Long, Function> funcs = module.funList;
		for (Long addr : funcs.keySet()) {
			Function fc = funcs.get(addr);
			
		}
		
	}
}
