package APIUsageExamples;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import Config.Config;
import utils.BasicBlock;
import utils.CFGEdg;
import utils.CGEdge;
import utils.Function;
import utils.Instruction;
import utils.Module;
import utils.MyPGSql;
import utils.RawCFG;
import utils.RawCG;

public class CFGExtractor {

	public static void main(String[] args) {
		// ignore all modules specified with these IDs in database
		int[] filterThese = {};
		int lowBoundary = 1;	// ignore all modules whose IDs are smaller than the boundary value
		int upperBoundary = 3;
		boolean keepReg = true;
		
		try {
			String baseDirPath = "F:/tmp/test/usage/cfgs";
				
			MyPGSql sql = new MyPGSql();
			sql.estConn();
			ArrayList<Module> modules = sql.retriModules();
			sql.closeConn();
			
			BufferedWriter[] bWriter = null;
			BufferedWriter cgWriter = null;
			HashMap<Long, Function> funList =null;
			HashSet<Long> libFunAddrs = null;
			String baseFilePath = "";
			String mFilePath = "";
			
			for (Module module : modules) {
				//[start] ignore certain modules
				if (module.moduleID < lowBoundary || module.moduleID > upperBoundary) {
					continue;
				}
				boolean pass = false;
				for (int i = 0; i < filterThese.length; i++) {
					if (filterThese[i]==module.moduleID) {
						pass = true;
					}
				}
				if (pass) {
					continue;
				}
				//[end]
				
				System.out.println("Processing "+module.moduleID+":"+module.moduleName);
				module.doExtract();	// this API extracts almost everything about a module
				funList = module.funList;	
				libFunAddrs= new HashSet<>();
				for (Long addr : funList.keySet()) {
					Function func = funList.get(addr);
					if (func.funType=="import"||func.funType=="thunk"||func.funType=="adjustor_thunk") {
						libFunAddrs.add(addr);
					}
				}
				System.err.println(funList.size()+","+libFunAddrs.size());
				
				
				//[start] store all cfgs to file
				String stg = "CFG";
				int trivalFunCount = 0;
				baseFilePath = baseDirPath+"/"+module.moduleID+"#"+module.moduleName;
				bWriter = new BufferedWriter[2];
				mFilePath = baseFilePath+"#"+stg+"#corase.csv";
				bWriter[0] = new BufferedWriter(new FileWriter(mFilePath));
				mFilePath = baseFilePath+"#"+stg+"#fine.csv";
				bWriter[1] = new BufferedWriter(new FileWriter(mFilePath));
				
				trivalFunCount = doPathExtraction_CFG(keepReg, bWriter, funList, libFunAddrs);
				//Format: ModuleID#ModuleName#AllFunNum#LibFunNum#TrivalFunNum#ValidFunNum
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>MODULE&"+module.moduleID+"&"+module.moduleName+"&"+funList.size()+"&"+libFunAddrs.size());
					bw.write("&"+trivalFunCount+"&"+(funList.size()-libFunAddrs.size()-trivalFunCount));
					bw.newLine();
					bw.flush();
					bw.close();
				}
				
				//[start] store the call graph of the module
				mFilePath = baseFilePath+"#callgraph.csv";
				cgWriter = new BufferedWriter(new FileWriter(mFilePath));
				storeCG(module, cgWriter);		
				cgWriter.write(">>>MODULE&"+module.moduleID+"&"+module.moduleName+"&"+funList.size()+"&"+libFunAddrs.size());
				cgWriter.write("&"+trivalFunCount+"&"+(funList.size()-libFunAddrs.size()-trivalFunCount));
				cgWriter.newLine();
				cgWriter.flush();
				cgWriter.close();
				
				//[end]
				
				//[end]

				
				module.cleaner();
				libFunAddrs = null;
				bWriter = null;
				funList =null;	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void storeCG(Module module, BufferedWriter cgWriter) {
		try {
			RawCG cg = module.cg;
			HashMap<Long, Function> funs = module.funList;
			
			cgWriter.write(">>>Nodes:"+funs.size());
			cgWriter.newLine();
			for (Long addr : funs.keySet()) {
				Function fun = funs.get(addr);
				cgWriter.write(addr.toString()+" "+fun.fName+" "+fun.funType);
				cgWriter.newLine();
			}
			
			cgWriter.write(">>>Edges:"+cg.edges.size());
			cgWriter.newLine();
			for (CGEdge edg : cg.edges) {
				long srcFunAddr = edg.srcFun.fAddr;
				long dstFunAddr = edg.dstFun.fAddr;
				cgWriter.write(srcFunAddr+" "+dstFunAddr);
				cgWriter.newLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * simplify each instruction in the input sequence with two different abstraction strategies
	 * @param insList
	 * @param keepReg: set to True is preferred currently
	 * @param bWriter: an array consisted of two buffered file writers
	 * @param funList: function list of the module being analyzed
	 * @param libFunAddrs: addresses of library functions
	 */
	public static void insSeqAbstraction(ArrayList<Instruction> insList, Boolean keepReg, BufferedWriter[] bWriter,
			HashMap<Long, Function> funList, HashSet<Long> libFunAddrs) {
		try {
			StringBuffer sBuffer = new StringBuffer();
			StringBuffer sBuffer2 = new StringBuffer();
			String libStr = "";
			for (Instruction ins : insList) {
				if (ins.isJumpIns() || ins.isCallIns()) {
					Long libFunAddr = ins.getLibCallStr(libFunAddrs);
					if (libFunAddr != null) {
						Function libF = funList.get(libFunAddr);
						String opt = ins.operator.replaceAll("\\s+", "-");
						libStr = opt + "-" + libF.fName.replace(".", "").trim() + " ";
						sBuffer.append(libStr);
						sBuffer2.append(libStr);
					} else {
						sBuffer.append(ins.getInstructionAbstractStr_Merge(keepReg) + " ");
						sBuffer2.append(ins.getInstructionAbstractStr_Fine() + " ");
					}
				} else {
					sBuffer.append(ins.getInstructionAbstractStr_Merge(keepReg) + " ");
					sBuffer2.append(ins.getInstructionAbstractStr_Fine() + " ");
				}
			}
			bWriter[0].write(sBuffer.toString());
			bWriter[1].write(sBuffer2.toString());
			bWriter[0].newLine();
			bWriter[1].newLine();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * store the CFG to file
	 * @param cfg
	 * @param bWriter
	 */
	public static void storeCFG(RawCFG cfg, BufferedWriter[] bWriter) {	
		try {
			
			bWriter[0].write(">>>CFG&"+cfg.edges.size());
			bWriter[1].write(">>>CFG&"+cfg.edges.size());
			for (CFGEdg edg : cfg.edges) {
				int p1 = cfg.getPosition(edg.srcBlk);
				int p2 = cfg.getPosition(edg.dstBlk);
				bWriter[0].write(" "+p1+"->"+p2);
				bWriter[1].write(" "+p1+"->"+p2);
			}
			bWriter[0].newLine();
			bWriter[1].newLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Path Form III:
	 * use reduced shortest paths collected from a function's CFG
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 */
	public static int doPathExtraction_CFG(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) {
		try {
			int trivalFunCount = 0;
			ArrayList<Instruction> insList = new ArrayList<>();
	
			for (Long funAddr : funList.keySet()) {
				Function func = funList.get(funAddr);
				//ignore trivial functions
				if (Function.isLibraryOrSystemFunction(func) || Function.isThunkFunction(func)
						|| Function.isFunctionContainingNoCode(func)) {
					continue;
				} else if (Function.isTrivalFunction(func, Config.MIN_INS_NUM)) {
					trivalFunCount++;
					continue;
				}
				
				int entryID = -1;
				int bbNum = func.blocks.size();
				int edgNum = 0;
				if (bbNum <= 1) {
					// return an empty string if the function contains no basic
					// blocks or there exists no corresponding CFG for the
					// function if it
					// contains only one basic block
					for (BasicBlock bb : func.blocks) {
						insList.addAll(bb.insList);
					}
					insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
					insList.clear();
				} else {
					RawCFG cfg = func.cfg;
					BasicBlock entry = cfg.getEntryNode();
					if (entry == null) {
						throw new Exception("CFG应当有一个根节点?");
					} else {
						entryID = cfg.getPosition(entry);
						edgNum = cfg.edges.size();
						ArrayList<BasicBlock> nodes = cfg.nodes;
						for (BasicBlock bb : nodes) {
							insSeqAbstraction(bb.insList, keepReg, bWriter, funList, libFunAddrs);
						}
						storeCFG(cfg, bWriter);
					}
				}
				//Format: fName#fAddr#BBNum#EdgeNum#entryID
				//edgeNum==0 means there's no edges 
				//entryID==-1 means there's no CFG for the function
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>Func&"+func.fName+"&"+funAddr+"&"+bbNum+"&"+edgNum+"&"+entryID);
					bw.newLine();
					bw.flush();
				}
			}
			return trivalFunCount;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}