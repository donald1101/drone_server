package com.hkxx.drone.db.service;


import com.hkxx.drone.db.MybatisUtil;
import org.apache.ibatis.session.SqlSession;

public class BasicDbService {
	protected SqlSession sqlSession = null;

	protected void openDB() throws Exception {
		this.sqlSession = MybatisUtil.getSqlSessionFactory().openSession();
		if (this.sqlSession == null) {
			throw new Exception("Failed to get db connection.");
		}
	}

	protected void rollbackDB() {
		if (this.sqlSession != null) {
			this.sqlSession.rollback();
		}
	}

	protected void closeDB() {
		if (this.sqlSession != null) {
			this.sqlSession.close();
		}
	}
}
