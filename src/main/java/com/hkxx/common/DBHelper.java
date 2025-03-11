package com.hkxx.drone.common;

import java.sql.*;

public final class DBHelper {

	public static String driver = "com.mysql.jdbc.Driver"; // JDBC 驱动名称

	public static String url = "jdbc:MySQL://127.0.0.1:3306/controller"; // JDBC
																			// url

	public static String user = "root"; // 数据库用户名

	public static String password = "whljxx@709"; // 数据库密码

	// 获取JDBC数据库连接

	public static Connection getConnection() {

		Connection conn = null;

		try {

			// String driver = "com.mysql.jdbc.Driver";
			//
			// String url = "jdbc:MySQL://127.0.0.1:3306/school";
			//
			// String user = "root";
			//
			// String password = "hadoop";

			Class.forName(driver); // 加载JDBC驱动

			if (null == conn) {

				conn = DriverManager.getConnection(url, user, password);
			}

		} catch (ClassNotFoundException e) {

			System.out.println("Sorry,can't find the Driver!");

			e.printStackTrace();

		} catch (SQLException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return conn;

	}

	/**
	 * 
	 * 增删改查，简单查询
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @return int
	 */

	public static int executeNonQuery(String sql) {

		int result = 0;

		Connection conn = null;

		Statement stmt = null;

		try {

			conn = getConnection();

			stmt = conn.createStatement();

			result = stmt.executeUpdate(sql);

		} catch (SQLException err) {

			err.printStackTrace();

			free(null, stmt, conn);

		} finally {

			free(null, stmt, conn);

		}

		return result;

	}

	/**
	 * 
	 * 执行带参数的SQL语句
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @param obj
	 * 
	 * @return int
	 */

	public static int executeNonQuery(String sql, Object... obj) {

		int result = 0;

		Connection conn = null;

		PreparedStatement pstmt = null;

		try {

			conn = getConnection();

			pstmt = conn.prepareStatement(sql);

			for (int i = 0; i < obj.length; i++) {

				pstmt.setObject(i + 1, obj[i]);

			}

			result = pstmt.executeUpdate();

		} catch (SQLException err) {

			err.printStackTrace();

			free(null, pstmt, conn);

		} finally {

			free(null, pstmt, conn);

		}

		return result;

	}

	/**
	 * 
	 * 查询query
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @return ResultSet
	 */

	// public static ResultSet executeQuery(String sql) {
	//
	// Connection conn = null;
	//
	// Statement stmt = null;
	//
	// ResultSet rs = null;
	//
	// try {
	//
	// conn = getConnection();
	//
	// stmt = conn.createStatement();
	//
	// rs = stmt.executeQuery(sql);
	//
	// } catch (SQLException err) {
	//
	// err.printStackTrace();
	//
	// free(rs, stmt, conn);
	//
	// }
	//
	// return rs;
	//
	// }

	public static DataSet executeQuery(String sql) {

		DataSet ds = new DataSet();

		Connection conn = null;

		Statement stmt = null;

		ResultSet rs = null;

		try {

			conn = getConnection();

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql);

			ds.Conn = conn;

			ds.St = stmt;

			ds.Rs = rs;

		} catch (SQLException err) {

			err.printStackTrace();

			free(rs, stmt, conn);

			ds = null;
		}

		return ds;

	}

	/**
	 * 
	 * 带参数查询query
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @param obj
	 * 
	 * @return ResultSet
	 */

	// public static ResultSet executeQuery(String sql, Object... obj) {
	//
	// Connection conn = null;
	//
	// PreparedStatement pstmt = null;
	//
	// ResultSet rs = null;
	//
	// try {
	//
	// conn = getConnection();
	//
	// pstmt = conn.prepareStatement(sql);
	//
	// for (int i = 0; i < obj.length; i++) {
	//
	// pstmt.setObject(i + 1, obj[i]);
	//
	// }
	//
	// rs = pstmt.executeQuery();
	//
	// } catch (SQLException err) {
	//
	// err.printStackTrace();
	//
	// free(rs, pstmt, conn);
	//
	// }
	// return rs;
	//
	// }

