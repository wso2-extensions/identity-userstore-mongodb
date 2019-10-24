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

/**
 * MongoDBRealmConstants contain all the case sensitive user store queries.
 */
public class MongoDBRealmConstants {

    public static final String SELECT_USER = "SelectUserMONGO_QUERY";
    public static final String SELECT_USER_USE_MOBILE = "SelectUserUseMobileMONGO_QUERY";
    public static final String GET_ROLE_LIST = "GetRoleListMONGO_QUERY";
    public static final String GET_USER_FILTER = "UserFilterMONGO_QUERY";
    public static final String GET_USER_ROLE = "UserRoleMONGO_QUERY";
    public static final String GET_IS_ROLE_EXISTING = "IsRoleExistingMONGO_QUERY";
    public static final String GET_USERS_IN_ROLE = "GetUserListOfRoleMONGO_QUERY";
    public static final String GET_SHARED_ROLE_LIST = "GetSharedRoleListMONGO_QUERY";
    public static final String GET_SHARED_ROLE = "UserSharedRoleMONGO_QUERY";
    public static final String GET_USER_LIST_OF_ROLE = "GetUserListOfRoleMONGO_QUERY";

    public static final String GET_IS_USER_EXISTING = "IsUserExistingMONGO_QUERY";
    public static final String GET_PROPS_FOR_PROFILE = "GetUserPropertiesForProfileMONGO_QUERY";
    public static final String GET_PROPS_FOR_PROFILE_BY_MOBILE = "GetUserPropertiesForProfileByMobileMONGO_QUERY";
    public static final String GET_PROP_FOR_PROFILE = "GetUserPropertyForProfileMONGO_QUERY";
    public static final String GET_PROFILE_NAMES = "GetProfileNamesMONGO_QUERY";
    public static final String GET_PROFILE_NAMES_FOR_USER = "GetUserProfileNamesMONGO_QUERY";
    public static final String GET_USER_ID_FROM_USERNAME = "GetUserIDFromUserNameMONGO_QUERY";
    public static final String GET_USERNAME_FROM_TENANT_ID = "GetUserNameFromTenantIDMONGO_QUERY";
    public static final String GET_TENANT_ID_FROM_USERNAME = "GetTenantIDFromUserNameMONGO_QUERY";
    public static final String GET_USERS_FOR_PROP = "GetUserLisForPropertySQL";

    public static final String ADD_USER = "AddUserMONGO_QUERY";
    public static final String ADD_USER_TO_ROLE = "AddUserToRoleMONGO_QUERY";
    public static final String ADD_USER_PERMISSION = "AddUserPermission";
    public static final String ADD_ROLE = "AddRoleMONGO_QUERY";
    public static final String ADD_ROLE_TO_USER = "AddRoleToUserMONGO_QUERY";
    public static final String ADD_ROLE_PERMISSION = "AddRolePermissionMONGO_QUERY";
    public static final String ADD_SHARED_ROLE_TO_USER = "AddSharedRoleToUserMONGO_QUERY";
    public static final String REMOVE_USER_FROM_ROLE = "RemoveUserFromRoleMONGO_QUERY";
    public static final String REMOVE_ROLE_FROM_USER = "RemoveRoleFromUserMONGO_QUERY";
    public static final String REMOVE_USER_FROM_SHARED_ROLE = "RemoveUserFromSharedRoleMONGO_QUERY";
    public static final String DELETE_ROLE = "DeleteRoleMONGO_QUERY";
    public static final String ON_DELETE_ROLE_REMOVE_USER_ROLE = "OnDeleteRoleRemoveUserRoleMappingMONGO_QUERY";
    public static final String ON_DELETE_ROLE_DELETE_PERMISSION = "OnDeleteRoleRemovePermissionsMONGO_QUERY";
    public static final String DELETE_USER = "DeleteUserMONGO_QUERY";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE = "OnDeleteUserRemoveUserRoleMappingMONGO_QUERY";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE = "OnDeleteUserRemoveUserAttributeMONGO_QUERY";
    public static final String ON_DELETE_USER_DELETE_PERMISSION = "OnDeleteUserRemovePermissionsMONGO_QUERY";

