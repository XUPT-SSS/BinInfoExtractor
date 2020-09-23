package utils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.ldap.StartTlsRequest;

import com.sun.corba.se.spi.orbutil.fsm.State;
import com.sun.xml.internal.ws.transport.http.server.EndpointImpl;

import sun.misc.Cleaner;



public class Module {
	public String moduleName;
	public int moduleID;
	public RawCG cg;
	public HashMap<Long, Function> funList;
	
	
	public Module(String moduleName, int moduleID) {
		this.moduleName=moduleName;
		this.moduleID=moduleID;
		this.funList=new HashMap<>();
	}
	
	public void cleaner(){
		this.funList.clear();
		this.funList = null;
		this.cg = null;
	}
	
	/**
	 * Check whether the module exists in the database
	 * @return
	 */
	public boolean exist() {
		MyPGSql pgSql = new MyPGSql();
		pgSql.estConn();
		// Check whether the module exists in the database
		ArrayList<Module> modules = pgSql.retriModules();
		pgSql.closeConn();
		if (modules.contains(this)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Extract required information (almost all) about the module from the database
	 * @return 1 if no problem occurs
	 */
	public int doExtract() {
		funList=new HashMap<>();

		MyPGSql pgSql = new MyPGSql();
		pgSql.estConn();

		// Check whether the module exists in the database
		ArrayList<Module> modules = pgSql.retriModules();
		if (modules.contains(this)) {
			long startMemUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			long startTime = System.currentTimeMillis();
			addFuns(pgSql.retriFunctions(moduleID));
			long endTime = System.currentTimeMillis();
			System.out.println("Retriving functions takes: "+(endTime-startTime)/1000.0+"s");

			startTime = System.currentTimeMillis();
			addCG(pgSql.retriCallGraph(moduleID, funList));
			endTime = System.currentTimeMillis();
			System.out.println("Retriving call graph takes: "+(endTime-startTime)/1000.0+"s");

			startTime = System.currentTimeMillis();
			// Fill other contents (i.e. basic blocks and cfg) to each function
			// besides function name, address and type
			HashMap<Integer, BasicBlock> allBBs = pgSql.retriBasicBlocks(moduleID);
			// distribute each bb to its corresponding function
			for (Integer bbID : allBBs.keySet()) { 
				BasicBlock bb = allBBs.get(bbID);
				long fBelongsTo = bb.funBelongsTo;
				if (funList.containsKey(fBelongsTo)) {
					funList.get(fBelongsTo).addblock(bb);
				}
			}
			endTime = System.currentTimeMillis();
			System.out.println("retriving all bbs takes:"+(endTime-startTime)/1000.0+"s");
			
			startTime = System.currentTimeMillis();
			HashMap<Long, RawCFG> funCFGMap = pgSql.retriCFGs(moduleID, allBBs);
			for (Long fAddr : funCFGMap.keySet()) {
				funList.get(fAddr).setCFG(funCFGMap.get(fAddr));
			}
			endTime = System.currentTimeMillis();
			System.out.println("retriving all cfgs takes:"+(endTime-startTime)/1000.0+"s");

			// Fill other contents (i.e. instructions) to each basic block
			// besides its name, address and function-belongings
			startTime = System.currentTimeMillis();
			ArrayList<Instruction> allIns = pgSql.retriInstructions(moduleID);
			endTime = System.currentTimeMillis();
			System.out.println("retriving all instructions takes:"+(endTime-startTime)/1000.0+"s");
			
			startTime = System.currentTimeMillis();
			long endMemUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			System.out.println("Storing information(Fun, CG, CFG) retrived from db costs :"+(endMemUsed-startMemUsed)/(1024*1024.0)+"M");
			startMemUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			// obtain all operand items by retrieving the database only once,
			// then group them to each instruction
			HashMap<Integer, ArrayList<OperandTree>> insAddrOprndMap = pgSql.retriOperandsBatch(moduleID);
			ArrayList<OperandTree> operandTrees = null;
			for (Instruction ins : allIns) {
				int bbBelongsTo = ins.bbBelongsTo;
				allBBs.get(bbBelongsTo).addIns(ins);
//				ArrayList<OperandTree> operandTrees = new ArrayList<>();
				if (insAddrOprndMap.containsKey(ins.address)) {
					operandTrees = insAddrOprndMap.get(ins.address);
				}else{
					operandTrees = new ArrayList<>();
				}
				ins.setOperandTrees(operandTrees);
			}
			endTime = System.currentTimeMillis();
			System.out.println("retriving all operands takes:"+(endTime-startTime)/1000.0+"s");
			endMemUsed = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			System.out.println("Storing information(instructions) retrived from db costs :"+(endMemUsed-startMemUsed)/(1024*1024.0)+"M");

		} else {
			System.err.println("There exists no such module in the database!");
			return -1;
		}

		pgSql.closeConn();
		return 1;
	}
	
	/**
	 * Extract required information about the module from the database.
	 * Neither call graph nor CFGs will be extracted from the database with this method to save space
	 * @return 1 if no problem occurs
	 */
	public int doExtract_BasicInfoOnly() {
		MyPGSql pgSql = new MyPGSql();
		pgSql.estConn();

		// Check whether the module exists in the database
		ArrayList<Module> modules = pgSql.retriModules();
		if (modules.contains(this)) {
			addFuns(pgSql.retriFunctions(moduleID));
			// Fill other contents (i.e. basic blocks and cfg) to each function
			// besides function name, address and type
			HashMap<Integer, BasicBlock> allBBs = pgSql.retriBasicBlocks(moduleID);
			// distribute each bb to its corresponding function
			for (Integer bbID : allBBs.keySet()) { 
				BasicBlock bb = allBBs.get(bbID);
				long fBelongsTo = bb.funBelongsTo;
				if (funList.containsKey(fBelongsTo)) {
					funList.get(fBelongsTo).addblock(bb);
				}
			}
			
			// Fill other contents (i.e. instructions) to each basic block
			// besides its name, address and function-belongings
			ArrayList<Instruction> allIns = pgSql.retriInstructions(moduleID);
			
			// obtain all operand items by retrieving the database only once,
			// then group them to each instruction
			HashMap<Integer, ArrayList<OperandTree>> insAddrOprndMap = pgSql.retriOperandsBatch(moduleID);
			ArrayList<OperandTree> operandTrees = null;
			for (Instruction ins : allIns) {
				int bbBelongsTo = ins.bbBelongsTo;
				allBBs.get(bbBelongsTo).addIns(ins);
				if (insAddrOprndMap.containsKey(ins.address)) {
					operandTrees = insAddrOprndMap.get(ins.address);
				}else{
					operandTrees = new ArrayList<>();
				}
				ins.setOperandTrees(operandTrees);
			}
		} else {
			System.err.println("There exists no such module in the database!");
			return -1;
		}

		pgSql.closeConn();
		pgSql = null;
		return 1;
	}
	
	/**
	 * Extract required information about the module from the database.
	 * Call graph will not be extracted from the database with this method to save space
	 * @return 1 if no problem occurs
	 */
	public int doExtract_BasicInfos() {
		MyPGSql pgSql = new MyPGSql();
		pgSql.estConn();

		// Check whether the module exists in the database
		ArrayList<Module> modules = pgSql.retriModules();
		if (modules.contains(this)) {
			addFuns(pgSql.retriFunctions(moduleID));
			// Fill other contents (i.e. basic blocks and cfg) to each function
			// besides function name, address and type
			HashMap<Integer, BasicBlock> allBBs = pgSql.retriBasicBlocks(moduleID);
			// distribute each bb to its corresponding function
			for (Integer bbID : allBBs.keySet()) { 
				BasicBlock bb = allBBs.get(bbID);
				long fBelongsTo = bb.funBelongsTo;
				if (funList.containsKey(fBelongsTo)) {
					funList.get(fBelongsTo).addblock(bb);
				}
			}
			
			// extract CFG of each function
			HashMap<Long, RawCFG> funCFGMap = pgSql.retriCFGs(moduleID, allBBs);
			for (Long fAddr : funCFGMap.keySet()) {
				funList.get(fAddr).setCFG(funCFGMap.get(fAddr));
			}
			
			
			// Fill other contents (i.e. instructions) to each basic block
			// besides its name, address and function-belongings
			ArrayList<Instruction> allIns = pgSql.retriInstructions(moduleID);
			
			// obtain all operand items by retrieving the database only once,
			// then group them to each instruction
			HashMap<Integer, ArrayList<OperandTree>> insAddrOprndMap = pgSql.retriOperandsBatch(moduleID);
			ArrayList<OperandTree> operandTrees = null;
			for (Instruction ins : allIns) {
				int bbBelongsTo = ins.bbBelongsTo;
				allBBs.get(bbBelongsTo).addIns(ins);
				if (insAddrOprndMap.containsKey(ins.address)) {
					operandTrees = insAddrOprndMap.get(ins.address);
				}else{
					operandTrees = new ArrayList<>();
				}
				ins.setOperandTrees(operandTrees);
			}
		} else {
			System.err.println("There exists no such module in the database!");
			return -1;
		}

		pgSql.closeConn();
		pgSql = null;
		return 1;
	}
	
	
	/**
	 * Set the moudle's attribute: call graph
	 * @param cg
	 */
	public void addCG(RawCG cg)
	{
		this.cg=cg;
		cg.moduleName=moduleName;
	}
	
	/**
	 * Add to the module's function list
	 * @param funs
	 */
	public void addFuns(HashMap<Long, Function> funs)
	{
		this.funList.putAll(funs);
	}
	
	/**
	 * Add to the module's function list with filtering
	 * @param funs
	 * @param filter
	 */
	public void addFuns(HashMap<Long, Function> funs, String filter)
	{
		for (long addr : funs.keySet()) {
			Function fun=funs.get(addr);
			if (fun.funType.equals(filter)) {
				this.funList.put(addr, fun);
			}
		}
	}

	@Override
	public boolean equals(Object obj) { 
        if(this ==obj){
            return true;            
        } 
        if (obj!=null && obj instanceof Module) {   
            Module u = (Module) obj;   
            return this.moduleName.equals(u.moduleName);   
        }else{
            return false;
        }
    }
}
