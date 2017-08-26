package authoringaid;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Worker class to execute calls to ffmpeg on a thread other than EDT. Use of this class
 * should prevent the GUI freezing when the user is in create mode.
 * @author Darcy Cox
 *
 */
public class CreationWorker extends SwingWorker<Integer, Void> {
	//	public enum CreationPart {
	//		VIDEO, AUDIO, COMBINED
	//	}

	//	private CreationPart _compToCreate;
	private Creation _creation;
	private JComponent _gui;
	private DefaultListModel<Creation> _existingCrtns;

	/**
	 * Creates a new CreationWorker instance. 
	 * @param creation The creation to generate the video/audio files for
	 * @param gui The gui component the CreationWorker can communicate with
	 * @param existing The list of existing creations
	 */
	public CreationWorker(Creation creation, JComponent gui, DefaultListModel<Creation> existing) {
		_creation = creation;
		_gui = gui;
		_existingCrtns = existing;
	}

	protected Integer doInBackground() {
		
		try {
			// generate video component
			File vidPath = _creation.getFileName(Creation.Components.VIDEO);
			ProcessBuilder vid = new ProcessBuilder("bash","-c",
					"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
							"x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creation + "'\" -t 3 " + vidPath.getPath());
			Process vidP = vid.start();
			int exitVal =  vidP.waitFor();
			if (exitVal != 0) {
				return exitVal; // exit if error occurred
			}
			
			File audioPath = _creation.getFileName(Creation.Components.AUDIO);
			// let user know they are about to be recorded. Then generate audio file once they confirm
			JOptionPane.showMessageDialog(_gui, "You may now record the audio for this creation." + "\n" +
											"Press OK to start recording");
			ProcessBuilder audio = new ProcessBuilder("bash","-c",
					"ffmpeg -f alsa -ac 2 -i default -t 3 " + audioPath.getPath());
			Process audioP = audio.start();
			exitVal = audioP.waitFor();
			if (exitVal != 0) {
				return exitVal; // exit if error occurred
			}
			
			// generate combined file
			File combinedPath = _creation.getFileName(Creation.Components.COMBINED);
			ProcessBuilder combine = new ProcessBuilder("bash","-c",
					"ffmpeg -i " + audioPath.getPath() + " -i " + vidPath.getPath() + 
					" -codec copy " + combinedPath.getPath());
			
			// Tell user creation has been successfully generated
			JOptionPane.showMessageDialog(_gui, "Creation \"" + _creation + "\" successfully created");
			return 0;
			
			

		} catch (IOException | InterruptedException e) {
				CreationManager.deleteCreation(_creation); // delete all component files if something went wrong
		}
		
		return null;
	}

	protected void done() {
		try {
			int exit = get();
			if (exit == 0) {
				// update the gui with new creation
				_existingCrtns.addElement(_creation);
			}
		} catch (InterruptedException | ExecutionException e) {
			
		}
	}

}