    public static final String UPDATE_USER_PASSWORD = "UpdateUserPasswordMONGO_QUERY";
    public static final String UPDATE_ROLE_NAME = "UpdateRoleNameMONGO_QUERY";
    public static final String ADD_USER_PROPERTY = "AddUserPropertyMONGO_QUERY";
    public static final String UPDATE_USER_PROPERTY = "UpdateUserPropertyMONGO_QUERY";
    public static final String DELETE_USER_PROPERTY = "DeleteUserPropertyMONGO_QUERY";
    public static final String USER_NAME_UNIQUE = "UserNameUniqueAcrossTenantsMONGO_QUERY";
    public static final String IS_DOMAIN_EXISTS = "IsDomainExistingMONGO_QUERY";
    public static final String ADD_DOMAIN = "AddDomainMONGO_QUERY";
    public static final String GET_SHARED_ROLES_FOR_USER = "UserSharedRoleMONGO_QUERY";
    public static final String ADD_SHARED_ROLE = "AddSharedRoleMONGO_QUERY";
    public static final String GET_USERS_IN_SHARED_ROLE = "GetUserListOfSharedRoleMONGO_QUERY";

    public static final String SELECT_USER_MONGO_QUERY =
            "{'collection' : 'UM_USER','UM_USER_NAME' : '?','UM_TENANT_ID' : '?'}";
    public static final String GET_SHARED_ROLE_LIST_MONGO_QUERY = "{'collection' : 'UM_ROLE','UM_ROLE_NAME' : '?'," +
            "'UM_SHARED_ROLE' : '1','projection' : {'UM_ROLE_NAME' : '1','UM_TENANT_ID' : '1','UM_SHARED_ROLE' : '1'}}";
    public static final String GET_ROLE_LIST_MONGO_QUERY = "{'collection' : 'UM_ROLE','UM_TENANT_ID' : '?'," +
            "'UM_ROLE_NAME' : '?','UM_SHARED_ROLE' : '0','projection': {'UM_ROLE_NAME' : '1','UM_TENANT_ID' : 1," +
            "'UM_SHARED_ROLE' : 1,'_id' : '0'}}";
    public static final String GET_USER_FILTER_MONGO_QUERY = "{'collection' : 'UM_USER','$match' : " +
            "{'UM_USER_NAME' : '?','UM_TENANT_ID' : '?'},'$sort' : {'UM_USER_NAME' : 1}, '$limit' : '?'}";
    public static final String GET_USER_ROLE_MONGO_QUERY = "{'collection' : 'UM_ROLE',$match : {'UM_TENANT_ID' : '?'," +
            "'userRole.UM_TENANT_ID' : '?','userRole.UM_USER_ID' : '?'},'$project' : " +
            "{'UM_ROLE_NAME' : 1,'_id' : 0},'$lookup' : {'from' : 'UM_USER_ROLE','localField' : 'UM_ID'," +
            "'foreignField' : 'UM_ROLE_ID','as' : 'userRole'},'$unwind' : {'path' : '$userRole'," +
            "'preserveNullAndEmptyArrays' : false}}";
    public static final String GET_IS_ROLE_EXISTING_MONGO_QUERY =
            "{'collection' : 'UM_ROLE','UM_ROLE_NAME' : '?','UM_TENANT_ID' : '?'}";
    public static final String GET_USERS_IN_ROLE_MONGO_QUERY = "{'collection' : 'UM_USER',$match : " +
            "{'UM_TENANT_ID' : '?','role.UM_ROLE_NAME' : '?','role.UM_TENANT_ID' : '?'," +
            "'userRole.UM_TENANT_ID' : '?'},'$project' : {'UM_USER_NAME' : 1,'_id' : 0},'$lookup' : " +
            "{'from' : 'UM_USER_ROLE','localField' : 'UM_ID','foreignField' : 'UM_USER_ID','as' : 'userRole'}," +
            "'$lookup_sub' : {'from' : 'UM_ROLE','localField' : 'userRole.UM_ROLE_ID','foreignField' : 'UM_ID'," +
            "'as' : 'role','dependency' : 'userRole'},'$unwind' : {'path' : '$userRole','preserveNullAndEmptyArrays'" +
            " : false},'$unwind_sub' : {'path' : '$role','preserveNullAndEmptyArrays' : false}}";
    public static final String GET_IS_USER_EXISTING_MONGO_QUERY = "{'collection' : 'UM_USER','UM_USER_NAME' : '?'," +
            "'UM_TENANT_ID' : '?','projection' : {'UM_ID' : '1','_id' : '0'}}";
    public static final String GET_PROPS_FOR_PROFILE_MONGO_QUERY1 = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'$match' : {'UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?','users.UM_USER_NAME' : '?'," +
            "'users.UM_TENANT_ID' : '?'},'$lookup' : {'from' : 'UM_USER','localField' : 'UM_USER_ID'," +
            "'foreignField' : 'UM_ID','as' : 'users'},'$unwind' : {'path' : '$users'," +
            "'preserveNullAndEmptyArrays' : false}}";
    public static final String GET_PROPS_FOR_PROFILE_MONGO_QUERY = "{'collection' : 'UM_USER'," +
            "'$match' : {'attrs.UM_PROFILE_ID' : '?','attrs.UM_TENANT_ID' : '?','UM_CASE_INSENSITIVE_USER_NAME' : '?'," +
            "'UM_TENANT_ID' : '?'},'$lookup' : {'from' : 'UM_USER_ATTRIBUTE','localField' : 'UM_ID'," +
            "'foreignField' : 'UM_USER_ID','as' : 'attrs'},'$unwind' : {'path' : '$attrs'," +
            "'preserveNullAndEmptyArrays' : false}}";
    public static final String GET_PROPS_FOR_PROFILE_BY_MOBILE_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE','mobile' : '?','UM_TENANT_ID' : '?'}";
    public static final String GET_PROP_FOR_PROFILE_MONGO_QUERY =
            "{'collection' : 'UM_USER_ATTRIBUTE','UM_USER_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String GET_PROFILE_NAMES_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_TENANT_ID' : '?','projection' : {'UM_PROFILE_ID' : '1'},'distinct' : 'UM_PROFILE_ID'}";
    public static final String GET_PROFILE_NAMES_FOR_USER_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','projection' : {'UM_PROFILE_ID' : 1,_id : 0},'distinct' : 'UM_PROFILE_ID'}";
    public static final String GET_PROFILE_NAMES_FOR_USER_MONGO_QUERY_CONDITION = "{'collection' : 'UM_USER'," +
            "'UM_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'UM_ID' : '1'}}";
    public static final String GET_USER_ID_FROM_USERNAME_MONGO_QUERY_WITHOUT_MOBILE =
            "{'collection' : 'UM_USER','UM_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'UM_ID' : 1}}";
    public static final String GET_USER_ID_FROM_USERNAME_MONGO_QUERY =
            "{'collection' : 'UM_USER_ATTRIBUTE', $or : [{'uid' : '?'}, {'mobile' : '?'}],'UM_TENANT_ID' : '?','projection' : {'UM_USER_ID' : 1}}";
    public static final String GET_USERNAME_FROM_TENANT_ID_MONGO_QUERY =
            "{'collection' : 'UM_USER','UM_TENANT_ID' : '?','projection' : {'UM_USER_NAME' : '1'}}";
    public static final String GET_TENANT_ID_FROM_USERNAME_MONGO_QUERY =
            "{'collection' : 'UM_USER','UM_USER_NAME' : '?','projection' : {'UM_TENANT_ID' : '1','_id' : '0'}}";
    public static final String GET_SHARED_ROLE_MONGO_QUERY = "{'collection' : 'UM_SHARED_USER_ROLE'," +
            "'user.UM_USER_NAME' : '?','UM_USER_TENANT_ID' : 'role.UM_TENANT_ID','UM_USER_TENANT_ID' : '?'," +
            "'$lookup' : [{'from' : 'UM_USER','localField' : 'UM_USER_ID','foreignField' : 'UM_ID','as' : 'user'}," +
            "{'from' : 'UM_ROLE','localField' : 'UM_ROLE_ID','foreignField' : 'UM_ID','as' : 'role'}]}";
    public static final String GET_USER_LIST_OF_ROLE_MONGO_QUERY = "{'collection' : 'UM_USER'," +
            "'attribute.UM_ATTR_NAME' : '?','attribute.UM_ATTR_VALUE' : '?','attribute.UM_ATTR_NAME' : '?'," +
            "'attribute.UM_PROFILE_ID' : '?','attribute.UM_TENANT_ID' : '?','user.UM_TENANT_ID' : '?','$lookup' : " +
            "{'from' : 'UM_USER_ATTRIBUTE','localField' : 'UM_ID','foreignField' : 'UM_USER_ID','as' : 'attribute'}," +
            "'projection' : {'UM_USER_NAME' : '1'}}";
//    public static final String GET_USERS_FOR_PROP_MONGO_QUERY = "{'collection' : 'UM_USER','$match' : " +
//            "{'UM_TENANT_ID' : ?,<INSERT_STATEMENT>," +
//            "'attribute.UM_PROFILE_ID' : '?'},'$lookup' : {'from' : 'UM_USER_ATTRIBUTE','localField' : 'UM_ID'," +
//            "'foreignField' : 'UM_USER_ID','as' : 'attribute'},'$unwind' : {'path' : '$attribute'," +
//            "'preserveNullAndEmptyArrays' : false},'$project' : {'UM_USER_NAME' : 1}}";
    public static final String GET_USERS_FOR_PROP_MONGO_QUERY = "{ 'collection': 'UM_USER_ATTRIBUTE', <INSERT_STATEMENT>, 'UM_PROFILE_ID': '?', 'UM_TENANT_ID': '?', '$project': {'uid': 1}}";
    public static final String GET_USERS_IN_SHARED_ROLE_MONGO_QUERY = "{'collection' : 'UM_SHARED_USER_ROLE'," +
            "'$match' : {'UM_ROLE_NAME' : '?','UM_USER_TENANT_ID' : 'user.UM_TENANT_ID'," +
            "'UM_ROLE_TENANT_ID' : 'role.UM_TENANT_ID'},'$lookup' : [{'from' : 'UM_USER','localField' : 'UM_USER_ID'," +
            "'foreignField' : 'UM_ID','as' : 'users'},{'from' : 'UM_ROLE','localField' : 'UM_ROLE_ID'," +
            "'foreignField' : 'UM_ID','as' : 'role'}],'$project' : {'UM_USER_NAME' : 1}}";
    public static final String ADD_USER_MONGO_QUERY = "{'collection' : 'UM_USER','UM_USER_NAME' : '?'," +
            "'UM_USER_PASSWORD' : '?','UM_SALT_VALUE' : '?','UM_REQUIRE_CHANGE' : '?','UM_CHANGED_TIME' : '?'," +
            "'UM_TENANT_ID' : '?','UM_ID' : '?', 'UM_CASE_INSENSITIVE_USER_NAME' : '?'}";
    public static final String ADD_USER_TO_ROLE_MONGO_QUERY = "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?'," +
            "'UM_ROLE_ID' : '?','UM_TENANT_ID' : '?','UM_ID' : '?'}";
    public static final String ADD_USER_TO_ROLE_MONGO_QUERY_CONDITION1 =
            "{'collection' : 'UM_USER','UM_USER_NAME' : '?','UM_TENANT_ID' : '?','projection' : {'UM_ID' : '1'}}";
    public static final String ADD_USER_PERMISSION_MONGO_QUERY = "AddUserPermission";
    public static final String ADD_ROLE_MONGO_QUERY =
            "{'collection' : 'UM_ROLE','UM_ROLE_NAME' : '?','UM_TENANT_ID' : '?','UM_ID' : '?','UM_SHARED_ROLE' : '?'}";
    public static final String ADD_ROLE_TO_USER_MONGO_QUERY =
            "{'collection' : 'UM_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_TENANT_ID' : '?','UM_ID' : '?'}";
    public static final String ADD_ROLE_PERMISSION_MONGO_QUERY = "AddRolePermissionMONGO_QUERY";
    public static final String REMOVE_USER_FROM_ROLE_MONGO_QUERY =
            "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?','UM_ROLE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String REMOVE_ROLE_FROM_USER_MONGO_QUERY =
            "{'collection' : 'UM_USER_ROLE','UM_ROLE_ID' : '?','UM_USER_ID': '?','UM_TENANT_ID' : '?'}";
    public static final String DELETE_ROLE_MONGO_QUERY =
            "{'collection' : 'UM_ROLE','UM_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_ROLE_REMOVE_USER_ROLE_MONGO_QUERY =
            "{'collection' : 'UM_USER_ROLE','UM_ROLE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_ROLE_DELETE_PERMISSION_MONGO_QUERY =
            "OnDeleteRoleRemovePermissionsMONGO_QUERY";
    public static final String DELETE_USER_MONGO_QUERY =
            "{'collection' : 'UM_USER','UM_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE_MONGO_QUERY =
            "{'collection' : 'UM_USER_ROLE','UM_USER_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE_MONGO_QUERY =
            "{'collection' : 'UM_USER_ATTRIBUTE','UM_USER_ID' : '?',UM_TENANT_ID : '?'}";
    public static final String ON_DELETE_USER_DELETE_PERMISSION_MONGO_QUERY =
            "OnDeleteUserRemovePermissionsMONGO_QUERY";
    public static final String ADD_SHARED_ROLE_TO_USER_MONGO_QUERY = "{'collection' : 'UM_SHARED_USER_ROLE'," +
            "'UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_USER_TENANT_ID' : '?','UM_ROLE_TENANT_ID' : '?'}";
    public static final String REMOVE_USER_FROM_SHARED_ROLE_MONGO_QUERY = "{'collection' : 'UM_SHARED_USER_ROLE'," +
            "'UM_ROLE_ID' : '?','UM_USER_ID' : '?','UM_USER_TENANT_ID' : '?','UM_ROLE_TENANT_ID' : '?'}";
    public static final String ADD_SHARED_ROLE_MONGO_QUERY = "{'collection' : 'UM_ROLE','UM_ROLE_NAME' : '?'," +
            "'UM_TENANT_ID' : '?','projection' : {'$set' : {'UM_SHARED_ROLE' : '?'}}}";

    public static final String UPDATE_USER_PASSWORD_MONGO_QUERY = "{'collection' : 'UM_USER','UM_USER_NAME' : '?'," +
            "'UM_TENANT_ID' : '?','projection' : {'$set'  : {'UM_USER_PASSWORD' : '?','UM_SALT_VALUE' : '?'," +
            "'UM_REQUIRE_CHANGE' : '?','UM_CHANGED_TIME' : '?'}}}";
    public static final String UPDATE_ROLE_NAME_MONGO_QUERY = "{'collection' : 'UM_ROLE','UM_ID' : '?'," +
            "'UM_TENANT_ID' : '?','projection' : {'$set' : {'UM_ROLE_NAME' : '?'}}}";

    public static final String ADD_USER_PROPERTY_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','UM_ATTR_NAME' : '?','UM_ATTR_VALUE' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String UPDATE_USER_PROPERTY_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','UM_ATTR_NAME' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?','projection' : " +
            "{$set' : '{<ATTR_TO_UPDATE>}}}";
//    public static final String DELETE_USER_PROPERTY_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
//            "'UM_USER_ID' : '?','UM_ATTR_NAME' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?'}";
    public static final String DELETE_USER_PROPERTY_MONGO_QUERY = "{'collection' : 'UM_USER_ATTRIBUTE'," +
            "'UM_USER_ID' : '?','UM_PROFILE_ID' : '?','UM_TENANT_ID' : '?', 'projection' : {'$unset' : {<ATTR_TO_REMOVE>}}}";
    public static final String USER_NAME_UNIQUE_MONGO_QUERY =
            "{'collection' : 'UM_USER','UM_USER_NAME' : '?','projection' : {'UM_ID' : '1','_id' : '0'}}";
    public static final String IS_DOMAIN_EXISTS_MONGO_QUERY = "{'collection' : 'UM_DOMAIN','UM_DOMAIN_NAME' : '?'," +
            "'UM_TENANT_ID' : '?','projection' : {'UM_DOMAIN_ID' : '1'}}";
    public static final String ADD_DOMAIN_MONGO_QUERY =
            "{'collection' : 'UM_DOMAIN','UM_DOMAIN_NAME' : '?','UM_TENANT_ID' : '?','UM_DOMAIN_ID' : '?'}";
    public static final String GET_SHARED_ROLES_FOR_USER_MONGO_QUERY = "{'collection' : 'UM_SHARED_USER_ROLE'," +
            "'user.UM_USER_NAME' : '?','UM_USER_TENANT_ID' : '?','UM_USER_TENANT_ID' : 'user.UM_TENANT_ID'," +
            "'UM_ROLE_TENANT_ID' : 'role.UM_TENANT_ID','$lookup' : [{'from' : 'UM_USER','localField' : 'UM_USER_ID'," +
            "'foreignField' : 'UM_ID','as' : 'user'},{'from' : 'UM_ROLE','localField' : 'UM_ROLE_ID'," +
            "'foreignField' : 'UM_ID','as' : 'roles'}]}";
    public static final String SELECT_USER_USE_MOBILE_MONGO_QUERY = "{'collection':'UM_USER_ATTRIBUTE','$lookup':{'from':'UM_USER','localField':'UM_USER_ID','foreignField':'UM_ID','as':'UM_USER'}, '$match':{'mobile':'?'}}";

    public static final String DIGEST_FUNCTION = "PasswordDigest";
    public static final String STORE_SALTED_PASSWORDS = "StoreSaltedPassword";

    // Properties
    public static final String URL = "ConnectionURL";
    public static final String USERNAME = "ConnectionName";
    public static final String PASSWORD = "ConnectionPassword";
    public static final String IS_MOBILE_USERNAME = "IsMobileUserName";
}
