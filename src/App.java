import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import javax.sound.sampled.LineUnavailableException;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.combobox.WebComboBox;
import com.alee.managers.style.StyleId;

@SuppressWarnings("serial")
public class App extends JFrame implements KeyListener {
	
	static final String 	ABOUT = "<html><center><h1>Version 1.1</h1><font size=3>de R1CBU</font></center></html>";
	
	private Storage			storage = null;
	private Sound			sound = null;
	private Lession			lession = null;
	private Timer			timer = null;
	private Question		question = null;
	private String			answer_buf;

	private JPanel			opt_panel = null;
	private GridBagLayout	grid = null;
	private JCheckBox 		run_box = null;
	private JSpinner 		wpm_spinner = null;
	private JLabel			info_label = null;
	private WebComboBox		lession_list = null;
	private WebComboBox		timeout_list = null;
	private JSpinner 		adv_level_spinner = null;
	private JSpinner 		adv_max_spinner = null;
	private JSpinner 		adv_repeat_spinner = null;
	
	private int				question_wait = 0;
	private int				help_wait = 3000;
	
	private String[]		timeout_labels = {"Forever", "1 sec", "2 sec", "3 sec"};
	private int[]			timeout_items = {0, 1000, 2000, 3000};
	
	/* * */
	
	private class LessionTask extends TimerTask {
		public void run() {
			question = lession.getQuestion();
			answer_buf = "";

			boolean	help = question.correct <= 3;  

			info_label.setBackground(Color.white);
			
			int ms = playQuestion((Integer) adv_repeat_spinner.getValue());
			
			if (help) {
				info_label.setText(question.symbol);
				startTimer(ms + help_wait);
			} else {
				info_label.setText(question.getSecret(""));
				
				if (question_wait > 0) {
					startTimer(ms + question_wait);
				}
			}
		}
	}
	
	private class RunListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (run_box.isSelected()) {
				info_label.setText("Get ready!");
				int ms = sound.code("...- ...- ...-");
				
