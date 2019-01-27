
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.util.IoUtils;

@SuppressWarnings("unused")
public class Test {
	
	private static final Logger LOG = LoggerFactory.getLogger(Test.class);

	/**
	 * 基本方式连接DB
	 */
	public static void testJDBC() {
		String url = "jdbc:mysql://10.22.25.49:3306/smc?useUnicode=true&characterEncoding=utf8&generateSimpleParameterMetadata=true";
		String username = "smc";
		String password = "123456";
		
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, password);
			statement = conn.createStatement();
			rs = statement.executeQuery("select * from mst_user");
			while(rs.next()) {
				LOG.info(rs.getString(1));
			}
		} catch(ClassNotFoundException ce) {
			LOG.error(ce.getMessage());
		} catch(SQLException se) {
			LOG.error(se.getMessage());
		} finally {
			IoUtils.closeQuietly(rs, statement, conn);
		}
	}
	
	/** 
     * 获得MyBatis SqlSessionFactory   
     * SqlSessionFactory负责创建SqlSession，一旦创建成功，就可以用SqlSession实例来执行映射语句，commit，rollback，close等方法。 
     * @return 
     */  
    private static SqlSessionFactory getSessionFactory() {  
        SqlSessionFactory sessionFactory = null;  
        String resource = "j2se/jdbc/mysql/config/configuration.xml";  
        try {  
            sessionFactory = new SqlSessionFactoryBuilder().build(Resources  
                    .getResourceAsReader(resource));  
        } catch (IOException e) {  
        	LOG.error(e.getMessage());
        }  
        return sessionFactory;  
    }  
  
    public static void main(String[] args) {  
        // TODO
    }
}
