package utils;

public class CFGEdg {
	public BasicBlock srcBlk;
	public BasicBlock dstBlk;

	public CFGEdg(BasicBlock srcBlk, BasicBlock dstBlk) {
		this.srcBlk = srcBlk;
		this.dstBlk = dstBlk;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof CFGEdg) {
			CFGEdg u = (CFGEdg) obj;
			return this.srcBlk.equals(u.srcBlk) && this.dstBlk.equals(u.dstBlk);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return srcBlk.bbID + "-->" + dstBlk.bbID;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

}
