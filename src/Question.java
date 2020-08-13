
public class Question {

	public String 	symbol;
	public int		correct;
	
	public Question(String symbol, int correct) {
		this.symbol = symbol;
		this.correct = correct;
	}

	public int length() {
		return symbol.length();
	}
	
	public String getSecret(String prefix) {
		String res = prefix;
		
		for (int i = prefix.length(); i < symbol.length(); i++)
			res += "*";
			
		return res;
	}

}
