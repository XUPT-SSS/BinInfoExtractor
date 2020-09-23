package APIUsageExamples;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import Config.Config;
import APIUsageExamples.Utils.ShorestPathBBCover;
import utils.BasicBlock;
import utils.CFGEdg;
import utils.Function;
import utils.Instruction;
import utils.Module;
import utils.MyPGSql;
import utils.RawCFG;

/**
 * Implement phase I of extracting abstracted paths with different abstraction strategies
 * @author Administrator
 *
 */
public class SequenceRepresentationExtractor {

	public static void main(String[] args) {
		// ignore all modules specified with these IDs in database
		int[] filterThese = {};
		int lowBoundary = 2;	// ignore all modules whose IDs are smaller than the boundary value
		int upperBoundary = 3;
		boolean keepReg = true;
//		String[] PATH_STGs = {"BB","EDGE","RSP","BCP","WHOLE"};
		String[] PATH_STGs = {"WHOLE"};
		try {
			String baseDirPath = "F:/tmp/test/usage/insSeqs";
				
			MyPGSql sql = new MyPGSql();
			sql.estConn();
			ArrayList<Module> modules = sql.retriModules();
			sql.closeConn();
			
			BufferedWriter[] bWriter = null;
			HashMap<Long, Function> funList =null;
			HashSet<Long> libFunAddrs = null;
			String baseFilePath = "";
			String mFilePath = "";
			
			for (Module module : modules) {
				//[start] ignore certain modules
//				if (module.moduleID!=7) {
//					continue;
//				}
				
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
				module.doExtract_BasicInfos();
				funList = module.funList;
				libFunAddrs= new HashSet<>();
				for (Long addr : funList.keySet()) {
					Function func = funList.get(addr);
					if (func.funType=="import"||func.funType=="thunk"||func.funType=="adjustor_thunk") {
						libFunAddrs.add(addr);
					}
				}
				System.err.println(funList.size()+","+libFunAddrs.size());
				
				
				//[start]
				int trivalFunCount = 0;
				baseFilePath = baseDirPath+"/"+module.moduleID+"#"+module.moduleName;
				for (String stg : PATH_STGs) {
					bWriter = new BufferedWriter[2];
					mFilePath = baseFilePath+"#"+stg+"#coarse.csv";
					bWriter[0] = new BufferedWriter(new FileWriter(mFilePath));
					mFilePath = baseFilePath+"#"+stg+"#fine.csv";
					bWriter[1] = new BufferedWriter(new FileWriter(mFilePath));
					switch (stg) {
					case "BB":
						trivalFunCount = doPathExtraction_BB(keepReg, bWriter, funList, libFunAddrs);
						break;
					case "EDGE":
						trivalFunCount = doPathExtraction_Edge(keepReg, bWriter, funList, libFunAddrs);
						break;
					case "RSP":
						trivalFunCount = doPathExtraction_RSP(keepReg, bWriter, funList, libFunAddrs);
						break;
					case "BCP":
						trivalFunCount = doPathExtraction_BSP(keepReg, bWriter, funList, libFunAddrs);
						break;
					case "WHOLE":
						trivalFunCount = doPathExtraction_FunAsWhole(keepReg, bWriter, funList, libFunAddrs);
						break;
					default:
						System.err.println("No such path extraction strategy");
						break;
					}
					
					//Format: ModuleID#ModuleName#AllFunNum#LibFunNum#TrivalFunNum#ValidFunNum
					for (BufferedWriter bw : bWriter) {
						bw.write(">>>MODULE&"+module.moduleID+"&"+module.moduleName+"&"+funList.size()+"&"+libFunAddrs.size());
						bw.write("&"+trivalFunCount+"&"+(funList.size()-libFunAddrs.size()-trivalFunCount));
						bw.newLine();
						bw.flush();
						bw.close();
					}
				}
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
	
	
	/**
	 * simply each instruction in the input sequence with two different abstraction strategies
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
	 * Path Form I:
	 * instructions contained within a single basic block is treated as a path
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 * @throws IOException
	 */
	public static int doPathExtraction_BB(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) {
		try {
			int trivalFunCount = 0;
			for (Long funAddr : funList.keySet()) {
				Function func = funList.get(funAddr);
				//ignore trivial functions
				if (Function.isLibraryOrSystemFunction(func) || Function.isThunkFunction(func)) {
					continue;
				} else if (Function.isTrivalFunction(func, Config.MIN_INS_NUM)) {
					trivalFunCount++;
					continue;
				}
				
				int insCount = 0;
				HashSet<BasicBlock> bbs = func.blocks;
				for (BasicBlock bb : bbs) {
					ArrayList<Instruction> insList = bb.insList;
					insCount += insList.size();
					insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
				}
				
				//Format: fName#BBNum#InsNum#PathNum
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>Func&"+func.fName+"&"+bbs.size()+"&"+insCount+"&"+bbs.size());
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
	
	
	/**
	 * Path Form II:
	 * a single edge connecting two basic blocks form a path
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 */
	public static int doPathExtraction_Edge(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) {
		try {
			int trivalFunCount = 0;
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
				
				int pathNum = 0;
				ArrayList<Instruction> insList = new ArrayList<>();
				int bbNum = func.blocks.size();
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
					pathNum++;
				}else{
					for (CFGEdg edg : func.cfg.edges) {
						insList.addAll(edg.srcBlk.insList);
						insList.addAll(edg.dstBlk.insList);
						insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
						insList.clear();
					}
					pathNum = func.cfg.edges.size();
				}
				//Format: fName#BBNum#InsNum#PathNum
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>Func&"+func.fName+"&"+bbNum+"&"+0+"&"+pathNum);
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
	
	
	/**
	 * Path Form III:
	 * use reduced shortest paths collected from a function's CFG
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 */
	public static int doPathExtraction_RSP(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) {
		try {
			int trivalFunCount = 0;
			HashSet<ArrayList<Integer>> paths = new HashSet<>();
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
				
				int pathNum = 0;
				int bbNum = func.blocks.size();
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
					pathNum++;
				} else {
					RawCFG cfg = func.cfg;
					BasicBlock entry = cfg.getEntryNode();
					if (entry == null) {
						throw new Exception("怎么可能！CFG必须有一个根节点");
					} else {
						int[][] matrix = cfg.getAdjMatrix();
						ShorestPathBBCover sPathBBCover = new ShorestPathBBCover(matrix);
						int entryID = cfg.getPosition(entry);
						if (Config.FLOYDALG) {
							paths = sPathBBCover.doPathFind(entryID);
						} else {
							// this branch will be executed by default
							paths = sPathBBCover.doPathFind_DijkstraII(entryID);
						}

						for (ArrayList<Integer> path : paths) {
							for (Integer id : path) {
								BasicBlock bb = cfg.nodes.get(id);
								insList.addAll(bb.insList);
							}
							insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
							insList.clear();
						}
						pathNum = paths.size();
						paths.clear();
					}
				}
				
				//Format: fName#BBNum#InsNum#PathNum
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>Func&"+func.fName+"&"+bbNum+"&"+0+"&"+pathNum);
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
	
	
	
	/**
	 * Path Form IV:
	 * use branch-splitting/separated paths collected from a function's CFG
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 */
	public static int doPathExtraction_BSP(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) {
		try {
			int trivalFunCount = 0;
			HashSet<ArrayList<BasicBlock>> pathSet = new HashSet<>();
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
				
				int pathNum = 0;
				int bbNum = func.blocks.size();
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
					pathNum++;
				} else {
					RawCFG cfg = func.cfg;
					BasicBlock entry = cfg.getEntryNode();
					if (entry == null) {
						throw new Exception("怎么可能！CFG必须有一个根节点");
					} else {
						// get minimum branch separated path set
						pathSet = collectBranchSeparatedPaths(func);
						for (ArrayList<BasicBlock> path : pathSet) {
							for (BasicBlock bb : path) {
								insList.addAll(bb.insList);
							}
							insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
							insList.clear();
						}
						pathNum = pathSet.size();
						pathSet.clear();
					}
				}
				
				//Format: fName#BBNum#InsNum#PathNum
				for (BufferedWriter bw : bWriter) {
					bw.write(">>>Func&"+func.fName+"&"+bbNum+"&"+0+"&"+pathNum);
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
	
	/**
	 * all instructions in a single function form a path
	 * @param keepReg
	 * @param bWriter
	 * @param funList
	 * @param libFunAddrs
	 * @return 
	 * @throws IOException
	 */
	public static int doPathExtraction_FunAsWhole(boolean keepReg, BufferedWriter[] bWriter, HashMap<Long, Function> funList,
			HashSet<Long> libFunAddrs) throws IOException {
		int trivalFunCount = 0;
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
			
			TreeSet<BasicBlock> bbs = func.getOrderedBlocks();
			
			ArrayList<Instruction> insList = new ArrayList<>();
			for (BasicBlock bb : bbs) {
				insList.addAll(bb.insList);
			}
			insSeqAbstraction(insList, keepReg, bWriter, funList, libFunAddrs);
			
			//Format: fName#BBNum#InsNum#PathNum
			for (BufferedWriter bw : bWriter) {
				bw.write(">>>Func&"+func.fName+"&"+bbs.size()+"&"+insList.size()+"&0");
				bw.newLine();
				bw.flush();
			}
		}
		return trivalFunCount;
	}
	
	
	private static HashSet<ArrayList<BasicBlock>> collectBranchSeparatedPaths(Function function){
		try {
			HashSet<BasicBlock> bbs = function.blocks;
			HashSet<ArrayList<BasicBlock>> pathSet = new HashSet<>();
			HashSet<ArrayList<BasicBlock>> paths = null;
			HashSet<BasicBlock> inBlocks = null;
			ArrayList<BasicBlock> tmp = null;
			for (BasicBlock bb : bbs) {
				if (bb.outBlocks.size()==0) {	// for each leaf node
					paths = nodeToNearestBranchPaths(bb);
					pathSet.addAll(paths);
				}
				if (bb.outBlocks.size()>=2) {	// for each branch node
					paths = new HashSet<>();
					inBlocks = bb.inBlocks;
					if (inBlocks.size()==0) {	// the root node may also have two branches
						//replace all statements in this branch with empty if want to include root node in the path
						//correspondingly you need to modify near line 207 commented with 'you can delete the if branch... '
						tmp = new ArrayList<>();
						tmp.add(bb);
						paths.add(tmp);
					}else{
						for (BasicBlock inblk : inBlocks) {
							paths.addAll(nodeToNearestBranchPaths(inblk));
						}
						for (ArrayList<BasicBlock> subPath : paths) {
							subPath.add(bb);
						}
					}
					pathSet.addAll(paths);
					paths = null;
				}
			}
			return pathSet;	
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private static HashSet<ArrayList<BasicBlock>> nodeToNearestBranchPaths(BasicBlock node){
		try {
			HashSet<ArrayList<BasicBlock>> paths = new HashSet<>();
			ArrayList<BasicBlock> path = new ArrayList<>();
			Stack<BasicBlock> stk = new Stack<>();
			BasicBlock top = null;
			ArrayList<BasicBlock> tail = null;
			while (true) {
				// for root node of a cfg; 
				// if the root has two or more out edges, handle it in this case 
				// rather than in the else if branch
				if (node.inBlocks.size()==0) {
					// you can delete the if branch to include root node in the path
					if (node.outBlocks.size()<2) {	// if the root node is not a branch node
						stk.push(node);
					}
					while (!stk.isEmpty()) {
						top = stk.pop();
						path.add(top);
					}
					paths.add(path);
					return paths;
				}else if (node.outBlocks.size()>=2) {	// a branch node(and not a root node) is encountered
					while (!stk.isEmpty()) {
						top = stk.pop();
						path.add(top);
					}
					paths.add(path);
					return paths;
				}else{
					stk.push(node);
					HashSet<BasicBlock> parents = node.inBlocks;
					if (parents.size()==1) {
						node = (BasicBlock)parents.toArray()[0];
						continue;
					}else {
						tail = new ArrayList<>();
						for (BasicBlock bb : stk) {
							tail.add(bb);
						}
						for (BasicBlock bb : parents) {
							HashSet<ArrayList<BasicBlock>> subPaths = nodeToNearestBranchPaths(bb);
							for (ArrayList<BasicBlock> subPath : subPaths) {
								subPath.addAll(tail);
								paths.add(subPath);
							}
						}
						return paths;
					}
					
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
//	public static void doPathExtraction(boolean keepReg, BufferedWriter bWriter, HashMap<Long, Function> funList,
//			HashSet<Long> libFunAddrs) throws IOException {
//		for (Long funAddr : funList.keySet()) {
//			Function func = funList.get(funAddr);
//			if (Function.isLibraryOrSystemFunction(func) || Function.isThunkFunction(func)
//					|| Function.isFunctionContainingNoCode(func)) {
//				continue;
//			} else if (Function.isTrivalFunction(func, Config.MIN_INS_NUM)) {
//				continue;
//			}
//								
//			HashSet<BasicBlock> bbs = func.blocks;
//			StringBuffer sBuffer = new StringBuffer();
//			for (BasicBlock bb : bbs) {
//				ArrayList<Instruction> insList = bb.insList;
//				for (Instruction ins : insList) {
//					if (ins.isJumpIns()||ins.isCallIns()) {
//						Long libFunAddr=ins.getLibCallStr(libFunAddrs);
//						if (libFunAddr!=null) {
//							Function libF = funList.get(libFunAddr);
//							String opt = ins.operator.replaceAll("\\s+", "-");
//							sBuffer.append(opt+"-"+libF.fName.replace(".", "").trim()+" ");
//						}
//						else {
//							sBuffer.append(ins.getInstructionAbstractStr_Merge(keepReg)+" ");
//						}
//					}else{
//						sBuffer.append(ins.getInstructionAbstractStr_Merge(keepReg)+" ");
//					}
//					bWriter.write(sBuffer.toString());
//				}
//				bWriter.newLine();
//				sBuffer.setLength(0);
//			}
//			bWriter.flush();
//		}
//	}
}
