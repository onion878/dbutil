package onion.util.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class DataTable {
	private String[] head;
	private ArrayList<Object[]> data = new ArrayList();
	private int len;

	public DataTable(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		this.len = meta.getColumnCount();
		Logger.log(0, "DataTable.DataTable:" + this.len);
		this.head = new String[this.len];

		for (int record = 0; record < this.len; ++record) {
			this.head[record] = meta.getColumnName(record + 1);
			Logger.log(0, "DataTable.DataTable:" + this.head[record]);
		}
		int jj = 1;
		while (rs.next()) {
			Object[] var5 = new Object[this.len];
			Logger.lognoln(0, "###LOGTIME " + DateUtil.getnow()
					+ " =DataTable.DataTable:rs" + jj);
			for (int i = 0; i < this.len; ++i) {
				var5[i] = rs.getObject(this.head[i]);
				Logger.lognoln(0, "@" + var5[i]);
			}
			Logger.logln(0, "");
			jj++;
			this.data.add(var5);
		}

	}

	public String[] getHead() {
		return this.head;
	}

	public Object[][] getData() {
		Object[] lines = this.data.toArray();
		Object[][] table = new Object[lines.length][this.len];

		for (int i = 0; i < table.length; ++i) {
			for (int j = 0; j < table[i].length; ++j) {
				table[i][j] = ((Object[]) lines[i])[j];
			}
		}

		return table;
	}
}


