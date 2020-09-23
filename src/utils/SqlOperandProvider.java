package utils;

import java.sql.Array;
import java.sql.ResultSet;



public class SqlOperandProvider {

	private final ResultSet resultSet;
	
	public SqlOperandProvider(final ResultSet resultSet) {
		this.resultSet=resultSet;
	}
	
	
	/**
	 * Get the expression tree id corresponding to the operand
	 * @return the id, or -1 if an exception occurs
	 */
	public int getExpressionTreeID() {
		try {
			int treeID=resultSet.getInt("expression_tree_id");
			if (resultSet.wasNull()) {
				return -1;
			}else{
				return treeID;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the position of the operand within an instruction
	 * @return the position, or -1 if no operand exists within the instruction
	 */
	public int getOperandPosition() {
		try {
			final int position = resultSet.getInt("operand_position");
			return resultSet.wasNull() ? -1 : position;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the expression tree type corresponding to the operand
	 * @return an integer indicating the specific type, or -1 if an exception occurs
	 */
	public int getExpressionTreeType() {
		try {
			int treeType = resultSet.getInt("expression_tree_type");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return treeType;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the symbol corresponding to the operand
	 * @return null if an exception occurs or the symbol field of the operand is empty
	 */
	public String getSymbol()
	{
		try {
			return resultSet.getString("symbol");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the immediate corresponding to the operand
	 * @return null if an exception occurs or the immediate field of the operand is empty
	 */
	public String getImmediate()
	{
		try {
			return resultSet.getString("immediate");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the replacement string corresponding to the operand
	 * @return null if an exception occurs 
	 * or the value of the field is not given
	 */
	public String getReplacement()
	{
		try {
			return resultSet.getString("replacement");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the parent expression tree id corresponding to the node
	 * @return the parent id, 
	 * or -1 if the node is a root node or an exception occurs
	 */
	public int getParentID() {
		try {
			int parentID = resultSet.getInt("expression_tree_parent_id");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return parentID;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the target corresponding to the node
	 * @return null if an exception occurs or the value of the filed is empty
	 */
	public String getTarget()
	{
		try {
			String targetAddrStr=resultSet.getString("target");
			return resultSet.wasNull()?null:targetAddrStr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the type substitution for this operand tree node
	 * @return -1 if an exception occurs or the value of the filed is empty
	 */
	public int getSubstitutionTypeID() {
		try {
			final int typeID = resultSet.getInt("expression_types_type");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return typeID;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the substitution position for the operand tree node
	 * @return -1 if an exception occurs or the value of the filed is empty
	 */
	public int getSubstitutionPosition() {
		try {
			int substitutePosition = resultSet.getInt("expression_types_position");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return substitutePosition;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the substitution position for the operand tree node
	 * @return null if an exception occurs or the value of the filed is empty
	 */
	public Array getSubstitutionPath() {
		try {
			Array pathArray = resultSet.getArray("expression_types_path");
			if (resultSet.wasNull()) {
				return null;
			} else {
				return pathArray;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the substitution offset for the operand tree node
	 * @return -1 if an exception occurs or the value of the filed is empty
	 */
	public int getSubstitutionOffset() {
		try {
			int substitutionOffset = resultSet.getInt("expression_types_offset");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return substitutionOffset;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Get the address reference type for the operand tree node
	 * @return null if an exception occurs or the value of the field is empty
	 */
	public String getReferencesType() {
		try {
			String referType = resultSet.getString("address_references_type");
			if (resultSet.wasNull()) {
				return null;
			} else {
				return referType.toUpperCase();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the type instance id for this operand tree node.
	 * @return -1 if an exception occurs or the value of the filed is empty
	 */
	public int getTypeInstanceID() {
		try {
			int instanceID = resultSet.getInt("type_instance_id");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return instanceID;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public Reference getReference()
	{
		try {
			final String target=getTarget();
			if (target!=null) {
				String type=getReferencesType();
				return new Reference(target, type);
			}else{
				return null;
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Move the curse to the previous row
	 * @return
	 */
	public boolean prev()
	{
		try {
			return resultSet.previous();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Move the curse forward one row from current position
	 * @return
	 */
	public boolean next()
	{
		try {
			return resultSet.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Get the address of the instruction
	 * @return
	 */
	public int getInsAddr()
	{
		try {
			int insAddr = resultSet.getInt("address");
			if (resultSet.wasNull()) {
				return -1;
			} else {
				return insAddr;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
