package utils;

import java.util.ArrayList;
import java.util.HashSet;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Instruction {

	public int address;
	public String operator;
	public String operand;
	public int bbBelongsTo;
	public int locInBlock;
	private ArrayList<OperandTree> operandTreeList;

	public Instruction(int addr, String operator, String operand, int bbBelongsTo, int locInBlock) {
		this.address = addr;
		this.operator = operator;
		this.operand = operand;
		this.bbBelongsTo = bbBelongsTo;
		this.locInBlock = locInBlock;
		this.operandTreeList = new ArrayList<>();
	}

	public boolean isJumpIns() {
		if (operator.startsWith("J") || operator.startsWith("j")) {
			return true;
		}
		return false;
	}

	public boolean isCallIns() {
		if (operator.equalsIgnoreCase("call")) {
			return true;
		}
		return false;
	}

	public boolean isMovIns() {
		if (operator.startsWith("mov")) {
			return true;
		}
		return false;
	}

	/**
	 * add to the instruction its corresponding operand trees
	 * 
	 * @param opTree
	 */
	public void addOperandTree(OperandTree opTree) {
		this.operandTreeList.add(opTree);
	}

	public void setOperandTrees(ArrayList<OperandTree> opTreeList) {
		this.operandTreeList = opTreeList;
	}

	@Override
	public String toString() {
		return getInstructionStr();
	}

	public String showBasicInfo() {
		String str = address + ", " + operator + ", " + bbBelongsTo + ":" + locInBlock;
		return str;
	}

	/**
	 * Get the string representation of the instruction,
	 * 
	 * @return a string like: Mnemonic operand1, operand2, ...
	 */
	private String getInstructionStr() {
		verifyOperands(operandTreeList);
		final StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(operator);
		strBuffer.append(" ");

		for (int i = 0; i < operandTreeList.size(); ++i) {
			strBuffer.append(operandTreeList.get(i).toString());
			if ((operandTreeList.size() - 1) != i) { // not the last element
				strBuffer.append(", ");
			}
		}
		return strBuffer.toString();
	}

	/**
	 * extract the library function address from the instruction if possible
	 * 
	 * @param funAddrs
	 * @return
	 */
	public Long getLibCallStr(HashSet<Long> funAddrs) {
		String insStr = getInstructionStr();
		for (Long addr : funAddrs) {
			if (insStr.contains(addr.toString())) {
				return addr;
			}
		}
		return null;
	}

	public ArrayList<OperandTree> getOpTreeList() {
		return this.operandTreeList;
	}

	/**
	 * Abstracted Instruction Representation: REG, MEM, IME
	 * 
	 * @return
	 */
	public String getInstructionAbstractStr() {
		verifyOperands(operandTreeList);
		final StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(operator);
		strBuffer.append(" ");

		for (int i = 0; i < operandTreeList.size(); ++i) {
			switch (operandTreeList.get(i).type) {
			case 0:
				strBuffer.append("REG");
				break;
			case 1:
				strBuffer.append("IMM");
				break;
			case 2:
				strBuffer.append("MEM");
				break;
			}
			// strBuffer.append(operandTreeList.get(i).type);
			if ((operandTreeList.size() - 1) != i) { // not the last element
				strBuffer.append(", ");
			}
		}
		return strBuffer.toString();
	}

	/**
	 * Example: 
	 * mov eax, 6 --> mov-eax-VAL
	 * @param keepReg
	 * @return
	 */
	public String getInstructionAbstractStr_Merge(boolean keepReg) {
		verifyOperands(operandTreeList);
		final StringBuffer strBuffer = new StringBuffer();
		String tmpOp = operator.replaceAll("\\s+", "-");
		strBuffer.append(tmpOp);
		
		for (int i = 0; i < operandTreeList.size(); ++i) {
			strBuffer.append("-");
			switch (operandTreeList.get(i).type) {
			case 0:
				if (keepReg) {
					strBuffer.append(operandTreeList.get(i).toString());
				} else {
					strBuffer.append("REG");
				}
				break;
			case 1:
				strBuffer.append("IMM");
				break;
			case 2:
				strBuffer.append("MEM");
				break;
			}
		}
		return strBuffer.toString();
	}

	/**
	 * keep more information when abstracting
	 * Example: 
	 * mov eax, 6 --> mov-eax-6
	 * mov eax, 6000 --> mov-eax-IMM
	 * @return
	 */
	public String getInstructionAbstractStr_Fine( ) {
		verifyOperands(operandTreeList);
		final StringBuffer strBuffer = new StringBuffer();
		String tmpOp = operator.replaceAll("\\s+", "-");
		strBuffer.append(tmpOp);
		
		String operandStr = "";
		for (int i = 0; i < operandTreeList.size(); ++i) {
			strBuffer.append("-");
			operandStr = operandTreeList.get(i).toString();
			operandStr = operandStr.replaceAll("\\s+", "");

			operandStr = operandStr.replaceAll("\\[\\d+\\]", "MEM");
			// "ss:[ebp+100]" will be abstracted to "ss:[ebp+IMM]"
			operandStr = operandStr.replaceAll("(\\d{3,}])", "IMM]");
			// "mov eax 5000" will be abstracted to "mov eax IMM"
			operandStr = operandStr.replaceAll("(\\d{5,})|([5-9]\\d{3})", "IMM");

			strBuffer.append(operandStr);		
		}
//		return strBuffer.toString().replaceAll("\\s+", "");
		return strBuffer.toString();

	}

	
	/**
	 * 稍后实现
	 * 
	 * @param operands
	 */
	private void verifyOperands(ArrayList<OperandTree> operands) {
		for (OperandTree operand : operands) {
			verifyOperand(operand.getRoot());
		}
	}

	/**
	 * 稍后实现
	 * 
	 * @param node
	 */
	private void verifyOperand(OperandTreeNode node) {
		if (node == null) {
			System.err.println("Not a valid operand tree: Operand tree node with value null detected");
		}
		for (OperandTreeNode child : node.getChildern()) {
			verifyOperand(child);
		}
	}

	/**
	 * Far from being complete
	 * 
	 * @return
	 */
	public String getInsType() {
		if (operator.startsWith("mov")) {
			return "DT";
		} else if (operator.startsWith("add")) {
			return "AR";
		}
		return null;
	}
}
