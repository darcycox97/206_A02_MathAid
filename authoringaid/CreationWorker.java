package authoringaid;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import authoringaid.Creation.Components;

/**
 * Worker class to execute calls to ffmpeg on a thread other than EDT. Use of this class
 * should prevent the GUI freezing when the user is recording.
 * @author Darcy Cox
 *
 */
public class CreationWorker extends SwingWorker<Integer, Void> {

	private Creation _creation;
	private JComponent _gui;
	private JLabel _statusUpdates;
	private JButton _btnRecordCreate;


	public CreationWorker(Creation creation, JComponent gui, JLabel statusLabel, 
			JButton btnToChange) {
		_creation = creation;
		_gui = gui;
		_statusUpdates = statusLabel;
		_btnRecordCreate = btnToChange;
	}

	protected Integer doInBackground() {
		
		publish(); // process method will disable recording button on EDT
		
		try {
			File audioPath = _creation.getFileName(Components.AUDIO);
			ProcessBuilder audio = new ProcessBuilder("bash","-c",
					"ffmpeg -f alsa -ac 2 -i default -t 3 " + audioPath.getPath());
			Process audioP = audio.start();
			int exitVal = audioP.waitFor();
			if (exitVal != 0) {
				CreationManager.deleteCreation(_creation);
				return exitVal; // exit if error occurred
			}
			return 0;

		} catch (IOException | InterruptedException e) {
			CreationManager.deleteCreation(_creation);
			return 1; // indicates error
		}
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


	protected void done() {
		try {
			int exit = get();
			if (exit == 0) {
				_statusUpdates.setText("Audio component generated successfully.");
				_btnRecordCreate.setEnabled(true);
			} else {
				// failure, delete the creation to clean up
				deleteCrtnAndShowErrorMessage();
			}
		} catch (InterruptedException | ExecutionException e) {
			deleteCrtnAndShowErrorMessage();
		}
	}

	@Override
	protected void process(List<Void> chunks) {
		_btnRecordCreate.setEnabled(false);
		_statusUpdates.setText("Recording......");
	}

	/**
	 * Updates the gui when something goes wrong, and removes any corrupt files
	 */
	private void deleteCrtnAndShowErrorMessage() {
		CreationManager.deleteCreation(_creation);
		_statusUpdates.setText("");
		JOptionPane.showMessageDialog(_gui, "Something went wrong. Please try again", "Error", JOptionPane.ERROR_MESSAGE);
	}

}
