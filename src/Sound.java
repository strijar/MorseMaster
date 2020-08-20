import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Sound {
	private static final int	SAMPLE_RATE = 8000;

	private int					freq = 700;			// Hz
	private int					attack = 3;			// ms
	private int					dit = 100;			// ms
	private int					dah = 300;			// ms
	private int					symbol_pause = 3;	// dits
	private int					word_pause = 7;		// dits
	
	SourceDataLine				line;
	byte[]						buf;

	public Sound() throws LineUnavailableException {
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, Sound.SAMPLE_RATE);
        line.start();
	}
	
	public void wpm(int x) {
		dit = (int) Math.round(60.0/(x*50.0)*1000.0);
		dah = dit * 3;
		
		dit += attack;
		dah += attack;
	}
	
	public int tone(int ms, int freq, int from) {
		int 			length = SAMPLE_RATE * ms / 1000;
		int				a = SAMPLE_RATE * attack / 1000;
		int				r = length - a;
		
		for (int i = 0; i < length; i++) {
			double period = (double)SAMPLE_RATE / freq;
            double angle = 2.0 * Math.PI * i / period;
            double amp = 1.0f;
            
            if (i < a) {
            	amp = (double) i/a;
            } else if (i > r) {
            	amp = 1.0f - (double)(i-r)/a;
            }
            
            buf[from + i] = (byte)(Math.sin(angle) * amp * 127f);
        }
		
		return from + length;
	}

	public int pause(int ms, int from) {
		int 			length = SAMPLE_RATE * ms / 1000;
		
		for (int i = 0; i < length; i++) {
			buf[from + i] = 0;
        }
		
		return from + length;
	}
	
	public int code(final String text) {
		final char[]	chars = text.toCharArray();
		int				length = 100;
		
		for (char c : chars) {
			switch (c) {
			case '.':
				length += dit + dit;
				break;
			case '-':
				length += dah + dit;
				break;
			case ' ':
				length += dit * (symbol_pause-1);
				break;
			case '|':
				length += dit * (word_pause-1);
				break;
			}
		}
		
		buf = new byte[SAMPLE_RATE * length / 1000];
		
		new Thread(new Runnable() {
		    @Override 
		    public void run() {
		    	int from;

				from = pause(100, 0);

		    	for (char c : chars) {
					switch (c) {
					case '.':
						from = tone(dit, freq, from);
						from = pause(dit, from);
						break;
					case '-':
						from = tone(dah, freq, from);
						from = pause(dit, from);
						break;
					case ' ':
						from = pause(dit * (symbol_pause - 1), from);
						break;
					case '|':
						from = pause(dit * (word_pause - 1), from);
						break;
					}
				}
		    	
				line.write(buf, 0, buf.length);
		    }
		}).start();
		
		return length;
	}
	
	public void alarm() {
		new Thread(new Runnable() {
		    @Override 
		    public void run() {
		    	int from = 0;
		    	int length = (50+50)*3; 

    			buf = new byte[SAMPLE_RATE * length / 1000];
		    	
		    	for (int i = 0; i < 3; i++) {
		    		from = tone(50, 220, from);
		    		from = tone(50, 440, from);
		    	}
	    		line.write(buf, 0, buf.length);
		    }
		}).start();
	}

}
