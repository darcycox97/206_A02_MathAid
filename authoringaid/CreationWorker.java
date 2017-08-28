package authoringaid;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import authoringaid.Creation.Components;

/**
 * Worker class to execute calls to ffmpeg on a thread other than EDT. Use of this class
 * should prevent the GUI freezing when the user is in create mode.
 * @author Darcy Cox
 *
 */
public class CreationWorker extends SwingWorker<Integer, Void> {

	/**
	 * Enum to tell the CreationWorker instance which components of the creation to generate.
	 */
	enum WorkerUse {
		VIDEO, REMAINING
	}
	
	private static final String RECORD_BUTTON = "Record";
	private final String CREATE_BUTTON;

	private Creation _creation;
	private JComponent _gui;
	private DefaultListModel<Creation> _existingCrtns;
	private JLabel _statusUpdates;
	private WorkerUse _partToCreate;
	private JButton _btnRecordCreate;
	
	

	/**
	 * Creates a new CreationWorker instance. 
	 * @param creation The creation to generate the video/audio files for
	 * @param gui The gui component the CreationWorker can communicate with
	 * @param existing The list of existing creations
	 * @param label The JLabel instance to display status on
	 * @param creationComp the part of the creation this instance is to generate
	 */
	public CreationWorker(Creation creation, JComponent gui, DefaultListModel<Creation> existing, JLabel label, 
			WorkerUse creationComp, JButton btnToChange) {
		_creation = creation;
		_gui = gui;
		_existingCrtns = existing;
		_statusUpdates = label;
		_partToCreate = creationComp;
		_btnRecordCreate = btnToChange;
		CREATE_BUTTON = btnToChange.getText();
	}

	/**
	 * Generates either the video component of the specified creation, or both the audio and combined components, depending
	 * on the enum value given to this instance.
	 */
	protected Integer doInBackground() {
		
		if (_partToCreate.equals(WorkerUse.VIDEO)) {
			try {
				File vidPath = _creation.getFileName(Components.VIDEO);
				ProcessBuilder vid = new ProcessBuilder("bash","-c",
						"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
								"x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creation + "'\" -t 3 " + vidPath.getPath());
				Process vidP = vid.start();
				int exitVal =  vidP.waitFor();
				if (exitVal != 0) {
					// delete partial files and return if error occurred
					CreationManager.deleteCreation(_creation);
					return exitVal;
				}
				return 0; // indicates success
			} catch (IOException | InterruptedException e) {
				CreationManager.deleteCreation(_creation);
				return 1; // indicates error
			}
		} else {
			return 0;
		}
		
//		try {
//			// generate video component
//			File vidPath = _creation.getFileName(Creation.Components.VIDEO);
//			ProcessBuilder vid = new ProcessBuilder("bash","-c",
//					"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
//							"x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creation + "'\" -t 3 " + vidPath.getPath());
//			Process vidP = vid.start();
//			int exitVal =  vidP.waitFor();
//			if (exitVal != 0) {
//				return exitVal; // exit if error occurred
//			}
//			
//			File audioPath = _creation.getFileName(Creation.Components.AUDIO);
//			// let user know they are about to be recorded. Then generate audio file once they confirm
//			JOptionPane.showMessageDialog(_gui, "You may now record the audio for this creation." + "\n" +
//											"Press OK to start recording");
//			ProcessBuilder audio = new ProcessBuilder("bash","-c",
//					"ffmpeg -f alsa -ac 2 -i default -t 3 " + audioPath.getPath());
//			Process audioP = audio.start();
//			exitVal = audioP.waitFor();
//			if (exitVal != 0) {
//				return exitVal; // exit if error occurred
//			}
//			
//			// generate combined file
//			File combinedPath = _creation.getFileName(Creation.Components.COMBINED);
//			ProcessBuilder combine = new ProcessBuilder("bash","-c",
//					"ffmpeg -i " + audioPath.getPath() + " -i " + vidPath.getPath() + 
//					" -codec copy " + combinedPath.getPath());
//			Process combineP = combine.start();
//			exitVal = combineP.waitFor();
//			if (exitVal != 0) {
//				return exitVal; // exit if error occurred
//			}
//			
//			// Tell user creation has been successfully generated
//			JOptionPane.showMessageDialog(_gui, "Creation \"" + _creation + "\" successfully created");
//			return 0;
//			
//			
//
//		} catch (IOException | InterruptedException e) {
//				CreationManager.deleteCreation(_creation); // delete all component files if something went wrong
//				return 1;
//		}
//		
	}

	protected void done() {
		try {
			int exit = get();
			if (exit == 0) {
				// success
				if (_partToCreate.equals(WorkerUse.VIDEO)) {
					_statusUpdates.setText("Click the record button to start recording");
					_btnRecordCreate.setText(RECORD_BUTTON);
				} else {
					// update the gui with the new creation
					_statusUpdates.setText("Creation \"" + _creation + "\" successfully created");
					_existingCrtns.addElement(_creation);
					_btnRecordCreate.setText(CREATE_BUTTON);
				}
			} else {
				// failure, delete the creation to clean up
				showErrorMessage();
				
			}
		} catch (InterruptedException | ExecutionException e) {
			showErrorMessage();
		}
	}
	
	
	/**
	 * Updates the gui when something goes wrong, and removes any corrupt files
	 */
	private void showErrorMessage() {
		CreationManager.deleteCreation(_creation);
		_statusUpdates.setText("");
		JOptionPane.showMessageDialog(_gui, "Something went wrong. Please try again", "Error", JOptionPane.ERROR_MESSAGE);
	}

}
