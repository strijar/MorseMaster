import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Storage {
	private Connection connection = null;
	
	public Storage() throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
	}
	
	public String getCode(String text) {
		String		res = "";
		Statement	stm;

		try {
			stm = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			return res;
		}

		for (char c : text.toCharArray()) {
			if (c == ' ') {
				res += "|";
			} else {
				try {
					ResultSet	rs = stm.executeQuery(String.format("SELECT code FROM codes WHERE symbol = '%s'", c));
					String		code = rs.getString("code");
					
					if (!code.isEmpty()) {
						res += code + " ";
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return res;
				}
			}
		}
		return res;
	}
}
