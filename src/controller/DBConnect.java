package controller;

import java.sql.*;

public class DBConnect {
	/** 定义一个Connection 用来连接数据库 */
	private Connection conn = null;
	/** 连接数据库的URL */
	private final String url = "jdbc:mysql://59.110.217.208:3306/qikanlunwen?" + "useUnicode=true&characterEncoding=UTF-8&useSSL=false";
	/** 指定数据的用户名和密码 */
	private final String username = "root";
	private final String password = "root";
	/** 定义一个int记录更新的记录数量 */
	int count = 0;

	/// **定义一个结果集 用于返回查询结果*/
	private ResultSet resultSet = null;
	private PreparedStatement pstmt = null;

	public Connection connect() {
		conn = connectionDB();
		return conn;
	}

	/**
	 * 建立数据的连接
	 * 
	 * @exception SQLException,
	 *                ClassNotFoundException
	 */
	@SuppressWarnings("finally")

	public Connection connectionDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, password);
			System.out.println("连接数据库成功");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("建立数据库发生错误！");
		} finally {
			return conn;
		}
	}

	public int dbinsert(String sql) {
		try {
			conn = connectionDB();
			pstmt = conn.prepareStatement(sql);
			count = pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}

	public ResultSet dbselect(String sql) {
		try {
			conn = connectionDB();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultSet;
	}

	public int dbupdate(String sql) {
		try {
			conn = connectionDB();
			pstmt = conn.prepareStatement(sql);
			count = pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}

	public int dbdelete(String sql) {
		try {
			conn = connectionDB();
			pstmt = conn.prepareStatement(sql);
			count = pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public void dbClose()
	{
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
