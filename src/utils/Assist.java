package utils;

public class Assist {
	
	/**
	 * Check whether obj is null
	 * @param obj
	 * @return
	 */
	public static boolean checkNotNull(Object obj) {
		try {
			if (obj == null) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
