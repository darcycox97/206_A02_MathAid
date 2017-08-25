package authoringaid;

import javax.swing.SwingUtilities;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class MathsAid extends JFrame {

	private JPanel _panelCreate;
	private JTextField _txtCrtnName;
	private JButton _btnCreate;

	private JPanel _panelOptions;
	private JButton _btnPlay;
	private JButton _btnDelete;

	private JPanel _panelDisplay;
	private JLabel _lblExisting;
	private JScrollPane _scrExisting;
	private JList<Creation> _listCrtns;
	private DefaultListModel<Creation> _existingCrtns; 

	private EmbeddedMediaPlayerComponent _video;
	private final EmbeddedMediaPlayer _player;

	public MathsAid() {
		super("Maths Authoring Aid");
		setSize(700,600);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				_video.release();
				System.exit(0);
			}
		});

		/***************** Layout **************************/

		// set up creation menu
		_txtCrtnName = new JTextField("Enter name of creation");
		_btnCreate = new JButton("Create");
		_panelCreate = new JPanel();
		_panelCreate.setPreferredSize(new Dimension(100,70));
		_panelCreate.add(_txtCrtnName);
		_panelCreate.add(_btnCreate);
		add(_panelCreate, BorderLayout.NORTH);

		// set up display area for existing creations
		initializeCreationsList();
		_listCrtns = new JList<Creation>(_existingCrtns);
		_listCrtns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_scrExisting = new JScrollPane(_listCrtns);
		_scrExisting.setPreferredSize(new Dimension(150,100));
		_lblExisting = new JLabel("Existing Creations");
		_panelDisplay = new JPanel();
		_panelDisplay.setLayout(new BoxLayout(_panelDisplay, BoxLayout.Y_AXIS));
		_panelDisplay.add(_lblExisting);
		_panelDisplay.add(_scrExisting);
		add(_panelDisplay, BorderLayout.WEST);

		// set up options area
		_btnPlay = new JButton("Play");
		_btnDelete = new JButton("Delete");
		_panelOptions = new JPanel();
		_panelOptions.setPreferredSize(new Dimension(100,70));
		_panelOptions.add(_btnPlay);
		_panelOptions.add(_btnDelete);
		add(_panelOptions, BorderLayout.SOUTH);

		// set up embedded media player
		_video = new EmbeddedMediaPlayerComponent();
		_player = _video.getMediaPlayer();
		add(_video, BorderLayout.CENTER);

		/***************** Event Handlers ******************/

		// functionality for delete button
		_btnDelete.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent e) {

				Creation toDelete = _listCrtns.getSelectedValue();
				if (toDelete != null) {
					// ask user if they wish to delete
					int selection = JOptionPane.showConfirmDialog(_video, 
							"Are you sure you want to delete the creation \"" + toDelete +"\"?", 
							"Select an option",JOptionPane.YES_NO_OPTION);

					if (selection == JOptionPane.YES_OPTION) {
						removeCreation(toDelete);
					}
				}
			}
		});

		// functionality for play button
		_btnPlay.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {

				Creation toPlay = _listCrtns.getSelectedValue();
				if (toPlay != null) {
					_player.mute(false);
					_player.playMedia(toPlay.getFileName(Creation.COMBINED).getPath(),"");
				}
			}
		});


		// functionality for create button
		_btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String name = _txtCrtnName.getText();
				boolean validName = validCrtnName(name);
				if (!validName) {
					// let user know the creation name is not valid
					JOptionPane.showMessageDialog(_panelCreate, "The name you entered is not valid.\n" +
							"Names may only include alphanumeric character, hyphens, or underscores.", "Invalid Name",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				Creation c = new Creation(name);
				boolean overwrite = true;
				if (_existingCrtns.contains(c)) {
					// ask for overwrite confirmation
					int overWriteSel = JOptionPane.showConfirmDialog(_panelCreate, "The creation \"" + name + "\" already exists.\n"
							+ "Do you wish to overwrite it?","Please select an option", JOptionPane.YES_NO_OPTION);
					if (overWriteSel != JOptionPane.YES_OPTION) {
						overwrite = false;
						return;
					}
					// user has chosen to overwrite
					removeCreation(c);
				}

				if (overwrite) {
					c.getFileName(Creation.ROOT).mkdirs(); // set up root directory
					
					// get paths to each component of the creation
					File pathToVideo = c.getFileName(Creation.VIDEO);
					File pathToAudio = c.getFileName(Creation.AUDIO);
					File pathToCombined = c.getFileName(Creation.COMBINED);
					
					try {
						// bash command for creating the video
						ProcessBuilder vid = new ProcessBuilder("bash","-c",
								"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
										"x=(w-text_w)/2:y=(h-text_h)/2:text='" + c + "'\" -t 3 " + pathToVideo.getPath());
						Process vidP = vid.start();
						if (vidP.waitFor() == 1){
							removeCreation(c); // remove partially finished creation
							return; 
						}

						JOptionPane.showMessageDialog(_panelCreate, "Video component created!\n"
								+ "Press OK to record the audio for this creation");

						while (true) {
							// bash command for recording audio component
							ProcessBuilder audio = new ProcessBuilder("bash","-c",
									"ffmpeg -f alsa -ac 2 -i default -t 3 " + pathToAudio.getPath());
							Process rec = audio.start();
							if (rec.waitFor() == 1) {
								removeCreation(c); // remove partially finished creation
								return; // do not proceed if something went wrong
							}

							int playBackSel = JOptionPane.showConfirmDialog(_panelCreate, "Would you like to listen to the recording?",
									"Please select an option", JOptionPane.YES_NO_OPTION);
							
							if (playBackSel == JOptionPane.YES_OPTION) {
								_player.mute(false);
								_player.playMedia(pathToAudio.getPath(), "");
							} else {
								break;
							}

							// show dialog asking user to keep or redo recording
							Object[] options = {"Keep","Redo"};
							int selection = JOptionPane.showOptionDialog(_panelCreate, "Do you want to keep or redo this recording?",
									"Please choose an action to take", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
									null, options, null);

							if (selection != 1) {
								break;
							} else {
								pathToAudio.delete();
								JOptionPane.showMessageDialog(_panelCreate, "Press OK to record");
							}
						}

						// bash command to combine audio and video components
						ProcessBuilder combine = new ProcessBuilder("bash","-c",
								"ffmpeg -i " + pathToAudio.getPath() + " -i " + pathToVideo.getPath() + 
								" -codec copy " + pathToCombined.getPath());
						Process p = combine.start();
						if (p.waitFor() == 0) {
							_existingCrtns.addElement(c); // add creation to gui if merge was successful
						} else {
							removeCreation(c); // delete all files if an error occurred
						}

					} catch (IOException | InterruptedException e1) {
						// clean up by deleting all files if an exception was thrown
						removeCreation(c);
					}
				}
			}
		});

	}


	public static void main(String[] args) {

		NativeDiscovery nd = new NativeDiscovery();
		nd.discover();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MathsAid frame = new MathsAid();
				frame.setVisible(true);
			}
		});
	}

	/**
	 *  Initialize the list model on program startup, by scanning through 
	 *  existing creations and adding them to the list.
	 */
	private void initializeCreationsList() {
		_existingCrtns = new DefaultListModel<Creation>();

		File crtnsFolder = new File("creations");
		if (crtnsFolder.exists()) {
			String[] crtns = crtnsFolder.list();
			if (crtns != null) {
				for (String c : crtns) {
					_existingCrtns.addElement(new Creation(c));
				}
			}
		}
	}

	/**
	 * Deletes all files related to a specified creation
	 * and removes from the list.
	 * @param toDelete The creation to be deleted
	 */
	public void removeCreation(Creation toDelete) {
		toDelete.getFileName(Creation.AUDIO).delete();
		toDelete.getFileName(Creation.VIDEO).delete();
		toDelete.getFileName(Creation.COMBINED).delete();
		toDelete.getFileName(Creation.ROOT).delete();

		_existingCrtns.removeElement(toDelete);
	}

	/**
	 * Runs a regex pattern on the given name to determine if it is
	 * valid (consists only of alphanumeric characters,"-",or "_").
	 * @param name The user input to validate
	 * @return
	 */
	private boolean validCrtnName(String name) {
		return Pattern.matches("[\\w\\-]+", name);
	}


}
