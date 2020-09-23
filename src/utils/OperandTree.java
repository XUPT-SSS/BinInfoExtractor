package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




/**
 * Operands of an instruction is organized as operand trees.
 * Each operand is depicted by a corresponding OperandTree object
 * @author ZZTian
 *
 */
public class OperandTree {
	
	 /**
	   * ID of the operand.
	   */
	  private final int m_id;

	  /**
	   * All nodes of the operand tree.
	   */
	  private final List<OperandTreeNode> m_nodes = new ArrayList<OperandTreeNode>();

	  private OperandTreeNode root;
	  
	  /**
	   * 0 for REG, 1 for IME, 2 for MEM
	   */
	  public int type=0;
	  
	  /**
	   * Creates a new operand tree.
	   * 
	   * @param operandId ID of the operand.
	   */
	  public OperandTree(final int operandId) {
	    m_id = operandId;
	  }

	  /**
	   * Returns the ID of the operand.
	   * 
	   * @return The ID of the operand.
	   */
	  public int getId() {
	    return m_id;
	  }

	  /**
	   * Returns the nodes of the operand tree.
	   * 
	   * @return The nodes of the operand tree.
	   */
	  public List<OperandTreeNode> getNodes() {
	    // ESCA-JAVA0259: Return of collection is OK, speed is more important
	    // when creating nodes.
	    return m_nodes;
	  }
	  
	  /**
	   * Get the string representation of the operand
	   * 有点问题，请用toString
	   * @return
	   */
	  public String getOperandStr()
	  {
		HashMap<String, OperandTreeNode> idNodeMap = new HashMap<>();
		HashMap<String, String> childParentMap = new HashMap<>();
		for (OperandTreeNode operandTreeNode : m_nodes) {
			String parentID = "" + operandTreeNode.parentID;
			String currentID = "" + operandTreeNode.operandID;

			if (childParentMap.containsKey(parentID)) {
				System.err.println("This case should not happen!");

			}
			childParentMap.put(parentID, currentID);

			if (idNodeMap.containsKey(currentID)) {
				System.err.println("Parent ID not unique!");
			}
			idNodeMap.put(currentID, operandTreeNode);
		}

		String next = childParentMap.get("-1");
		if (next == null) {
			System.err.println("Root node not found!");
		}
		next=childParentMap.get(next);
		String operandStr = "";
		while (next != null) {
			OperandTreeNode optNode = idNodeMap.get(next);
			String tmpStr = optNode.getOperandNodeStr();
			operandStr += tmpStr;
			next = childParentMap.get(next);
		}
		return operandStr;
	  }
	  
	  /**
	   * Get the root node of the tree
	   * @return
	   */
	  private OperandTreeNode recognizeRoot()
	  {
		  for (OperandTreeNode operandTreeNode : m_nodes) {
			  if (operandTreeNode.parentID==-1) {
				return operandTreeNode;
			}
		  }
		  return null;
	  }
	  
	/**
	 * Construct the tree by figuring out the 
	 * parent-child relationship between the nodes in the tree  
	 */
	public void generateTree() {
		root = recognizeRoot();
			
		HashMap<String, OperandTreeNode> idNodeMap = new HashMap<>();
		for (OperandTreeNode operandTreeNode : m_nodes) {
			String nodeID = "" + operandTreeNode.operandID;
			if (idNodeMap.containsKey(nodeID)) {
				System.err.println("Node ID not unique!");
			}
			idNodeMap.put(nodeID, operandTreeNode);
		}

		for (OperandTreeNode operandTreeNode : m_nodes) {
			int parentID = operandTreeNode.parentID;
			if (parentID == -1) {
				continue;
			}
			OperandTreeNode parentNode=idNodeMap.get(""+parentID);
			OperandTreeNode.link(parentNode, operandTreeNode);
		}
		
		//Determine the tree type based on the type of its root node	
		ArrayList<OperandTreeNode> children=root.getChildern();
		if (children.isEmpty()) {
			this.type=-1;
		}else
		{
			OperandTreeNode next=root.getChildern().get(0);
			switch (next.getType()) {
			case TreeType.NODE_TYPE_REGISTER_ID:
					this.type=0;
				break;
			case TreeType.NODE_TYPE_IMMEDIATE_INT_ID:
			case TreeType.NODE_TYPE_IMMEDIATE_FLOAT_ID:
					this.type=1;
				break;
			default:
				this.type=2;
				break;
			}
		}
		idNodeMap = null;
		
	}
	
	private String toString(final OperandTreeNode node)
	{
		final ArrayList<String> flattenedChildren=new ArrayList<>();
		
		for (OperandTreeNode child : node.getChildern()) {
			flattenedChildren.add(toString(child));
		}
		
		if (flattenedChildren.isEmpty()) {	//has no child
			return node.toString();
		}else if (flattenedChildren.size()==1) {	//has only one child
			int type=node.getType();
			String value=node.getValue();
			if (type==TreeType.NODE_TYPE_DEREFERENCE_ID) {
				return "["+ flattenedChildren.get(0)+"]";
			}else if (type==TreeType.NODE_TYPE_OPERATOR_ID) {
				if (value.equals("{")) {
					StringBuffer strBuffer=new StringBuffer();
					for (int i = 0; i < flattenedChildren.size(); i++) {
						strBuffer.append(flattenedChildren.get(i));
						if (i!=(flattenedChildren.size()-1)) {
							strBuffer.append(", ");
						}
					}
					return "{"+strBuffer.toString()+"}";
				}else{
					return node.toString() + (node.toString().isEmpty() ? "" : " ") 
							+ flattenedChildren.get(0);
				}
			}else {
				return node.toString() + (node.toString().isEmpty() ? "" : " ") 
						+ flattenedChildren.get(0);
			}
		}else {	//has multiple children
			StringBuffer strBuffer=new StringBuffer();
			for (int i = 0; i < flattenedChildren.size(); i++) {
				strBuffer.append(flattenedChildren.get(i));
				if (i!=(flattenedChildren.size()-1)) {
					strBuffer.append(" ");
					strBuffer.append(node.toString());
					strBuffer.append(" ");
				}
			}
			return strBuffer.toString();
		}
	}
	
	
	@Override
	public String toString() {	
		return this.toString(root);
	}
	
	public OperandTreeNode getRoot()
	{
		return this.root;
	}
	

}

class TreeType{
	public static final int NODE_TYPE_MNEMONIC_ID = 0;
	public static final int NODE_TYPE_SYMBOL_ID = 1;
	public static final int NODE_TYPE_IMMEDIATE_INT_ID = 2;
	public static final int NODE_TYPE_IMMEDIATE_FLOAT_ID = 3;
	public static final int NODE_TYPE_OPERATOR_ID = 4;
	public static final int NODE_TYPE_REGISTER_ID = 5;
	public static final int NODE_TYPE_SIZE_PREFIX_ID = 6;
	public static final int NODE_TYPE_DEREFERENCE_ID = 7;
}
