import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import javax.sound.sampled.LineUnavailableException;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.StyleId;

@SuppressWarnings("serial")
public class App extends JFrame {
	
	private Storage	storage = null;
	private Sound	sound = null;
	private Lession	lession = null;
	private Timer	timer = null;
	
	private class LessionTask extends TimerTask {
		public void run() {
			System.out.println(lession.getNext());
		}
	}
	
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
		
		setVisible(true);
		
		loadLession("Koch 3");
	}
	
	private void createToolbar() {
		final JToolBar	toolbar = new JToolBar(SwingConstants.HORIZONTAL);
		
		toolbar.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.toolbarAttachedNorth);
		
		final JCheckBox runBox = new JCheckBox("Run", false);
		runBox.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkbox);
		
		runBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (runBox.isSelected()) {
					timer = new Timer();
					timer.schedule(new LessionTask(), 1000, 1000);
				} else {
					timer.cancel();
				}
		    }
		});
		  
		toolbar.add(runBox);
		add(toolbar, BorderLayout.PAGE_START);
	}
	
	private void loadLession(String info) {
		lession = storage.getLession(info);
		
		if (lession != null) {
			// storage.clearStat();
			lession.initStat();
		}
	}
	
}
