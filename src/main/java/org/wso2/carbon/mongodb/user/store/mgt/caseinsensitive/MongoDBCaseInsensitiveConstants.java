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

package org.wso2.carbon.mongodb.user.store.mgt.caseinsensitive;

/**
 * Case insensitive MongoDB queries.
 */
public class MongoDBCaseInsensitiveConstants {

    public static final String SELECT_USER_CASE_INSENSITIVE = "SelectUserMongoCaseInsensitive";
    public static final String GET_USER_FILTER_CASE_INSENSITIVE = "UserFilterMongoCaseInsensitive";
    public static final String GET_USER_ROLE_CASE_INSENSITIVE = "UserRoleMongoCaseInsensitive";
    public static final String GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE = "UserSharedRoleMongoCaseInsensitive";
    public static final String GET_IS_USER_EXISTING_CASE_INSENSITIVE = "IsUserExistingMongoCaseInsensitive";
    public static final String GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE =
            "GetUserPropertiesForProfileMongoCaseInsensitive";
    public static final String GET_PROP_FOR_PROFILE_CASE_INSENSITIVE = "GetUserPropertyForProfileMongoCaseInsensitive";
    public static final String GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE = "GetUserProfileNamesMongoCaseInsensitive";
    public static final String GET_USER_ID_FROM_USERNAME_CASE_INSENSITIVE = "GetUserIDFromUserNameMongoCaseInsensitive";
    public static final String GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE =
            "GetTenantIDFromUserNameMongoCaseInsensitive";
    public static final String ADD_USER_TO_ROLE_CASE_INSENSITIVE = "AddUserToRoleMongoCaseInsensitive";
    public static final String ADD_ROLE_TO_USER_CASE_INSENSITIVE = "AddRoleToUserMongoCaseInsensitive";
    public static final String ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE = "AddSharedRoleToUserMongoCaseInsensitive";
    public static final String REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE = "RemoveUserFromRoleMongoCaseInsensitive";
    public static final String REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE =
            "RemoveUserFromSharedRoleMongoCaseInsensitive";
    public static final String REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE = "RemoveRoleFromUserMongoCaseInsensitive";
    public static final String DELETE_USER_CASE_INSENSITIVE = "DeleteUserMongoCaseInsensitive";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE_CASE_INSENSITIVE =
            "OnDeleteUserRemoveUserRoleMappingMongoCaseInsensitive";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE =
            "OnDeleteUserRemoveUserAttributeMongoCaseInsensitive";
    public static final String UPDATE_USER_PASSWORD_CASE_INSENSITIVE = "UpdateUserPasswordMongoCaseInsensitive";
    public static final String UPDATE_USER_PROPERTY_CASE_INSENSITIVE = "UpdateUserPropertyMongoCaseInsensitive";
    public static final String DELETE_USER_PROPERTY_CASE_INSENSITIVE = "DeleteUserPropertyMongoCaseInsensitive";
    public static final String USER_NAME_UNIQUE_CASE_INSENSITIVE = "UserNameUniqueAcrossTenantsMongoCaseInsensitive";
    public static final String SELECT_USER_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER','UM_CASE_INSENSITIVE_USER_NAME' : '?','UM_TENANT_ID' : '?'}";
    public static final String GET_USER_FILTER_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER','$match' : " +
            "{'UM_CASE_INSENSITIVE_USER_NAME' : '?','UM_TENANT_ID' : '?'},'$sort' : {'UM_USER_NAME' : 1}, '$limit' : '?'}";
    public static final String GET_USER_ROLE_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_ROLE',$match : " +
            "{'UM_TENANT_ID' : '?','userRole.UM_TENANT_ID' : '?','users.UM_TENANT_ID' : '?','users.UM_ID' : '?'}," +
            "'$project' : {'UM_ROLE_NAME' : 1,'_id' : 0},'$lookup' : {'from' : 'UM_USER_ROLE','localField' : 'UM_ID'," +
            "'foreignField' : 'UM_ROLE_ID','as' : 'userRole'},'$unwind' : {'path' : '$userRole'," +
            "'preserveNullAndEmptyArrays' : false},'$lookup_sub' : {'from' : 'UM_USER','localField' : " +
            "'userRole.UM_USER_ID','foreignField' : 'UM_ID','as' : 'users','dependency' : 'userRole'},'$unwind_sub' :" +
            " {'path' : '$users','preserveNullAndEmptyArrays' : false}}";
    public static final String GET_SHARED_ROLES_FOR_USER_MONGO_CASE_INSENSITIVE = "{'collection' : " +
            "'UM_SHARED_USER_ROLE',$match :{'user.UM_USER_NAME' : {'$regex' : '?','$options' : 'i'}," +
            "'UM_USER_TENANT_ID' : '?','UM_USER_TENANT_ID' : 'user.UM_TENANT_ID','UM_ROLE_TENANT_ID' : " +
            "'role.UM_TENANT_ID'},'$lookup' : {'from' : 'UM_USER','localField' : 'UM_USER_ID','foreignField' : " +
            "'UM_ID','as' : 'user'},'$unwind' : {'path' : '$user','preserveNullAndEmptyArrays' : false}," +
            "'$lookup_sub' : {'from' : 'UM_ROLE','localField' : 'UM_ROLE_ID','foreignField' : 'UM_ID','as' : " +
            "'roles'},'$unwind_sub' : {'path' : '$roles','preserveNullAndEmptyArrays' : false}}";
    public static final String GET_IS_USER_EXISTING_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER'," +
            "'UM_CASE_INSENSITIVE_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'UM_ID' : 1," +
            "'_id' : 0}}";
    public static final String GET_PROPS_FOR_PROFILE_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'$match' : {'UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?','users.UM_USER_NAME' : {'$regex' : '?'," +
            "'$options' : 'i'},'users.UM_TENANT_ID' : '?'},'$lookup' : {'from' : 'UM_USER','localField' : " +
            "'UM_USER_ID','foreignField' : 'UM_ID','as' : 'users'},'$unwind' : " +
            "{'path' : '$users','preserveNullAndEmptyArrays' : false}}";
    public static final String GET_PROP_FOR_PROFILE_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'$match' : {'user.UM_USER_NAME' : {'$regex' : '?','$options' : 'i'},'UM_ATTR_NAME' : '?'," +
            "'UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?','user.UM_TENANT_ID' : '?'},'$lookup' : {'from' : 'UM_USER'," +
            "'localField' : 'UM_USER_ID','foreignField' : 'UM_ID','as' : 'user'},'$unwind' : {'path' : '$user'," +
            "'preserveNullAndEmptyArrays' : false},$project : {'UM_ATTR_VALUE' : 1,'_id' : 0}}";
    public static final String GET_PROFILE_NAMES_FOR_USER_MONGO_CASE_INSENSITIVE = "{'collection' : " +
            "'UM_USER_ATTRIBUTE','UM_USER_ID' : '?','projection' : {'UM_PROFILE_ID' : 1,_id : 0},'distinct' : " +
            "'UM_PROFILE_ID'}";
    public static final String GET_USER_ID_FROM_USERNAME_WITHOUT_MOBILE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER','UM_CASE_INSENSITIVE_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'UM_ID' : 1}}";
    public static final String GET_USER_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ATTRIBUTE', $or : [{'caseInsensitiveUid' : '?'}, {'mobile' : '?'}],'UM_TENANT_ID' : '?','projection' : {'UM_USER_ID' : 1}}";
    public static final String GET_TENANT_ID_FROM_USERNAME_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER'," +
            "'UM_USER_NAME' : {'$regex' : '?','$option' : 'i'},'projection' : {'UM_TENANT_ID' : 1,_id : 0}}";
    public static final String ADD_USER_TO_ROLE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?','UM_ROLE_ID' : '?','UM_TENANT_ID' : '?','UM_ID' : '?'}";
    public static final String ADD_ROLE_TO_USER_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_TENANT_ID' : '?','UM_ID' : '?'}";
    public static final String ADD_SHARED_ROLE_TO_USER_MONGO_CASE_INSENSITIVE = "{'collection' : " +
            "'UM_SHARED_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_USER_TENANT_ID' : '?'," +
            "'UM_ROLE_TENANT_ID' : '?'}";
    public static final String REMOVE_USER_FROM_SHARED_ROLE_MONGO_CASE_INSENSITIVE = "{'collection' : " +
            "'UM_SHARED_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_USER_TENANT_ID' : '?'," +
            "'UM_ROLE_TENANT_ID' : '?'}";
    public static final String REMOVE_USER_FROM_ROLE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?','UM_ROLE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String REMOVE_ROLE_FROM_USER_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID': '?','UM_TENANT_ID' : '?'}";
    public static final String DELETE_USER_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER','UM_USER_NAME' : {'$regex' : '?','$options' : 'i'},'UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER_ATTRIBUTE','UM_USER_ID' : '?',UM_TENANT_ID : '?'}";
    public static final String UPDATE_USER_PASSWORD_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER'," +
            "'UM_CASE_INSENSITIVE_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'$set'  : " +
            "{'UM_USER_PASSWORD' : '?','UM_SALT_VALUE' : '?','UM_REQUIRE_CHANGE' : '?','UM_CHANGED_TIME' : '?'}}}";
    public static final String UPDATE_USER_PROPERTY_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','UM_ATTR_NAME' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?','projection' : " +
            "{$set' : '{<ATTR_TO_UPDATE>}}}";
    public static final String DELETE_USER_PROPERTY_MONGO_CASE_INSENSITIVE = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','UM_ATTR_NAME' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String USER_NAME_UNIQUE_MONGO_CASE_INSENSITIVE =
            "{'collection' : 'UM_USER','UM_USER_NAME' : {'$regex' : '?','$options' : 'i'}}";

    public static final String CASE_SENSITIVE_USERNAME = "CaseInsensitiveUsername";
    public static final String CASE_SENSITIVE_USERNAME_DESCRIPTION = "Whether the username is case sensitive or not";


    /**
     * Private constructor to prevent other classes from creating objects of this class.
     */
    private MongoDBCaseInsensitiveConstants() {
    }

}
