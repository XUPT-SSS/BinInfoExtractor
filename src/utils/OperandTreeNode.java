package utils;

import java.util.ArrayList;

import Config.Config;


/**
 * Operands of an instruction is organized as operand trees
 * @author Administrator
 *
 */
public class OperandTreeNode {
	
	public int opTreeID;
	public int opPosition;
	
	public int operandID;
	private int type;
	private String value;
	public int parentID;
	private String replacement;
	private ArrayList<Reference> references;
	private final String rawTypeSubstitution;
    private final int instanceId;
    private final int operandPosition;
    private final int address;
	
    private ArrayList<OperandTreeNode> children=new ArrayList<>();
    private OperandTreeNode parent;
    public String nodeValue;
	
	/**
	   * Creates a new operand tree node object.
	   *
	   * @param operandId The ID of the node.
	   * @param type The numerical representation of the node type.
	   * @param value The value of the node.
	   * @param parentId The parent ID of the node. This value can be null if the node is a root node.
	   * @param replacement Replacement string of the node. This value can be null if no replacement is
	   *        given.
	   * @param references List of references from the operand to memory locations.
	   * @param substitution The type substitution for this operand tree node or null.
	   * @param address The address of the operand tree node.
	   * @param operandPosition The position of the operand tree node.
	   * @param instanceId The type instance id for this operand tree node.
	   */
	  public OperandTreeNode(final int operandId,
	      final int type,
	      final String value,
	      final Integer parentId,
	      final String replacement,
	      final ArrayList<Reference> references,
	      final String substitution,
	      final int instanceId,
	      final int operandPosition,
	      final int address) {
		  
		  this.operandID=operandId;
		  this.type=type;
		  this.parentID=parentId.intValue();
		  this.replacement=replacement;
		  this.references=references;
		  this.rawTypeSubstitution=substitution;
		  this.instanceId=instanceId;
		  this.operandPosition=operandPosition;
		  this.address=address;
		  this.value=value;
		  this.nodeValue=value;
		  initValue();
	  }
	  
	  public OperandTreeNode(final int operandId,
		      final int type,
		      final String value,
		      final Integer parentId,
		      final int operandPosition,
		      final int address) {
			  
			  this.operandID=operandId;
			  this.type=type;
			  this.parentID=parentId.intValue();
			  this.rawTypeSubstitution="";
			  this.instanceId=1;
			  this.operandPosition=operandPosition;
			  this.address=address;
			  this.value=value;
			  this.nodeValue=value;
			  initValue();
		  }
	  
	  
	  /**
	   * Links two operand tree nodes, as parent-child relationship
	   * @param parent
	   * @param child
	   */
	public static void link(final OperandTreeNode parent, final OperandTreeNode child) {
		if (parent == null)
			System.err.println("Parent Node is Null!");
		if (child == null)
			System.err.println("Child Node is Null!");
		parent.children.add(child);
		child.parent = parent;
	}
	  
	public String getOperandNodeStr() {
		String operandStr = "";
		if (replacement != null) {
			operandStr = replacement;
		} else {
			operandStr = value;
		}
		//operandStr+="("+type+")";
		return operandStr;
	}
	
	/**
	 * Initialize the node value based on the node type
	 */
	private void initValue()
	{
		switch (type) {
		case TreeType.NODE_TYPE_SYMBOL_ID:
		case TreeType.NODE_TYPE_IMMEDIATE_INT_ID:
		case TreeType.NODE_TYPE_IMMEDIATE_FLOAT_ID:
		case TreeType.NODE_TYPE_REGISTER_ID:
			nodeValue=value;
			break;
		case TreeType.NODE_TYPE_OPERATOR_ID:
			if (value.equals("{")) {
				nodeValue=value;
			}else {
				nodeValue=value;
			}
			break;
		case TreeType.NODE_TYPE_SIZE_PREFIX_ID:
			if (value.equals("b1")) {
		          nodeValue = "byte";
		        } else if (value.equals("b2")) {
		          nodeValue = "word";
		        } else if (value.equals("b4") || value.equals("dword")) {
		        	nodeValue = "dword";
		        } else if (value.equals("b6")) {
		          nodeValue = "fword";
		        } else if (value.equals("b8")) {
		          nodeValue = "qword";
		        } else if (value.equals("b10")) {
		          nodeValue = "double";
		        } else if (value.equals("b16")) {
		          nodeValue = "oword";
		        } else if (value.equals("b_var")) {
		          nodeValue = "b_var";
		        }
			//do not use byte information
			if (!Config.NODE_TYPE_SIZE_PREFIX_USE) {
				nodeValue="";
			}
			break;
		case TreeType.NODE_TYPE_DEREFERENCE_ID:
			nodeValue=value;
			break;
		default:
			System.err.println(String.format("Unknown node type: %d", type));
			break;
		}
	}
	
	public ArrayList<OperandTreeNode> getChildern()
	{
		return this.children;
	}
	
	public int getType(){
		return this.type;
	}
	
	/**
	 * Get raw value of the node
	 * @return
	 */
	public String getValue()
	{
		return this.value;
	}

	@Override
	public String toString() {
		if (replacement==null || type==TreeType.NODE_TYPE_SIZE_PREFIX_ID) {
			if (nodeValue.equals("dword")) {
				return "";
			}else{
				return nodeValue;
			}
		}else{
			return replacement;
		}
	}

	public String showBasicInfo() {
		String tmpStr = operandID + "#" + type + "#" + parentID + "#" + replacement + "#" + rawTypeSubstitution + "#"
				+ instanceId + "#" + operandPosition + "#" + address + "#" + value;
		for (Reference ref : references) {
			tmpStr += "(" + ref.target + "," + ref.type + ")";
		}
		return tmpStr;
	}

}

