import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Storage {
	private Connection connection = null;
	
	public Storage() {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
