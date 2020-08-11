import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import javax.sound.sampled.LineUnavailableException;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.StyleId;

@SuppressWarnings("serial")
public class App extends JFrame implements KeyListener {
	
	static final String AUTHOR = "<html><center>MorseMaster<br>Version 1.0<br>R1CBU</center></html>";
	
	private Storage		storage = null;
	private Sound		sound = null;
	private Lession		lession = null;
	private Timer		timer = null;
	private JCheckBox 	run_box = null;
	private JLabel		info = null;
	private Question	question = null;
	
	private int			question_delay = 2000;
	private int			help_delay = 3000;
	
	/* * */
	
	private class LessionTask extends TimerTask {
		public void run() {
			question = lession.getQuestion();

			String 	code = storage.getCode(question.symbol);
			boolean	help = question.correct <= 3 || question.ratio < 3;  

			info.setBackground(Color.white);
			
			if (help) {
				info.setText(question.symbol);
				startTimer(help_delay);
			} else {
				info.setText("❔");
				startTimer(question_delay);
			}
			sound.code(code);
		}
	}
	
	private class RunListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (run_box.isSelected()) {
				startTimer(1000);
			} else {
				timer.cancel();
				info.setText(AUTHOR);
				info.setBackground(Color.WHITE);
			}
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

		addKeyListener(this);
		setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        setLocationRelativeTo(null);
		setVisible(true);
		
		loadLession("Koch 4");
	}
	
	private void createToolbar() {
		final JToolBar	toolbar = new JToolBar(SwingConstants.HORIZONTAL);
		
		toolbar.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.toolbarAttachedNorth);
		
		run_box = new JCheckBox("Run", false);
		run_box.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkbox);
		run_box.setFocusable(false);
		
		run_box.addItemListener(new RunListener());
		toolbar.add(run_box);

		add(toolbar, BorderLayout.PAGE_START);
	}
	
	private void createCenter( ) {
		info = new JLabel(AUTHOR);
		
		info.setOpaque(true);
		info.setBackground(Color.WHITE);
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setVerticalAlignment(SwingConstants.CENTER);
		info.setFont(new Font("Courier New", Font.BOLD, 72));
		
		add(info, BorderLayout.CENTER);
	}
	
	private void loadLession(String info) {
		lession = storage.getLession(info);
		
		if (lession != null) {
			// storage.clearStat();
			lession.initStat();
		}
	}

	private void startTimer(int delay) {
		timer = new Timer();
		timer.schedule(new LessionTask(), delay);
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
			
			timer.cancel();

			if (lession.setAnswer(String.valueOf(key))) {
				info.setBackground(Color.GREEN);
				startTimer(500);
			} else {
				info.setText(question.symbol);
				info.setBackground(Color.RED);
				sound.alarm();
				startTimer(help_delay);
			}
			question = null;
		}
	}
	
}
