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

// Function to handle auto-increment IDs
db.system.js.save(
    {
        _id: "getNextSequence",
        value : function(name) {
            var ret = db.COUNTERS.findAndModify(
                {
                    query: { _id: name },
                    update: { $inc: { seq: NumberLong("1") } },
                    new: true
                }
            );
            return NumberLong(ret.seq);
        }
    }
);

// Above function will be stored in the system.js script
// We need to load the server scripts before using it
db.loadServerScripts();


// #####################################
//  USER MANAGEMENT RELATED COLLECTIONS
// #####################################

db.COUNTERS.insert({

    _id: "UM_TENANT",

    seq: NumberLong("0")

});
db.UM_TENANT.createIndex({UM_ID: 16},{unique: true});
db.UM_TENANT.createIndex({UM_DOMAIN_NAME: 5},{unique: true});


db.COUNTERS.insert({

    _id: "UM_DOMAIN",

    seq: NumberLong("0")

});
db.UM_DOMAIN.createIndex({UM_DOMAIN_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_USER",

    seq: NumberLong("0")

});
db.UM_USER.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_USER.createIndex({UM_USER_NAME: 5,UM_TENANT_ID: 16},{unique: true});
db.UM_USER.createIndex({UM_CASE_INSENSITIVE_USER_NAME: 5,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_SYSTEM_USER",

    seq: NumberLong("0")

});
db.UM_SYSTEM_USER.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_SYSTEM_USER.createIndex({UM_USER_NAME: 5,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_ROLE",

    seq: NumberLong("0")

});
db.UM_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_ROLE.createIndex({UM_ROLE_NAME: 5,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_MODULE",

    seq: NumberLong("0")

});
db.UM_MODULE.createIndex({UM_ID: 16},{unique: true});
db.UM_MODULE.createIndex({UM_MODULE_NAME: 5},{unique: true});


db.UM_MODULE_ACTIONS.createIndex({UM_ACTION: 5,UM_MODULE_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_PERMISSION",

    seq: NumberLong("0")

});
db.UM_PERMISSION.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_PERMISSION.createIndex({UM_RESOURCE_ID: 5,UM_ACTION: 5,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_ROLE_PERMISSION",

    seq: NumberLong("0")

});
db.UM_ROLE_PERMISSION.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_ROLE_PERMISSION.createIndex({UM_PERMISSION_ID: 16,UM_ROLE_NAME: 5,UM_TENANT_ID: 16,UM_DOMAIN_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_USER_PERMISSION",

    seq: NumberLong("0")

});
db.UM_USER_PERMISSION.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_USER_ROLE",

    seq: NumberLong("0")

});
db.UM_USER_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_USER_ROLE.createIndex({UM_USER_ID: 16,UM_ROLE_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.UM_SHARED_USER_ROLE.createIndex({UM_USER_ID: 16,UM_ROLE_ID: 16,UM_USER_TENANT_ID: 16,UM_ROLE_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_ACCOUNT_MAPPING",

    seq: NumberLong("0")

});
db.UM_ACCOUNT_MAPPING.createIndex({UM_ID: 16},{unique: true});
db.UM_ACCOUNT_MAPPING.createIndex({UM_USER_NAME: 5,UM_TENANT_ID: 16,UM_USER_STORE_DOMAIN: 5,UM_ACC_LINK_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_USER_ATTRIBUTE",

    seq: NumberLong("0")

});
db.UM_USER_ATTRIBUTE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_USER_ATTRIBUTE.createIndex({UM_USER_ID: 16});
db.UM_USER_ATTRIBUTE.createIndex({mobile: 16,UM_TENANT_ID: 16,UM_PROFILE_ID: 16});
db.UM_USER_ATTRIBUTE.createIndex({scimId: 16,UM_TENANT_ID: 16,UM_PROFILE_ID: 16});
db.UM_USER_ATTRIBUTE.createIndex({uid: 16,UM_TENANT_ID: 16,UM_PROFILE_ID: 16},{unique: true});
db.UM_USER_ATTRIBUTE.createIndex({caseInsensitiveUid: 16,UM_TENANT_ID: 16,UM_PROFILE_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_DIALECT",

    seq: NumberLong("0")

});
db.UM_DIALECT.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_DIALECT.createIndex({UM_DIALECT_URI: 5,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_CLAIM",

    seq: NumberLong("0")

});
db.UM_CLAIM.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_CLAIM.createIndex({UM_CLAIM_URI: 5,UM_DIALECT_ID: 16,UM_TENANT_ID: 16,UM_MAPPED_ATTRIBUTE_DOMAIN: 5},{unique: true});


db.COUNTERS.insert({

    _id: "UM_PROFILE_CONFIG",

    seq: NumberLong("0")

});
db.UM_PROFILE_CONFIG.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_CLAIM_BEHAVIOR",

    seq: NumberLong("0")

});
db.UM_CLAIM_BEHAVIOR.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_HYBRID_ROLE",

    seq: NumberLong("0")

});
db.UM_HYBRID_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_HYBRID_USER_ROLE",

    seq: NumberLong("0")

});
db.UM_HYBRID_USER_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_HYBRID_USER_ROLE.createIndex({UM_USER_NAME: 5,UM_ROLE_ID: 16,UM_TENANT_ID: 16,UM_DOMAIN_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_SYSTEM_ROLE",

    seq: NumberLong("0")

});
db.UM_SYSTEM_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_SYSTEM_ROLE.createIndex({UM_ROLE_NAME: 5,UM_TENANT_ID: 16});


db.COUNTERS.insert({

    _id: "UM_SYSTEM_USER_ROLE",

    seq: NumberLong("0")

});
db.UM_SYSTEM_USER_ROLE.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
db.UM_SYSTEM_USER_ROLE.createIndex({UM_USER_NAME: 5,UM_ROLE_ID: 16,UM_TENANT_ID: 16},{unique: true});


db.COUNTERS.insert({

    _id: "UM_HYBRID_REMEMBER_ME",

    seq: NumberLong("0")

});
db.UM_HYBRID_REMEMBER_ME.createIndex({UM_ID: 16,UM_TENANT_ID: 16},{unique: true});
