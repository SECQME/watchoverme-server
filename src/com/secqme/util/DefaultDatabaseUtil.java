package com.secqme.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import javax.persistence.EntityManagerFactory;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author coolboykl
 */
public class DefaultDatabaseUtil implements DatabaseUtil {

    private static final Logger myLog = Logger.getLogger(DefaultDatabaseUtil.class);
    private EntityManagerFactory emf;

    public DefaultDatabaseUtil(EntityManagerFactory emFactory) {
        emf = emFactory;
    }

    public JSONArray executeStatement(String sqlStatement) {
        ResultSet resultSet = null;
        Connection conn = null;
        Statement stmt = null;
        JSONArray jArray = null;
        try {
            myLog.debug("Execute SQL->" + sqlStatement);
            EntityManager em;
            em = emf.createEntityManager();
            em.getTransaction().begin();
            conn = getDBConnection(em);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sqlStatement);
            jArray = convert(resultSet);
            em.getTransaction().commit();

        } catch (JSONException ex) {
            myLog.error("JSON Error->" + ex.getMessage(), ex);
        } catch (SQLException ex) {
            myLog.error("SQL Error->" + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    stmt.close();
                    conn.close();
                } catch (SQLException ex) {
                    myLog.error("SQL Error when closing->" + ex.getMessage(), ex);
                }
            }
        }
        return jArray;

    }

    private JSONArray convert(ResultSet rs) throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i = 1; i < numColumns + 1; i++) {
                String column_name = rsmd.getColumnName(i);

                if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
                    obj.put(column_name, rs.getArray(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
                    obj.put(column_name, rs.getBoolean(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
                    obj.put(column_name, rs.getBlob(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
                    obj.put(column_name, rs.getDouble(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
                    obj.put(column_name, rs.getFloat(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
                    obj.put(column_name, rs.getNString(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
                    obj.put(column_name, rs.getString(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
                    obj.put(column_name, rs.getDate(column_name));
                } else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                    obj.put(column_name, rs.getTimestamp(column_name));
                } else {
                    obj.put(column_name, rs.getObject(column_name));
                }
            }

            json.put(obj);
        }

        return json;
    }

    private Connection getDBConnection(EntityManager em) {
        UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl) ((JpaEntityManager) em.getDelegate()).getActiveSession();
        unitOfWork.beginEarlyTransaction();
        Accessor accessor = unitOfWork.getAccessor();
        accessor.incrementCallCount(unitOfWork.getParent());
        accessor.decrementCallCount();
        return accessor.getConnection();

    }
}