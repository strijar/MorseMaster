
public class Lession {

	private Storage		storage = null;
	private String[]	symbols = null;
	private Question	question = null;	
	
	public Lession(Storage storage, String[] symbols) {
		this.storage = storage;
		this.symbols = symbols;
	}
	
	public void initStat() {
		storage.initStat(symbols);
	}
	
	public Question getQuestion() {
		if (true && Math.random() >= 0.5) {
			question = storage.getNextAdv();
			
			if (question == null) {
				question = storage.getNextSymbol();
			}
		} else {
			question = storage.getNextSymbol();
		}

		return question;
	}
	
	public boolean setAnswer(String symbol) {
		boolean correct = question.symbol.equals(symbol);
		storage.updateStat(question.symbol, correct);
		
		return correct;
	}

}
