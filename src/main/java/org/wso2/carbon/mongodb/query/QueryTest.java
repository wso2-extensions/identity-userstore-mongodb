package org.wso2.carbon.mongodb.query;

import com.mongodb.*;
import org.json.JSONObject;
import org.wso2.carbon.mongodb.query.MongoDBQueryException;
import org.wso2.carbon.mongodb.query.MongoPreparedStatement;
import org.wso2.carbon.mongodb.query.MongoPreparedStatementImpl;
import org.wso2.carbon.mongodb.user.store.mgt.MongoDBCoreConstants;
import org.wso2.carbon.mongodb.user.store.mgt.MongoDBRealmConstants;
import org.wso2.carbon.mongodb.user.store.mgt.caseinsensitive.MongoDBCaseInsensitiveConstants;
import org.wso2.carbon.mongodb.util.MongoDatabaseUtil;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class QueryTest {

    public static void main(String[] args) {
        MongoClientURI clientURI = new MongoClientURI("mongodb://wso2_carbon_db:wso2_carbon_db@0.0.0.0:27017/wso2_carbon_db?minPoolSize=10&maxPoolSize=1000&waitQueueMultiple=10");
        MongoClient mongoClient = new MongoClient(clientURI);
        System.out.println("mongoClientOptions: " + mongoClient.getMongoClientOptions().toString());
        DB db = mongoClient.getDB(clientURI.getDatabase());
        System.out.println("readPreference: " + mongoClient.getMongoClientOptions().getReadPreference());
        MongoPreparedStatement prepStmt = null;

        try {

            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.SELECT_USER_USE_MOBILE_MONGO_QUERY);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "13106070001");
            prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "18612345679");
            AggregationOutput cursor = prepStmt.aggregate();
            if(cursor != null) {
                for(DBObject curr: cursor.results()) {
                    BasicDBList userList = (BasicDBList)curr.get(MongoDBCoreConstants.UM_USER);
                    if(userList != null && !userList.isEmpty()) {
                        System.out.println(((BasicDBObject)userList.get(0)).get(MongoDBCoreConstants.UM_USER_PASSWORD));
                    }
                }
            }


//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_BY_MOBILE_MONGO_QUERY);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, "13177776666");
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_MONGO_QUERY);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, "SH000064");
//            prepStmt.setString("attrs." + MongoDBCoreConstants.UM_PROFILE_NAME, "default");
//
//            prepStmt.setInt("attrs." + MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            AggregationOutput cursor = prepStmt.aggregate();
//            Map<String, String> attrs = new HashMap<>();
//            if(cursor != null) {
//                int count = 0;
//                for (DBObject curr : cursor.results()) {
//                    DBObject attrsObj = (DBObject) curr.get("attrs");
//                    Optional.ofNullable(attrsObj).ifPresent(obj -> obj.keySet().stream().filter(attrKey -> !MongoDBCoreConstants.ID.equals(attrKey)).forEach(attrKey -> attrs.put(attrKey, attrsObj.get(attrKey).toString())));
//                    System.out.println(attrsObj);
//                    count++;
//                }
//                System.out.println("query result count: " + count);
//                System.out.println(attrs.entrySet().stream().map(entry -> "[" + entry.getKey() + ":" + entry.getValue() + "]").reduce("", String::concat));
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, "admin");
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE);
//            prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, "AdMiN".toUpperCase());
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBCaseInsensitiveConstants.GET_IS_USER_EXISTING_MONGO_CASE_INSENSITIVE);
//            prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, "SH000064");
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_USERS_FOR_PROP_MONGO_QUERY.replace("<INSERT_STATEMENT>", "'attribute.mobile' : '?'"));
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            prepStmt.setString("attribute.mobile", "13177776666");
//            prepStmt.setString("attribute." + MongoDBCoreConstants.UM_PROFILE_ID, "default");
//            AggregationOutput cursor = prepStmt.aggregate();
//            if(cursor != null) {
//                for(DBObject curr: cursor.results()) {
//                    System.out.println(curr.get(MongoDBCoreConstants.UM_USER_NAME));
//                }
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_USERS_FOR_PROP_MONGO_QUERY.replace("<INSERT_STATEMENT>", "'caseInsensitiveUid' : '?'"));
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            prepStmt.setString("caseInsensitiveUid", "SH0000641");
//            prepStmt.setString(MongoDBCoreConstants.UM_PROFILE_ID, "default");
//            DBCursor cursor = prepStmt.find();
//            Iterable<DBObject> iterable = () -> cursor.iterator();
//            String[] userList = StreamSupport.stream(iterable.spliterator(), false).map(entry -> entry.get(MongoDBCoreConstants.UID_FIELD)).toArray(String[]::new);
//            System.out.println(Arrays.stream(userList).map(item -> "[" + item + "]").reduce(String::concat));

