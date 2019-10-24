
# MongoDB User Store Extension for WSO2 IS

## Introduction
This is an extension, which consists of a user store implemented using MongoDB (A NoSQL Database) for WSO2 Product-IS. This MongoDB user store extension can be used as both primary and secondary user store for product-IS. This extension is compatible with IS version 5.5.0.

## Prerequisites
- [MongoDB user store extension](https://github.com/pranavan15/mongodb-user-store-wso2-is/archive/master.zip)
- [WSO2 IS version 5.5.0](https://wso2.com/identity-and-access-management/install)
- [MongoDB](https://www.mongodb.com/download-center?jmp=nav#community)
- [MongoDB-Java-driver](https://oss.sonatype.org/content/repositories/releases/org/mongodb/mongo-java-driver/3.7.0/mongo-java-driver-3.7.0.jar)

## Steps to Configure
1. First, build the `MongoDB user store extension` using maven by executing the following command from the root folder of this extension
```bash
   mvn clean install
```

2. Copy the extension jar file created inside the `target` folder and add it into the `/repository/components/dropins` folder of product-IS

3. Copy the MongoDB-Java-driver jar into the `/repository/components/lib` folder of product-IS

4. start the MongoDB server using the following command
```bash
   sudo service mongod start
```

5. Start a Mongo shell using the below command
```bash
   mongo --host 127.0.0.1:27017
```

6. Create a database named `wso2_carbon_db` by entering the following command in the Mongo shell
```bash
   use wso2_carbon_db
```

7. Create the necessary collections by running the MongoDB script file [user_mgt_collections.js](/dbscripts/user_mgt_collections.js) provided by executing the following command in the Mongo shell
```bash
   load(<PATH_TO_THE_SCRIPT_FILE>)
```

8. Finally, open a terminal, navigate to the `bin` folder of product-IS and start the IS server by executing the following command
```bash
   ./wso2server.sh
```

Now you have successfully added the mongoDB user store extension to the product-IS. You should see MongoDB user store listed along with other user stores using which you can create a MonogDB secondary user store and started using it for your user management operations.


### Configuring MongoDB as the Primary User Store

The above configurations are good enough for you to use the MongoDB as a secondary user store manager. However, in order to use the MongoDB as the primary user store of product-IS you require some additional configurations as follow.

9. After following steps 1-7, prior to start the IS server, add the following in the `user-mgt.xml` file of product-IS. You can find this file inside `/repository/conf` folder. Make sure to replace the `ConnectionName` and `ConnectionPassword` with your credentials for the specified MongoDB database.

##### user-mgt.xml
```xml
  <UserStoreManager class="org.wso2.carbon.mongodb.user.store.mgt.MongoDBUserStoreManager">
      <Property name="TenantManager">org.wso2.carbon.user.core.tenant.JDBCTenantManager</Property>
      <Property name="ConnectionURL">mongodb://localhost/wso2_carbon_db</Property>
      <Property name="ConnectionName">USERNAME</Property>
      <Property name="ConnectionPassword">PASSWORD</Property>
      <Property name="ReadGroups">true</Property>
      <Property name="ReadOnly">false</Property>
      <Property name="IsEmailUserName">false</Property>
      <Property name="IsMobileUserName">true</Property>
      <Property name="DomainCalculation">default</Property>
      <Property name="WriteGroups">true</Property>
      <Property name="UserNameUniqueAcrossTenants">false</Property>
      <Property name="PasswordJavaRegEx">^[\S]{5,30}$</Property>
      <Property name="PasswordJavaScriptRegEx">^[\S]{5,30}$</Property>
      <Property name="PasswordJavaRegExViolationErrorMsg">Password pattern policy violated.</Property>
      <Property name="UsernameJavaRegEx">^[\S]{5,30}$</Property>
      <Property name="UsernameJavaScriptRegEx">^[\S]{5,30}$</Property>
      <Property name="UsernameJavaRegExViolationErrorMsg">Username pattern policy violated.</Property>
      <Property name="RolenameJavaRegEx">^[\S]{5,30}$</Property>
      <Property name="RolenameJavaScriptRegEx">^[\S]{5,30}$</Property>
      <Property name="validationInterval"/>
      <Property name="CaseInsensitiveUsername">true</Property>
      <Property name="SCIMEnabled">true</Property>
      <Property name="IsBulkImportSupported">true</Property>
      <Property name="PasswordDigest">MD5</Property>
      <Property name="MultiAttributeSeparator">,</Property>
      <Property name="StoreSaltedPassword">false</Property>
      <Property name="MaxUserNameListLength">100</Property>
      <Property name="MaxRoleNameListLength">100</Property>
      <Property name="EnableUserRoleCache">true</Property>
      <Property name="UserNameAttribute">uid</Property>
      <Property name="MultipleAttributeEnable">true</Property>         
  </UserStoreManager>
```

10. The format of the `ConnectionURL` is given below. In case if the port is not specified, then `27017` will be used, which is the default port for MongoDB.
```
   mongodb://host[:port]/database[?options]
```

11. Comment the existing primary user store xml configurations in `user-mgt.xml` and save the file.

12. Now, open a terminal, navigate to the `bin` folder of product-IS and start the IS server by executing the following command
```bash
   ./wso2server.sh
```

This will start the IS server with MongoDB as the primary user store. Hence, all your user management related tasks will be stored in MongoDB by default.
