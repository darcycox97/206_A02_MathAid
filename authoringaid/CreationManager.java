package authoringaid;

import java.util.regex.Pattern;

/**
 * Class consisting of static methods with the intention of allowing ease of
 * operations with creations.
 * @author Darcy Cox
 *
 */
public class CreationManager {
	
	/**
	 * Deletes all files to do with a creation.
	 * @param c the creation to be deleted
	 */
	public static void deleteCreation(Creation c) {
		c.getFileName(Creation.Components.AUDIO).delete();
		c.getFileName(Creation.Components.VIDEO).delete();
		c.getFileName(Creation.Components.COMBINED).delete();
		c.getFileName(Creation.Components.ROOT).delete();
		c.getFileName(Creation.Components.PREVIEW).delete();
		
	}
	
	/**
	 * Creates the root folder for a given creation, if it does not already
	 * exist.
	 * @param c The creation to initialize the root folder for
	 */
	public static void setUpCreation(Creation c) {
		c.getFileName(Creation.Components.ROOT).mkdirs();
	}
	
	/**
	 * Determines if the given name is appropriate for a creation.
	 * Allows alphanumeric characters, hyphens and underscores.
	 * @param name the proposed name for a creation
	 */
	public static boolean validName(String name) {
		return Pattern.matches("[\\w\\-+=]+", name);
	}

}