				startTimer(ms + 1000);
			} else {
				stopTimer();
			}
	    }
	}
	
	private class WPMListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = (Integer) wpm_spinner.getValue();

			sound.wpm(val);
			storage.setOptInt("wpm", val);
		}
	}
	
	private class LessionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String item = (String) lession_list.getSelectedItem();
			
			stopTimer();
			storage.setOptString("lession", item);
			loadLession(item);
		}
	}

	private class TimeoutListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int index = timeout_list.getSelectedIndex();
			
			storage.setOptInt("timeout", index);
			question_wait = timeout_items[index];
		}
	}

	private class AdvLevelListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = (Integer) adv_level_spinner.getValue();
			
			storage.setAdvLevel(val);
			storage.setOptInt("adv_level", val);
		}
	}

	private class AdvMaxListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = (Integer) adv_max_spinner.getValue();
			
			storage.setAdvMax(val);
			storage.setOptInt("adv_max", val);
		}
	}

	private class AdvRepeatListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = (Integer) adv_repeat_spinner.getValue();
			
			storage.setOptInt("adv_repeat", val);
		}
	}
	
	/* * */
	
	protected App() {
		try {
			sound = new Sound();
			storage = new Storage();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(ERROR);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(ERROR);
		}

		setSize(640, 480);
		setTitle("MorseMaster");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getRootPane().putClientProperty(StyleId.STYLE_PROPERTY, StyleId.frameDecorated);
		setIconImages(WebLookAndFeel.getImages());

		createOpt();
		createCenter();
		loadOpts();

		addKeyListener(this);
		setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void createOpt() {
		opt_panel = new JPanel();
		grid = new GridBagLayout();
		
		opt_panel.setLayout(grid);
		opt_panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagHelper helper = new GridBagHelper();

		// -- //
		
		helper.nextCell().alignLeft().gap(10);
		opt_panel.add(new JLabel("Lession:"), helper.get());

		helper.nextCell().fillHorizontally();
		lession_list = new WebComboBox(storage.getLessions());
		lession_list.addActionListener(new LessionListener());
		lession_list.setFocusable(false);
		opt_panel.add(lession_list, helper.get());
		
		// -- //
		
		helper.nextRow().nextCell().alignLeft().gap(10);
		opt_panel.add(new JLabel("Speed (WPM):"), helper.get());
		helper.nextCell().fillHorizontally();

		JSpinner.DefaultEditor editor;
		
		wpm_spinner = new JSpinner(new SpinnerNumberModel(18, 15, 40, 1));
		editor = (DefaultEditor) wpm_spinner.getEditor();
		editor.getTextField().setEditable(false);
		editor.getTextField().setFocusable(false);
		
		wpm_spinner.addChangeListener(new WPMListener());
		opt_panel.add(wpm_spinner, helper.get());

		// -- //

		helper.nextRow().nextCell().alignLeft().gap(10);
		opt_panel.add(new JLabel("Answer timeout:"), helper.get());
		
		helper.nextCell().fillHorizontally();
		timeout_list = new WebComboBox(timeout_labels);
		timeout_list.addActionListener(new TimeoutListener());
		timeout_list.setFocusable(false);
		opt_panel.add(timeout_list, helper.get());
		
		// -- //
		
		helper.insertEmptyRow(opt_panel, 24);
		helper.nextRow().nextCell().setGridWidth(2);

		opt_panel.add(new JLabel("Advanced"), helper.get());

		helper.nextRow().nextCell().alignLeft().gap(10);
		opt_panel.add(new JLabel("Level (%):"), helper.get());
		helper.nextCell().fillHorizontally();

		adv_level_spinner = new JSpinner(new SpinnerNumberModel(75, 50, 95, 1));
		editor = (DefaultEditor) adv_level_spinner.getEditor();
		editor.getTextField().setEditable(false);
		editor.getTextField().setFocusable(false);
		adv_level_spinner.addChangeListener(new AdvLevelListener());
		opt_panel.add(adv_level_spinner, helper.get());
		helper.nextRow().nextCell().alignLeft().gap(10);

		// --//
		
		opt_panel.add(new JLabel("Max (char):"), helper.get());
		helper.nextCell().fillHorizontally();

		adv_max_spinner = new JSpinner(new SpinnerNumberModel(3, 2, 7, 1));
		editor = (DefaultEditor) adv_max_spinner.getEditor();
		editor.getTextField().setEditable(false);
		editor.getTextField().setFocusable(false);
		adv_max_spinner.addChangeListener(new AdvMaxListener());
		opt_panel.add(adv_max_spinner, helper.get());

		// -- //
		
		helper.nextRow().nextCell().alignLeft().gap(10);

		opt_panel.add(new JLabel("Repeat:"), helper.get());
		helper.nextCell().fillHorizontally();

		adv_repeat_spinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
		editor = (DefaultEditor) adv_repeat_spinner.getEditor();
		editor.getTextField().setEditable(false);
		editor.getTextField().setFocusable(false);
		adv_repeat_spinner.addChangeListener(new AdvRepeatListener());
		opt_panel.add(adv_repeat_spinner, helper.get());
		
		// -- //
		
		helper.insertEmptyRow(opt_panel, 24);
		helper.nextRow().nextCell().alignLeft();
		
		run_box = new JCheckBox("Run", false);
		run_box.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkbox);
		run_box.setFocusable(false);
		run_box.setMnemonic(KeyEvent.VK_R);
		run_box.addItemListener(new RunListener());
		opt_panel.add(run_box, helper.get());
		helper.insertEmptyFiller(opt_panel);
		
		add(opt_panel, BorderLayout.EAST);
	}
	
	private void createCenter() {
		info_label = new JLabel(ABOUT);
		
		info_label.setOpaque(true);
		info_label.setBackground(Color.WHITE);
		info_label.setHorizontalAlignment(SwingConstants.CENTER);
		info_label.setVerticalAlignment(SwingConstants.CENTER);
		info_label.setFont(new Font("Arial", Font.BOLD, 72));
		
		add(info_label, BorderLayout.CENTER);
	}
	
	private void loadLession(String info) {
		lession = storage.loadLession(info);
		
		if (lession != null) {
			// storage.clearStat();
			lession.initStat();
		}
	}
	
	private void loadOpts() {
		int wpm = storage.getOptInt("wpm", 18);
		wpm_spinner.setValue(wpm);
		sound.wpm(wpm);
		
		String lession = storage.getOptString("lession");
		lession_list.setSelectedItem(lession);
		loadLession(lession);
		
		int timeout = storage.getOptInt("timeout", 0);
		timeout_list.setSelectedIndex(timeout);
		question_wait = timeout_items[timeout];

		int adv_level = storage.getOptInt("adv_level", 75);
		adv_level_spinner.setValue(adv_level);
		storage.setAdvLevel(adv_level);

		int adv_max = storage.getOptInt("adv_max", 2);
		adv_max_spinner.setValue(adv_max);
		storage.setAdvMax(adv_max);
		
		int adv_repeat = storage.getOptInt("adv_repeat", 1);
		adv_repeat_spinner.setValue(adv_repeat);
	}

	private void startTimer(int delay) {
		timer = new Timer();
		timer.schedule(new LessionTask(), delay);
	}
	
	private void stopTimer( ) {
		if (timer != null) {
			timer.cancel();
		}

		run_box.setSelected(false);
		info_label.setText(ABOUT);
		info_label.setBackground(Color.WHITE);
	}
	
	private int playQuestion(Integer x) {
		String 	q = storage.getCode(question.symbol);
		String	code = q;
		
		if (question.length() > 1)
			for (int i = 1; i < x; i++)
				code += "|" + q;
	
		return sound.code(code);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.isAltDown())
			return;
		
		if (!run_box.isSelected())
			return;
		
		if (question == null)
			return;
		
		char key = Character.toUpperCase(e.getKeyChar());
			
		if (key == ' ') {
			playQuestion(1);
			return;
		}
			
		answer_buf += key;
		info_label.setText(question.getSecret(answer_buf));
				
		if (answer_buf.length() == question.length()) {
			timer.cancel();

			if (lession.setAnswer(answer_buf)) {
				startTimer(100);
			} else {
				info_label.setText(question.symbol);
				info_label.setBackground(Color.RED);
				sound.alarm();
				startTimer(help_wait);
			}
			question = null;
		}
	}
	
}
