package dbconstruction;


import java.util.ArrayList;

import utils.Module;
import utils.MyPGSql;

/**
 * you can delete mistakenly imported tables or 
 * all the tables in the database with this class
 * @author Administrator
 *
 */
public class clearDeletedTables {
		
	public static void main(String[] args) {
		//clear all tables in the database
		MyPGSql pgSql=new MyPGSql();
		pgSql.estConn();
		ArrayList<Module> modules = pgSql.retriModules();
		for (Module md : modules) {
			if (!md.moduleName.contains("#")) {
				System.out.println(md.moduleID+","+md.moduleName);
				pgSql.dropTable("ex_"+md.moduleID);
			}
		}
		pgSql.closeConn();
	}
}
