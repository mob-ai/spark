/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.CatalogManager
import org.apache.spark.sql.execution.LeafExecNode

/**
 * Physical plan node for setting the current catalog and/or namespace.
 */
case class SetCatalogAndNamespaceExec(
    catalogManager: CatalogManager,
    catalogName: Option[String],
    namespace: Option[Seq[String]])
    extends LeafExecNode {
  override protected def doExecute(): RDD[InternalRow] = {
    // The catalog is updated first because CatalogManager resets the current namespace
    // when the current catalog is set.
    catalogName.map(catalogManager.setCurrentCatalog)
    namespace.map(ns => catalogManager.setCurrentNamespace(ns.toArray))

    sqlContext.sparkContext.parallelize(Seq.empty, 1)
  }

  override def output: Seq[Attribute] = Seq.empty
}
