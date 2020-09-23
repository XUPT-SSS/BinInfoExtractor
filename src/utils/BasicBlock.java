package utils;
import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock implements Comparable{

	public int bbID;
	public long bbAddr;
	public ArrayList<Instruction> insList;
	public HashSet<BasicBlock> outBlocks;
	public HashSet<BasicBlock> inBlocks;
	public long funBelongsTo;
	
	public HashSet<BasicBlock> childVisited=new HashSet<>();

	
	public BasicBlock(int bbID, long bbAddr) {
		this.bbID=bbID;
		this.bbAddr=bbAddr;
		this.insList=new ArrayList<>();
		this.outBlocks=new HashSet<>();
		this.inBlocks=new HashSet<>();
	}
	
	public BasicBlock(int bbID, long bbAddr, long funBelongsTo) {
		this.bbID=bbID;
		this.bbAddr=bbAddr;
		this.insList=new ArrayList<>();
		this.funBelongsTo=funBelongsTo;
		this.outBlocks=new HashSet<>();
		this.inBlocks=new HashSet<>();
	}

	public void addIns(String ins) {
	}
	
	public void addIns(Instruction ins)
	{
		insList.add(ins);
	}

	public void addOut(BasicBlock bb) {
		this.outBlocks.add(bb);
	}
	
	public void addIn(BasicBlock bb) {
		this.inBlocks.add(bb);
	}
	
	@Override
	public String toString() {
		return getBBContent();
	}
	
	/**
	 * Concatenate all instruction string as the content of the BB
	 * @return
	 */
	private String getBBContent()
	{
		StringBuffer sBuffer=new StringBuffer();
		for (Instruction ins : insList) {
			sBuffer.append(ins.toString());
		}
		return sBuffer.toString();
	}
	
	public int insCount()
	{
		return this.insList.size();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof BasicBlock) {
			BasicBlock u = (BasicBlock) obj;
			return this.bbID == u.bbID;
		} else {
			return false;
		}
	}
	
	
	@Override
	public int hashCode() {
		return String.valueOf(this.bbID).hashCode();
	}
	
	@Override
	public int compareTo(Object o) {
		BasicBlock bb = (BasicBlock)o;
		int rst = bbAddr > bb.bbAddr? 1 : (bbAddr==bb.bbAddr? 0 : -1);	//ascending order
		return rst;
	}
}
