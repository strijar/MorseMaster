import javax.swing.*;
import java.awt.*;

import java.sql.SQLException;
import javax.sound.sampled.LineUnavailableException;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.managers.style.StyleId;

public class App extends JFrame {
	
	private Storage	storage = null;
	private Sound	sound = null;
	
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
	}
	
	private void createToolbar() {
		final JToolBar	toolbar = new JToolBar(SwingConstants.HORIZONTAL);
		
		toolbar.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.toolbarAttachedNorth);
		
		toolbar.add(new WebButton("Start"));
		add(toolbar, BorderLayout.PAGE_START);
	}
	
}
