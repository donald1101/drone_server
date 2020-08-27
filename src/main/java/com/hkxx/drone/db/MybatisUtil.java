package com.hkxx.drone.db;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.InputStream;

/*
 * Utils to use mybatis.
 */
public class MybatisUtil {

    private static SqlSessionFactory sqlSessionFactory;
    private static ThreadLocal<SqlSession> threadLocal = null;

    /*
     * Load mybatis config from res file.
     */
    static {
        String resource = "mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            MybatisSqlSessionFactoryBuilder builder = new MybatisSqlSessionFactoryBuilder();
            sqlSessionFactory = builder.build(inputStream);

            threadLocal = new ThreadLocal<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Get the default sqlSessionFactory.
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public static SqlSession getSqlSession() {
        SqlSession sqlSession = threadLocal.get();  // 从当前线程获取
        if (sqlSession == null) {
            sqlSession = sqlSessionFactory.openSession();
            threadLocal.set(sqlSession);  // 将sqlSession与当前线程绑定
        }
        return sqlSession;
    }

    public static void close() {
        SqlSession sqlSession = threadLocal.get();  // 从当前线程获取
        if (sqlSession != null) {
            sqlSession.close();
            threadLocal.remove();
        }
    }
}
