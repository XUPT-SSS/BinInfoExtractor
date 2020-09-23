package utils;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import Config.Config;



public class MyPGSql {
	public String dbPath=Config.dbPath;
	public String usrName=Config.usrName;
	public String pssWord=Config.pssWord;
	private Connection c;
	private Statement stmt;
	
	/**
	 * use the db information as specified in class Config
	 */
	public MyPGSql() {
	}
	
	/**
	 * Or you can specify the db information with this one
	 * @param dbPath
	 * @param usrName
	 * @param pssWord
	 */
	public MyPGSql(String dbPath, String usrName, String pssWord)
	{
		this.dbPath=dbPath;
		this.usrName=usrName;
		this.pssWord=pssWord;
	}
	
	/**
	 * Establish connection to the database
	 * @return true indicates db connected successfully
	 */
	public boolean estConn()
	{
		c = null;
		stmt = null;		
		try {
			Class.forName("org.postgresql.Driver");
			c=DriverManager.getConnection(dbPath,usrName,pssWord);
			//System.out.println("Database is opened successfully");
			stmt = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Remember to close the established connection
	 * @return
	 */
	public boolean closeConn()
	{
		try {
			stmt.close();
			c.close();
			stmt = null;
			c = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public void dropTable(String name)
	{
		String[] tableNames = {"_type_instances","_types","_address_comments","_address_references",
				"_base_types", "_basic_block_instructions","_basic_blocks","_callgraph",
				"_control_flow_graphs","_expression_nodes","_expression_substitutions",
				"_expression_tree_nodes","_expression_trees","_expression_type_instances",
				"_expression_types","_functions","_instructions","_operands",
				"_operands","_type_instances","_sections"};
		try {
			String sql="";
		    for (String str : tableNames) {
				sql = "DROP TABLE IF EXISTS "+name+str+ " CASCADE";	//级联删除
				stmt.execute(sql);
			}
		    sql="DROP TABLE IF EXISTS "+name+"_type_renderers";
		    stmt.executeUpdate(sql);
		    sql="DROP TABLE IF EXISTS "+name+"_type_substitution_paths";
		    stmt.executeUpdate(sql);
		    String id = name.split("_")[1];
		    deleteItems(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void deleteItems(String id)
	{
		try {
			String sql = "DELETE FROM modules WHERE id = "+id;
		    stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Retrieve all modules from the database
	 * @return
	 */
	public ArrayList<Module> retriModules()
	{
		/*Module=>
		 * id integer NOT NULL DEFAULT nextval('bn_modules_id_seq'::regclass), -- The id of the module.
		 * raw_module_id integer, -- The id of the corresponding raw module.
		 * name text NOT NULL, -- The name of the module.
		 * description text NOT NULL, -- The description of the module.
		 * md5 character(32) NOT NULL, -- The md5 hash of the binary which corresponds to this module.
		 * sha1 character(40) NOT NULL, -- The sha1 has of the binary which corresponds to this module.
		 * debugger_id integer, -- The id of the debugger currently active for this module.
		 * image_base bigint NOT NULL DEFAULT 0, -- The image base of the executable represented by the module.
		 * file_base bigint NOT NULL DEFAULT 0, -- The file base of the executable represented by the module.
		 * import_time timestamp without time zone NOT NULL DEFAULT now(), -- The time of import.
		 * modification_date timestamp without time zone NOT NULL DEFAULT now(), -- The time when the database was last updated.
		 * data bytea, -- The data of binary represented by the module.
		 * stared boolean NOT NULL DEFAULT false, -- Flags if the module has been stared.
		 * initialization_state integer NOT NULL DEFAULT 0, -- Indicates the initialization state of the module
		 * */
		try {	
			String sql = "SELECT id,name FROM modules";
			ResultSet modules = stmt.executeQuery(sql);
			ArrayList<Module> moduleList=new ArrayList<>();
			Module module = null;
			while (modules.next()) {
				String moduleName=modules.getString("name");
				int moduleID=modules.getInt("id");
				module=new Module(moduleName, moduleID);
				moduleList.add(module);
			}
			modules.close();
			module = null;
			return moduleList;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the map from the Raw IDs of modules to their IDs
	 * @return
	 */
	public HashMap<String, String> retriModuleRawID2IDMap ()
	{
		/* bn_modules =>
		 * id integer NOT NULL DEFAULT nextval('bn_modules_id_seq'::regclass), -- The id of the module.
		 * raw_module_id integer, -- The id of the corresponding raw module.
		 * name text NOT NULL, -- The name of the module.
		 * description text NOT NULL, -- The description of the module.
		 * md5 character(32) NOT NULL, -- The md5 hash of the binary which corresponds to this module.
		 * sha1 character(40) NOT NULL, -- The sha1 has of the binary which corresponds to this module.
		 * debugger_id integer, -- The id of the debugger currently active for this module.
		 * image_base bigint NOT NULL DEFAULT 0, -- The image base of the executable represented by the module.
		 * file_base bigint NOT NULL DEFAULT 0, -- The file base of the executable represented by the module.
		 * import_time timestamp without time zone NOT NULL DEFAULT now(), -- The time of import.
		 * modification_date timestamp without time zone NOT NULL DEFAULT now(), -- The time when the database was last updated.
		 * data bytea, -- The data of binary represented by the module.
		 * stared boolean NOT NULL DEFAULT false, -- Flags if the module has been stared.
		 * initialization_state integer NOT NULL DEFAULT 0, -- Indicates the initialization state of the module
		 */
		try {
			String sql="SELECT id, raw_module_id FROM bn_modules";
			ResultSet modules = stmt.executeQuery(sql);
			HashMap<String, String> rawID2IDMap=new HashMap<>();
			while(modules.next())
			{
				String id=modules.getString("id");
				String rawID=modules.getString("raw_module_id");
				rawID2IDMap.put(rawID, id);
			}			
			modules.close();
			return rawID2IDMap;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retrieve all functions within the specified module
	 * @param moduleID
	 * @return  the function list organized in a HashMap
	 */
	public HashMap<Long, Function> retriFunctions(int moduleID)
	{
		/* Function=>
		 * Such as: ex_10(module ID)_functions, this tables contains the following fields:
		 * address bigint NOT NULL,	-- The address of the function.
		 * name text NOT NULL,	-- The original name of the function, corresponds to original_name in bn_functions
		 * demangled_name text,
		 * has_real_name boolean NOT NULL,
		 * type integer NOT NULL DEFAULT 0,	--0 indicates normal, 2 indicates import
		 * module_name text,	-- corresponds to parent_module_name in bn_functions
		 * stack_frame integer,	-- corresponds to stack_frame
		 * prototype integer,	-- corresponds to prototype
		 * 
		 * Examples:
		 * 16785804,strchr,,TRUE,2,msvcrt,135
		 * 16787160,sub_10026D8,,FALSE,0,vcredist_x86.exe,10,156
		 * 4201268,?terminate@@YAXXZ,terminate(void),TRUE,3,dot.exe,,
		 */
		try {
			String sql = "SELECT address,name,has_real_name,type,module_name FROM ex_"+moduleID+"_functions"
					+ " ORDER BY type";
	      	ResultSet functions = stmt.executeQuery(sql);
	      	HashMap<Long, Function> funList=new HashMap<>();
	      	Function function = null;
	      	while(functions.next()){
	      		int funType=functions.getInt("type");
	      		long funAddr=functions.getLong("address");
	      		boolean hasRealName = functions.getBoolean("has_real_name");
	      		String funName=functions.getString("name");
	      		String moduleName=functions.getString("module_name");
	      		function=new Function(funName, funAddr, funType, moduleName, hasRealName);
	      		funList.put(funAddr,function);
	         }
	         functions.close();
	         functions = null;
	         return funList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Retrieve all instructions within the specified module
	 * @param moduleID
	 * @return
	 */
	public ArrayList<Instruction> retriInstructions(int moduleID)
	{
		/*
		 * Instructions=> 
		 * organize instruction with the following fields:
		 * 1. instruction address
		 * 2. mnemonic
		 * 3. operator //以后处理
		 * 4. ID of the basic block the instruction belongs to
		 * 5. relative location of the instruction within the basic block
		 * 
		 * By conducting union query on the tables:
		 * 1. ex_10(moduleID)_instructions
		 * 2. ex_10(moduleID)_basic_block_instructions
		 * 
		 */
		try {
			String instructionTable="ex_" + moduleID + "_instructions";
			String bbInstructionTable="ex_" + moduleID + "_basic_block_instructions";
			String sql="SELECT "+instructionTable+".address,"+instructionTable+".mnemonic,"
					+ bbInstructionTable+".basic_block_id,"+bbInstructionTable+".sequence FROM "
					+instructionTable+" JOIN "+bbInstructionTable
					+" ON "+instructionTable+".address="+bbInstructionTable+".instruction"
					+ " ORDER BY " + instructionTable+".address";
			ResultSet instructions=stmt.executeQuery(sql);
			ArrayList<Instruction> insList=new ArrayList<>();
			Instruction ins = null;
			while (instructions.next()) {
				int insAddr=instructions.getInt(1);
				String insMnemonic=instructions.getString(2);
				int insBBIDBelongs=instructions.getInt(3);
				int insLocaInBB=instructions.getInt(4);
				ins=new Instruction(insAddr, insMnemonic, insMnemonic, insBBIDBelongs, insLocaInBB);
				insList.add(ins);
			}
			instructions.close();
			instructions = null;
			return insList; 
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * obtain all operand items by retrieving the database only once, 
	 * then group them to each instruction
	 * 
	 * @param moduleID
	 * @return
	 */
	public HashMap<Integer,ArrayList<OperandTree>> retriOperandsBatch(int moduleID) {
		try {
			String operandsTable = "ex_" + moduleID + "_operands";
			String treeMapingTable = "ex_" + moduleID + "_expression_tree_nodes";
			String treeTable = "ex_" + moduleID + "_expression_nodes";

			String sql = "SELECT address,"
					+ treeTable + ".id AS expression_tree_id," + operandsTable
					+ ".position AS operand_position," + treeTable + ".type AS expression_tree_type,"
					+ "symbol, immediate," + treeTable + ".parent_id AS expression_tree_parent_id"

					+ " FROM " + operandsTable + " JOIN " + treeMapingTable + " ON " + operandsTable
					+ ".expression_tree_id=" + treeMapingTable + ".expression_tree_id"

					+ " LEFT JOIN " + treeTable + " ON " + treeTable + ".id=" + treeMapingTable + ".expression_node_id"

					// the following ordering is very very very important!!!
					+ " ORDER BY " + operandsTable + ".address," +operandsTable + ".position," + treeTable + ".position," + treeTable + ".id";
			
			// System.out.println(sql);
			ResultSet allInsOPTress=stmt.executeQuery(sql);
			SqlOperandProvider provider = new SqlOperandProvider(allInsOPTress);
			HashMap<Integer, ArrayList<OfflineOperandProvider>> offLineProvider=new HashMap<>();
			OfflineOperandProvider item = null;
			while(allInsOPTress.next()){
				int insAddr=provider.getInsAddr();
				int opTreeID=provider.getExpressionTreeID();
				int oprndPosition = provider.getOperandPosition();
				int opTType = provider.getExpressionTreeType();
				String symbol = provider.getSymbol();
				String immediate = provider.getImmediate();
				int parentID = provider.getParentID();
				
//				String tmp=insAddr+"#"+opTreeID+"#"+oprndPosition;
//				if (opTType == 2) {
//					tmp +="#"+immediate;
//				} else {
//					tmp +="#"+symbol;
//				}
//				System.out.println(tmp+"#"+parentID);
				
				item=new OfflineOperandProvider(opTreeID, oprndPosition, opTType, symbol, immediate, parentID);
				if (!offLineProvider.containsKey(insAddr)) {
					offLineProvider.put(insAddr, new ArrayList<OfflineOperandProvider>());
				}
				offLineProvider.get(insAddr).add(item);		
			}
			
			HashMap<Integer, ArrayList<OperandTree>> insAddrOprndsMap=new HashMap<>(offLineProvider.size());
			for (Integer insAddr : offLineProvider.keySet()) {
				insAddrOprndsMap.put(insAddr, 
						retriOperands(offLineProvider.get(insAddr), insAddr));
			}
			allInsOPTress.close();
			allInsOPTress = null;
			return insAddrOprndsMap;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the operands of the specified instruction
	 * @param offLineprovider
	 * @param insAddr
	 * @return
	 */
	private ArrayList<OperandTree> retriOperands(ArrayList<OfflineOperandProvider> offLineprovider, int insAddr)
	{
		ArrayList<OperandTree> opTreeList = new ArrayList<>();
		OperandTree tree = null;
		int operandPositionCounter = 0;
		
		for (OfflineOperandProvider provider : offLineprovider) {
			int opTreeID = provider.getExpressionTreeID();
			int oprndPosition = provider.getOperandPosition();
			if (opTreeID == -1) {
				System.err.println("Error: can not get expresssion tree id from the retrived data!\n");
				continue;
			}
			if (tree == null) { // initialize a tree with the retrieved tree
								// ID;
				tree = new OperandTree(opTreeID);
			}
			
			if ((oprndPosition == -1) || (operandPositionCounter != oprndPosition)) {
				// New operand found => Save the old operand
				if (tree.getNodes().size() != 0) {
					tree.generateTree();
					opTreeList.add(tree);
				}
				// Create a new tree for the new operand.
				tree = new OperandTree(opTreeID);
				operandPositionCounter = oprndPosition;
			}
			
			OperandTreeNode operandTreeNode = null;
			if (oprndPosition != -1) {
				// No matter what happened before this if, at this
				// point the right operand tree is stored in the tree
				// variable. That means we can simply create the new
				// operand tree node and put it into the tree.

				// Note that at this point we assume that IDs of 0 are
				// invalid. This might not be the case at other databases
				// and we need to look into it.

				// Type value:
				// 1: EXPRESSION_LIS or SYMBOL
				// 2: IMMEDIATE_INTEGER
				// 3: IMMEDIATE_FLOAT
				// 4: OPERATOR
				// 5: REGISTER
				// 6: SIZE_PREFIX
				// 7: MEMDEREF
				// 2 indicates that the operand is an immediate;
				// for other values, we use their symbols, say operand string;
				int opTType = provider.getExpressionTreeType();
				String symbol = provider.getSymbol();
				String immediate = provider.getImmediate();
				String value = ""; // determine the value of the operand
									// according to the type
				if (opTType == 2) {
					value = immediate;
				} else {
					value = symbol;
				}

				int parentID = provider.getParentID();

				operandTreeNode = new OperandTreeNode(opTreeID, opTType, value, parentID, oprndPosition, insAddr);
				tree.getNodes().add(operandTreeNode);
			}
			operandTreeNode = null;
		}
		if (tree != null && tree.getNodes().size() != 0) {
			tree.generateTree();
			opTreeList.add(tree);
		}
		tree = null;
		return opTreeList;
	}
	
	/**
	 * Retrieve the ex_xxx tables to get operand tree information.
	 * Less fields are retrieved comparing with retriOperandsII(int, int),
	 * but also takes about half the time
	 * 
	 * @param moduleID
	 * @param insAddr
	 * @return
	 */
	public ArrayList<OperandTree> retriOperands(int moduleID, int insAddr) {
		try {
			String operandsTable = "ex_" + moduleID + "_operands";
			String treeMapingTable = "ex_" + moduleID + "_expression_tree_nodes";
			String treeTable = "ex_" + moduleID + "_expression_nodes";

			String sql = "SELECT " + treeTable + ".id AS expression_tree_id," + operandsTable
					+ ".position AS operand_position," + treeTable + ".type AS expression_tree_type,"
					+ "symbol, immediate," + treeTable + ".parent_id AS expression_tree_parent_id"

					+ " FROM " + operandsTable + " JOIN " + treeMapingTable + " ON " + operandsTable
					+ ".expression_tree_id=" + treeMapingTable + ".expression_tree_id"

					+ " LEFT JOIN " + treeTable + " ON " + treeTable + ".id=" + treeMapingTable + ".expression_node_id"

					+ " WHERE " + operandsTable + ".address=" + insAddr

					// the following ordering is very very very important!!!
					+ "ORDER BY " + operandsTable + ".position," + treeTable + ".position," + treeTable + ".id";
			// System.out.println(sql);
			if (insAddr==39926) {
				System.out.println("sfda ");
			}
			ResultSet opTrees = stmt.executeQuery(sql);
			ArrayList<OperandTree> opTreeList = new ArrayList<>();
			OperandTree tree = null;
			int operandPositionCounter = 0;
			SqlOperandProvider provider = new SqlOperandProvider(opTrees);

			while (opTrees.next()) {
				int opTreeID = provider.getExpressionTreeID();
				int oprndPosition = provider.getOperandPosition();
				
				
				if (opTreeID == -1) {
					System.err.println("Error: can not get expresssion tree id from the retrived data!\n");
					continue;
				}
				if (tree == null) { // initialize a tree with the retrieved tree
									// ID;
					tree = new OperandTree(opTreeID);
				}

				if ((oprndPosition == -1) || (operandPositionCounter != oprndPosition)) {
					// New operand found => Save the old operand
					if (tree.getNodes().size() != 0) {
						tree.generateTree();
						opTreeList.add(tree);
					}
					// Create a new tree for the new operand.
					tree = new OperandTree(opTreeID);
					operandPositionCounter = oprndPosition;
				}

				if (oprndPosition != -1) {
					// No matter what happened before this if, at this
					// point the right operand tree is stored in the tree
					// variable. That means we can simply create the new
					// operand tree node and put it into the tree.

					// Note that at this point we assume that IDs of 0 are
					// invalid. This
					// might not be the case at other databases and we need to
					// look into
					// it.

					// Type value:
					// 1: EXPRESSION_LIS or SYMBOL
					// 2: IMMEDIATE_INTEGER
					// 3: IMMEDIATE_FLOAT
					// 4: OPERATOR
					// 5: REGISTER
					// 6: SIZE_PREFIX
					// 7: MEMDEREF
					// 2 indicates that the operand is an immediate;
					// for other values, we use their symbols, say operand
					// string;
					int opTType = provider.getExpressionTreeType();
					String symbol = provider.getSymbol();
					String immediate = provider.getImmediate();
					String value = ""; // determine the value of the operand
										// according to the type
					if (opTType == 2) {
						value = immediate;
					} else {
						value = symbol;
					}

					int parentID = provider.getParentID();

					OperandTreeNode operandTreeNode = new OperandTreeNode(opTreeID, opTType, value, parentID,
							oprndPosition, insAddr);
					// System.out.println(operandTreeNode.toString());
					/*
					 * String
					 * line=opTreeID+"#"+oprndPosition+"#"+opTType+"#"+symbol
					 * +"#"+immediate+"#"+replacement+"#"+target+"#"+parentID
					 * +"#"+substituteTypeID+"#"+substitutionOffset+"#"
					 * +substitutePosition+"#"+referType+"#"+rawTypeSubstitution
					 * +"#"+instanceID; System.out.println(line);
					 */

					tree.getNodes().add(operandTreeNode);
				}
			}
			if (tree != null && tree.getNodes().size() != 0) {
				tree.generateTree();
				opTreeList.add(tree);
			}
			opTrees.close();
			opTrees.close();
			return opTreeList;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 当前的实现，时间开销太大，必须想办法处理
	 * Retrieve the ex_xxx tables to get operand tree information.
	 * More fields are retrieved comparing with retriOperands(int, int),
	 * but also takes more time
	 * @param moduleID
	 * @param insAddr
	 * @return
	 */
	public ArrayList<OperandTree> retriOperandsII(int moduleID, int insAddr)
	{
		/*
		 * OperandTrees correlated with an instruction=> 
		 * the following fields are required to construct an operandtree:
		 * 1. instruction address
		 * 2. mnemonic
		 * 3. operator //以后处理
		 * 4. ID of the basic block the instruction belongs to
		 * 5. relative location of the instruction within the basic block
		 * 
		 * By conducting union query on the tables:
		 * 1. ex_10(moduleID)_instructions
		 * 2. ex_10(moduleID)_basic_block_instructions
		 * 
		 */
		try {
			String operandsTable="ex_"+moduleID+"_operands";
			String treeMapingTable="ex_"+moduleID+"_expression_tree_nodes";
			String treeTable="ex_"+moduleID+"_expression_nodes";
			String expTypesTable="ex_"+moduleID+"_expression_types";
			String addRefTable="ex_"+moduleID+"_address_references";
			String expSubstateTable="ex_"+moduleID+"_expression_substitutions";
			String expTypeInstanceTable="ex_"+moduleID+"_expression_type_instances";
			
			String sql="SELECT "+treeTable+".id AS expression_tree_id,"
					+ operandsTable+".position AS operand_position,"
					+ treeTable+".type AS expression_tree_type,"
					+ "symbol, immediate, replacement,"
					+ addRefTable+".destination AS target,"
					+ treeTable+".parent_id AS expression_tree_parent_id,"
					+ expTypesTable+".type AS expression_types_type,"
					+ expTypesTable+".offset AS expression_types_offset,"
					+ expTypesTable+".position AS expression_types_position,"
					+ expTypesTable+".path AS expression_types_path,"
					+ addRefTable+".type AS address_references_type,"
					+ expTypeInstanceTable+".type_instance_id AS type_instance_id"
					
					+ " FROM "+operandsTable+" JOIN " +treeMapingTable
					+ " ON "+operandsTable+".expression_tree_id="+treeMapingTable+".expression_tree_id"
					
					+" LEFT JOIN "+treeTable
					+" ON "+treeTable+".id="+treeMapingTable+".expression_node_id"
					
					+" LEFT JOIN "+addRefTable
					+" ON "+addRefTable+".address="+operandsTable+".address"
					+" AND "+addRefTable+".position="+operandsTable+".position"
					
					+" LEFT JOIN "+expSubstateTable
					+" ON "+expSubstateTable+".address="+operandsTable+".address"
					+" AND "+expSubstateTable+".position="+operandsTable+".position"
					+" AND "+expSubstateTable+".expression_node_id="+treeTable+".id"
					
					+" LEFT JOIN "+expTypesTable
					+" ON "+expTypesTable+".address="+operandsTable+".address"
					+" AND "+expTypesTable+".expression_id="+treeTable+".id"
					
					+" LEFT JOIN "+expTypeInstanceTable
					+" ON "+expTypeInstanceTable+".address="+operandsTable+".address"
					+" AND "+expTypeInstanceTable+".position="+operandsTable+".position"
					+" AND "+expTypeInstanceTable+".expression_node_id="+treeTable+".id"
					
					+" WHERE "+operandsTable+".address="+insAddr
					
					//the following ordering is very very very important!!!
					+ "ORDER BY "+operandsTable+".position,"
					+ treeTable+".position,"
					+ treeTable+".id";
			
			ResultSet opTrees=stmt.executeQuery(sql);
			ArrayList<OperandTree> opTreeList=new ArrayList<>();
			OperandTree tree=null;
			int operandPositionCounter=0;
			SqlOperandProvider provider=new SqlOperandProvider(opTrees);
			
			while (opTrees.next()) {
				int opTreeID=provider.getExpressionTreeID();
				int oprndPosition=provider.getOperandPosition();
				if (opTreeID==-1) {
					System.err.println("Error: can not get expresssion tree id from the retrived data!\n");
					continue;
				}
				if (tree == null) {	//initialize a tree with the retrieved tree ID; 
					tree = new OperandTree(opTreeID);
				}				
				
				if((oprndPosition==-1)||(operandPositionCounter!=oprndPosition)){
					// New operand found => Save the old operand
					if(tree.getNodes().size()!=0){
						tree.generateTree();
						opTreeList.add(tree);
					}				
					// Create a new tree for the new operand.
					tree=new OperandTree(opTreeID);
					operandPositionCounter=oprndPosition;
				}
				
				if (oprndPosition!=-1) {
					// No matter what happened before this if, at this
			        // point the right operand tree is stored in the tree
			        // variable. That means we can simply create the new
			        // operand tree node and put it into the tree.

			        // Note that at this point we assume that IDs of 0 are invalid. This
			        // might not be the case at other databases and we need to look into
			        // it.
					
					//Type value:
					//1: EXPRESSION_LIS or SYMBOL
					//2: IMMEDIATE_INTEGER
					//3: IMMEDIATE_FLOAT
					//4: OPERATOR
					//5: REGISTER
					//6: SIZE_PREFIX
					//7: MEMDEREF
					//2 indicates that the operand is an immediate;
					//for other values, we use their symbols, say operand string;		 
					int opTType=provider.getExpressionTreeType();
					String symbol=provider.getSymbol();
					String immediate=provider.getImmediate();					
					String value="";	//determine the value of the operand according to the type
					if (opTType==2) {
						value=immediate;
					}else{
						value=symbol;
					}
					
					String replacement=provider.getReplacement();
					int parentID=provider.getParentID();
					
					int substituteTypeID=provider.getSubstitutionTypeID();		
					int substitutionOffset=provider.getSubstitutionOffset();
					int substitutePosition=provider.getSubstitutionPosition();
					Array pathArray=provider.getSubstitutionPath();
					//构建rawTypeSubstitution，暂时先写在这，该功能以后再封装成单独的函数
					String rawTypeSubstitution = null;
					Integer[] integ=null;
					if (substituteTypeID != -1) {	//即不为空
						rawTypeSubstitution="("+insAddr+","+opTreeID+","+substitutePosition
								+","+substituteTypeID+","+substitutionOffset;
						if (pathArray==null) {
							rawTypeSubstitution+="Empty Array)";
						}else{
							integ=(Integer[])pathArray.getArray();
							for (int i = 0; i < integ.length; i++) {
								rawTypeSubstitution+=integ[i].toString()+":";
							}
						}						
					}
					
					
					int instanceID=provider.getTypeInstanceID();
					
					//String target=provider.getTarget();
					//String referType=provider.getReferencesType();
					ArrayList<Reference> referenceList=parseReferences(opTreeID, provider);
					    
					
					OperandTreeNode operandTreeNode=new OperandTreeNode(opTreeID,opTType,value,
							parentID,replacement,referenceList,
							rawTypeSubstitution,
							instanceID,oprndPosition,insAddr);
					//System.out.println(operandTreeNode.toString());
					/*String line=opTreeID+"#"+oprndPosition+"#"+opTType+"#"+symbol
					+"#"+immediate+"#"+replacement+"#"+target+"#"+parentID
					+"#"+substituteTypeID+"#"+substitutionOffset+"#"
					+substitutePosition+"#"+referType+"#"+rawTypeSubstitution
					+"#"+instanceID;
					System.out.println(line);*/
					
					tree.getNodes().add(operandTreeNode);
				}			
			}
			if (tree!=null&&tree.getNodes().size()!=0) {
				tree.generateTree();
				opTreeList.add(tree);
			}
			return opTreeList; 
						
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	
	/**
	 * 备份，直接读取
	 * @param moduleID
	 * @param insAddr
	 * @return
	 */
//	public ArrayList<OperandTree> retriOperandTree(int moduleID, int insAddr)
//	{
//		/*
//		 * OperandTrees correlated with an instruction=> 
//		 * the following fields are required to construct an operandtree:
//		 * 1. instruction address
//		 * 2. mnemonic
//		 * 3. operator //以后处理
//		 * 4. ID of the basic block the instruction belongs to
//		 * 5. relative location of the instruction within the basic block
//		 * 
//		 * By conducting union query on the tables:
//		 * 1. ex_10(moduleID)_instructions
//		 * 2. ex_10(moduleID)_basic_block_instructions
//		 * 
//		 */
//		try {
//			String operandsTable="bn_operands";
//			String treeMapingTable="bn_expression_tree_mapping";
//			String treeTable="bn_expression_tree";
//			String expTypesTable="bn_expression_types";
//			String addRefTable="bn_address_references";
//			String expSubstateTable="bn_expression_substitutions";
//			String expTypeInstanceTable="bn_expression_type_instances";
//			
//			String sql="SELECT "+treeTable+".id AS expression_tree_id,"
//					+ operandsTable+".position AS operand_position,"
//					+ treeTable+".type AS expression_tree_type,"
//					+ "symbol, immediate, replacement, target,"
//					+ treeTable+".parent_id AS expression_tree_parent_id,"
//					+ expTypesTable+".base_type_id AS expression_types_type,"
//					+ expTypesTable+".offset AS expression_types_offset,"
//					+ expTypesTable+".position AS expression_types_position,"
//					+ expTypesTable+".path AS expression_types_path,"
//					+ addRefTable+".type AS address_references_type,"
//					+ expTypeInstanceTable+".type_instance_id AS type_instance_id"
//					
//					+ " FROM "+operandsTable+" JOIN " +treeMapingTable
//					+ " ON "+operandsTable+".module_id="+treeMapingTable+".module_id"
//					+ " AND "+operandsTable+".expression_tree_id="+treeMapingTable+".tree_id"
//					
//					+" LEFT JOIN "+treeTable
//					+" ON "+treeTable+".module_id="+treeMapingTable+".module_id"
//					+" AND "+treeTable+".id="+treeMapingTable+".tree_node_id"
//					
//					+" LEFT JOIN "+addRefTable
//					+" ON "+addRefTable+".module_id="+treeMapingTable+".module_id"
//					+" AND "+addRefTable+".address="+operandsTable+".address"
//					+" AND "+addRefTable+".position="+operandsTable+".position"
//					
//					+" LEFT JOIN "+expSubstateTable
//					+" ON "+expSubstateTable+".module_id="+treeTable+".module_id"
//					+" AND "+expSubstateTable+".address="+operandsTable+".address"
//					+" AND "+expSubstateTable+".position="+operandsTable+".position"
//					+" AND "+expSubstateTable+".expression_id="+treeTable+".id"
//					
//					+" LEFT JOIN "+expTypesTable
//					+" ON "+expTypesTable+".module_id="+treeTable+".module_id"
//					+" AND "+expTypesTable+".address="+operandsTable+".address"
//					+" AND "+expTypesTable+".expression_id="+treeTable+".id"
//					
//					+" LEFT JOIN "+expTypeInstanceTable
//					+" ON "+expTypeInstanceTable+".module_id="+treeTable+".module_id"
//					+" AND "+expTypeInstanceTable+".address="+operandsTable+".address"
//					+" AND "+expTypeInstanceTable+".position="+operandsTable+".position"
//					+" AND "+expTypeInstanceTable+".expression_id="+treeTable+".id"
//					
//					+" WHERE "+operandsTable+".module_id="+moduleID
//					+" AND "+operandsTable+".address="+insAddr;
//			
//			ResultSet opTrees=stmt.executeQuery(sql);
//			//ArrayList<OperandTreeNode> opTreeNodeList=new ArrayList<>();
//			ArrayList<OperandTree> opTreeList=new ArrayList<>();
//			OperandTree tree=null;
//			int operandPositionCounter=0;
//			SqlOperandProvider sqlOperandProvider=new SqlOperandProvider(opTrees);
//			
//			while (opTrees.next()) {
//				int opTreeID=opTrees.getInt("expression_tree_id");
//				int oprndPosition=opTrees.getInt("operand_position");
//				if (opTrees.wasNull()) {
//					oprndPosition=-1;
//				}
//				
//				if (tree==null) {
//					tree=new OperandTree(opTreeID);					
//				}
//				
//				if((oprndPosition==-1)||(operandPositionCounter!=oprndPosition)){
//					// New operand found => Save the old operand
//					if(tree.getNodes().size()!=0){
//						opTreeList.add(tree);
//					}
//					
//					// Create a new tree for the new operand.
//					tree=new OperandTree(opTreeID);
//					operandPositionCounter=oprndPosition;
//				}
//				
//				if (oprndPosition!=-1) {
//					/*Type value:
//					 * 2 indicates that the operand is an immediate
//					 * for other values, we use their symbols, say operand string
//					 * */
//					int opTType=opTrees.getInt("expression_tree_type");
//					String symbol=opTrees.getString("symbol");
//					long immediate=opTrees.getLong("immediate");
//					String value="";
//					if (opTType==2) {
//						value+=immediate;
//					}else{
//						value=symbol;
//					}
//					
//					String replacement=opTrees.getString("replacement");
//					int target=opTrees.getInt("target");
//					int parentID=opTrees.getInt("expression_tree_parent_id");
//					int substituteTypeID=opTrees.getInt("expression_types_type");
//					int substitutionOffset=opTrees.getInt("expression_types_offset");
//					int substitutePosition=opTrees.getInt("expression_types_position");
//					String referType=opTrees.getString("address_references_type");
//					Array pathArray=opTrees.getArray("expression_types_path");
//					
//					//构建rawTypeSubstitution，暂时先写在这，该功能以后再封装成单独的函数
//					String rawTypeSubstitution = null;
//					Integer[] integ=null;
//					if (substituteTypeID != 0) {	//即不为空
//						rawTypeSubstitution="("+insAddr+","+substitutePosition
//								+","+opTreeID+","+substituteTypeID+",";
//						if (pathArray==null) {
//							integ=new Integer[0];
//						}else{
//							integ=(Integer[])pathArray.getArray();
//						}
//						
//						rawTypeSubstitution+="{},"+substitutionOffset+")";	
//					}
//					
//					int instanceID=opTrees.getInt("type_instance_id");
//					ArrayList<Reference> referenceList=parseReferences(opTreeID, opTrees);
//					    
//					OperandTreeNode operandTreeNode=new OperandTreeNode(opTreeID,opTType,value,
//							parentID,replacement,referenceList,
//							rawTypeSubstitution,
//							instanceID,oprndPosition,insAddr);
//					System.out.println(operandTreeNode.toString());
//					/*String line=opTreeID+"#"+oprndPosition+"#"+opTType+"#"+symbol
//					+"#"+immediate+"#"+replacement+"#"+target+"#"+parentID
//					+"#"+substituteTypeID+"#"+substitutionOffset+"#"
//					+substitutePosition+"#"+referType+"#"+rawTypeSubstitution
//					+"#"+instanceID;
//					System.out.println(line);*/
//					
//					tree.getNodes().add(operandTreeNode);
//				}			
//			}
//			if (tree!=null&&tree.getNodes().size()!=0) {
//				opTreeList.add(tree);
//			}
//			return opTreeList; 
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	
	
	
	/**
	 * Retrieve all call pairs to generate a static call graph for the module
	 * @param moduleID
	 * @param funMap
	 * @return
	 */
	public RawCG retriCallGraph(int moduleID, HashMap<Long, Function> funMap)
	{
		 /*Function Call Graph =>
         *  id integer NOT NULL DEFAULT nextval('ex_2_callgraph_id_seq'::regclass),	--ID that uniquely identify this call
         *  source bigint NOT NULL,	--the start address of the caller
         *  source_basic_block_id integer NOT NULL,	--ID of the basic block(within the caller function) in which the call happens
         *  source_address bigint NOT NULL,	--instruction address of this 'call' instruction
         *  destination bigint NOT NULL,	--the start address of the callee
         */
		try {
			String sql = "SELECT source,source_basic_block_id,source_address,destination FROM ex_" 
					+ moduleID + "_callgraph";
			ResultSet funCalls = stmt.executeQuery(sql);
			RawCG cg = new RawCG(); // Store a call graph
			CGEdge edge = null;
			while(funCalls.next()){
				int srcFunAddr = funCalls.getInt("source");
				int dstFunAddr = funCalls.getInt("destination");
				int srcBBID=funCalls.getInt("source_basic_block_id");
				int callInsAddr=funCalls.getInt("source_address");
				Function srcFun = funMap.get(Long.valueOf(srcFunAddr));	//must get existing functions constructed before rather than creating a new one
				Function dstFun = funMap.get(Long.valueOf(dstFunAddr));			
				if (srcFun != null) {
					cg.addNodes(srcFun);
				}
				if (dstFun != null) {
					cg.addNodes(dstFun);
				}
				if (srcFun != null && dstFun != null) {
					edge = new CGEdge(srcFun, dstFun, srcBBID, callInsAddr);
					cg.addEdges(edge);
				}
			}
			funCalls.close();
			funCalls = null;
			return cg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retrieve all CFGs for the functions
	 * @param moduleID
	 * @param blockMap
	 * @return
	 */
	public HashMap<Long, RawCFG> retriCFGs(int moduleID, HashMap<Integer, BasicBlock> blockMap){
		/*Control Flow Graph =>
		 * id integer NOT NULL DEFAULT nextval('ex_1_control_flow_graphs_id_seq'::regclass),
		 * parent_function bigint NOT NULL,
		 * source integer NOT NULL,
		 * destination integer NOT NULL,
		 * type integer NOT NULL DEFAULT 0
		 * */
		try {
			String sql = "SELECT parent_function,source,destination FROM ex_" + moduleID + "_control_flow_graphs ORDER BY parent_function";
			ResultSet cfgs = stmt.executeQuery(sql);
			HashMap<Long, RawCFG> funCFGMap=new HashMap<>();
			CFGEdg edge = null;
			RawCFG tmp = null;
			while (cfgs.next()) {
				long parentFunAddr=cfgs.getLong("parent_function");
				//if a new CFG retrieved
				if (!funCFGMap.containsKey(parentFunAddr)) {
					tmp=new RawCFG();
					funCFGMap.put(parentFunAddr, tmp);
				}
				//otherwise fill the edge to corresponding CFG
				tmp=funCFGMap.get(parentFunAddr);
				int srcBBAddr = cfgs.getInt("source");
				int dstBBAddr = cfgs.getInt("destination");
				BasicBlock srcBB = blockMap.get(srcBBAddr);
				BasicBlock dstBB = blockMap.get(dstBBAddr);			
				if (srcBB != null) {	//用HashSet可以试试直接添加
					if (!tmp.nodes.contains(srcBB)) {
						tmp.addNodes(srcBB);
					}
				}
				if (dstBB != null) {
					if (!tmp.nodes.contains(dstBB)) {
						tmp.addNodes(dstBB);
					}
				}
				if (srcBB != null && dstBB != null) {
					edge = new CFGEdg(srcBB, dstBB);
					if (!tmp.edges.contains(edge)) {
						tmp.addEdges(edge);
						srcBB.outBlocks.add(dstBB);
						dstBB.inBlocks.add(srcBB);
					}
				}
			}
			cfgs.close();
			cfgs = null;
			return funCFGMap;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	/**
	 * Retrieve all basic blocks within the specified module
	 * @param moduleID
	 * @return a hashmap with elements <ID, BB>
	 */
	public HashMap<Integer,BasicBlock> retriBasicBlocks(int moduleID)
	{
		/*
		 * Basic Blocks=> 
		 * organize the basic block into a hashmap <blockID,blockAddress>, 
		 * and add the block into the function's block list that it resides in
		 *
		 * Database table information: 
		 * id integer NOT NULL, --block ID that uniquely identify the block 
		 * parent_function bigint NOT NULL,	--the start address of the function the block resides in address
		 * bigint NOT NULL, --the start address of the basic block
		 *
		 * Examples: 
		 * 1,16787160,16787160 
		 * 2,16787160,16787178
		 */
		try {
			String sql = "SELECT id,parent_function,address FROM ex_" + moduleID + "_basic_blocks";			
			ResultSet blocks = stmt.executeQuery(sql);			
			HashMap<Integer, BasicBlock> blockMap = new HashMap<Integer, BasicBlock>();
			BasicBlock bb = null;
			while (blocks.next()) {
				int blockId = blocks.getInt("id");
				long blockAddr = blocks.getLong("address");
				long funBelongsTo = blocks.getLong("parent_function");
				bb = new BasicBlock(blockId, blockAddr, funBelongsTo);
				blockMap.put(blockId, bb);
			}
			blocks.close();
			blocks = null;
			return blockMap;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * Parses the outgoing references of an operand expression.
	 * @param expressionID: expressionId The expression ID of the operand expression.
	 * @param dataSet: Provides the reference data.
	 * @return The outgoing references of the operand expression.
	 */
	private ArrayList<Reference> parseReferences(int expressionID, SqlOperandProvider dataSet) {
		try {
			ArrayList<Reference> references = new ArrayList<>();
			boolean hasReferences = false;
			
			Reference reference = null;
			do {
				reference = dataSet.getReference();
				if (reference == null) {
					if (hasReferences) {
						dataSet.prev();
					}
					break;
				}

				hasReferences = true;
				int currentExprID = dataSet.getExpressionTreeID();

				if (expressionID != currentExprID) {
					dataSet.prev();
					break;
				}

				references.add(reference);

			} while (dataSet.next());

			return references;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
