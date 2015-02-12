/*
 *  Licensed to STRATIO (C) under one or more contributor license agreements.
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership. The STRATIO (C) licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.stratio.deep.mongodb

import com.stratio.deep.DeepConfig
import com.stratio.deep.mongodb.MongodbConfig._
import com.stratio.deep.mongodb.rdd.MongodbRDD
import com.stratio.deep.mongodb.schema.{MongodbRowConverter, MongodbSchema}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.sources.{Filter, PrunedFilteredScan}

/**
 * Created by jsantos on 12/02/15.
 */
case class MongodbRelation(
  config: DeepConfig,
  schemaProvided: Option[StructType]=None)(
  @transient val sqlContext: SQLContext) extends PrunedFilteredScan {

  @transient lazy val lazySchema = {
    MongodbSchema(
      new MongodbRDD(sqlContext, config),
      config[Double](SamplingRatio)).schema()
  }

  override val schema: StructType = schemaProvided.getOrElse(lazySchema)

  def pruneSchema(
    schema: StructType,
    requiredColumns: Array[String]): StructType =
    StructType(
      requiredColumns.flatMap(column =>
        schema.fields.find(_.name==column)))

  override def buildScan(
    requiredColumns : Array[String],
    filters : Array[Filter]): RDD[Row] = {
    val rdd = new MongodbRDD(sqlContext,config,requiredColumns,filters)
    MongodbRowConverter.asRow(pruneSchema(schema,requiredColumns), rdd)
  }

}
