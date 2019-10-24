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

import java.util.Map;

import org.wso2.carbon.mongodb.user.store.mgt.MongoDBRealmConstants;
import org.wso2.carbon.mongodb.user.store.mgt.caseinsensitive.MongoDBCaseInsensitiveConstants;

/**
 * Map default MongoDB User store queries if they are not configured in user-mgt.xml.
 */
public class MongoDBRealmUtil {

    /**
     * Get MongoDB user store properties (Sets default properties if already not available).
     *
     * @param properties map with user store properties
     * @return map with default properties added
     */
    public static Map<String, String> getMongoProperties(Map<String, String> properties) {

        // Realm properties
        if (!properties.containsKey(MongoDBRealmConstants.SELECT_USER)) {
            properties.put(MongoDBRealmConstants.SELECT_USER, MongoDBRealmConstants.SELECT_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.SELECT_USER_USE_MOBILE)) {
            properties.put(MongoDBRealmConstants.SELECT_USER_USE_MOBILE, MongoDBRealmConstants.SELECT_USER_USE_MOBILE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_ROLE_LIST)) {
            properties.put(MongoDBRealmConstants.GET_ROLE_LIST, MongoDBRealmConstants.GET_ROLE_LIST_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USER_ROLE)) {
            properties.put(MongoDBRealmConstants.GET_USER_ROLE, MongoDBRealmConstants.GET_USER_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USER_FILTER)) {
            properties.put(MongoDBRealmConstants.GET_USER_FILTER,
                    MongoDBRealmConstants.GET_USER_FILTER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_IS_ROLE_EXISTING)) {
            properties.put(MongoDBRealmConstants.GET_IS_ROLE_EXISTING,
                    MongoDBRealmConstants.GET_IS_ROLE_EXISTING_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USERS_IN_ROLE)) {
            properties.put(MongoDBRealmConstants.GET_USERS_IN_ROLE,
                    MongoDBRealmConstants.GET_USERS_IN_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_IS_USER_EXISTING)) {
            properties.put(MongoDBRealmConstants.GET_IS_USER_EXISTING,
                    MongoDBRealmConstants.GET_IS_USER_EXISTING_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE)) {
            properties.put(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE,
                    MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_BY_MOBILE)) {
            properties.put(MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_BY_MOBILE,
                    MongoDBRealmConstants.GET_PROPS_FOR_PROFILE_BY_MOBILE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_PROP_FOR_PROFILE)) {
            properties.put(MongoDBRealmConstants.GET_PROP_FOR_PROFILE,
                    MongoDBRealmConstants.GET_PROP_FOR_PROFILE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_PROFILE_NAMES)) {
            properties.put(MongoDBRealmConstants.GET_PROFILE_NAMES,
                    MongoDBRealmConstants.GET_PROFILE_NAMES_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_PROFILE_NAMES_FOR_USER)) {
            properties.put(MongoDBRealmConstants.GET_PROFILE_NAMES_FOR_USER,
                    MongoDBRealmConstants.GET_PROFILE_NAMES_FOR_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME)) {
            properties.put(MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME,
                    MongoDBRealmConstants.GET_USER_ID_FROM_USERNAME_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_TENANT_ID_FROM_USERNAME)) {
            properties.put(MongoDBRealmConstants.GET_TENANT_ID_FROM_USERNAME,
                    MongoDBRealmConstants.GET_TENANT_ID_FROM_USERNAME_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_USER)) {
            properties.put(MongoDBRealmConstants.ADD_USER, MongoDBRealmConstants.ADD_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_USER_TO_ROLE)) {
            properties.put(MongoDBRealmConstants.ADD_USER_TO_ROLE,
                    MongoDBRealmConstants.ADD_USER_TO_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_USER_PERMISSION)) {
            properties.put(MongoDBRealmConstants.ADD_USER_PERMISSION,
                    MongoDBRealmConstants.ADD_USER_PERMISSION_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_ROLE)) {
            properties.put(MongoDBRealmConstants.ADD_ROLE, MongoDBRealmConstants.ADD_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_ROLE_TO_USER)) {
            properties.put(MongoDBRealmConstants.ADD_ROLE_TO_USER,
                    MongoDBRealmConstants.ADD_ROLE_TO_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_ROLE_PERMISSION)) {
            properties.put(MongoDBRealmConstants.ADD_ROLE_PERMISSION,
                    MongoDBRealmConstants.ADD_ROLE_PERMISSION_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.REMOVE_USER_FROM_ROLE)) {
            properties.put(MongoDBRealmConstants.REMOVE_USER_FROM_ROLE,
                    MongoDBRealmConstants.REMOVE_USER_FROM_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.REMOVE_ROLE_FROM_USER)) {
            properties.put(MongoDBRealmConstants.REMOVE_ROLE_FROM_USER,
                    MongoDBRealmConstants.REMOVE_ROLE_FROM_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.DELETE_ROLE)) {
            properties.put(MongoDBRealmConstants.DELETE_ROLE, MongoDBRealmConstants.DELETE_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE)) {
            properties.put(MongoDBRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE,
                    MongoDBRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION)) {
            properties.put(MongoDBRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION,
                    MongoDBRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.DELETE_USER)) {
            properties.put(MongoDBRealmConstants.DELETE_USER, MongoDBRealmConstants.DELETE_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE)) {
            properties.put(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE,
                    MongoDBRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE)) {
            properties.put(MongoDBRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE,
                    MongoDBRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ON_DELETE_USER_DELETE_PERMISSION)) {
            properties.put(MongoDBRealmConstants.ON_DELETE_USER_DELETE_PERMISSION,
                    MongoDBRealmConstants.ON_DELETE_USER_DELETE_PERMISSION_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.UPDATE_USER_PASSWORD)) {
            properties.put(MongoDBRealmConstants.UPDATE_USER_PASSWORD,
                    MongoDBRealmConstants.UPDATE_USER_PASSWORD_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_USER_PROPERTY)) {
            properties.put(MongoDBRealmConstants.ADD_USER_PROPERTY,
                    MongoDBRealmConstants.ADD_USER_PROPERTY_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.USER_NAME_UNIQUE)) {
            properties.put(MongoDBRealmConstants.USER_NAME_UNIQUE,
                    MongoDBRealmConstants.USER_NAME_UNIQUE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.UPDATE_USER_PROPERTY)) {
            properties.put(MongoDBRealmConstants.UPDATE_USER_PROPERTY,
                    MongoDBRealmConstants.UPDATE_USER_PROPERTY_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.DELETE_USER_PROPERTY)) {
            properties.put(MongoDBRealmConstants.DELETE_USER_PROPERTY,
                    MongoDBRealmConstants.DELETE_USER_PROPERTY_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.UPDATE_ROLE_NAME)) {
            properties.put(MongoDBRealmConstants.UPDATE_ROLE_NAME,
                    MongoDBRealmConstants.UPDATE_ROLE_NAME_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_SHARED_ROLE_LIST)) {
            properties.put(MongoDBRealmConstants.GET_SHARED_ROLE_LIST,
                    MongoDBRealmConstants.GET_SHARED_ROLE_LIST_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_SHARED_ROLE)) {
            properties.put(MongoDBRealmConstants.GET_SHARED_ROLE,
                    MongoDBRealmConstants.GET_SHARED_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USER_LIST_OF_ROLE)) {
            properties.put(MongoDBRealmConstants.GET_USER_LIST_OF_ROLE,
                    MongoDBRealmConstants.GET_USER_LIST_OF_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USERNAME_FROM_TENANT_ID)) {
            properties.put(MongoDBRealmConstants.GET_USERNAME_FROM_TENANT_ID,
                    MongoDBRealmConstants.GET_USERNAME_FROM_TENANT_ID_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER)) {
            properties.put(MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER,
                    MongoDBRealmConstants.ADD_SHARED_ROLE_TO_USER_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.REMOVE_USER_FROM_SHARED_ROLE)) {
            properties.put(MongoDBRealmConstants.REMOVE_USER_FROM_SHARED_ROLE,
                    MongoDBRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.IS_DOMAIN_EXISTS)) {
            properties.put(MongoDBRealmConstants.IS_DOMAIN_EXISTS,
                    MongoDBRealmConstants.IS_DOMAIN_EXISTS_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.ADD_DOMAIN)) {
            properties.put(MongoDBRealmConstants.ADD_DOMAIN,
                    MongoDBRealmConstants.ADD_DOMAIN_MONGO_QUERY);
        }
        if (properties.containsKey(MongoDBRealmConstants.GET_SHARED_ROLES_FOR_USER)) {
            properties.put(MongoDBRealmConstants.GET_SHARED_ROLES_FOR_USER,
                    MongoDBRealmConstants.GET_SHARED_ROLES_FOR_USER_MONGO_QUERY);
        }
        if (properties.containsKey(MongoDBRealmConstants.ADD_SHARED_ROLE)) {
            properties.put(MongoDBRealmConstants.ADD_SHARED_ROLE,
                    MongoDBRealmConstants.ADD_SHARED_ROLE_MONGO_QUERY);
        }
        if (properties.containsKey(MongoDBRealmConstants.GET_USERS_IN_SHARED_ROLE)) {
            properties.put(MongoDBRealmConstants.GET_USERS_IN_SHARED_ROLE,
                    MongoDBRealmConstants.GET_USERS_IN_SHARED_ROLE_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBRealmConstants.GET_USERS_FOR_PROP)) {
            properties.put(MongoDBRealmConstants.GET_USERS_FOR_PROP,
                    MongoDBRealmConstants.GET_USERS_FOR_PROP_MONGO_QUERY);
        }

        // Case insensitive properties
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_IS_USER_EXISTING_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.UPDATE_USER_PASSWORD_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.SELECT_USER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.UPDATE_USER_PROPERTY_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE,
                    MongoDBRealmConstants.DELETE_USER_PROPERTY_MONGO_QUERY);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.ADD_ROLE_TO_USER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_USER_ROLE_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.ADD_USER_TO_ROLE_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_USER_FILTER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(MongoDBCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_CASE_INSENSITIVE)) {
            properties.put(MongoDBCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_CASE_INSENSITIVE,
                    MongoDBCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_MONGO_CASE_INSENSITIVE);
        }
        return properties;
    }
}
