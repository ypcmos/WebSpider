package com.ypcl.jd;

public class Commodity {
	public int id;
	public String name;
	public float price;
	
	public Commodity() {
		
	}
	
	public Commodity(int id, String name, float price) {
		this.id = id;
		this.name = name;
		this.price = price;	
	}
	
	@Override
	public String toString() {
		return id + ", " + name + ", " + price;
	}
}
