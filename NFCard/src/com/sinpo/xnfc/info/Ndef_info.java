package com.sinpo.xnfc.info;

public class Ndef_info {
	private String name;
	private String card_nums;
	private String account;
	private String passwd;
	private String txt;
	private String balance;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCard_nums() {
		return card_nums;
	}
	public void setCard_nums(String card_nums) {
		this.card_nums = card_nums;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	public String toString(){
		return "name:"+name+" card_nums:"+card_nums+" account:"+account+" passwd:"+passwd+" txt:"+txt
		+" balance:"+balance;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}

}
