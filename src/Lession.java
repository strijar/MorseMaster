
public class Lession {

	private Storage		storage = null;
	private String[]	symbols = null;
	private Question	question = null;	
	private int			count = 0;
	
	public Lession(Storage storage, String[] symbols) {
		this.storage = storage;
		this.symbols = symbols;
	}
	
	public void initStat() {
		storage.initStat(symbols);
	}
	
	public Question getQuestion() {
		int	adv = storage.getCountAdv();
		int remain = symbols.length - adv;
		
		if (adv > 0 && (remain == 0 || count++ % (remain+1) == 0)) {
			question = storage.getNextAdv(adv);
		} else {
			question = storage.getNextSymbol(remain);
		}
		
		// System.out.println(question.symbol);
		
		return question;
	}
	
	public boolean setAnswer(String answer) {
		boolean correct = true;
		
		for (int i = 0; i < Math.min(answer.length(), question.length()); i++) {
			char a = answer.charAt(i);
			char q = question.symbol.charAt(i);
			
			if (a == q) {
				storage.updateStat(String.valueOf(q), true);
			} else {
				storage.updateStat(String.valueOf(q), false);
				storage.updateStat(String.valueOf(a), false);
				correct = false;
			}
		}
		
		storage.commit();
		return correct;
	}

}
