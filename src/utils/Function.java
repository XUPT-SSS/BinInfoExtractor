package utils;

import java.util.HashSet;
import java.util.TreeSet;

public class Function {

	public String fName;
	public long fAddr;
	public HashSet<BasicBlock> blocks;
	public RawCFG cfg = null;
	public String funType = "normal";
	public String moduleName = "";
	public boolean hasRealName = false;

	/**
	 * Used to construct a fake function
	 */
	public Function(String fakeName, String type) {
		this.fName = fakeName;
		this.funType = type;
	}

	// Test
	public Function(String fName, long fAddr) {
		this.fName = fName;
		this.fAddr = fAddr;
		this.blocks = new HashSet<>();
	}

	public Function(String fName, long fAddr, int fType, String moduleName, boolean hasRealName) {
		this.fName = fName;
		this.fAddr = fAddr;
		this.blocks = new HashSet<>();
		this.moduleName = moduleName;
		this.hasRealName = hasRealName;
		switch (fType) {
		case 0: // standard function
			funType = "normal";
			break;
		case 1: // library function that can be called by external or used by
				// inside
			funType = "library";
			break;
		case 2: // function imported from abroad
			funType = "import";
			break;
		case 3:
			funType = "thunk";
			break;
		case 4:
			funType = "adjustor_thunk";
			break;
		case 5:
			funType = "invalid";
			break;
		case 6:
			funType = "unknown";
			break;
		default:
			System.err.println("No such type should exists!");
		}
	}

	/**
	 * add to the function's block set
	 * @param basicBlock
	 */
	public void addblock(BasicBlock basicBlock) {
		this.blocks.add(basicBlock);
	}

	/**
	 * calculate the amount of instructions within the function
	 * 
	 * @return
	 */
	public int insCount() {
		int num = 0;
		for (BasicBlock bb : this.blocks) {
			num += bb.insCount();
		}
		return num;
	}

	@Override
	public String toString() {
		return this.fName;
	}

	/**
	 * Strict Equal: A function is only equal to itself or to another function
	 * that has the same function address and name.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean exactEquals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof Function) {
			Function u = (Function) obj;
			return (this.fAddr == u.fAddr) && (this.fName.equals(u.fName));
		} else {
			return false;
		}
	}

	/**
	 * Relaxed Equal: two functions are deemed the same if their names are the
	 * same
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof Function) {
			Function u = (Function) obj;
			return this.fName.equals(u.fName);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.fName.hashCode();
	}

	/**
	 * A function is considered trivial if its instruction number is less than
	 * the specified threshold
	 * 
	 * @param f
	 * @param threshold
	 * @return
	 */
	public static boolean isTrivalFunction(Function f, long threshold) {
		long insNum = 0;
		for (BasicBlock bb : f.blocks) {
			insNum += bb.insCount();
		}
		if (insNum < threshold)
			return true;
		return false;
	}

	/**
	 * Check whether the specified function is an external library function,
	 * such as malloc, printf or api functions provided from a library
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isLibraryOrSystemFunction(Function f) {
		if (f.funType.equals("import")) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether the specified function is a thunk function
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isThunkFunction(Function f) {
		if (f.funType.equals("thunk") || f.funType.equals("adjustor_thunk")) {
			return true;
		}
		return false;
	}

	/**
	 * Function that belongs to normal or library(exported function of the binary) types
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isBulidInFunction(Function f) {
		if (f.funType.equals("normal") || f.funType.equals("library")) {
			return true;
		}
		return false;
	}

	/**
	 * check whether it is an empty function
	 * @param f
	 * @return
	 */
	public static boolean isFunctionContainingNoCode(Function f) {
		if (f.blocks.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * check whether it is a manually crafted fake function(to support MWBPM)
	 * @param f
	 * @return
	 */
	public static boolean isFakeEmptyFunction(Function f) {
		if (f.funType.equals("FAKE-EMPTY")) {
			return true;
		}
		return false;
	}

	/**
	 * correlate the function with its control flow graph
	 * @param cfg
	 */
	public void setCFG(RawCFG cfg) {
		this.cfg = cfg;
	}
	
	/**
	 * Get an ordered (by address) set of all basic blocks within the function.
	 * Be sure to call this method if you need to represent a function with its whole body.
	 * @return basic blocks organized in (address)ascending order
	 */
	public TreeSet<BasicBlock> getOrderedBlocks(){
		TreeSet<BasicBlock> treeSet = new TreeSet<>();
		treeSet.addAll(blocks);
		return treeSet;
	}

}