//            Map<String, Object> map = new HashMap<>();
//            map.put(MongoDBCoreConstants.USER_ROLE_UM_USER_ID, 393263);
//            map.put(MongoDBCoreConstants.USER_ROLE_UM_TENANT_ID, -1234);
//            map.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            String[] result = MongoDatabaseUtil.getStringValuesFromDatabase(db, MongoDBRealmConstants.GET_USER_ROLE_MONGO_QUERY, map, true, true);
//            Arrays.stream(result).forEach(i -> System.out.println(i));

//            Map<String, Object> map = new HashMap<>();
//            map.put(MongoDBCoreConstants.UM_ID, 9999999);
//            map.put(MongoDBCoreConstants.UM_ROLE_ID, 35);
//            map.put(MongoDBCoreConstants.UM_USER_ID, 343998);
//            map.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            MongoDatabaseUtil.updateUserRoleMappingInBatchMode(db, MongoDBRealmConstants.ADD_ROLE_TO_USER_MONGO_QUERY, map);

//            Map<String, Object> map = new HashMap<>();
//            int[] roleId = {35};
//            map.put(MongoDBCoreConstants.UM_USER_ID, 343998);
//            map.put(MongoDBCoreConstants.UM_ROLE_ID, roleId);
//            map.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            System.out.println("params [" + map.entrySet().stream().map(e -> "[ " + e.getKey() + ": " + e.getValue() + " ]").reduce("", String::concat) + "]");
//            MongoDatabaseUtil.deleteUserRoleMappingInBatchMode(db, MongoDBRealmConstants.REMOVE_ROLE_FROM_USER_MONGO_QUERY, map);

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_USER_FILTER_MONGO_QUERY);
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, "%");
//            prepStmt.setInt(MongoDBCoreConstants.LIMIT_FIELD, 5);
//            AggregationOutput cursor = prepStmt.aggregate();
//            if(cursor != null) {
//                for(DBObject curr: cursor.results()) {
//                    System.out.println(curr);
//                }
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBCaseInsensitiveConstants.GET_USER_FILTER_MONGO_CASE_INSENSITIVE);
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, "SH000064".toUpperCase());
//            prepStmt.setInt(MongoDBCoreConstants.LIMIT_FIELD, 5);
//            AggregationOutput cursor = prepStmt.aggregate();
//            if(cursor != null) {
//                int count = 0;
//                for(DBObject curr: cursor.results()) {
//                    System.out.println(curr);
//                    count++;
//                }
//                System.out.println("query result count: " + count);
//            }

//            String mongoQuery = MongoDBRealmConstants.ADD_USER_MONGO_QUERY;
//            Map<String, Object> params = new HashMap<>();
//            params.put(MongoDBCoreConstants.UM_USER_PASSWORD, "WFjqIozC7fiHIWmbLIY45Q==");
//            params.put(MongoDBCoreConstants.UM_USER_NAME, "djtestscim");
//            params.put(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, "djtestscim".toUpperCase());
//            params.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
//            params.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
//            params.put(MongoDBCoreConstants.UM_ID, 85810);
//            params.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
//            params.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//
//            new QueryTest().updateUserValue(db, mongoQuery, params);

