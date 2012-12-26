package net.whzxt.zxtexam;

public class Action {
	private String itemname;
	private int dataid = 0;
	private int times = 0;
	private int min = 0;
	private int max = 0;
	private String err = "";
	private int fenshu = 0;
	
	public Action() {
		dataid = times = min = max = fenshu = 0;
		itemname = err = "";
	}
	
	public int getDataid() {
		return dataid;
	}
	public void setDataid(int dataid) {
		this.dataid = dataid; 
	}
	
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times; 
	}
	
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min; 
	}
	
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max; 
	}
	
	public int getFenshu() {
		return fenshu;
	}
	public void setFenshu(int fenshu) {
		this.fenshu = fenshu; 
	}
	
	public String getErr() {
		return err;
	}
	public void setErr(String err) {
		this.err = err; 
	}
	public String getItemname() {
		return itemname;
	}
	public void setItemname(String itemname) {
		this.itemname = itemname;
	}
}
