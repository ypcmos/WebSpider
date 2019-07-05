package com.ypcl.jd.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.ypcl.jd.Commodity;

public class CommodityDao {
	Connection conn = null;
	
	public CommodityDao(Connection conn) {
		this.conn = conn;
	}
	
	public boolean insert(Commodity c) throws SQLException {
		Statement stmt;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) totalCount FROM commodity WHERE id=" + c.id);
		rs.next();
		if (rs.getInt("totalCount") > 0) {
			return false;
		} 
		
		rs.close();
		stmt.execute("INSERT INTO commodity(id, name, price) VALUES(" + c.id + ", '" + c.name + "', " + c.price + ")");
		stmt.close();
		return true;
	}
	
	public List<Commodity> getAll() throws SQLException {
		List<Commodity> list = new LinkedList<Commodity>();
		Statement stmt;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM commodity");
		
		while (rs.next()) {
			Commodity c = new Commodity(rs.getInt("id"), rs.getString("name"), rs.getFloat("price"));
			list.add(c);
		}
		
		rs.close();
		stmt.close();
		return list;
	}
}
