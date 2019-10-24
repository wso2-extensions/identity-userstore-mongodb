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

package org.wso2.carbon.mongodb.user.store.mgt;

import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import com.mongodb.*;

import org.apache.commons.logging.Log;
import org.wso2.carbon.mongodb.query.MongoPreparedStatement;
import org.wso2.carbon.mongodb.query.MongoPreparedStatementImpl;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mongodb.user.store.mgt.caseinsensitive.MongoDBCaseInsensitiveConstants;
import org.wso2.carbon.mongodb.util.MongoDatabaseUtil;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.hybrid.HybridJDBCConstants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.jdbc.JDBCRoleContext;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.mongodb.util.MongoDBRealmUtil;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.mongodb.query.MongoDBQueryException;

/**
 * MongoDBUserStoreManager class for create MongoDBUserStore.
 */
public class MongoDBUserStoreManager extends AbstractUserStoreManager {

    private static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    private static DataSource dataSourceLocal = null;
    private static final Log log = LogFactory.getLog(MongoDBUserStoreManager.class);
    private DB db;
    private SecureRandom random = new SecureRandom();
    private boolean isMobileUserName = false;

//    private String currUser = null;
//    private Map<String, String> currUserProperties = null;

    /**
     * Empty Constructor.
     */
    public MongoDBUserStoreManager() {
        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager()");
        }
    }

    /**
     * Constructor which accept two parameters.
     *
     * @param configuration RealmConfiguration to user store
     * @param tenantId      currently logged in tenantId
     */
    public MongoDBUserStoreManager(RealmConfiguration configuration, int tenantId) {

        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager(RealmConfiguration configuration, int tenantId)");
        }

        this.realmConfig = configuration;
        this.tenantId = tenantId;
        realmConfig.setUserStoreProperties(MongoDBRealmUtil.getMongoProperties(realmConfig.getUserStoreProperties()));
        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
            readGroupsEnabled = Boolean.parseBoolean(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null) {
            writeGroupsEnabled = Boolean.parseBoolean(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED));
        } else if (!isReadOnly()) {
            writeGroupsEnabled = true;
        }

        if (writeGroupsEnabled) {
            readGroupsEnabled = true;
        }
        // Initialize user role cache
        initUserRolesCache();

        this.isMobileUserName = Boolean.parseBoolean(realmConfig.getUserStoreProperty(MongoDBRealmConstants.IS_MOBILE_USERNAME));

        if(log.isDebugEnabled()) {
            log.debug("init set isMobileUserName [" + this.isMobileUserName + "]");
        }
    }

    /**
     * Constructor with four arguments.
     *
     * @param db          data source of user store
     * @param realmConfig realm configuration
     * @param tenantId    currently logged in tenantID
     * @param addInitData boolean status to filter whether initial data add or not to user store
     */
    public MongoDBUserStoreManager(DB db, RealmConfiguration realmConfig, int tenantId, boolean addInitData)
            throws UserStoreException {

        this(realmConfig, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager(DB db, RealmConfiguration realmConfig, int tenantId, boolean addInitData)");
        }
        this.db = db;
        this.dataSource = dataSourceLocal;

        if (db == null) {
            db = MongoDatabaseUtil.getRealmDataSource(realmConfig);
        }
        if (dataSource == null) {
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
        }
        if (db == null || dataSource == null) {
            throw new UserStoreException("User management data source is null");
        }
        doInitialSetup();
        this.persistDomain();

        // Required to add the initial admin data for primary user stores
        if (addInitData && realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()), !isInitSetupDone());
        }
    }

    /**
     * Constructor with two parameters.
     *
     * @param db          mongodb data source
     * @param realmConfig realm configuration
     */
    public MongoDBUserStoreManager(DB db, RealmConfiguration realmConfig) {

        this(realmConfig, MultitenantConstants.SUPER_TENANT_ID);

        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager(DB db, RealmConfiguration realmConfig)");
        }

        realmConfig.setUserStoreProperties(MongoDBRealmUtil.getMongoProperties(realmConfig.getUserStoreProperties()));
        this.db = db;
    }

    /**
     * constructor with  6 parameters.
     *
     * @param realmConfig    realm configuration
     * @param properties     realm properties
     * @param claimManager   claim manager details
     * @param profileManager Profile Configuration Manager instance
     * @param realm          User Realm instance
     * @param tenantId       currently logged in tenantId
     */
    public MongoDBUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                   ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                   UserRealm realm, Integer tenantId) throws UserStoreException {

        this(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);

        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties, ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)");
        }
    }

    /**
     * Constructor with seven parameters.
     *
     * @param realmConfig    realm configuration
     * @param properties     realm properties
     * @param claimManager   claim manager details
     * @param profileManager Profile Configuration Manager instance
     * @param realm          User Realm instance
     * @param tenantId       currently logged in tenantId
     * @param skipInitData   boolean status to check whether to skip initial data or not
     */
    public MongoDBUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                   ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                   UserRealm realm, Integer tenantId, boolean skipInitData) throws UserStoreException {
        this(realmConfig, tenantId);

        if (log.isDebugEnabled()) {
            log.debug("MongoDBUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties, ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId, boolean skipInitData)");
        }

        this.claimManager = claimManager;
        this.userRealm = realm;

        if (log.isDebugEnabled()) {
            log.debug("value of profileManager: " + profileManager);
            log.debug("value of skipInitData: " + skipInitData);
        }
        try {
            db = loadUserStoreSpecificDataSource();
            if (db == null) {
                db = (DB) properties.get(UserCoreConstants.DATA_SOURCE);
            }
            if (db == null) {
                db = MongoDatabaseUtil.getRealmDataSource(realmConfig);
                properties.put(UserCoreConstants.DATA_SOURCE, db);
            }
        } catch (UserStoreException e) {
            log.error("Failed to load the data source", e);
        }

        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
        }
        if (dataSource == null) {
            throw new UserStoreException("User management data source is null");
        }
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        realmConfig.setUserStoreProperties(MongoDBRealmUtil.getMongoProperties(realmConfig.getUserStoreProperties()));

        this.persistDomain();
        doInitialSetup();
        if (!skipInitData && realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()), !isInitSetupDone());
        }
        // Initialize user roles cache
        initUserRolesCache();
    }

    public static void setDBDataSource(DataSource source) {
        dataSourceLocal = source;
    }

    /**
     * Get all user properties belong to provided user profile.
     *
     * @param userName      username of user
     * @param propertyNames names of properties to get
     * @param profileName   profile name of user
     * @return map object of properties
     */
    protected Map<String, String> getUserPropertyValues(String userName, String[] propertyNames, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getUserPropertyValues(String userName, String[] propertyNames, String profileName) [" + userName + "] [" + Arrays.stream(propertyNames).map(item -> "[" + item + "]").reduce("", String::concat) + "] [" + profileName + "]");
        }

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        MongoPreparedStatement prepStmt = null;
        String[] propertyNamesSorted = propertyNames.clone();
        Arrays.sort(propertyNamesSorted);
        Map<String, String> map = new HashMap<>();

        try {
            if (db == null) {
                db = loadUserStoreSpecificDataSource();
            }
            String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE);
            prepStmt = new MongoPreparedStatementImpl(db, mongoQuery);
            prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, userName.toUpperCase());
            prepStmt.setString("attrs." + MongoDBCoreConstants.UM_PROFILE_NAME, profileName);

            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt("attrs." + MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            //noinspection deprecation
            AggregationOutput aggregationOutput = prepStmt.aggregate();
            Iterable<DBObject> results = aggregationOutput.results();
            if((results == null || !results.iterator().hasNext()) && userName.matches("[0-9]+") && this.isMobileUserName) {
                // username is mobile
                prepStmt.close();
                mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_BY_MOBILE);
                prepStmt = new MongoPreparedStatementImpl(db, mongoQuery);
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, userName);
                if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                    prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                }
                DBCursor cursor = prepStmt.find();
                if(cursor != null && cursor.hasNext()) {
                    DBObject object = cursor.next();
                    object.removeField(MongoDBCoreConstants.ID);
                    Set<String> keys = object.keySet();
                    for (String key : keys) {
                        String value = object.get(key).toString();
                        if (Arrays.binarySearch(propertyNamesSorted, key) < 0) {
                            continue;
                        }
                        map.put(key, value);
                    }
                }
            } else {
                for (DBObject object : results) {
                    DBObject attrsObj = (DBObject) object.get("attrs");
                    Optional.ofNullable(attrsObj).ifPresent(obj -> obj.keySet().stream().filter(attrKey -> !MongoDBCoreConstants.ID.equals(attrKey)).forEach(attrKey -> map.put(attrKey, attrsObj.get(attrKey).toString())));
                }
            }

        } catch(MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred while getting the user property values", e);
        } finally {
            if(log.isDebugEnabled()) {
                log.debug("getUserPropertyValues return: " + map.entrySet().stream().map(entry -> "[" + entry.getKey() + ":" + entry.getValue() + "]").reduce("", String::concat));
            }

            if (prepStmt != null) {
                prepStmt.close();
            }
        }

        return map;
    }

    /**
     * Check whether the supplied role is available in user store.
     *
     * @param roleName role name to check
     * @return boolean
     */
    protected boolean doCheckExistingRole(String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckExistingRole(String roleName)");
        }

        RoleContext roleContext = createRoleContext(roleName);
        return isExistingMongoDBRole(roleContext);
    }

    /**
     * Create context of given role.
     *
     * @param roleName role name to create context
     * @return RoleContext created for given role
     */
    protected RoleContext createRoleContext(String roleName) {

        if (log.isDebugEnabled()) {
            log.debug("createRoleContext(String roleName)");
        }

        JDBCRoleContext searchCtx = new JDBCRoleContext();
        String[] roleNameParts = roleName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER);
        int tenantId;
        if (roleNameParts.length > 1) {
            tenantId = Integer.parseInt(roleNameParts[1]);
            searchCtx.setTenantId(tenantId);
        } else {
            tenantId = this.tenantId;
            searchCtx.setTenantId(tenantId);
        }

        if (tenantId != this.tenantId) {
            searchCtx.setShared(true);
        }
        searchCtx.setRoleName(roleNameParts[0]);
        return searchCtx;
    }

    /**
     * Check whether the role is exists in mongodb.
     *
     * @param context of role created
     * @return boolean status whether the role exists or not
     */
    private boolean isExistingMongoDBRole(RoleContext context) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("isExistingMongoDBRole(RoleContext context)");
        }

        boolean isExisting;
        String roleName = context.getRoleName();
        Map<String, Object> map = new HashMap<>();
        map.put(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
        String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_IS_ROLE_EXISTING);
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query cannot be null");
        }
        if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, ((JDBCRoleContext) context).getTenantId());
            isExisting = isValueExisting(mongoQuery, map);
        } else {
            isExisting = isValueExisting(mongoQuery, map);
        }
        return isExisting;
    }

    private boolean isValueExisting(String mongoQuery, Map<String, Object> params) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("isValueExisting(String mongoQuery, Map<String, Object> params)");
        }

        try {
            boolean isExisting = false;
            if (db == null) {
                db = loadUserStoreSpecificDataSource();
            }
            if (MongoDatabaseUtil.getIntegerValueFromDatabase(db, mongoQuery, params) > -1) {
                isExisting = true;
            }
            return isExisting;
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while checking the existence of value", e);
        }
    }

    /**
     * check whether the user is exists in user store.
     *
     * @param userName given to check
     * @return boolean true or false respectively for user exists or not
     */
    protected boolean doCheckExistingUser(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckExistingUser(String userName) [" + userName + "]");
        }

        Map<String, Object> map = new HashMap<>();
        String mongoQuery;
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_IS_USER_EXISTING);
            map.put(MongoDBCoreConstants.UM_USER_NAME, userName);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE);
            map.put(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, userName.toUpperCase());
        }
        if (mongoQuery == null) {
            if(log.isDebugEnabled()) {
                log.debug("Mongo query is null. Cannot check the existence of user");
            }
            throw new UserStoreException("Mongo query is null. Cannot check the existence of user");
        }
        boolean isExisting;

        String isUnique = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
        if ("true".equals(isUnique)
                && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            String uniquenessMongo = realmConfig.getUserStoreProperty(MongoDBRealmConstants.USER_NAME_UNIQUE);
            isExisting = isValueExisting(uniquenessMongo, map);
            if (log.isDebugEnabled()) {
                log.debug("The username should be unique across tenants.");
            }
        } else if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            isExisting = isValueExisting(mongoQuery, map);
        } else {
            isExisting = isValueExisting(mongoQuery, map);
        }

        if (!isExisting) {
            isExisting = this.getUserObjectByMobile(userName) == null ? false : true;
        }

        if (log.isDebugEnabled()) {
            log.debug("doCheckExistingUser result [" + isExisting + "]");
        }

        return isExisting;
    }

    /**
     * Get user list from provided properties.
     *
     * @param property    name
     * @param value       of property name
     * @param profileName where property belongs to
     * @return String[] of users
     */
    protected String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getUserListFromProperties(String property, String value, String profileName) [" + property + "] [" + value + "] [" + profileName + "]");
        }

        if (MongoDBCoreConstants.UID_FIELD.equals(property)) {
            property = MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD;
            value = value.toUpperCase();
        }

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        MongoPreparedStatement prepStmt = null;
        String[] users = new String[0];
        try {
            db = loadUserStoreSpecificDataSource();
            String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USERS_FOR_PROP).replace("<INSERT_STATEMENT>", "'" + property + "' : '?'");
            prepStmt = new MongoPreparedStatementImpl(db, mongoQuery);
            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            prepStmt.setString(property, value);
            prepStmt.setString(MongoDBCoreConstants.UM_PROFILE_ID, profileName);
            DBCursor cursor = prepStmt.find();
            Iterable<DBObject> iterable = () -> cursor.iterator();
            users = StreamSupport.stream(iterable.spliterator(), false).map(entry -> entry.get(MongoDBCoreConstants.UID_FIELD)).toArray(String[]::new);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred while getting the user list", e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getUserListFromProperties return user list [" + Arrays.stream(users).map(item -> item + ",").reduce("", String::concat) + "]");
            }
            if (prepStmt != null) {
                prepStmt.close();
            }
        }
        return users;
    }

    /**
     * Responsible for authenticate user.
     *
     * @param userName   of authenticating user
     * @param credential include user password of authenticating user
     * @return boolean if authenticate fail or not
     */
    protected boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAuthenticate(String userName, Object credential) [" + userName + "] [" + credential + "]");
        }

        if (!checkUserNameValid(userName)) {
            return false;
        }
        if (!checkUserPasswordValid(credential)) {
            return false;
        }
        if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            log.error("Anonymous user trying to login");
            return false;
        }

        String mongoQuery;
        String password;
        boolean isAuthed = false;
        MongoPreparedStatement prepStmt = null;

        // useranme authenticate
        try {
            if (isCaseSensitiveUsername()) {
                mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.SELECT_USER);
            } else {
                mongoQuery = realmConfig.getUserStoreProperty(
                        MongoDBCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE
                );
            }
            prepStmt = new MongoPreparedStatementImpl(db, mongoQuery);
            if (log.isDebugEnabled()) {
                log.debug("doAuthenticate use username with mongo query: " + mongoQuery);
            }
            if (isCaseSensitiveUsername()) {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, userName);
            } else {
                prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, userName.toUpperCase());
            }
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            DBCursor cursor = prepStmt.find();
            if (cursor.hasNext()) {
                isAuthed = this.validateUserPassword(cursor.next(), credential);
            }
        } catch (MongoDBQueryException e) {
            log.error("MongoDBQueryException occurred while authenticating", e);
            throw new UserStoreException("MongoDBQueryException occurred while authenticating", e);
        } finally {
            if (prepStmt != null) {
                prepStmt.close();
            }
        }

        // mobile authenticate
        try {
            prepStmt = null;
            if (!isAuthed && this.isMobileUserName) {
                // try validate user use mobile number
                DBObject userObject = this.getUserObjectByMobile(userName);
                if (userObject != null) {
                    isAuthed = this.validateUserPassword(userObject, credential);
                }
            }
        } catch (MongoException e) {
            log.error("MongoDBQueryException occurred while authenticating", e);
            throw new UserStoreException("MongoException occurred while authenticating", e);
        } finally {
            if (prepStmt != null) {
                prepStmt.close();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Login attempt from: '" + userName + "'; Is login successful: " + isAuthed);
        }

        return isAuthed;
    }

    private boolean validateUserPassword(DBObject umUser, Object credential) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("validateUserPassword(DBObject umUser, Object credential)");
        }

        boolean isAuthed = false;
        String password;
        String storedPassword = umUser.get(MongoDBCoreConstants.UM_USER_PASSWORD).toString();
        String saltValue = null;
        if ("true".equalsIgnoreCase(
                realmConfig.getUserStoreProperty(MongoDBRealmConstants.STORE_SALTED_PASSWORDS))) {
            saltValue = umUser.get(MongoDBCoreConstants.UM_SALT_VALUE).toString();
        }

        boolean requireChange =
                Boolean.parseBoolean(umUser.get(MongoDBCoreConstants.UM_REQUIRE_CHANGE).toString());
        Date timestamp = (Date) umUser.get(MongoDBCoreConstants.UM_CHANGED_TIME);
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.HOUR, -24);
        Date date = gc.getTime();

        if (requireChange && (timestamp.getTime() < date.getTime())) {
            isAuthed = false;
        } else {
            password = this.preparePassword(credential, saltValue);
            if ((storedPassword != null) && (storedPassword.equals(password))) {
                isAuthed = true;
            }
        }
        return isAuthed;
    }

    private String preparePassword(Object password, String saltValue) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("preparePassword(Object password, String saltValue)");
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(password);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        try {
            String passwordString;
            if (saltValue != null) {
                credentialObj.addChars(saltValue.toCharArray());
            }
            String digestFunction = realmConfig.getUserStoreProperties().get(MongoDBRealmConstants.DIGEST_FUNCTION);
            if (digestFunction != null) {
                if (digestFunction.equals(UserCoreConstants.RealmConfig.PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
                    passwordString = new String(credentialObj.getChars());
                    return passwordString;
                }

                MessageDigest digest = MessageDigest.getInstance(digestFunction);
                byte[] byteValue = digest.digest(credentialObj.getBytes());
                passwordString = Base64.encode(byteValue);
            } else {
                passwordString = new String(credentialObj.getChars());
            }
            return passwordString;
        } catch (NoSuchAlgorithmException e) {
            throw new UserStoreException("Error occurred while preparing password", e);
        } finally {
            credentialObj.clear();
        }
    }

    /**
     * Add new user to mongodb user store.
     *
     * @param userName              of new user
     * @param credential            of new user
     * @param roleList              of new user
     * @param claims                user claim values
     * @param profileName           user profile name
     * @param requirePasswordChange status to change password
     */
    protected void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                             String profileName, boolean requirePasswordChange) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profileName, boolean requirePasswordChange)");
        }

        persistUser(userName, credential, roleList, claims, profileName, requirePasswordChange);
    }

    /**
     * Update user credentials in user store.
     *
     * @param userName      of user to update credentials
     * @param oldCredential of user
     * @param newCredential of user to update
     */
    protected void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateCredential(String userName, Object newCredential, Object oldCredential)");
        }

        if (this.doAuthenticate(userName, oldCredential)) {
            this.doUpdateCredentialByAdmin(userName, newCredential);
        }
    }

    /**
     * Update admin user credentials in user store.
     *
     * @param userName      of admin to update credentials
     * @param newCredential of user to update
     */
    protected void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateCredentialByAdmin(String userName, Object newCredential)");
        }

        this.doUpdateCredentialByAdmin(userName, newCredential, isMobileUserName);
    }

    private void doUpdateCredentialByAdmin(String userName, Object newCredential, boolean allowMobileUsername) throws UserStoreException {
        String mongoQuery;
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.UPDATE_USER_PASSWORD);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE);
        }
        Map<String, Object> map = new HashMap<>();
        String saltValue = null;
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot update credentials");
        }
        if ("true".equalsIgnoreCase(realmConfig.getUserStoreProperties().get(
                MongoDBRealmConstants.STORE_SALTED_PASSWORDS))) {
            saltValue = generateSaltValue();
        }
        String password = this.preparePassword(newCredential, saltValue);
        if (isCaseSensitiveUsername()) {
            map.put(MongoDBCoreConstants.UM_USER_NAME, userName);
        } else {
            map.put(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, userName.toUpperCase());
        }
        map.put(MongoDBCoreConstants.UM_USER_PASSWORD, password);

        WriteResult writeResult = null;
        if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID) && saltValue == null) {
            map.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            map.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
            map.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
            writeResult = updateStringValuesToDatabase(null, mongoQuery, map);
        } else if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID) && saltValue != null) {
            map.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            map.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
            map.put(MongoDBCoreConstants.UM_SALT_VALUE, saltValue);
            writeResult = updateStringValuesToDatabase(null, mongoQuery, map);
        } else if (!mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID) && saltValue == null) {
            map.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
            map.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
            map.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
            writeResult = updateStringValuesToDatabase(null, mongoQuery, map);
        } else {
            map.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, false);
            map.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
            map.put(MongoDBCoreConstants.UM_SALT_VALUE, saltValue);
            writeResult = updateStringValuesToDatabase(null, mongoQuery, map);
        }

        if (!writeResult.isUpdateOfExisting() && this.isMobileUserName && allowMobileUsername) {
            DBObject userObj = this.getUserObjectByMobile(userName);
            if (userObj != null) {
                doUpdateCredentialByAdmin(userObj.get(MongoDBCoreConstants.UM_USER_NAME).toString(), newCredential, false);
            }
        }
    }

    private String generateSaltValue() {

        if (log.isDebugEnabled()) {
            log.debug("generateSaltValue()");
        }

        // Create a random salt, returning 128-bit (16 bytes) of binary data
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[16];
        // SecureRandom is automatically seeded by calling nextBytes
        secureRandom.nextBytes(bytes);
        return Base64.encode(bytes);
    }

    private WriteResult updateStringValuesToDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateStringValuesToDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params)");
        }

        MongoPreparedStatement prepStmt;
        if (dbConnection == null) {
            dbConnection = loadUserStoreSpecificDataSource();
        }
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
            if (log.isDebugEnabled()) {
                if (!result.isUpdateOfExisting()) {
                    log.debug("No documents updated");
                }
            }

            return result;
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while updating string values", e);
        }

    }

    private void updateUserClaimValuesToDatabase(DB dbConnection, Map<String, Object> map, boolean isUpdateTrue)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateUserClaimValuesToDatabase with params [" + map.entrySet().stream().map(entry -> "[" + entry.getKey() + ": " + entry.getValue() + "]").reduce("", String::concat) + "] [" + isUpdateTrue + "]");
        }

        if (map == null) {
            throw new UserStoreException("Parameters cannot be null");
        } else {
            DBCollection collection = dbConnection.getCollection(MongoDBCoreConstants.UM_USER_ATTRIBUTE);
            if (!isUpdateTrue) {
                long id = MongoDatabaseUtil.getIncrementedSequence(dbConnection, MongoDBCoreConstants.UM_USER_ATTRIBUTE);
                BasicDBObject query = new BasicDBObject(MongoDBCoreConstants.UM_ID, id);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if(MongoDBCoreConstants.UM_USER_MOBILE.equals(entry.getKey()) && "-1".equals(entry.getValue())) {
                        throw new UserStoreException("user mobile set to '-1' is not allowed.");
                    } else {
                        query.append(entry.getKey(), entry.getValue());
                    }
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

    private void deleteStringValuesFromDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("deleteStringValuesFromDatabase(DB dbConnection, String mongoQuery, Map<String, Object> params)");
        }

        MongoPreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = loadUserStoreSpecificDataSource();
            }
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            JSONObject jsonKeys = new JSONObject(mongoQuery);
            List<String> keys = MongoDatabaseUtil.getKeys(jsonKeys);
            for (String key : keys) {
                if (!key.equals(MongoDBCoreConstants.COLLECTION_FIELD)) {
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
            WriteResult result = prepStmt.remove();
            if (log.isDebugEnabled()) {
                if (!result.isUpdateOfExisting()) {
                    log.debug("No documents were deleted");
                }
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot delete string values", e);
        } finally {
            if (localConnection && prepStmt != null) {
                prepStmt.close();
            }
        }
    }

    /**
     * Delete user from user store.
     *
     * @param userName of user to delete
     * @throws UserStoreException if loading user store fails
     */
    protected void doDeleteUser(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUser(String userName)");
        }

        int user_id;
        DB dbConnection = loadUserStoreSpecificDataSource();
        user_id = getUserId(userName);
        if (user_id == 0) {
            log.warn("No registered user found for given user name");
        } else {
            String mongoQuery;
            String mongoQuery2;
            String mongoQuery3;
            Map<String, Object> map = new HashMap<>();
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE);
            mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE);
            mongoQuery3 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.DELETE_USER);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                map.put(MongoDBCoreConstants.UM_USER_ID, user_id);
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                map.put(MongoDBCoreConstants.UM_USER_NAME, userName);
                map.put(MongoDBCoreConstants.UM_ID, user_id);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery, map);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery2, map);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery3, map);
            }
        }
    }

    /**
     * Set user claim value of registered user in user store.
     *
     * @param userName    of registered user
     * @param claimValue  of user to set
     * @param claimURI    of user claim
     * @param profileName of user claims belongs to
     * @throws UserStoreException if error occurred
     */
    protected void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName) [" + userName + "] [" + claimURI + "] [" + claimValue + "]");
        }

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        if (claimValue == null) {
            throw new UserStoreException("Cannot set null values.");
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            String property = getClaimAtrribute(claimURI, userName, null);
            int userId = getUserId(userName);
            String userPropertyExist = getProperty(dbConnection, userId);
            Map<String, Object> map = new HashMap<>();
            map.put(MongoDBCoreConstants.UM_USER_ID, userId);
            map.put(MongoDBCoreConstants.UM_PROFILE_ID, profileName);
            map.put(property, claimValue);
            if (userPropertyExist == null) {
                addProperty(dbConnection, map);
            } else {
                updateProperty(dbConnection, map);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error occurred while getting claim attribute for user: " + userName, e);
        }
    }

    /**
     * Get a user claim property of given user.
     *
     * @param dbConnection of mongodb
     * @param userId       of user to get property
     * @return property of given user
     * @throws UserStoreException if error occurred
     */
    private String getProperty(DB dbConnection, int userId) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getProperty(DB dbConnection, int userId) [" + userId + "]");
        }

        String mongoQuery;
        mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_PROP_FOR_PROFILE);
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot get property");
        }
        String value = null;

        try {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            prepStmt.setInt(MongoDBCoreConstants.UM_USER_ID, userId);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            DBCursor cursor = prepStmt.find();
            while (cursor.hasNext()) {
                value = cursor.next().get(MongoDBCoreConstants.UM_ID).toString();
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot get property", e);
        }
        return value;
    }

    /**
     * Set user claim values of registered user in user store.
     *
     * @param userName    of registered user
     * @param claims      of user to set
     * @param profileName of user to whom claims belong
     * @throws UserStoreException if error occurred
     */
    protected void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doSetUserClaimValues(String userName, Map<String, String> claims, String profileName) [" + userName + "] [" + claims.entrySet().stream().map(entry -> "[ " + entry.getKey() + ": " + entry.getValue() + "]").reduce("", String::concat) + "] [" + profileName + "]");
        }

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        claims.putIfAbsent(UserCoreConstants.PROFILE_CONFIGURATION, UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            Iterator<Map.Entry<String, String>> ite = claims.entrySet().iterator();
            Map<String, Object> map = new HashMap<>();
            while (ite.hasNext()) {
                Map.Entry<String, String> entry = ite.next();
                String claimUri = entry.getKey();
                String property = getClaimAtrribute(claimUri, userName, null);
                String value = entry.getValue();
                if (value.length() > 0) {
                    map.put(property, value);
                } else {
                    this.deleteProperty(dbConnection, userName, property, profileName);
                }
            }
            int userId = getUserId(userName);
            map.put(MongoDBCoreConstants.UM_USER_ID, userId);
            map.put(MongoDBCoreConstants.UM_PROFILE_ID, profileName);

            if (log.isDebugEnabled()) {
               log.debug("doSetUserClaimValues processed map: " + map.entrySet().stream().map(entry -> "[" + entry.getKey() + ": " + entry.getValue() + "]").reduce("", String::concat));
            }

            String userPropertyExist = getProperty(dbConnection, userId);
            if (userPropertyExist == null) {

                // in case no uid set
                if (map.containsKey(MongoDBCoreConstants.UID_FIELD)) { // persiste uppercase username to user attribute
                    map.put(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, ((String)map.get(MongoDBCoreConstants.UID_FIELD)).toUpperCase());
                } else {
                    map.put(MongoDBCoreConstants.UID_FIELD, userName);
                    map.put(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, userName.toUpperCase());
                }

                addProperty(dbConnection, map);
            } else {
                updateProperty(dbConnection, map);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException | MongoDBQueryException e) {
            throw new UserStoreException("Error occurred while getting claim attribute for user: " + userName, e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("doSetUserClaimValues completed");
            }
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private void updateProperty(DB dbConnection, Map<String, Object> map) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("updateProperty(DB dbConnection, Map<String, Object> map)");
        }

        String mongoQuery;

        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.UPDATE_USER_PROPERTY);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE);
        }
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot update property");
        }
        if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            updateUserClaimValuesToDatabase(dbConnection, map, true);
        } else {
            updateUserClaimValuesToDatabase(dbConnection, map, true);
        }
    }

    /**
     * Delete user claim value of given user claim.
     *
     * @param userName    of user
     * @param claimURI    to delete from user
     * @param profileName where claim belongs to
     * @throws UserStoreException if error occurred
     */
    protected void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUserClaimValue(String userName, String claimURI, String profileName) [" + userName + "] [" + claimURI + "] [" + profileName + "]");
        }

        DB dbConnection = loadUserStoreSpecificDataSource();
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        try {
            String property;
            if (UserCoreConstants.PROFILE_CONFIGURATION.equals(claimURI)) {
                property = UserCoreConstants.PROFILE_CONFIGURATION;
            } else {
                property = getClaimAtrribute(claimURI, userName, null);
            }
            this.deleteProperty(dbConnection, userName, property, profileName);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error occurred while getting the claim attribute for user: " + userName, e);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot delete user claim value", e);
        } catch (Exception e) {
            log.error("Error when doDeleteUserClaimValue", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private void deleteProperty(DB dbConnection, String userName, String property, String profileName)
            throws UserStoreException, MongoDBQueryException {

        if (log.isDebugEnabled()) {
            log.debug("deleteProperty(DB dbConnection, String userName, String property, String profileName) [" + userName + "] [" + property + "] [" + profileName + "]");
        }

        String mongoQuery;
        Map<String, Object> map = new HashMap<>();
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.DELETE_USER_PROPERTY);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE);
        }

        int userId = this.getUserId(userName);
        map.put(MongoDBCoreConstants.UM_USER_ID, userId);
        map.put(MongoDBCoreConstants.UM_ATTR_NAME, property);
        map.put(MongoDBCoreConstants.UM_PROFILE_ID, profileName);
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot delete property");
        }
        mongoQuery = mongoQuery.replace("<ATTR_TO_REMOVE>", String.format("'%s' : 1", property));
        if (log.isDebugEnabled()) {
            log.debug("MongoQuery to delete property [" + mongoQuery + "]");
        }
        if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            updateStringValuesToDatabase(dbConnection, mongoQuery, map);
        } else {
            updateStringValuesToDatabase(dbConnection, mongoQuery, map);
        }
    }

    /**
     * Delete user claim values of given user claims.
     *
     * @param userName    of user
     * @param claims      to delete from user
     * @param profileName where claim belongs to
     * @throws UserStoreException if error occurred
     */
    protected void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteUserClaimValues(String userName, String[] claims, String profileName)");
        }

        DB dbConnection = loadUserStoreSpecificDataSource();
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        try {
            for (String claimURI : claims) {
                String property = getClaimAtrribute(claimURI, userName, null);
                this.deleteProperty(dbConnection, userName, property, profileName);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error occurred while getting the claim attribute for user: " + userName, e);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot delete user claim values", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Update user list of a given role.
     *
     * @param roleName     Role name of user to update
     * @param deletedUsers Send this param fill with if want to remove user from role
     * @param newUsers     Send this param fill with if want to add user to role
     * @throws UserStoreException if any error occurred
     */
    protected void doUpdateUserListOfRole(String roleName, String deletedUsers[], String[] newUsers)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateUserListOfRole(String roleName, String deletedUsers[], String[] newUsers)");
        }

        JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(roleName);
        roleName = ctx.getRoleName();
        int roleTenantId = ctx.getTenantId();
        boolean isShared = ctx.isShared();
        String mongoQuery;
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(isShared ? MongoDBRealmConstants.REMOVE_USER_FROM_SHARED_ROLE
                    : MongoDBRealmConstants.REMOVE_USER_FROM_ROLE);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(isShared ?
                    MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE
                    : MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE);
        }
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot update user list of given role");
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            String mongoQuery2;
            if (isShared) {
                mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER);
            } else {
                mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_USER_TO_ROLE);
            }
            if (mongoQuery2 == null) {
                throw new UserStoreException("Mongo query is null. Cannot update user list of given role");
            }
            int userIds[];
            if (deletedUsers != null && deletedUsers.length > 0) {
                userIds = getUserIDS(dbConnection, deletedUsers);
            } else {
                userIds = getUserIDS(dbConnection, newUsers);
            }

            String[] roles = {roleName};
            int roleIds[] = getRolesIDS(dbConnection, roles);
            Map<String, Object> mapRole = new HashMap<>();
            mapRole.put(MongoDBCoreConstants.UM_USER_ID, userIds);
            mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, roleIds[0]);
            if (isShared) {
                mapRole.put(MongoDBCoreConstants.UM_ROLE_TENANT_ID, roleTenantId);
                mapRole.put(MongoDBCoreConstants.UM_USER_TENANT_ID, this.tenantId);
                if (newUsers.length > 0) {
                    MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, mapRole);
                }
                if (deletedUsers != null && deletedUsers.length > 0) {
                    MongoDatabaseUtil.deleteUserMappingInBatchMode(dbConnection, mongoQuery, mapRole);
                }
            } else {
                mapRole.put(MongoDBCoreConstants.UM_TENANT_ID, roleTenantId);
                if (newUsers.length > 0) {
                    long userRoleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection,
                            MongoDBCoreConstants.UM_USER_ROLE);
                    mapRole.put(MongoDBCoreConstants.UM_ID, userRoleId);
                    MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, mapRole);
                }
                if (deletedUsers != null && deletedUsers.length > 0) {
                    MongoDatabaseUtil.deleteUserMappingInBatchMode(dbConnection, mongoQuery, mapRole);
                }
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot update user list of given role", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Update role list of user.
     *
     * @param userName     of user to update
     * @param deletedRoles send this param fill with if want to remove role from user
     * @param newRoles     send this param fill with if want to add role to user
     * @throws UserStoreException if any error occurred
     */
    protected void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles) [" + userName + "] [" + Arrays.toString(deletedRoles) + "] [" + Arrays.toString(newRoles) + "]");
        }

        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            String mongoQuery;
            String[] userNames = userName.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (userNames.length > 1) {
                userName = userNames[1];
            }
            if (deletedRoles != null && deletedRoles.length > 0) {
                // If username and role names are prefixed with domain name, remove the domain name
                RoleBreakdown breakdown = getSharedRoleBreakdown(deletedRoles);
                String[] roles = breakdown.getRoles();

                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantIds();
                Map<String, Object> mapRole = new HashMap<>();
                if (roles.length > 0) {
                    if (isCaseSensitiveUsername()) {
                        mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.REMOVE_ROLE_FROM_USER);
                    } else {
                        mongoQuery = realmConfig.getUserStoreProperty(
                                MongoDBCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE);
                    }
                    if (mongoQuery.equals("")) {
                        throw new UserStoreException("Mongo query is empty. Cannot update role list of user");
                    }
                    MongoPreparedStatement prepStmt2;
                    if (isCaseSensitiveUsername()) {
                        prepStmt2 = new MongoPreparedStatementImpl(dbConnection, MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY);
                        prepStmt2.setString(MongoDBCoreConstants.UID_FIELD, userName);
                    } else {
                        prepStmt2 = new MongoPreparedStatementImpl(dbConnection, MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE);
                        prepStmt2.setString(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, userName.toUpperCase());
                    }
                    if (this.isMobileUserName) {
                        prepStmt2.setString(MongoDBCoreConstants.UM_USER_MOBILE, userName);
                    } else {
                        prepStmt2.setString(MongoDBCoreConstants.UM_USER_MOBILE, "-1");
                    }
                    int rolesID[] = getRolesIDS(dbConnection, roles);
                    int userID = -1;
                    prepStmt2.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                    DBCursor cursor = prepStmt2.find();
                    if (cursor.hasNext()) {
                        userID = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_USER_ID).toString());
                    } else {
                        userID = this.getUserIDWithoutMobile(dbConnection, userName);
                    }

                    mapRole.put(MongoDBCoreConstants.UM_USER_ID, userID);
                    mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, rolesID);
                    if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                        mapRole.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                        MongoDatabaseUtil.deleteUserRoleMappingInBatchMode(dbConnection, mongoQuery, mapRole);
                    } else {
                        MongoDatabaseUtil.deleteUserRoleMappingInBatchMode(dbConnection, mongoQuery, mapRole);
                    }
                }

                if (sharedRoles.length > 0) {
                    if (isCaseSensitiveUsername()) {
                        mongoQuery = realmConfig.getUserStoreProperty(
                                MongoDBRealmConstants.REMOVE_USER_FROM_SHARED_ROLE);
                    } else {
                        mongoQuery = realmConfig.getUserStoreProperty(
                                MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE);
                    }
                    if (mongoQuery == null) {
                        throw new UserStoreException("Mongo query is null. Cannot update role list of user");
                    }
                    MongoDatabaseUtil.updateUserRoleMappingWithExactParams(dbConnection, mongoQuery, sharedRoles,
                            userName, sharedTenantIds, tenantId);
                }
            }
            String mongoQuery2 = null;
            if (newRoles != null && newRoles.length > 0) {
                // If user name and role names are prefixed with domain name, remove the domain name
                RoleBreakdown breakdown = getSharedRoleBreakdown(newRoles);
                String[] roles = breakdown.getRoles();
                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantIds();
                int roleIds[] = getRolesIDS(dbConnection, roles);
                String users[] = {userName};
                int userIds[] = getUserIDS(dbConnection, users);
                Map<String, Object> map = new HashMap<>();
                long userRoleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection,
                        MongoDBCoreConstants.UM_USER_ROLE);
                map.put(MongoDBCoreConstants.UM_ID, userRoleId);
                map.put(MongoDBCoreConstants.UM_ROLE_ID, roleIds);
                map.put(MongoDBCoreConstants.UM_USER_ID, userIds[0]);

                if (roles.length > 0) {
                    if (isCaseSensitiveUsername()) {
                        mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_ROLE_TO_USER);
                    } else {
                        mongoQuery2 = realmConfig.getUserStoreProperty(
                                MongoDBCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE);
                    }
                }
                if (mongoQuery2 == null) {
                    if (isCaseSensitiveUsername()) {
                        mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_ROLE_TO_USER);
                    } else {
                        mongoQuery2 = realmConfig.getUserStoreProperty(
                                MongoDBCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE);
                    }
                }
                if (mongoQuery2 == null) {
                    throw new UserStoreException("Mongo query is null. Cannot update role list of user");
                } else {
                    if (mongoQuery2.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                        map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                        MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, map);
                    } else {
                        MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, map);
                    }
                }

                if (sharedRoles.length > 0) {
                    mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER);
                    if (mongoQuery2 == null) {
                        throw new UserStoreException("Mongo query is null. Cannot update role list of user");
                    }
                    MongoDatabaseUtil.updateUserRoleMappingWithExactParams(dbConnection, mongoQuery2, sharedRoles,
                            userName, sharedTenantIds, tenantId);
                }
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot update role list of user", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private RoleBreakdown getSharedRoleBreakdown(String[] rolesList) {

        if (log.isDebugEnabled()) {
            log.debug("getSharedRoleBreakdown(String[] rolesList)");
        }

        List<String> roles = new ArrayList<>();
        List<Integer> tenantIds = new ArrayList<>();
        List<String> sharedRoles = new ArrayList<>();
        List<Integer> sharedTenantIds = new ArrayList<>();

        for (String role : rolesList) {
            String[] deletedRoleNames = role.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (deletedRoleNames.length > 1) {
                role = deletedRoleNames[1];
            }
            JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(role);
            role = ctx.getRoleName();
            int roleTenantId = ctx.getTenantId();
            boolean isShared = ctx.isShared();
            if (isShared) {
                sharedRoles.add(role);
                sharedTenantIds.add(roleTenantId);
            } else {
                roles.add(role);
                tenantIds.add(roleTenantId);
            }
        }
        RoleBreakdown breakdown = new RoleBreakdown();

        // Non shared roles and tenant ids
        breakdown.setRoles(roles.toArray(new String[roles.size()]));
        breakdown.setTenantIds(tenantIds.toArray(new Integer[tenantIds.size()]));

        // Shared roles and tenant ids
        breakdown.setSharedRoles(sharedRoles.toArray(new String[sharedRoles.size()]));
        breakdown.setSharedTenantIds(sharedTenantIds.toArray(new Integer[sharedTenantIds.size()]));

        return breakdown;
    }

    /**
     * Get all roles list of a user.
     *
     * @param userName of user to get role list
     * @param filter   if any filtering apply
     * @return String[] of role list of user
     * @throws UserStoreException if any error occurred
     */
    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("doGetExternalRoleListOfUser(String userName, String filter) [" + userName + "] [" + filter + "]");
        }

        String mongoQuery;
        String query;
        if (isCaseSensitiveUsername()) {
            query = MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY;
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USER_ROLE);
        } else {
            query = MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE;
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USER_ROLE);
        }

        try {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(this.db, query);
            if (isCaseSensitiveUsername()) {
                prepStmt.setString(MongoDBCoreConstants.UID_FIELD, userName);
            } else {
                prepStmt.setString(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, userName.toUpperCase());
            }
            if (this.isMobileUserName) {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, userName);
            } else {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "-1");
            }
            if (MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            DBCursor cursor = prepStmt.find();
            int userId = -1;
            if (cursor.hasNext()) {
                userId = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_USER_ID).toString());
            } else {
                userId = this.getUserIDWithoutMobile(this.db, userName);
            }
            List<String> roles = new ArrayList<>();
            String[] names;
            if (mongoQuery == null) {
                throw new UserStoreException("Mongo query is null. Cannot get external role list of user");
            }
            Map<String, Object> map = new HashMap<>();
            map.put(MongoDBCoreConstants.USER_ROLE_UM_USER_ID, userId);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                map.put(MongoDBCoreConstants.USER_ROLE_UM_TENANT_ID, tenantId);
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                names = getStringValuesFromDatabase(mongoQuery, map);
            } else {
                names = getStringValuesFromDatabase(mongoQuery, map);
            }
            Collections.addAll(roles, names);

            String[] result = roles.toArray(new String[roles.size()]);

            if(log.isDebugEnabled()) {
                log.debug("doGetExternalRoleListOfUser [" + userName + "] [ " + Arrays.toString(result) + " ]");
            }

            return result;
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot get external role list of user", e);
        }
    }

    private String[] getStringValuesFromDatabase(String mongoQuery, Map<String, Object> params)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getStringValuesFromDatabase(String mongoQuery, Map<String, Object> params)");
        }

        String[] values;
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            values = MongoDatabaseUtil.getStringValuesFromDatabase(dbConnection, mongoQuery, params, true, true);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while getting string values from database", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return values;
    }

    private String[] getDistinctStringValues(String mongoQuery, Map<String, Object> params) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getDistinctStringValues(String mongoQuery, Map<String, Object> params)");
        }

        String[] values;
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            values = MongoDatabaseUtil.getDistinctStringValuesFromDatabase(dbConnection, mongoQuery, params);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while getting distinct string values", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return values;
    }

    /**
     * Get all shared role list of user.
     *
     * @param userName     of user to get shared role list
     * @param filter       if any filter
     * @param tenantDomain of currently logged in
     * @return String[] of shred roles list of user
     * @throws UserStoreException if any exception occurred
     */
    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)
            throws UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)");
            log.debug("Looking for shared roles for user: " + userName + " for tenant: " + tenantDomain);
        }
        if (isSharedGroupEnabled()) {
            String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_SHARED_ROLES_FOR_USER);
            return getRoleNamesWithDomain(mongoQuery, userName, tenantId);
        }
        return new String[0];
    }

    private String[] getRoleNamesWithDomain(String mongoQuery, String username, int tenantId) throws
            UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getRoleNamesWithDomain(String mongoQuery, String username, int tenantId) [" + mongoQuery + "] [" + username + "] [" + tenantId + "]");
        }

        DB dbConnection = loadUserStoreSpecificDataSource();
        MongoPreparedStatement prepStmt;
        List<String> roles = new ArrayList<>();
        try {
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, username);
            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            DBCursor cursor = prepStmt.find();
            while (cursor.hasNext()) {
                String name = cursor.next().get(MongoDBCoreConstants.UM_ROLE_NAME).toString();
                int tenant = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_TENANT_ID).toString());
                //noinspection unused
                String tenantEntry = UserCoreUtil.addTenantDomainToEntry(name, String.valueOf(tenant));
                roles.add(name);
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("MongoDBQueryException occurred. Cannot get role names with domain", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return roles.toArray(new String[roles.size()]);
    }

    /**
     * Add new role to mongodb user store.
     *
     * @param roleName of new role
     * @param userList of new role to add
     * @param shared   status of whether the role is shared or not
     * @throws UserStoreException if any exception occurred
     */
    protected void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAddRole(String roleName, String[] userList, boolean shared)");
        }

        Map<String, Object> map = new HashMap<>();
        if (shared && isSharedGroupEnabled()) {
            doAddSharedRole(roleName, userList);
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        String mongoQuery = "";
        String mongoQuery2;
        Map<String, Object> mapRole = new HashMap<>();
        try {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_ROLE);
            map.put(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
            long roleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection, MongoDBCoreConstants.UM_ROLE);
            map.put(MongoDBCoreConstants.UM_ID, roleId);
            map.put(MongoDBCoreConstants.UM_SHARED_ROLE, 0);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            } else {
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            }
            if (userList != null) {
                if (isCaseSensitiveUsername()) {
                    mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_USER_TO_ROLE);
                } else {
                    mongoQuery2 = realmConfig.getUserStoreProperty(
                            MongoDBCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE);
                }
                if (mongoQuery2 == null) {
                    throw new UserStoreException("Mongo query is null. Cannot add role");
                }
                MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(this.db,
                        MongoDBRealmConstants.ADD_USER_TO_ROLE_MONGO_QUERY_CONDITION1);
                if (mongoQuery2.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                    String mongoCondition = MongoDBRealmConstants.GET_IS_ROLE_EXISTING_MONGO_QUERY;
                    MongoPreparedStatement prepStmt2 = new MongoPreparedStatementImpl(dbConnection, mongoCondition);
                    prepStmt2.setString(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
                    prepStmt2.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                    DBCursor cursor = prepStmt2.find();
                    roleId = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_ID).toString());
                    int[] userID = getUserIDS(dbConnection, userList);
                    long userRoleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection,
                            MongoDBCoreConstants.UM_USER_ROLE);
                    mapRole.put(MongoDBCoreConstants.UM_ID, userRoleId);
                    mapRole.put(MongoDBCoreConstants.UM_USER_ID, userID);
                    mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, roleId);
                    mapRole.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                    if (userID.length != 0) {
                        MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, mapRole);
                    }
                } else {
                    String mongoCondition = MongoDBRealmConstants.GET_IS_ROLE_EXISTING_MONGO_QUERY;
                    MongoPreparedStatement prepStmt2 = new MongoPreparedStatementImpl(dbConnection, mongoCondition);
                    int roleID;
                    prepStmt2.setString(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
                    DBCursor cursor = prepStmt.find();
                    roleID = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_ID).toString());
                    int[] userID = getUserIDS(dbConnection, userList);
                    long userRoleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection,
                            MongoDBCoreConstants.UM_USER_ROLE);
                    mapRole.put(MongoDBCoreConstants.UM_ID, userRoleId);
                    mapRole.put(MongoDBCoreConstants.UM_USER_ID, roleID);
                    mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, userID);
                    if (userID.length != 0) {
                        MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, mapRole);
                    }
                }
            }
        } catch (MongoDBQueryException e) {
            this.deleteStringValuesFromDatabase(dbConnection, mongoQuery, map);
            throw new UserStoreException("Error occurred while adding role: " + roleName, e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private void doAddSharedRole(String roleName, String[] userList) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doAddSharedRole(String roleName, String[] userList) [" + roleName + "] [" + Arrays.stream(userList).map(item -> "[" + item + "]").reduce(String::concat) + "]");
        }

        Map<String, Object> map = new HashMap<>();
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_SHARED_ROLE);
            map.put(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
            map.put(MongoDBCoreConstants.UM_SHARED_ROLE, roleName);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {

                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            } else {
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            }
            if (userList != null) {
                String mongoQuery2;
                if (isCaseSensitiveUsername()) {
                    mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER);
                } else {
                    mongoQuery2 = realmConfig.getUserStoreProperty(
                            MongoDBCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE);
                }
                String[] roles = {roleName};
                int roleID[] = getRolesIDS(dbConnection, roles);
                int[] userID = getUserIDS(dbConnection, userList);
                Map<String, Object> mapRole = new HashMap<>();
                mapRole.put(MongoDBCoreConstants.UM_USER_ID, roleID[0]);
                mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, userID);
                if (mongoQuery2.contains(MongoDBCoreConstants.UM_TENANT_ID)) {

                    mapRole.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                    MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2,
                            mapRole);
                } else {
                    MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoQuery2, mapRole);
                }
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error occurred while adding shared role: " + roleName, e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private int[] getUserIDS(DB dbConnection, String[] userList) throws MongoDBQueryException {

        if (log.isDebugEnabled()) {
            log.debug("getUserIDS(DB dbConnection, String[] userList) [" + Arrays.stream(userList).map(item -> "[" + item + "]").reduce("", String::concat) + "]");
        }

        String query;
        if (isCaseSensitiveUsername()) {
            query = MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY;
        } else {
            query = MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE;
        }
        int userID[] = new int[userList.length];
        int index = 0;
        for (String user : userList) {

            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(dbConnection, query);
            if (query.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            if (isCaseSensitiveUsername()) {
                prepStmt.setString(MongoDBCoreConstants.UID_FIELD, user);
            } else {
                prepStmt.setString(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, user.toUpperCase());
            }
            if (this.isMobileUserName) {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, user);
            } else {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "-1");
            }
            DBCursor cursor = prepStmt.find();
            if (cursor.hasNext()) {
                int id = (int) Double.parseDouble(cursor.next().get(MongoDBCoreConstants.UM_USER_ID).toString());
                if (id > 0) {
                    userID[index] = id;
                }
            } else {
                int id = getUserIDWithoutMobile(dbConnection, user);
                if (id > 0) {
                    userID[index] = id;
                }
            }
            index++;
            prepStmt.close();
        }

        if (log.isDebugEnabled()) {
            log.debug("getUserIDS return userId [" + Arrays.stream(userID).mapToObj(String::valueOf).map(item -> "[" + item + "]").reduce("", String::concat) + "]");
        }

        return userID;
    }

    /**
     * Delete given role.
     *
     * @param roleName to delete from user store
     * @throws UserStoreException if any exception occurred
     */
    protected void doDeleteRole(String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doDeleteRole(String roleName)");
        }

        Map<String, Object> map = new HashMap<>();
        String mongoQuery1 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE);
        if (mongoQuery1 == null) {
            throw new UserStoreException("Mongo query is null. Cannot delete role");
        }
        String mongoQuery2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.DELETE_ROLE);
        if (mongoQuery2 == null) {
            throw new UserStoreException("Mongo query is null. Cannot delete role");
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            String roles[] = {roleName};
            int roleIds[] = getRolesIDS(dbConnection, roles);
            map.put(MongoDBCoreConstants.UM_ROLE_ID, roleIds[0]);
            if (mongoQuery1.contains(MongoDBCoreConstants.UM_TENANT_ID)) {

                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                map.put(MongoDBCoreConstants.UM_ID, roleIds[0]);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery1, map);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery2, map);
            } else {
                map.put(MongoDBCoreConstants.UM_ID, roleIds[0]);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery1, map);
                this.deleteStringValuesFromDatabase(dbConnection, mongoQuery2, map);
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error occurred while deleting role: " + roleName, e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Update role name of user store.
     *
     * @param roleName    to update
     * @param newRoleName to be updated
     * @throws UserStoreException if any exception occurred
     */
    protected void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doUpdateRoleName(String roleName, String newRoleName)");
        }

        JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(roleName);
        Map<String, Object> map = new HashMap<>();
        if (isExistingRole(newRoleName)) {
            throw new UserStoreException("Role name: " + newRoleName +
                    " already exists in the system. Please pick another name");
        }
        String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.UPDATE_ROLE_NAME);
        map.put(MongoDBCoreConstants.UM_ROLE_NAME, newRoleName);
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot update role name");
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            roleName = ctx.getRoleName();
            String roles[] = {roleName};
            int roleIds[] = getRolesIDS(dbConnection, roles);
            map.put(MongoDBCoreConstants.UM_ID, roleIds[0]);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            } else {
                this.updateStringValuesToDatabase(dbConnection, mongoQuery, map);
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error occurred while updating role name: " + roleName, e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get role name of user store.
     *
     * @param filter       to filter the search
     * @param maxItemLimit to display per page
     * @return String[] of roles
     * @throws UserStoreException if any exception occurred
     */
    protected String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetRoleNames(String filter, int maxItemLimit)");
        }

        String[] roles = new String[0];
        DB dbConnection = null;
        String mongoQuery;
        MongoPreparedStatement prepStmt;
        if (maxItemLimit == 0) {
            return roles;
        }
        try {
            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }
            List<String> lst = new LinkedList<>();
            dbConnection = loadUserStoreSpecificDataSource();
            if (dbConnection == null) {

                throw new UserStoreException("Null connection");
            }
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_ROLE_LIST);

            try {
                prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
                prepStmt.setString(MongoDBCoreConstants.UM_ROLE_NAME, filter);
                if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                    prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                }
                DBCursor cursor;
                cursor = prepStmt.find().limit(maxItemLimit);
                if (cursor != null) {
                    while (cursor.hasNext()) {
                        String name = cursor.next().get(MongoDBCoreConstants.UM_ROLE_NAME).toString();
                        // Append the domain if exist
                        String domain =
                                realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                        name = UserCoreUtil.addDomainToName(name, domain);
                        lst.add(name);
                    }
                }
                if (lst.size() > 0) {
                    roles = lst.toArray(new String[lst.size()]);
                }

            } catch (MongoDBQueryException e) {
                throw new UserStoreException("Error while fetching roles according to the filter", e);
            }
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return roles;
    }

    /**
     * Get role name of user store.
     *
     * @param filter       to filter the search
     * @param maxItemLimit to display per page
     * @return String[] of users
     * @throws UserStoreException if any exception occurred
     */
    protected String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doListUsers(String filter, int maxItemLimit) [" + filter + "] [" + maxItemLimit + "]");
        }

        String[] users = new String[0];
        DB dbConnection = null;
        String mongoQuery;
        MongoPreparedStatement prepStmt;
        //noinspection deprecation
        AggregationOutput cursor;
        if (maxItemLimit == 0) {
            return new String[0];
        }
        int givenMax;
        try {
            givenMax = Integer.parseInt(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
            );
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }
        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }
        try {
            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            List<String> lst = new LinkedList<>();
            dbConnection = loadUserStoreSpecificDataSource();
            if (dbConnection == null) {
                throw new UserStoreException("Data source is null. Cannot list users");
            }

            if (isCaseSensitiveUsername()) {
                mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USER_FILTER);
            } else {
                mongoQuery = realmConfig.getUserStoreProperty(MongoDBCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE);
            }

            try {
                prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
                if (isCaseSensitiveUsername()) {
                    prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, filter);
                } else {
                    prepStmt.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, filter.toUpperCase());
                }
                if ("%".equals(filter)) { // limit result count to speed up query
                    prepStmt.setInt(MongoDBCoreConstants.LIMIT_FIELD, maxItemLimit);
                }
                if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                    prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                }
                cursor = prepStmt.aggregate();
            } catch (MongoException e) {
                String errorMessage =
                        "Error while fetching users according to filter : " + filter + " & max Item limit " +
                                ": " + maxItemLimit;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
                throw new UserStoreException(errorMessage, e);
            } catch(MongoDBQueryException e) {
                throw new UserStoreException("Query MongoDB Error.", e);
            }
            if (cursor != null) {
                for (DBObject object : cursor.results()) {
                    String name = object.get(MongoDBCoreConstants.UM_USER_NAME).toString();
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(name)) {
                        continue;
                    }
                    // append the domain if exist
                    String domain =
                            realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    name = UserCoreUtil.addDomainToName(name, domain);
                    lst.add(name);
                }
            }
            if (lst.size() > 0) {
                users = lst.toArray(new String[lst.size()]);
            }
            Arrays.sort(users);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return users;
    }

    /**
     * Get internal role names of given user.
     *
     * @param userNames to filter the search
     * @return String[] of internal roles
     */
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames) {

        if (log.isDebugEnabled()) {
            log.debug("doGetDisplayNamesForInternalRole(String[] userNames)");
        }

        return userNames;
    }

    /**
     * Check whether the user in given role.
     *
     * @param userName to filter the search
     * @param roleName to display per page
     * @return boolean status
     * @throws UserStoreException if any exception occurred
     */
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doCheckIsUserInRole(String userName, String roleName) [" + userName + "] [" + roleName + "]");
        }

        String[] roles = doGetExternalRoleListOfUser(userName, "*");
        if (roles != null) {
            for (String role : roles) {
                if (role.equalsIgnoreCase(roleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get shared role names of user store.
     *
     * @param tenantDomain of currently logged in
     * @param filter       to filter the search
     * @param maxItemLimit to display per page
     * @return String[] of shared roles
     * @throws UserStoreException if any exception occurred
     */
    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit)");
        }

        String[] roles = new String[0];
        DB dbConnection = null;
        String mongoQuery;
        MongoPreparedStatement prepStmt;
        DBCursor cursor;

        if (maxItemLimit == 0) {
            return roles;
        }
        try {
            if (!isSharedGroupEnabled()) {
                return roles;
            }
            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim().replace("*", "%").replace("?", "_");
            } else {
                filter = "%";
            }
            List<String> lst = new LinkedList<>();
            dbConnection = loadUserStoreSpecificDataSource();
            if (dbConnection == null) {
                throw new UserStoreException("Data source is null. Cannot get shared roles");
            }
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_SHARED_ROLE_LIST);
            prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);
            prepStmt.setString(MongoDBCoreConstants.UM_ROLE_NAME, filter);
            cursor = prepStmt.find();
            // Expected columns UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE
            if (cursor != null) {
                while (cursor.hasNext()) {
                    String name = cursor.next().get(MongoDBCoreConstants.UM_SHARED_ROLE).toString();
                    int roleTenantId =
                            Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_TENANT_ID).toString());
                    // Append the domain if exist
                    String domain =
                            realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    name = UserCoreUtil.addDomainToName(name, domain);
                    name = UserCoreUtil.addTenantDomainToEntry(name, String.valueOf(roleTenantId));
                    lst.add(name);
                }
            }
            if (lst.size() > 0) {
                roles = lst.toArray(new String[lst.size()]);
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while retrieving roles", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return roles;
    }

    /**
     * Get user list of role.
     *
     * @param filter   to filter the search
     * @param roleName to search for users
     * @return String[] of users
     * @throws UserStoreException if any exception occurred
     */
    protected String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetUserListOfRole(String roleName, String filter)");
        }

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfMongoDBRole(roleContext);
    }

    private String[] getUserListOfMongoDBRole(RoleContext ctx) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getUserListOfMongoDBRole(RoleContext ctx)");
        }

        String roleName = ctx.getRoleName();
        String[] names = null;
        String mongoQuery;
        Map<String, Object> map = new HashMap<>();

        if (!ctx.isShared()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USERS_IN_ROLE);
            if (mongoQuery == null) {
                throw new UserStoreException("Mongo query is null. Cannot get roles list");
            }
            map.put(MongoDBCoreConstants.ROLE_UM_ROLE_NAME, roleName);
            if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                map.put(MongoDBCoreConstants.ROLE_UM_TENANT_ID, tenantId);
                map.put(MongoDBCoreConstants.USER_ROLE_UM_TENANT_ID, tenantId);
                names = getStringValuesFromDatabase(mongoQuery, map);
            } else {
                names = getStringValuesFromDatabase(mongoQuery, map);
            }
        } else if (ctx.isShared()) {
            map.put(MongoDBCoreConstants.UM_ROLE_NAME, roleName);
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USERS_IN_SHARED_ROLE);
            names = getStringValuesFromDatabase(mongoQuery, map);
        }
        List<String> userList = new ArrayList<>();
        String domainName = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (names != null) {
            for (String user : names) {
                user = UserCoreUtil.addDomainToName(user, domainName);
                userList.add(user);
            }
            names = userList.toArray(new String[userList.size()]);
        }
        return names;
    }

    private DB loadUserStoreSpecificDataSource() throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("loadUserStoreSpecificDataSource()");
        }

        return MongoDatabaseUtil.createRealmDataSource(realmConfig);
    }

    /**
     * Get profile names of user.
     *
     * @param userName to  search
     * @return String[] of profile names
     * @throws UserStoreException if any exception occurred
     */
    public String[] getProfileNames(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getProfileNames(String userName)");
        }

        userName = UserCoreUtil.removeDomainFromName(userName);
        String mongoQuery;
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_PROFILE_NAMES_FOR_USER);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE);
        }
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot retrieve profile names");
        }

        String[] names = null;
        try {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(this.db,
                MongoDBRealmConstants.GET_PROFILE_NAMES_FOR_USER_MONGO_QUERY_CONDITION);
            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            prepStmt.setString(MongoDBCoreConstants.UM_USER_NAME, userName);
            DBCursor cursor = prepStmt.find();
            if (cursor.hasNext()) {
                int userId = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_ID).toString());
                Map<String, Object> map = new HashMap<>();
                map.put(MongoDBCoreConstants.UM_USER_ID, userId);
                if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                    map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                    names = getDistinctStringValues(mongoQuery, map);
                } else {
                    names = getDistinctStringValues(mongoQuery, map);
                }
                if (names.length == 0) {
                    names = new String[]{UserCoreConstants.DEFAULT_PROFILE};
                } else {
                    Arrays.sort(names);
                    if (Arrays.binarySearch(names, UserCoreConstants.DEFAULT_PROFILE) < 0) {
                        // we have to add the default profile
                        String[] newNames = new String[names.length + 1];
                        int i;
                        for (i = 0; i < names.length; i++) {
                            newNames[i] = names[i];
                        }
                        newNames[i] = UserCoreConstants.DEFAULT_PROFILE;
                        names = newNames;
                    }
                }
            }
            return names;
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while getting profile names", e);
        }
    }

    /**
     * Get all profile names.
     *
     * @return String[] of profile names
     * @throws UserStoreException if any exception occurred
     */
    public String[] getAllProfileNames() throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getAllProfileNames()");
        }

        String mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_PROFILE_NAMES);
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot get profile names");
        }
        String[] names;
        Map<String, Object> map = new HashMap<>();
        if (mongoQuery.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            names = getDistinctStringValues(mongoQuery, map);
        } else {
            names = getDistinctStringValues(mongoQuery, map);
        }
        return names;
    }

    /**
     * Check the status if read only.
     *
     * @return boolean status
     */
    public boolean isReadOnly() {

        if (log.isDebugEnabled()) {
            log.debug("isReadOnly()");
        }

        return "true".equalsIgnoreCase(realmConfig.getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY)
        );
    }

    /**
     * Get user id of given user.
     *
     * @param username to find userId
     * @return int userId
     * @throws UserStoreException if any exception occurred
     */
    public int getUserId(String username) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getUserId(String username)");
        }

        String mongoQuery;

        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_CASE_INSENSITIVE);
        }

        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot get user ID");
        }

        int id = -1;
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(dbConnection, mongoQuery);

            prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);

            if (isCaseSensitiveUsername()) {
                prepStmt.setString(MongoDBCoreConstants.UID_FIELD, username);
            } else {
                prepStmt.setString(MongoDBCoreConstants.CASE_INSENSITIVE_UID_FIELD, username.toUpperCase());
            }

            if (this.isMobileUserName) {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, username);
            } else {
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, "-1");
            }

            DBCursor cursor = prepStmt.find();
            if(cursor.hasNext()) {
                id = Integer.parseInt(cursor.next().get(MongoDBCoreConstants.UM_USER_ID).toString());
            } else {
                id = this.getUserIDWithoutMobile(dbConnection, username);
            }
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while getting the user ID", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return id;
    }

    /**
     * Get tenantId of given user.
     *
     * @param username to find tenantId
     * @return int tenantId
     * @throws UserStoreException if any exception occurred
     */
    public int getTenantId(String username) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("getTenantId(String username)");
        }

        if (this.tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new UserStoreException("Unauthorized");
        }
        Map<String, Object> map = new HashMap<>();
        map.put(MongoDBCoreConstants.UM_USER_NAME, username);
        String mongoQuery;
        if (isCaseSensitiveUsername()) {
            mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.GET_TENANT_ID_FROM_USERNAME);
        } else {
            mongoQuery = realmConfig.getUserStoreProperty(
                    MongoDBCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE);
        }
        if (mongoQuery == null) {
            throw new UserStoreException("Mongo query is null. Cannot get tenant ID");
        }
        int id;
        DB dbConnection = loadUserStoreSpecificDataSource();
        try {
            id = MongoDatabaseUtil.getIntegerValueFromDatabase(dbConnection, mongoQuery, map);
        } catch (MongoDBQueryException e) {
            throw new UserStoreException("Error while retrieving the tenant ID", e);
        } finally {
            MongoDatabaseUtil.closeConnection(dbConnection);
        }
        return id;
    }

    /**
     * Get currently logged in tenantId.
     *
     * @return int tenantId
     */
    public int getTenantId() {

        if (log.isDebugEnabled()) {
            log.debug("getTenantId()");
        }

        return this.tenantId;
    }

    /**
     * Get properties of given tenant.
     *
     * @param tenant to  search for properties
     * @return Map of properties
     */
    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant) {

        if (log.isDebugEnabled()) {
            log.debug("getProperties(org.wso2.carbon.user.api.Tenant tenant)");
        }

        return getProperties((Tenant) tenant);
    }

    /**
     * This method is to check whether multiple profiles are allowed with a particular user-store.
     * Currently, MongoDB user store allows multiple profiles. Hence return true.
     *
     * @return Boolean status of multiple profile
     */
    public boolean isMultipleProfilesAllowed() {
        return true;
    }

    public void addRememberMe(String userName, String token) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("addRememberMe(String userName, String token)");
        }

        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            String[] values = DatabaseUtil.getStringValuesFromDatabase(dbConnection,
                    HybridJDBCConstants.GET_REMEMBERME_VALUE_SQL, userName, tenantId);
            Date createdTime = Calendar.getInstance().getTime();
            if (values != null && values.length > 0 && values[0].length() > 0) {
                // Update
                DatabaseUtil.updateDatabase(dbConnection,
                        HybridJDBCConstants.UPDATE_REMEMBERME_VALUE_SQL, token, createdTime, userName, tenantId);
            } else {
                // Add
                DatabaseUtil.updateDatabase(dbConnection,
                        HybridJDBCConstants.ADD_REMEMBERME_VALUE_SQL, userName, token, createdTime, tenantId);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database error occurred while saving remember me token for tenant: " +
                    tenantId, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public boolean isValidRememberMeToken(String userName, String token) {

        if (log.isDebugEnabled()) {
            log.debug("isValidRememberMeToken(String userName, String token)");
        }

        try {
            if (isExistingUser(userName)) {
                return isExistingRememberMeToken(userName, token);
            }
        } catch (Exception e) {
            log.error("Validating remember me token failed for " + userName);
            // Not throwing the exception
            // Because we need to seamlessly direct them to login UIs
        }
        return false;
    }

    /**
     * Load default user store configuration properties.
     *
     * @return Properties of default user store
     */
    public Properties getDefaultUserStoreProperties() {

        if (log.isDebugEnabled()) {
            log.debug("getDefaultUserStoreProperties()");
        }

        Property[] mandatoryProperties = MongoDBUserStoreProperties.MONGODB_UM_MANDATORY_PROPERTIES.toArray(
                new Property[MongoDBUserStoreProperties.MONGODB_UM_MANDATORY_PROPERTIES.size()]
        );
        Property[] optionalProperties = MongoDBUserStoreProperties.MONGODB_UM_OPTIONAL_PROPERTIES.toArray(
                new Property[MongoDBUserStoreProperties.MONGODB_UM_OPTIONAL_PROPERTIES.size()]
        );
        Property[] advancedProperties = MongoDBUserStoreProperties.MONGODB_UM_ADVANCED_PROPERTIES.toArray(
                new Property[MongoDBUserStoreProperties.MONGODB_UM_ADVANCED_PROPERTIES.size()]
        );
        Properties properties = new Properties();
        properties.setMandatoryProperties(mandatoryProperties);
        properties.setOptionalProperties(optionalProperties);
        properties.setAdvancedProperties(advancedProperties);
        return properties;
    }

    /**
     * Get properties of tenant.
     *
     * @param tenant to  search
     * @return Map of properties
     */
    public Map<String, String> getProperties(Tenant tenant) {

        if (log.isDebugEnabled()) {
            log.debug("getProperties(Tenant tenant)");
        }

        return this.realmConfig.getUserStoreProperties();
    }

    /**
     * MongoDB User store supports bulk import.
     *
     * @return Status of bulk import support
     */
    public boolean isBulkImportSupported() {

        if (log.isDebugEnabled()) {
            log.debug("isBulkImportSupported()");
        }

        return Boolean.valueOf(realmConfig.getUserStoreProperty("IsBulkImportSupported"));
    }

    /**
     * Get realm configuration.
     *
     * @return RealmConfiguration of logged in users
     */
    public RealmConfiguration getRealmConfiguration() {

        if (log.isDebugEnabled()) {
            log.debug("getRealmConfiguration()");
        }

        return this.realmConfig;
    }

    private void persistUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                             String profileName, boolean requirePasswordChange) throws UserStoreException {

        if (checkExistingUserName(userName)) {
            throw new UserStoreException("Username '" + userName +
                    "' already exists in the system. Please pick another username.");
        }
        DB dbConnection = loadUserStoreSpecificDataSource();
        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }
        String mongoStmt1 = "";
        String mongoStmt2 = "";
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapRole = new HashMap<>();
        long userId = -1;
        try {
            mongoStmt1 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_USER);
            String saltValue = null;
            if ("true".equals(realmConfig.getUserStoreProperties().get(MongoDBRealmConstants.STORE_SALTED_PASSWORDS))) {
                byte[] bytes = new byte[16];
                random.nextBytes(bytes);
                saltValue = Base64.encode(bytes);
            }
            String password = this.preparePassword(credentialObj, saltValue);
            map.put(MongoDBCoreConstants.UM_USER_PASSWORD, password);
            map.put(MongoDBCoreConstants.UM_USER_NAME, userName);
            map.put(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, userName.toUpperCase());
            map.put(MongoDBCoreConstants.UM_REQUIRE_CHANGE, requirePasswordChange);
            map.put(MongoDBCoreConstants.UM_CHANGED_TIME, new Date());
            userId = MongoDatabaseUtil.getIncrementedSequence(dbConnection, MongoDBCoreConstants.UM_USER);

            map.put(MongoDBCoreConstants.UM_ID, userId);

            // Do all 4 possibilities
            if (mongoStmt1.contains(MongoDBCoreConstants.UM_TENANT_ID) && (saltValue == null)) {
                map.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                this.updateUserValue(dbConnection, mongoStmt1, map);
            } else if (mongoStmt1.contains(MongoDBCoreConstants.UM_TENANT_ID) && (saltValue != null)) {
                map.put(MongoDBCoreConstants.UM_SALT_VALUE, saltValue);
                map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                this.updateUserValue(dbConnection, mongoStmt1, map);
            } else if (!mongoStmt1.contains(MongoDBCoreConstants.UM_TENANT_ID) && (saltValue == null)) {
                map.put(MongoDBCoreConstants.UM_SALT_VALUE, "");
                map.put(MongoDBCoreConstants.UM_TENANT_ID, 0);
                this.updateUserValue(dbConnection, mongoStmt1, map);
            } else {
                map.put(MongoDBCoreConstants.UM_SALT_VALUE, saltValue);
                map.put(MongoDBCoreConstants.UM_TENANT_ID, 0);
                this.updateUserValue(dbConnection, mongoStmt1, map);
            }

            String[] roles;
            if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                roles = new String[0];
            } else {
                if (roleList == null || roleList.length == 0) {
                    roles = new String[]{this.realmConfig.getEveryOneRoleName()};
                } else {
                    Arrays.sort(roleList);
                    if (Arrays.binarySearch(roleList, realmConfig.getEveryOneRoleName()) < 0) {
                        roles = new String[roleList.length + 1];
                        int i;
                        for (i = 0; i < roleList.length; i++) {
                            roles[i] = roleList[i];
                        }
                        roles[i] = realmConfig.getEveryOneRoleName();
                    } else {
                        roles = roleList;
                    }
                }
            }
            if (roles.length > 1) {
                // Add user to role
                mongoStmt2 = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_ROLE_TO_USER);

                int rolesID[] = getRolesIDS(dbConnection, roles);
                if (userId == -1) {
                    userId = getUserIDWithoutMobile(dbConnection, userName);
                }
                long userRoleId = MongoDatabaseUtil.getIncrementedSequence(dbConnection, MongoDBCoreConstants.UM_USER_ROLE);
                mapRole.put(MongoDBCoreConstants.UM_ID, userRoleId);
                mapRole.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
                mapRole.put(MongoDBCoreConstants.UM_USER_ID, userId);
                mapRole.put(MongoDBCoreConstants.UM_ROLE_ID, rolesID);
                MongoDatabaseUtil.updateUserRoleMappingInBatchMode(dbConnection, mongoStmt2, mapRole);
            }

            if (claims != null) {
                // add the properties
                if (profileName == null) {
                    profileName = UserCoreConstants.DEFAULT_PROFILE;
                }
                this.doSetUserClaimValues(userName, claims, profileName);
            }
        } catch (MongoDBQueryException e) {
            this.deleteStringValuesFromDatabase(dbConnection, mongoStmt1, map);
            this.deleteStringValuesFromDatabase(dbConnection, mongoStmt2, mapRole);
            throw new UserStoreException("Error while persisting user: " + userName, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while persisting user: " + userName, e);
        }
    }

    private int[] getRolesIDS(DB dbConnection, String[] roles) throws MongoDBQueryException {

        if (log.isDebugEnabled()) {
            log.debug("getRolesIDS(DB dbConnection, String[] roles)");
        }

        String query = MongoDBRealmConstants.GET_IS_ROLE_EXISTING_MONGO_QUERY;
        int rolesID[] = new int[roles.length];
        int index = 0;
        for (String role : roles) {
            MongoPreparedStatement prepStmt = new MongoPreparedStatementImpl(dbConnection, query);
            if (query.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
                prepStmt.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            }
            prepStmt.setString(MongoDBCoreConstants.UM_ROLE_NAME, role);
            DBCursor cursor = prepStmt.find();
            if (cursor.hasNext()) {

                int id = (int) Double.parseDouble(cursor.next().get(MongoDBCoreConstants.UM_ID).toString());
                if (id > 0) {
                    rolesID[index] = id;
                }
            }
            index++;
            prepStmt.close();
        }
        return rolesID;
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

        if(log.isDebugEnabled()) {
            log.debug("updateUserValue(DB connection, String query, Map<String, Object> map) [" + query + "] [" + map.entrySet().stream().map(entry -> "[" + entry.getKey() + ":" + entry.getValue() + "]").reduce("", String::concat) + "]");
        }

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
                            }else if (entry.getValue() instanceof Long) {
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

    /**
     * Add user property.
     *
     * @param dbConnection Connection to mongodb
     * @param map          User property
     * @throws UserStoreException If any exception occurred
     */
    private void addProperty(DB dbConnection, Map<String, Object> map) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("addProperty(DB dbConnection, Map<String, Object> map)");
        }

        String mongoStmt = realmConfig.getUserStoreProperty(MongoDBRealmConstants.ADD_USER_PROPERTY);
        if (mongoStmt == null) {
            throw new UserStoreException("Mongo query is null. Cannot add property");
        }
        if (mongoStmt.contains(MongoDBCoreConstants.UM_TENANT_ID)) {
            map.put(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
            updateUserClaimValuesToDatabase(dbConnection, map, false);
        } else {
            updateUserClaimValuesToDatabase(dbConnection, map, false);
        }
    }

    /**
     * Find if user name exists.
     *
     * @param userName to check
     * @return boolean status if user exists or not
     */
    private boolean checkExistingUserName(String userName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("checkExistingUserName(String userName) [" + userName + "]");
        }

        boolean isExisting;
        String isUnique = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
        if (this.db == null) {
            this.db = loadUserStoreSpecificDataSource();
        }
        DBCollection collection = this.db.getCollection(MongoDBCoreConstants.UM_USER);
        if ("true".equals(isUnique) && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            BasicDBObject uniqueUser = new BasicDBObject(MongoDBCoreConstants.UM_USER_NAME, userName);
            DBCursor cursor = collection.find(uniqueUser);
            isExisting = cursor.hasNext();
        } else {
            BasicDBObject userSearch;
            if (isCaseSensitiveUsername()) {
                userSearch = new BasicDBObject(MongoDBCoreConstants.UM_USER_NAME, userName).
                        append(MongoDBCoreConstants.UM_TENANT_ID, this.tenantId);
            } else {
                userSearch = new BasicDBObject(MongoDBCoreConstants.UM_USER_NAME,
                        new BasicDBObject(MongoDBCoreConstants.REGEX_FIELD, userName).append(
                                MongoDBCoreConstants.OPTIONS_FIELD, MongoDBCoreConstants.CASE_INSENSITIVE_OPTION)).
                        append(MongoDBCoreConstants.UM_TENANT_ID, this.tenantId);
            }
            DBCursor cursor = collection.find(userSearch);
            isExisting = cursor.hasNext();
        }
        return isExisting;
    }

    /**
     * Checks whether the token is existing or not.
     *
     * @param username Username
     * @param token    User property
     * @return boolean Status of token exists or not
     * @throws UserStoreException,SQLException if any exception occurred
     */
    private boolean isExistingRememberMeToken(String username, String token) throws SQLException,
            org.wso2.carbon.user.api.UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("isExistingRememberMeToken(String username, String token) [" + username + "] [" + token + "]");
        }

        boolean isValid = false;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String value = null;
        Date createdTime = null;
        Connection dbConnection = dataSource.getConnection();
        try {
            prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.GET_REMEMBERME_VALUE_SQL);
            prepStmt.setString(1, username);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                value = rs.getString(1);
                createdTime = rs.getTimestamp(2);
            }
        } catch (SQLException e) {
            throw new UserStoreException("Error occurred while checking 'isExistingRememberMeToken' for user: " +
                    username, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (value != null && createdTime != null) {
            Calendar calendar = Calendar.getInstance();
            Date nowDate = calendar.getTime();
            calendar.setTime(createdTime);
            calendar.add(Calendar.SECOND, CarbonConstants.REMEMBER_ME_COOKIE_TTL);
            Date expDate = calendar.getTime();
            if (expDate.before(nowDate)) {
                // Do nothing remember me expired.
                // Return the user gracefully
                if (log.isDebugEnabled()) {
                    log.debug("Remember me token has expired !!");
                }
            } else {
                // We also need to compare the token
                if (value.equals(token)) {
                    isValid = true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Remember me token in DB and token in request are different !!");
                    }
                }
            }
        }
        return isValid;
    }

    private int getUserIDWithoutMobile(DB dbConnection, String username) throws MongoDBQueryException {

        if (log.isDebugEnabled()) {
            log.debug("getUserIDWithoutMobile(DB dbConnection, String username) [" + username + "]");
        }

        int result = -1;
        MongoPreparedStatement prepStmtWithoutMobile;
        if (isCaseSensitiveUsername()) {
            prepStmtWithoutMobile = new MongoPreparedStatementImpl(dbConnection, MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY_WITHOUT_MOBILE);
            prepStmtWithoutMobile.setString(MongoDBCoreConstants.UM_USER_NAME, username);
        } else {
            prepStmtWithoutMobile = new MongoPreparedStatementImpl(dbConnection, MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_WITHOUT_MOBILE_MONGO_CASE_INSENSITIVE);
            prepStmtWithoutMobile.setString(MongoDBCoreConstants.UM_CASE_INSENSITIVE_USER_NAME, username.toUpperCase());
        }
        prepStmtWithoutMobile.setInt(MongoDBCoreConstants.UM_TENANT_ID, tenantId);
        DBCursor cursorWithoutMobile = prepStmtWithoutMobile.find();
        if (cursorWithoutMobile.hasNext()) {
            result = Integer.parseInt(cursorWithoutMobile.next().get(MongoDBCoreConstants.UM_ID).toString());
        }
        prepStmtWithoutMobile.close();
        return result;
    }

    private boolean isCaseSensitiveUsername() {

        if (log.isDebugEnabled()) {
            log.debug("isCaseSensitiveUsername()");
        }

        String isUsernameCaseInsensitiveString = realmConfig.getUserStoreProperty(CASE_INSENSITIVE_USERNAME);
        return !Boolean.parseBoolean(isUsernameCaseInsensitiveString);
    }

    public static class RoleBreakdown {
        private String[] roles;
        private Integer[] tenantIds;

        private String[] sharedRoles;
        private Integer[] sharedTenantIds;

        String[] getRoles() {
            return roles;
        }

        void setRoles(String[] roles) {
            this.roles = roles;
        }

        @SuppressWarnings("unused")
        Integer[] getTenantIds() {
            return tenantIds;
        }

        void setTenantIds(Integer[] tenantIds) {
            this.tenantIds = tenantIds;
        }


        String[] getSharedRoles() {
            return sharedRoles;
        }

        void setSharedRoles(String[] sharedRoles) {
            this.sharedRoles = sharedRoles;
        }

        Integer[] getSharedTenantIds() {
            return sharedTenantIds;
        }

        void setSharedTenantIds(Integer[] sharedTenantIds) {
            this.sharedTenantIds = sharedTenantIds;
        }
    }

    private DBObject getUserObjectByMobile(String mobile) {

        if (log.isDebugEnabled()) {
            log.debug("getUserObjectByMobile(String mobile) [" + mobile + "]");
        }

        DBObject result = null;
        String mongoQuery;
        MongoPreparedStatementImpl prepStmt = null;
        if(mobile != null && mobile.matches("[0-9]+") && this.isMobileUserName) {
            try {
                mongoQuery = realmConfig.getUserStoreProperty(MongoDBRealmConstants.SELECT_USER_USE_MOBILE);
                prepStmt = new MongoPreparedStatementImpl(db, mongoQuery);
                if (log.isDebugEnabled()) {
                    log.debug("getUserObjectByMobile: " + mongoQuery);
                }
                prepStmt.setString(MongoDBCoreConstants.UM_USER_MOBILE, mobile);
                AggregationOutput cursor = prepStmt.aggregate();
                if(cursor != null) {
                    for(DBObject curr: cursor.results()) {
                        BasicDBList userList = (BasicDBList)curr.get(MongoDBCoreConstants.UM_USER);
                        if(userList != null && !userList.isEmpty()) {
                            result = (DBObject)userList.get(0);
                        }
                    }
                }
            } catch(MongoDBQueryException e) {
                log.error("MongoDBQueryException occurred while authenticating", e);
            } catch (MongoException e) {
                log.error("MongoException occurred while authenticating", e);
            } finally {
                if (prepStmt != null) {
                    prepStmt.close();
                }
            }
        }
        return result;
    }

    private void logStackTrace() {
        log.info("Printing stack trace:");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            log.info("\tat " + s.getClassName() + "." + s.getMethodName()
                    + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    @Override
    protected String[] doGetInternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("doGetInternalRoleListOfUser(String userName, String filter) [" + userName + "] [" + filter + "]");
        }

        if (isMobileUserName && userName.matches("[0-9]+")) {
            super.clearUserRolesCache(userName);
            DBObject userObj = this.getUserObjectByMobile(userName);
            if (userObj != null) {
                userName = userObj.get("UM_USER_NAME").toString();
            }
        }

        return super.doGetInternalRoleListOfUser(userName, filter);
    }

    @Override
    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("getRoleListOfUser(String userName) [" + userName + "]");
        }

        if (isMobileUserName && userName.matches("[0-9]+")) {
            super.clearUserRolesCache(userName);
            DBObject userObj = this.getUserObjectByMobile(userName);
            if (userObj != null) {
                userName = userObj.get("UM_USER_NAME").toString();
            }
        }

        return super.getRoleListOfUser(userName);
    }

}
