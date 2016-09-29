package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.BaseDAO;
import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.Serializable;
import java.sql.*;
import java.util.List;
import java.util.Map;

public abstract class BaseJPADAO<T, PK extends Serializable> implements BaseDAO<T, PK> {

    protected static Logger myLog = Logger.getLogger(BaseJPADAO.class);
    private final Class<T> type;
    protected EntityManagerFactory emf;


    public BaseJPADAO(Class<T> type) {
        this.type = type;
    }

    public void create(T t) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

    }

    public T read(PK pk) {
        T aT = null;

        EntityManager em = getEntityManager();
        try {
            aT = em.find(type, pk);
            if (aT != null) {
                // to ensure the data is refresh for every new read.
                em.refresh(aT);
            }
        } finally {
            em.close();
        }

        return aT;
    }

    public void update(T t) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(t);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(T t) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.remove(em.merge(t));
            em.getTransaction().commit();
        } finally {
            em.close();
        }

    }

    public T refresh(T t) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            if (em.contains(t)) {
                em.refresh(t);
            } else {
                em.merge(t);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return t;
    }


    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    protected EntityManager getEntityManager() {
        EntityManager theEm = null;
        if(emf!=null) {
            theEm = emf.createEntityManager();
        }
        return theEm;
    }

    public T executeQueryWithSingleResult(String jpaQueryString) {
        return executeQueryWithSingleResult(jpaQueryString, null);
    }

    public T executeQueryWithSingleResult(String jpaQueryString, JPAParameter jpaParameter) {
        T vo = null;
        EntityManager em = getEntityManager();
        StringBuilder strBuilder = new StringBuilder();
        try {
            Query query = em.createNamedQuery(jpaQueryString);
            if (jpaParameter != null) {
                for (Map.Entry<String, Object> entrySet : jpaParameter.getParameterMap().entrySet()) {
                    query = query.setParameter(entrySet.getKey(), entrySet.getValue());
                    strBuilder.append(entrySet.getKey() + "=" + entrySet.getValue() + ", ");
                }

            }
            vo = (T) query.getSingleResult();

        } catch (NoResultException nre) {
            // Ignore
        } finally {
            em.close();
        }
        return vo;
    }


    public T executeQueryWithFirstResult(String jpaQueryString) {
        return executeQueryWithSingleResult(jpaQueryString, null);
    }

    public T executeQueryWithFirstResult(String jpaQueryString, JPAParameter jpaParameter) {
        T vo = null;
        EntityManager em = getEntityManager();
        StringBuilder strBuilder = new StringBuilder();
        try {
            Query query = em.createNamedQuery(jpaQueryString);
            if (jpaParameter != null) {
                for (Map.Entry<String, Object> entrySet : jpaParameter.getParameterMap().entrySet()) {
                    query = query.setParameter(entrySet.getKey(), entrySet.getValue());
                    strBuilder.append(entrySet.getKey() + "=" + entrySet.getValue() + ", ");
                }

            }
            vo = (T) query.getSingleResult();

        } catch (NoResultException nre) {
            // Ignore
        } finally {
            em.close();
        }
        return vo;
    }

    public List<T> executeQueryWithResultList(String jpaQueryString) {
        return executeQueryWithResultList(jpaQueryString, null);
    }

    public List<T> executeQueryWithResultList(String jpaQueryString, JPAParameter jpaParameter) {
        List<T> voList = null;
        EntityManager em = getEntityManager();
        StringBuilder strBuilder = new StringBuilder();
        try {
            Query query = em.createNamedQuery(jpaQueryString);
            if (jpaParameter != null) {
                for (Map.Entry<String, Object> entrySet : jpaParameter.getParameterMap().entrySet()) {
                    query = query.setParameter(entrySet.getKey(), entrySet.getValue());
                    strBuilder.append(entrySet.getKey() + "=" + entrySet.getValue());
                }
            }
            voList = (List<T>) query.getResultList();
        } catch (NoResultException nre) {
            myLog.debug("Error of executing query:" + jpaQueryString +
                    ", parameters:" + strBuilder.toString() + ", cause" + nre.getMessage(), nre);
        } finally {
            em.close();
        }
        return voList;
    }


    public JSONArray executeStatement(String sqlStatement) {
        ResultSet resultSet = null;
        Connection conn = null;
        Statement stmt = null;
        JSONArray jArray = null;
        EntityManager em = null;
        try {
            myLog.debug("Execute SQL->" + sqlStatement);
            em =getEntityManager();
            conn = getDBConnection(em);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sqlStatement);
            jArray = convert(resultSet);

        } catch (JSONException ex) {
            myLog.error("JSON Error->" + ex.getMessage(), ex);
        } catch (SQLException ex) {
            myLog.error("SQL Error->" + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    stmt.close();
//                    conn.close();
                    em.close();
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
