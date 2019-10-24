/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mongodb.util;

import java.util.*;
import java.util.stream.Collectors;

import com.mongodb.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import org.wso2.carbon.mongodb.query.MongoPreparedStatement;
import org.wso2.carbon.mongodb.query.MongoPreparedStatementImpl;
import org.wso2.carbon.mongodb.query.MongoDBQueryException;
import org.wso2.carbon.mongodb.user.store.mgt.MongoDBCoreConstants;
import org.wso2.carbon.mongodb.user.store.mgt.MongoDBRealmConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * MongoDB database operations.
 */
public class MongoDatabaseUtil {

    private static final Log log = LogFactory.getLog(MongoDatabaseUtil.class);
    private static long connectionsClosed;

    private static DB db = null;

    private static Map<String, MongoClient> mongoClients;

    /**
     * Return the realm data source of user store.
     *
     * @param realmConfiguration of user store
     * @return DB connection
     * @throws UserStoreException if any error occurred
     */
    public static synchronized DB getRealmDataSource(RealmConfiguration realmConfiguration) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getRealmDataSource(RealmConfiguration realmConfiguration)");
        }

        try {
            return (db == null ? createRealmDataSource(realmConfiguration) : db);
        } catch (UserStoreException e) {
            throw new UserStoreException("Failed to retrieve realm data source", e);
        }
    }

    /**
     * Create the realm data source.
     *
     * @param realmConfiguration of user store
     * @return DB connection
     * @throws UserStoreException if any error occurred
     */
    public static DB createRealmDataSource(RealmConfiguration realmConfiguration) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("createRealmDataSource(RealmConfiguration realmConfiguration)");
        }

        String password;
        String url;
        String username;

        if (realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.URL) != null) {
            url = realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.URL);
        } else {
            throw new UserStoreException("Required property '" + MongoDBRealmConstants.URL +
                    "' not found for the primary UserStoreManager in user_mgt.xml. Cannot start server!");
        }

        if (realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.USERNAME) != null) {
            username = realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.USERNAME);
        } else {
            throw new UserStoreException("Required property '" + MongoDBRealmConstants.USERNAME +
                    "' not found for the primary UserStoreManager in user_mgt.xml. Cannot start server!");
        }

        if (realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.PASSWORD) != null) {
            password = realmConfiguration.getUserStoreProperty(MongoDBRealmConstants.PASSWORD);
        } else {
            throw new UserStoreException("Required property '" + MongoDBRealmConstants.PASSWORD +
                    "' not found for the primary UserStoreManager in user_mgt.xml. Cannot start server!");
        }

        String urlWithCredentials = url.replaceFirst("://", "://" + username + ":" + password + "@");
        MongoClientURI clientURI = new MongoClientURI(urlWithCredentials);

        //noinspection ConstantConditions
        if (clientURI.getDatabase() == null) {
            if (log.isDebugEnabled()) {
                log.debug("URL provided: " + clientURI);
            }
            throw new UserStoreException("Property '" + MongoDBRealmConstants.URL +
                    "' provided in user_mgt.xml does not contain the database name. Cannot start server!");
        }

        MongoClient mongoClient = null;
        synchronized (MongoDatabaseUtil.class) {
            if (mongoClients == null) {
                mongoClients = new HashMap<>();
            } else {
                mongoClient = mongoClients.get(urlWithCredentials);
            }
            if (mongoClient == null) {
                mongoClient = new MongoClient(clientURI);
                mongoClients.put(urlWithCredentials, mongoClient);

                if(log.isDebugEnabled()) {
                    log.debug("create new mongoClient");
                }
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("use exist mongoClient");
                }
            }
        }

        //noinspection deprecation
        db = mongoClient.getDB(clientURI.getDatabase());
        return db;
    }

    /**
     * Retrieve integer values from database.
     *
     * @param dbConnection of user store
     * @param params       values to filter from database
     * @param stmt         query to execute in mongodb
     * @return int value
     * @throws MongoDBQueryException if null data provided to mongodb query
     */
    public static int getIntegerValueFromDatabase(DB dbConnection, String stmt, Map<String, Object> params)
            throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        int value = -1;
        JSONObject jsonKeys = new JSONObject(stmt);
        List<String> keys = getKeys(jsonKeys);
        try {
            prepStmt = new MongoPreparedStatementImpl(dbConnection, stmt);
            for (String key : keys) {
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (entry.getValue() == null) {
                                throw new MongoDBQueryException("Null Data Provided as the query parameter");
                            } else if (entry.getValue() instanceof String) {
                                prepStmt.setString(key, (String) entry.getValue());
                            } else if (entry.getValue() instanceof Integer) {
                                prepStmt.setInt(key, (Integer) entry.getValue());
                            } else if (entry.getValue() instanceof Long) {
                                prepStmt.setLong(key, (Long) entry.getValue());
                            }
                        }
                    }
                }
            }
            DBCursor cursor = prepStmt.find();
            while (cursor.hasNext()) {
                value = (int) Double.parseDouble(cursor.next().get(MongoDBCoreConstants.UM_ID).toString());
            }
            return value;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to retrieve integer values from database", e);
        } finally {
            MongoDatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    /**
     * Update user role in batch mode to database.
     *
     * @param dbConnection of user store
     * @param params       values to filter from database
     * @param stmt         query to execute in mongodb
     * @throws MongoDBQueryException if null data provided to mongodb query or any other query error occurred
     */
    public static void updateUserRoleMappingInBatchMode(DB dbConnection, String stmt, Map<String, Object> params)
            throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        boolean localConnection = false;
        JSONObject jsonKeys = new JSONObject(stmt);
        List<String> keys = getKeys(jsonKeys);
        try {
            prepStmt = new MongoPreparedStatementImpl(dbConnection, stmt);
            int batchParamIndex = -1;
            Iterator<String> searchKeys = keys.iterator();
            int[] values = null;
            String listKey = "";
            while (searchKeys.hasNext()) {
                String key = searchKeys.next();
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.SET_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (entry.getValue() == null) {
                                throw new MongoDBQueryException("Null Data Provided as the query parameter");
                            } else if (entry.getValue() instanceof int[]) {
                                values = (int[]) entry.getValue();
                                batchParamIndex = 1;
                                listKey = key;
                            } else if (entry.getValue() instanceof String) {
                                prepStmt.setString(key, (String) entry.getValue());
                            } else if (entry.getValue() instanceof Integer) {
                                prepStmt.setInt(key, (Integer) entry.getValue());
                            } else if (entry.getValue() instanceof Long) {
                                prepStmt.setLong(key, (Long) entry.getValue());
                            }
                        }
                    }
                }
            }
            if (batchParamIndex != -1) {
                for (int value : values) {
                    if (value > 0) {
                        prepStmt.setInt(listKey, value);
                        if (updateTrue(keys)) {
                            prepStmt.updateBatch();
                        } else {
                            long Id = MongoDatabaseUtil.getIncrementedSequence(dbConnection,
                                    MongoDBCoreConstants.UM_USER_ROLE);
                            prepStmt.setLong(MongoDBCoreConstants.UM_ID, Id);
                            prepStmt.addBatch();
                        }
                    }
                }
                if (updateTrue(keys)) {
                    BulkWriteResult updateResult = prepStmt.updateBulk();
                    if (log.isDebugEnabled()) {
                        log.debug("Bulk update results: " + updateResult);
                    }
                } else {
                    BulkWriteResult insertResult = prepStmt.insertBulk();
                    if (log.isDebugEnabled()) {
                        log.debug("Bulk insert results: " + insertResult);
                    }
                }
            } else {
                prepStmt.insert();
            }
            localConnection = true;
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query: " + stmt + "; Status: " + batchParamIndex);
            }
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to update user role mapping in batch mode, stmt [" + stmt + "], params [" + params.entrySet().stream().map(entry -> "[ " + entry.getKey() + ": " + entry.getValue() + " ]").reduce("", String::concat) + "]", e);
        } finally {
            if (localConnection) {
                MongoDatabaseUtil.closeAllConnections(dbConnection);
            }
            MongoDatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }


    /**
     * Delete user role in batch mode.
     *
     * @param dbConnection of user store
     * @param params       values to filter from database
     * @param stmt         query to execute in mongodb
     * @throws MongoDBQueryException if remove operation of MongoPreparedStatement fails
     */
    public static void deleteUserRoleMappingInBatchMode(DB dbConnection, String stmt, Map<String, Object> params)
            throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            int[] roleIDS = (int[]) params.get(MongoDBCoreConstants.UM_ROLE_ID);
            for (int roleID : roleIDS) {
                prepStmt = new MongoPreparedStatementImpl(dbConnection, stmt);
                int userID = (Integer) params.get(MongoDBCoreConstants.UM_USER_ID);
                prepStmt.setInt(MongoDBCoreConstants.UM_USER_ID, userID);
                prepStmt.setInt(MongoDBCoreConstants.UM_ROLE_ID, roleID);
                int tenantID = (Integer) params.get(MongoDBCoreConstants.UM_TENANT_ID);
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantID);
                prepStmt.remove();
            }
            localConnection = true;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to delete user role mapping in batch mode", e);
        } finally {
            if (localConnection) {
                MongoDatabaseUtil.closeAllConnections(dbConnection);
            }
            MongoDatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    /**
     * Delete user in batch mode from database.
     *
     * @param dbConnection of user store
     * @param params       values to filter from database
     * @param stmt         query to execute in mongodb
     * @throws MongoDBQueryException if remove operation of MongoPreparedStatement fails
     */
    public static void deleteUserMappingInBatchMode(DB dbConnection, String stmt, Map<String, Object> params)
            throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            int[] userIDS = (int[]) params.get(MongoDBCoreConstants.UM_USER_ID);
            for (int userID : userIDS) {
                prepStmt = new MongoPreparedStatementImpl(dbConnection, stmt);
                int roleID = (Integer) params.get(MongoDBCoreConstants.UM_ROLE_ID);
                prepStmt.setInt(MongoDBCoreConstants.UM_USER_ID, userID);
                prepStmt.setInt(MongoDBCoreConstants.UM_ROLE_ID, roleID);
                Object roleTenantValue = params.get(MongoDBCoreConstants.UM_ROLE_TENANT_ID);
                Object userTenantValue = params.get(MongoDBCoreConstants.UM_USER_TENANT_ID);
                Object tenantValue = params.get(MongoDBCoreConstants.UM_TENANT_ID);

                if (roleTenantValue != null) {
                    int roleTenantId = (Integer) roleTenantValue;
                    prepStmt.setInt(MongoDBCoreConstants.UM_ROLE_TENANT_ID, roleTenantId);
                }
                if (userTenantValue != null) {
                    int userTenantId = (Integer) userTenantValue;
                    prepStmt.setInt(MongoDBCoreConstants.UM_USER_TENANT_ID, userTenantId);
                }
                if (tenantValue != null) {
                    int tenantId = (Integer) tenantValue;
                    prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                }
                prepStmt.remove();
            }
            localConnection = true;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to delete user mapping in batch mode", e);
        } finally {
            if (localConnection) {
                MongoDatabaseUtil.closeAllConnections(dbConnection);
            }
            MongoDatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    /**
     * Check whether the query is update query.
     *
     * @param keys of json query
     * @return boolean status
     */
    public static boolean updateTrue(List<String> keys) {
        for (String key : keys) {
            if (key.contains(MongoDBCoreConstants.SET_FIELD) || key.contains(MongoDBCoreConstants.UNSET_FIELD)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve keys from json query.
     *
     * @param stmt of JSONObject
     */
    public static List<String> getKeys(JSONObject stmt) {
        int index = 0;
        List<String> keys = new ArrayList<>();
        Iterator<String> keysFind = stmt.keys();
        while (keysFind.hasNext()) {
            String key = keysFind.next();
            keys.add(index, key);
            if (stmt.get(key) instanceof JSONObject) {
                JSONObject value = stmt.getJSONObject(key);
                key = value.keys().next();
                if (MongoDBCoreConstants.SET_FIELD.equals(key)) {

                    String names[] = JSONObject.getNames(value.getJSONObject(key));
                    for (String name : names) {

                        keys.add(index, name);
                        index++;
                    }
                }
                keys.add(index, key);
            }
            index++;
        }
        return keys;
    }

    /**
     * Close the DB connection.
     *
     * @param dbConnection to close
     */
    public static void closeConnection(DB dbConnection) {
        if (dbConnection != null) {
            incrementConnectionsClosed();
        }
    }

    private static void closeStatement(MongoPreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }

    private static void closeStatements(MongoPreparedStatement... prepStatements) {
        if (prepStatements != null && prepStatements.length > 0) {
            for (MongoPreparedStatement stmt : prepStatements) {
                closeStatement(stmt);
            }
        }
    }

    /**
     * Close the connection to the database.
     *
     * @param dbConnection to be closed
     */
    private static void closeAllConnections(DB dbConnection, MongoPreparedStatement... prepStatements) {
        closeStatements(prepStatements);
        closeConnection(dbConnection);
    }

    private static void incrementConnectionsClosed() {
        if (connectionsClosed != Long.MAX_VALUE) {
            connectionsClosed++;
        }
    }

    /**
     * Update exact user role with params from database.
     *
     * @param dbConnection    of user store
     * @param sharedRoles     to update
     * @param mongoQuery      query to execute in mongodb
     * @param currentTenantId current logged in user tenantId
     * @param tenantIds       supplied tenantIds
     * @param userName        given user name
     * @throws MongoDBQueryException if insert or update operation of MongoPreparedStatement fails
     */
    public static void updateUserRoleMappingWithExactParams(DB dbConnection, String mongoQuery, String[] sharedRoles,
                                                            String userName, Integer[] tenantIds, int currentTenantId)
            throws MongoDBQueryException {

        MongoPreparedStatement ps = null;
        boolean localConnection = false;
        try {
            ps = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            JSONObject jsonKeys = new JSONObject(mongoQuery);
            List<String> keys = getKeys(jsonKeys);
            byte count;
            byte index = 0;
            for (String role : sharedRoles) {
                count = 0;
                ps.setString(keys.get(++count), role);
                ps.setInt(keys.get(++count), tenantIds[index]);
                ps.setString(keys.get(++count), userName);
                ps.setInt(keys.get(++count), currentTenantId);
                ps.setInt(keys.get(++count), currentTenantId);
                ps.setInt(keys.get(++count), tenantIds[index]);
                if (updateTrue(keys)) {
                    ps.insert();
                } else {
                    ps.update();
                }
                ++index;
            }
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query: " + mongoQuery);
            }
            localConnection = true;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to update exact user role", e);
        } finally {
            if (localConnection) {
                MongoDatabaseUtil.closeAllConnections(dbConnection);
            }
            MongoDatabaseUtil.closeAllConnections(null, ps);
        }
    }

    /**
     * Retrieve string values from database.
     *
     * @param dbConnection   of user store
     * @param params         values to filter from database
     * @param mongoQuery     query to execute in mongodb
     * @param isAggregate    status
     * @param multipleLookUp status
     * @throws MongoDBQueryException if find operation of MongoPreparedStatement fails
     */
    public static String[] getStringValuesFromDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params,
                                                       boolean isAggregate, boolean multipleLookUp)
            throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        String[] values = new String[0];
        JSONObject jsonKeys = new JSONObject(mongoQuery);
        List<String> keys;
        if (isAggregate) {
            keys = getKeys(jsonKeys.getJSONObject(MongoDBCoreConstants.MATCH_FIELD));
        } else {
            keys = getKeys(jsonKeys);
        }
        try {
            Iterator<String> searchKeys = keys.iterator();
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            while (searchKeys.hasNext()) {
                String key = searchKeys.next();
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (params.get(key) == null) {
                                prepStmt.setString(key, null);
                            } else if (params.get(key) instanceof String) {
                                prepStmt.setString(key, (String) params.get(key));
                            } else if (params.get(key) instanceof Integer) {
                                prepStmt.setInt(key, (Integer) params.get(key));
                            } else if (params.get(key) instanceof Long) {
                                prepStmt.setLong(key, (Long) params.get(key));
                            }
                        }
                    }
                }
            }
            if (!isAggregate) {
                DBCursor cursor = prepStmt.find();
                List<String> lst = new ArrayList<>();
                while (cursor.hasNext()) {
                    lst.add(cursor.next().toString());
                }
                if (lst.size() > 0) {
                    values = lst.toArray(new String[lst.size()]);
                }
            } else {
                prepStmt.setMultiLookUp(multipleLookUp);
                //noinspection deprecation
                AggregationOutput result = prepStmt.aggregate();
                Iterable<DBObject> ite = result.results();
                List<String> lst = new ArrayList<>();
                Iterator<DBObject> foundResults = ite.iterator();
                List<String> projection = getKeys(jsonKeys.getJSONObject(MongoDBCoreConstants.PROJECT_FIELD));
                String projectionKey = "";
                for (String pKey : projection) {
                    if (pKey.equals(MongoDBCoreConstants.ID)) {
                        continue;
                    }
                    projectionKey = pKey;
                }
                while (foundResults.hasNext()) {
                    lst.add(foundResults.next().get(projectionKey).toString());
                }
                if (lst.size() > 0) {
                    values = lst.toArray(new String[lst.size()]);
                }
            }
            return values;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to retrieve the string values from database", e);
        } finally {
            MongoDatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    /**
     * Get auto increment sequence.
     *
     * @param dbConnection of user store
     * @param collection   to auto increment
     * @return int sequence
     */
    public static synchronized long getIncrementedSequence(DB dbConnection, String collection) {

        if (log.isDebugEnabled()) {
            log.debug("getIncrementedSequence(DB dbConnection, String collection) [" + collection + "]");
        }

        DBObject queryObject = new BasicDBObject(MongoDBCoreConstants.ID, collection);
        DBObject updateObject = new BasicDBObject("$inc", new BasicDBObject("seq", Long.valueOf(1)));
        DBObject result = dbConnection.getCollection(MongoDBCoreConstants.COUNTERS).findAndModify(queryObject, null, null, false, updateObject, true, true);
        return (long)result.get("seq");
    }

    /**
     * Get distinct string value of key in document.
     *
     * @param dbConnection of user store
     * @param mongoQuery   to execute
     * @param params       to filter from database
     * @return String[] distinct string values
     * @throws MongoDBQueryException if distinct operation of MongoPreparedStatement fails
     */
    public static String[] getDistinctStringValuesFromDatabase(DB dbConnection, String mongoQuery, Map<String,
            Object> params) throws MongoDBQueryException {

        MongoPreparedStatement prepStmt = null;
        String[] values = new String[0];
        JSONObject jsonKeys = new JSONObject(mongoQuery);
        List<String> keys;
        keys = getKeys(jsonKeys);
        try {
            Iterator<String> searchKeys = keys.iterator();
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            while (searchKeys.hasNext()) {
                String key = searchKeys.next();
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (params.get(key) == null) {
                                prepStmt.setString(key, null);
                            } else if (params.get(key) instanceof String) {
                                prepStmt.setString(key, (String) params.get(key));
                            } else if (params.get(key) instanceof Integer) {
                                prepStmt.setInt(key, (Integer) params.get(key));
                            } else if (params.get(key) instanceof Long) {
                                prepStmt.setLong(key, (Long) params.get(key));
                            }
                        }
                    }
                }
            }
            List result = prepStmt.distinct();
            if (!result.isEmpty()) {

                values = new String[result.size()];
                int index = 0;
                for (Object res : result) {

                    values[index] = res.toString();
                    index++;
                }
            }
            return values;
        } catch (MongoDBQueryException e) {
            throw new MongoDBQueryException("Failed to get distinct string values from database", e);
        } finally {
            MongoDatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }
}
