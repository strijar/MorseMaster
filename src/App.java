import javax.swing.*;
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
	
	static final String 	ABOUT = "<html><center><h1>Version 1.0</h1><font size=3>de R1CBU</font></center></html>";
	
	private Storage			storage = null;
	private Sound			sound = null;
	private Lession			lession = null;
	private Timer			timer = null;
	private Question		question = null;
	private String			answer_buf;

	private JCheckBox 		run_box = null;
	private JSlider 		wpm_slider = null;
	private JLabel			info_label = null;
	private WebComboBox		lession_list = null;
	private JSlider 		adv_level_slider = null;
	private JSlider 		adv_max_slider = null;
	
	private int				question_wait = 0000;
	private int				help_wait = 3000;
	
	/* * */
	
	private class LessionTask extends TimerTask {
		public void run() {
			question = lession.getQuestion();
			answer_buf = "";

			boolean	help = question.correct <= 3;  

			info_label.setBackground(Color.white);
			
			if (help) {
				info_label.setText(question.symbol);
				startTimer(help_wait);
			} else {
				info_label.setText(question.getSecret(""));
				
				if (question_wait > 0) {
					startTimer(question_wait);
				}
			}
			playQuestion();
		}
	}
	
	private class RunListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (run_box.isSelected()) {
				startTimer(5000);
				info_label.setText("Get ready!");
				sound.code("...- ...- ...-");
			} else {
				timer.cancel();
				info_label.setText(ABOUT);
				info_label.setBackground(Color.WHITE);
			}
	    }
	}
	
	private class WPMListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = wpm_slider.getValue();
			
			sound.wpm(val);
			storage.setOptInt("wps", val);
		}
	}
	
	private class LessionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String item = (String) lession_list.getSelectedItem();
			
			storage.setOptString("lession", item);
			loadLession(item);
		}
	}

	private class AdvLevelListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = adv_level_slider.getValue();
			
			storage.setAdvLevel(val);
			storage.setOptInt("adv_level", val);
		}
	}

	private class AdvMaxListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			int val = adv_max_slider.getValue();
			
			storage.setAdvMax(val);
			storage.setOptInt("adv_max", val);
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

		createToolbar();
		createCenter();
		loadOpts();

		addKeyListener(this);
		setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void createToolbar() {
		final JToolBar	toolbar = new JToolBar(SwingConstants.VERTICAL);
		
		toolbar.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.toolbar);

		lession_list = new WebComboBox(storage.getLessions());
		lession_list.addActionListener(new LessionListener());
		lession_list.setFocusable(false);
		lession_list.setToolTip("Lession");
		toolbar.add(lession_list);

		toolbar.addSeparator(new Dimension(20, 10));

		wpm_slider = new JSlider(JSlider.HORIZONTAL, 15, 40, 18);
		wpm_slider.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.sliderTickLabel);
		wpm_slider.setFocusable(false);
		wpm_slider.addChangeListener(new WPMListener());
		wpm_slider.setMajorTickSpacing(5);
		wpm_slider.setMinorTickSpacing(1);
		wpm_slider.setPaintTicks(true);
		wpm_slider.setToolTipText("WPS");
		wpm_slider.setPaintLabels(true);
		
		toolbar.add(wpm_slider);

		toolbar.addSeparator(new Dimension(20, 10));
		
		adv_level_slider = new JSlider(JSlider.HORIZONTAL, 50, 90, 75);
		adv_level_slider.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.sliderTickLabel);
		adv_level_slider.setFocusable(false);
		adv_level_slider.addChangeListener(new AdvLevelListener());
		adv_level_slider.setMajorTickSpacing(5);
		adv_level_slider.setPaintTicks(true);
		adv_level_slider.setToolTipText("Advanced level");
		adv_level_slider.setPaintLabels(true);
		
		toolbar.add(adv_level_slider);

		adv_max_slider = new JSlider(JSlider.HORIZONTAL, 2, 7, 3);
		adv_max_slider.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.sliderTickLabel);
		adv_max_slider.setFocusable(false);
		adv_max_slider.addChangeListener(new AdvMaxListener());
		adv_max_slider.setMajorTickSpacing(1);
		adv_max_slider.setPaintTicks(true);
		adv_max_slider.setToolTipText("Advanced max symbols");
		adv_max_slider.setPaintLabels(true);
		
		toolbar.add(adv_max_slider);
		
		toolbar.addSeparator(new Dimension(20, 10));
		
		run_box = new JCheckBox("Run", false);
		run_box.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkbox);
		run_box.setFocusable(false);
		
		run_box.addItemListener(new RunListener());
		toolbar.add(run_box);
		
		add(toolbar, BorderLayout.EAST);
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
		int wpm = storage.getOptInt("wpm");
		wpm_slider.setValue(wpm);
		sound.wpm(wpm);
		
		String lession = storage.getOptString("lession");
		lession_list.setSelectedItem(lession);
		loadLession(lession);

		int adv_level = storage.getOptInt("adv_level");
		adv_level_slider.setValue(adv_level);
		storage.setAdvLevel(adv_level);

		int adv_max = storage.getOptInt("adv_max");
		adv_max_slider.setValue(adv_max);
		storage.setAdvMax(adv_max);
	}

	private void startTimer(int delay) {
		timer = new Timer();
		timer.schedule(new LessionTask(), delay);
	}
	
	private void playQuestion() {
		String 	code = storage.getCode(question.symbol);
		sound.code(code);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (run_box.isSelected() && question != null) {
			char key = Character.toUpperCase(e.getKeyChar());
			
			if (key == ' ') {
				playQuestion();
			} else {
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
	}
	
}
