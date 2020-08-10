
import javax.swing.SwingUtilities;
import com.alee.laf.WebLookAndFeel;

public class Main {

	public static void main(String[] args) {
        SwingUtilities.invokeLater( new Runnable ()
        {
            public void run ()
            {
                WebLookAndFeel.install ();
                App app = new App();
            }
        } 
        );
	}

}
