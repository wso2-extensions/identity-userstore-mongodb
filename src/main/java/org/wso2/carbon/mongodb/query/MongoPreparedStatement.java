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

package org.wso2.carbon.mongodb.query;

import java.util.Date;
import java.util.List;

import com.mongodb.WriteResult;
import com.mongodb.DBCursor;
import com.mongodb.AggregationOutput;
import com.mongodb.BulkWriteResult;

/**
 * MongoDB prepared statement interface.
 */
public interface MongoPreparedStatement {

    /**
     * Set int parameter value to respective query parameter.
     *
     * @param key       of json query
     * @param parameter value to set to query parameter
     */
    void setInt(String key, int parameter);

    /**
     * Set int parameter value to respective query parameter.
     *
     * @param key       of json query
     * @param parameter value to set to query parameter
     */
    void setLong(String key, long parameter);

    /**
     * Set String parameter value to respective query parameter.
     *
     * @param key       of json query
     * @param parameter value to set to query parameter
     */
    void setString(String key, String parameter);

    /**
     * Set date parameter value to respective query parameter.
     *
     * @param key  of json query
     * @param date value to set to query parameter
     */
    void setDate(String key, Date date);

    /**
     * Set boolean parameter value to respective query parameter.
     *
     * @param key       of json query
     * @param parameter value to set to query parameter
     */
    void setBoolean(String key, boolean parameter);

    /**
     * close the connection
     */
    void close();

    /**
     * Insert document to mongodb.
     *
     * @return WriteResult instance
     * @throws MongoDBQueryException if any exception occurred
     */
    WriteResult insert() throws MongoDBQueryException;

    /**
     * Search documents from mongodb.
     *
     * @return DBCursor instance
     * @throws MongoDBQueryException if any exception occurred
     */
    DBCursor find() throws MongoDBQueryException;

    /**
     * Search documents through aggregation pipeline from mongodb.
     *
     * @return AggregationOutput instance
     */
    @SuppressWarnings("deprecation")
    AggregationOutput aggregate();

    /**
     * Update document in mongodb.
     *
     * @return WriteResult instance
     * @throws MongoDBQueryException if any exception occurred
     */
    WriteResult update() throws MongoDBQueryException;

    /**
     * Remove document in mongodb.
     *
     * @return WriteResult instance
     * @throws MongoDBQueryException if any exception occurred
     */
    WriteResult remove() throws MongoDBQueryException;

    /**
     * Insert bulk documents to mongodb.
     *
     * @return BulkWriteResult instance
     */
    BulkWriteResult insertBulk();

    /**
     * Update bulk documents to mongodb.
     *
     * @return BulkWriteResult instance
     */
    BulkWriteResult updateBulk();

    /**
     * Add document to batch to bulk insert.
     *
     * @throws MongoDBQueryException if any exception occurred
     */
    void addBatch() throws MongoDBQueryException;

    /**
     * Add document to batch to bulk update.
     *
     * @throws MongoDBQueryException if any exception occurred
     */
    void updateBatch() throws MongoDBQueryException;

    /**
     * Get distinct set of values from mongodb.
     *
     * @return List of distinct
     * @throws MongoDBQueryException if any exception occurred
     */
    List distinct() throws MongoDBQueryException;

    /**
     * Multiple lookup status.
     *
     * @param status boolean status
     */
    void setMultiLookUp(boolean status);
}
