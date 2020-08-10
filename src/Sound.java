import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Sound {
	private static final int	SAMPLE_RATE = 16*1024;

	private int					freq = 700;
	private int					attack = 5;
	private int					dit = 100;
	private int					dah = 300;
	
	SourceDataLine			line;

	public Sound() throws LineUnavailableException {
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, Sound.SAMPLE_RATE);
        line.start();
	}
	
	public void wpm(int x) {
		dit = (int) Math.round(60.0/(x*50.0)*1000.0);
		dah = dit * 3;
	}
	
	public void tone(int ms) {
		int 			length = SAMPLE_RATE * ms / 1000;
		int				a = SAMPLE_RATE * attack / 1000;
		int				r = length - a;
		byte[]			sin = new byte[length];
		
		for (int i = 0; i < length; i++) {
			double period = (double)SAMPLE_RATE / freq;
            double angle = 2.0 * Math.PI * i / period;
            double amp = 1.0f;
            
            if (i < a) {
            	amp = (double) i/a;
            } else if (i > r) {
            	amp = 1.0f - (double)(i-r)/a;
            }
            
            sin[i] = (byte)(Math.sin(angle) * amp * 127f);
        }
		line.write(sin, 0, length);
	}

	public void pause(int ms) {
		int 			length = SAMPLE_RATE * ms / 1000;
		byte[]			sin = new byte[length];
		
		for (int i = 0; i < length; i++) {
			sin[i] = 0;
        }
		line.write(sin, 0, length);
	}
	
	public void code(String text) {
		for (char c : text.toCharArray()) {
			switch (c) {
			case '.':
				tone(dit);
				pause(dit);
				break;
			case '-':
				tone(dah);
				pause(dit);
				break;
			case ' ':
				pause(dit*2);
				break;
			case '|':
				pause(dit*6);
				break;
			}
		}
	}

}
