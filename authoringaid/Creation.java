package authoringaid;

import java.io.File;

public class Creation {
	private String _name;
	private File _path;
	private File _video;
	private File _audio;
	private File _combined;

	enum Components {
		ROOT, VIDEO, AUDIO, COMBINED
	}


	public Creation(String name) {
		_name = name;
		_path = new File("creations/" + name);
		_video = new File("creations/" + name + "/" + name + ".mp4");
		_audio = new File("creations/" + name + "/" + name + ".mp3");
		_combined = new File("creations/" + name + "/" + name + "-combined.mp4");
	}

	/**
	 * Queries the creation for the specified path.
	 * @param comp the component of the creation we want the path for
	 * @return The specified File object
	 */
	public File getFileName(Components comp) {

		switch (comp) {
		case ROOT: 
			return _path;
		case VIDEO:
			return _video;
		case AUDIO:
			return _audio;
		case COMBINED:
			return _combined;
		default:
			// this will never be reached
			throw new RuntimeException("Not a valid enum value");
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof Creation && obj != null) {
			return _name.equals(((Creation)obj)._name);
		} else {
			return false;
		}
	}

	public String toString() {
		return _name;
	}



}
