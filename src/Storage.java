import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Storage {
	
	private Connection			connection = null;
	private PreparedStatement	stm_code = null;
	private PreparedStatement	stm_next_symbol = null;
	private PreparedStatement	stm_next_adv = null;
	private PreparedStatement	stm_count_adv = null;
	private PreparedStatement	stm_get_opt = null;
	private PreparedStatement	stm_set_opt = null;
	
	private int					adv_level = 75;
	private int					adv_max = 3;
	
	public Storage() throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
		connection.setAutoCommit(false);

		stm_code = connection.prepareStatement("SELECT code FROM codes WHERE symbol = ?");
		stm_next_symbol = connection.prepareStatement("SELECT symbol, correct, 5*correct/(correct+mistake/3) AS ratio, 100*correct/(correct+mistake/2) as level FROM stat WHERE NOT (level >= ? AND correct >= 10) ORDER BY ratio, lastseen LIMIT 2");
		stm_next_adv = connection.prepareStatement("SELECT symbol, 100*correct/(correct+mistake/2) AS ratio FROM stat WHERE ratio >= ? AND correct >= 10 ORDER BY ratio DESC, lastseen ASC");
		stm_count_adv = connection.prepareStatement("SELECT count(*) as count, 100*correct/(correct+mistake/2) AS ratio FROM stat WHERE ratio >= ? AND correct >= 10");
		stm_get_opt = connection.prepareStatement("SELECT val FROM opts WHERE name = ?");
		stm_set_opt = connection.prepareStatement("UPDATE opts SET val = ? WHERE name = ?");
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
	
	public Lession loadLession(String info) {
		try {
			PreparedStatement	stm = connection.prepareStatement("SELECT symbols FROM lession WHERE info = ?");
			stm.setString(1, info);

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
			connection.commit();
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
			connection.commit();
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

	public Question getNextSymbol(int remain) {
		try {
			stm_next_symbol.setInt(1, adv_level);

			ResultSet	rs = stm_next_symbol.executeQuery();
			
			if (remain > 1 && Math.random() > 0.5) {
				rs.next();
				rs.next();
			}
			
			return new Question(rs.getString("symbol"), rs.getInt("correct"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getCountAdv() {
		try {
			stm_count_adv.setInt(1, adv_level);

			ResultSet		rs = stm_count_adv.executeQuery();
			
			return rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Question getNextAdv() {
		try {
			stm_next_adv.setInt(1, adv_level);

			ResultSet		rs = stm_next_adv.executeQuery();
			String			question = "";
			int				max_ratio = Math.min(rs.getInt("ratio"), 99);
			int				count = 2 + (adv_max - 1) * (max_ratio - adv_level) / (100 - adv_level);
			Vector<String>	items = new Vector<String>();
			
			while (rs.next()) {
				items.add(rs.getString("symbol"));
			}
			
			for (int i = 0; i < count; i++)
				question += items.elementAt((int) (Math.random() * items.size()));
			
			return new Question(question, 999);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Vector<String> getLessions() {
		Vector<String>		items = new Vector<String>();
		
		try {
			PreparedStatement	stm = connection.prepareStatement("SELECT info FROM lession");
			ResultSet			rs = stm.executeQuery();
			
			while (rs.next()) {
				items.add(rs.getString("info"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return items;
	}

	public int getOptInt(String name) {
		try {
			stm_get_opt.setString(1, name);

			ResultSet rs = stm_get_opt.executeQuery();
			
			return rs.getInt("val");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	public void setOptInt(String name, int x) {
		try {
			stm_set_opt.setInt(1, x);
			stm_set_opt.setString(2, name);

			stm_set_opt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getOptString(String name) {
		try {
			stm_get_opt.setString(1, name);

			ResultSet rs = stm_get_opt.executeQuery();
			
			return rs.getString("val");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return "";
	}

	public void setOptString(String name, String x) {
		try {
			stm_set_opt.setString(1, x);
			stm_set_opt.setString(2, name);

			stm_set_opt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setAdvLevel(int val) {
		adv_level = val;
	}

	public void setAdvMax(int val) {
		adv_max = val;
	}

	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
