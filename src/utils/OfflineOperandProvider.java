package utils;

public class OfflineOperandProvider {
	
	private int opTreeID;
	private int oprndPosition;
	private int opTType;
	private String symbol;
	private String immediate;
	private int parentID;

	public OfflineOperandProvider(int opTreeID, int oprndPosition, int opTType, String symbol, String immediate, int parentID) {
		this.opTreeID=opTreeID;
		this.oprndPosition=oprndPosition;
		this.opTType=opTType;
		this.symbol=symbol;
		this.immediate=immediate;
		this.parentID=parentID;
	}
	
	public int getExpressionTreeID() {
		return opTreeID;
	}

	public int getOperandPosition() {
		return oprndPosition;
	}

	public int getExpressionTreeType() {
		return opTType;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getImmediate() {
		return immediate;
	}
	
	public int getParentID()
	{
		return parentID;
	}
}