//            String mongoQuery = MongoDBRealmConstants.DELETE_USER_PROPERTY_MONGO_QUERY.replace("<ATTR_TO_REMOVE>", String.format("'%s' : 1", "uid"));
//            Map<String, Object> params = new HashMap<>();
//            params.put(MongoDBCoreConstants.UM_USER_ID, 85813);
//            params.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            params.put(MongoDBCoreConstants.UM_PROFILE_ID, "default");
//            new QueryTest().updateStringValuesToDatabase(db, mongoQuery, params);

//            String mongoQuery = MongoDBRealmConstants.UPDATE_USER_PASSWORD_MONGO_QUERY;
//            Map<String, Object> params = new HashMap<>();
//            params.put(MongoDBCoreConstants.UM_USER_NAME, "djtestscim");
//            params.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            params.put(MongoDBCoreConstants.UM_USER_PASSWORD, "default");
//            params.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
//            params.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
//            params.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
//            new QueryTest().updateStringValuesToDatabase(db, mongoQuery, params);

//            Map<String, Object> params = new HashMap<>();
//            params.put(MongoDBCoreConstants.UM_USER_ID, 86811);
//            params.put(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            params.put(MongoDBCoreConstants.UM_PROFILE_ID, "default");
//            params.put("uid", "ZSW10042247");
//            params.put("title", 318154);
//            new QueryTest().updateUserClaimValuesToDatabase(db, params, true);

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE);
//            prepStmt.setString(MongoDBCoreConstants.UID_FIELD, "ZSW10044695");
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "ZSW10044695");
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            prepStmt = new MongoPreparedStatementImpl(db, MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY_WITHOUT_MOBILE);
//            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, "ZSW10044695");
//            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, -1234);
//            DBCursor cursor = prepStmt.find();
//            if (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            } else {
//                System.out.println("query return null");
//            }

//            BasicDBObject command = new BasicDBObject();
//            command.put("eval", String.format("function() { return { seq: getNextSequence('%s')}; }", "UM_MODULE"));
//            CommandResult result = db.command(command);
//            System.out.println((long)((DBObject)result.get("retval")).get("seq"));

