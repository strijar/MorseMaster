import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Storage {
	
	private Connection			connection = null;
	private PreparedStatement	stm_code = null;
	private PreparedStatement	stm_next = null;
	
	public Storage() throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
		stm_code = connection.prepareStatement("SELECT code FROM codes WHERE symbol = ?");
		stm_next = connection.prepareStatement("SELECT symbol, correct, 5*correct/(correct+mistake) AS ratio FROM stat ORDER BY ratio, lastseen LIMIT 2");
	}
	
	public String getCode(String text) {
		String				res = "";

		for (char c : text.toCharArray()) {
			if (c == ' ') {
				res += "|";
			} else {
				try {
					stm_code.setString(1, Character.toString(c));

					ResultSet	rs = stm_code.executeQuery();
					String		code = rs.getString("code");
					
					if (!code.isEmpty()) {
						res += code + " ";
					}
					
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return res;
				}
			}
		}
		return res;
	}
	
	public Lession getLession(String info) {
		try {
			PreparedStatement	stm = connection.prepareStatement("SELECT symbols FROM lession WHERE info LIKE ?");
			stm.setString(1, info + "%");

			ResultSet	rs = stm.executeQuery();
			String		symbols = rs.getString("symbols");
			
			if (!symbols.isEmpty()) {
				return new Lession(this, symbols.split(" "));
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void clearStat() {
		try {
			Statement stm = connection.createStatement();

			stm.execute("DELETE FROM stat");
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void initStat(String[] symbols) {
		try {
			PreparedStatement stm = connection.prepareStatement("INSERT OR IGNORE INTO stat (symbol, correct, mistake, lastseen) VALUES (?, 0, 5, ?)");
			
			for (String symbol : symbols) {
				stm.setString(1, symbol);
				stm.setLong(2, System.currentTimeMillis());
				stm.execute();
			}
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateStat(String symbol, boolean correct) {
		PreparedStatement stm;

		try {
			if (correct) {
				stm = connection.prepareStatement("UPDATE stat SET correct = correct + 1, lastseen = ? WHERE symbol = ?");
			} else {
				stm = connection.prepareStatement("UPDATE stat SET mistake = mistake + 1, lastseen = ? WHERE symbol = ?");
			}

			stm.setLong(1, System.currentTimeMillis());
			stm.setString(2, symbol);
			stm.execute();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	public Question getNext() {
		try {
			ResultSet	rs = stm_next.executeQuery();
			
			if (Math.random() > 0.5) {
				rs.next();
				rs.next();
			}
			
			return new Question(rs.getString("symbol"), rs.getInt("correct"), rs.getDouble("ratio"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