	public static DataSet executeQuery(String sql, Object... obj) {

		DataSet ds = new DataSet();

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet rs = null;

		try {

			conn = getConnection();

			pstmt = conn.prepareStatement(sql);

			for (int i = 0; i < obj.length; i++) {

				pstmt.setObject(i + 1, obj[i]);

			}

			rs = pstmt.executeQuery();

			ds.Conn = conn;

			ds.St = pstmt;

			ds.Rs = rs;

		} catch (SQLException err) {

			err.printStackTrace();

			free(rs, pstmt, conn);

			ds = null;
		}
		return ds;

	}

	/**
	 * 
	 * 判断记录是否存在
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @return Boolean
	 */

	public static Boolean isExist(String sql) {

		ResultSet rs = null;

		try {

			rs = executeQuery(sql).Rs;

			rs.last();

			int count = rs.getRow();

			if (count > 0) {

				return true;

			} else {

				return false;

			}

		} catch (SQLException err) {

			err.printStackTrace();

			free(rs);

			return false;

		} finally {

			free(rs);

		}

	}

	/**
	 * 
	 * 带参数，判断记录是否存在
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @return Boolean
	 */

	public static Boolean isExist(String sql, Object... obj) {

		ResultSet rs = null;

		try {

			rs = executeQuery(sql, obj).Rs;

			rs.last();

			int count = rs.getRow();

			if (count > 0) {

				return true;

			} else {

				return false;

			}

		} catch (SQLException err) {

			err.printStackTrace();

			free(rs);

			return false;

		} finally {

			free(rs);

		}

	}

	/**
	 * 
	 * 获取查询记录的总行数
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @return int
	 */

	public static int getCount(String sql) {

		int result = 0;

		ResultSet rs = null;

		try {

			rs = executeQuery(sql).Rs;

			rs.last();

			result = rs.getRow();

		} catch (SQLException err) {

			free(rs);

			err.printStackTrace();

		} finally {

			free(rs);

		}

		return result;

	}

	/**
	 * 
	 * 带参数，获取查询记录的总行数
	 * 
	 *
	 * 
	 * @param sql
	 * 
	 * @param obj
	 * 
	 * @return int
	 */

	public static int getCount(String sql, Object... obj) {

		int result = 0;

		ResultSet rs = null;

		try {

			rs = executeQuery(sql, obj).Rs;

			rs.last();

			result = rs.getRow();

		} catch (SQLException err) {

			err.printStackTrace();

		} finally {

			free(rs);

		}

		return result;

	}

	/**
	 * 
	 * 释放【ResultSet】资源
	 * 
	 *
	 * 
	 * @param rs
	 */

	public static void free(ResultSet rs) {

		try {

			if (rs != null) {

				rs.close();

			}

		} catch (SQLException err) {

			err.printStackTrace();

		}

	}

	/**
	 * 
	 * 释放【Statement】资源
	 * 
	 *
	 * 
	 * @param st
	 */

	public static void free(Statement st) {

		try {

			if (st != null) {

				st.close();

			}

		} catch (SQLException err) {

			err.printStackTrace();

		}

	}

	/**
	 * 
	 * 释放【Connection】资源
	 * 
	 *
	 * 
	 * @param conn
	 */

	public static void free(Connection conn) {

		try {

			if (conn != null) {

				conn.close();

			}

		} catch (SQLException err) {

			err.printStackTrace();

		}

	}

	/**
	 * 
	 * 释放所有数据资源
	 * 
	 *
	 * 
	 * @param rs
	 * 
	 * @param st
	 * 
	 * @param conn
	 */

	public static void free(ResultSet rs, Statement st, Connection conn) {

		free(rs);

		free(st);

		free(conn);

	}

	public static void free(DataSet ds) {
		try {
			if (ds != null) {
				free(ds.Rs);

				free(ds.St);

				free(ds.Conn);
			}

		} catch (Exception err) {
			// TODO: handle exception
			err.printStackTrace();
		}
	}

}