//            DBObject queryObject = new BasicDBObject(MongoDBCoreConstants.ID, "UM_MODULE");
//            DBObject updateObject = new BasicDBObject("$inc", new BasicDBObject("seq", Long.valueOf(1)));
//            DBObject result = db.getCollection("COUNTERS").findAndModify(queryObject, null, null, false, updateObject, true, true);
//            System.out.println(result.get("seq"));

        } catch (MongoException e) {
            e.printStackTrace();
//        } catch (MongoDBQueryException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (prepStmt != null) {
                prepStmt.close();
            }
        }
    }

    /**
     * Update user values.
     *
     * @param connection to  mongodb
     * @param query      to update user value to mongodb
     * @param map        user property
     * @throws UserStoreException if any exception occurred
     */
    private void updateUserValue(DB connection, String query, Map<String, Object> map) throws UserStoreException {

        System.out.println("updateUserValue(DB connection, String query, Map<String, Object> map) [" + query + "] [" + map.entrySet().stream().map(entry -> "[" + entry.getKey() + ":" + entry.getValue() + "]").reduce("", String::concat) + "]");

        JSONObject jsonKeys = new JSONObject(query);
        List<String> keys = MongoDatabaseUtil.getKeys(jsonKeys);
        try {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(connection, query);
            for (String key : keys) {
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.SET_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (entry.getValue() == null) {
                                prepStmt.setString(key, null);
                            } else if (entry.getValue() instanceof String) {
                                prepStmt.setString(key, (String) entry.getValue());
                            } else if (entry.getValue() instanceof Date) {
                                prepStmt.setDate(key, (Date) entry.getValue());
                            } else if (entry.getValue() instanceof Integer) {
                                prepStmt.setInt(key, (Integer) entry.getValue());
                            } else if (entry.getValue() instanceof Long) {
                                prepStmt.setLong(key, (Long) entry.getValue());
                            } else if (entry.getValue() instanceof Boolean) {
                                prepStmt.setBoolean(key, (Boolean) entry.getValue());
                            }
                        }
                    }
                }
            }
            if (MongoDatabaseUtil.updateTrue(keys)) {
                prepStmt.update();
            } else {
                prepStmt.insert();
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while updating user values", e);
        } finally {
            MongoDatabaseUtil.closeConnection(connection);
        }
    }

    private void updateStringValuesToDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params)
            throws UserStoreException {

        MongoPreparedStatement prepStmt;
        JSONObject jsonKeys = new JSONObject(mongoQuery);
        List<String> keys = MongoDatabaseUtil.getKeys(jsonKeys);
        try {
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            for (String key : keys) {
                if (!(MongoDBCoreConstants.COLLECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.PROJECTION_FIELD.equals(key) ||
                        MongoDBCoreConstants.SET_FIELD.equals(key) ||
                        MongoDBCoreConstants.UNSET_FIELD.equals(key))) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            if (entry.getValue() == null) {
                                throw new UserStoreException("Invalid data provided");
                            } else if (entry.getValue() instanceof String) {
                                prepStmt.setString(key, (String) entry.getValue());
                            } else if (entry.getValue() instanceof Integer) {
                                prepStmt.setInt(key, (Integer) entry.getValue());
                            } else if (entry.getValue() instanceof Long) {
                                prepStmt.setLong(key, (Long) entry.getValue());
                            } else if (entry.getValue() instanceof Date) {
                                Date date = (Date) entry.getValue();
                                prepStmt.setDate(key, date);
                            } else if (entry.getValue() instanceof Boolean) {
                                prepStmt.setBoolean(key, (Boolean) entry.getValue());
                            }
                        }
                    }
                }
            }
            List<String> queryList = new ArrayList<>();
            queryList.add(mongoQuery);

            WriteResult result = MongoDatabaseUtil.updateTrue(queryList) ? prepStmt.update() : prepStmt.insert();
            if (!result.isUpdateOfExisting()) {
                System.out.println("No documents updated");
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while updating string values", e);
        }

    }

    private void updateUserClaimValuesToDatabase(DB dbConnection, Map<String, Object> map, boolean isUpdateTrue)
            throws UserStoreException {

        if (map == null) {
            throw new UserStoreException("Parameters cannot be null");
        } else {
            DBCollection collection = dbConnection.getCollection(MongoDBCoreConstants.UM_USER_ATTRIBUTE);
            if (!isUpdateTrue) {
                long id = MongoDatabaseUtil.getIncrementedSequence(dbConnection, MongoDBCoreConstants.UM_USER_ATTRIBUTE);
                BasicDBObject query = new BasicDBObject(MongoDBCoreConstants.UM_ID, id);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    query.append(entry.getKey(), entry.getValue());
                }
                collection.insert(query);
            } else {
                BasicDBObject condition = null;
                BasicDBObject setQuery = null;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getKey().equals(MongoDBCoreConstants.UM_USER_ID) ||
                            entry.getKey().equals(MongoDBCoreConstants.UM_PROFILE_ID)) {
                        if (condition == null) {
                            condition = new BasicDBObject(entry.getKey(), entry.getValue());
                        } else {
                            condition.append(entry.getKey(), entry.getValue());
                        }
                    } else {
                        if (setQuery == null) {
                            setQuery = new BasicDBObject(entry.getKey(), entry.getValue());
                        } else {
                            setQuery.append(entry.getKey(), entry.getValue());
                        }
                    }
                }
                if (condition != null && setQuery != null) {
                    setQuery = new BasicDBObject(MongoDBCoreConstants.SET_FIELD, setQuery);

                    collection.update(condition, setQuery);
                }
            }
        }
    }

}
