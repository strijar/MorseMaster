import javax.swing.JFrame;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.StyleId;

public class App extends JFrame {
	
	private Storage storage = null;
	
	protected App() {
		setSize(640, 480);
		setTitle("MorseMaster");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getRootPane().putClientProperty(StyleId.STYLE_PROPERTY, StyleId.frameDecorated);
		setIconImages (WebLookAndFeel.getImages());
		setVisible(true);
		
		storage = new Storage();
	}
}
