package onion.util.db;

public class Page {
	private int total;
	private int rows;
	private int page;
	private int stt;
	private int limit;
	private int offset;
	private int stb;
	private String sval;
	private String q;
	private String key;
	private String value;

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSval() {
		return sval;
	}

	public void setSval(String sval) {
		this.sval = sval;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getStb() {
		stb = offset;
		return stb;
	}

	public void setStb(int stb) {
		this.stb = stb;
	}

	public int getStt() {
		stt = rows * (page - 1);
		return stt;
	}

	public void setStt(int stt) {
		this.stt = stt;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "Page [key=" + key + ", limit=" + limit + ", offset=" + offset
				+ ", page=" + page + ", q=" + q + ", rows=" + rows + ", stb="
				+ stb + ", stt=" + stt + ", sval=" + sval + ", total=" + total
				+ ", value=" + value + "]";
	}

	
}


