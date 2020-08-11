
public class Lession {

	private Storage		storage = null;
	private String[]	symbols = null;
	private String		question = null;	
	
	public Lession(Storage storage, String[] symbols) {
		this.storage = storage;
		this.symbols = symbols;
	}
	
	public void initStat() {
		storage.initStat(symbols);
	}
	
	public String getQuestion() {
		question = storage.getNext();
		return question;
	}
	
	public void setAnswer(String symbol) {
		storage.updateStat(question, question.equals(symbol));
	}

}
