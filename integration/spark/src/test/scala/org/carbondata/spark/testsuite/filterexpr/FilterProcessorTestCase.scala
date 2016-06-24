/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.carbondata.spark.testsuite.filterexpr

import org.apache.spark.sql.common.util.CarbonHiveContext._
import org.apache.spark.sql.common.util.QueryTest
import org.apache.spark.sql.Row
import org.scalatest.BeforeAndAfterAll
import java.sql.Timestamp

import org.carbondata.core.constants.CarbonCommonConstants
import org.carbondata.core.util.CarbonProperties

/**
  * Test Class for filter expression query on String datatypes
  *
  * @author N00902756
  *
  */
class FilterProcessorTestCase extends QueryTest with BeforeAndAfterAll {

  override def beforeAll {
    sql("drop table if exists filtertestTables")
    sql("drop table if exists filtertestTablesWithDecimal")
    sql("drop table if exists filtertestTablesWithNull")
    sql("CREATE TABLE filtertestTables (ID int, date Timestamp, country String, " +
      "name String, phonetype String, serialname String, salary int) " +
        "STORED BY 'org.apache.carbondata.format'"
    )
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "yyyy/MM/dd")
    sql(
      s"LOAD DATA local inpath './src/test/resources/dataDiff.csv' INTO TABLE filtertestTables " +
        s"OPTIONS('DELIMITER'= ',', " +
        s"'FILEHEADER'= '')"
    )
    sql(
      "CREATE TABLE filtertestTablesWithDecimal (ID decimal, date Timestamp, country " +
        "String, " +
        "name String, phonetype String, serialname String, salary int) " +
      "STORED BY 'org.apache.carbondata.format'"
    )
    sql(
      s"LOAD DATA LOCAL INPATH './src/test/resources/dataDiff.csv' INTO TABLE " +
        s"filtertestTablesWithDecimal " +
        s"OPTIONS('DELIMITER'= ',', " +
        s"'FILEHEADER'= '')"
    )
    sql(
      "CREATE TABLE filtertestTablesWithNull (ID int, date Timestamp, country " +
        "String, " +
        "name String, phonetype String, serialname String,salary int) " +
      "STORED BY 'org.apache.carbondata.format'"
    )
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "dd-MM-yyyy")
    sql(
      s"LOAD DATA LOCAL INPATH './src/test/resources/data2.csv' INTO TABLE " +
        s"filtertestTablesWithNull " +
        s"OPTIONS('DELIMITER'= ',', " +
        s"'FILEHEADER'= '')"
    )
  }

  test("Is not null filter") {
    checkAnswer(
      sql("select id from filtertestTablesWithNull " + "where id is not null"),
      Seq(Row(4), Row(6))
    )
  }
    test("Multi column with invalid member filter") {
    checkAnswer(
      sql("select id from filtertestTablesWithNull " + "where id = salary"),
      Seq()
    )
  }

  test("Greater Than Filter") {
    checkAnswer(
      sql("select id from filtertestTables " + "where id >999"),
      Seq(Row(1000))
    )
  }
  test("Greater Than Filter with decimal") {
    checkAnswer(
      sql("select id from filtertestTablesWithDecimal " + "where id >999"),
      Seq(Row(1000))
    )
  }

  test("Greater Than equal to Filter") {
    checkAnswer(
      sql("select id from filtertestTables " + "where id >=999"),
      Seq(Row(999), Row(1000))
    )
  }

  test("Greater Than equal to Filter with decimal") {
    checkAnswer(
      sql("select id from filtertestTables " + "where id >=999"),
      Seq(Row(999), Row(1000))
    )
  }
  test("Include Filter") {
    checkAnswer(
      sql("select id from filtertestTables " + "where id =999"),
      Seq(Row(999))
    )
  }
  test("In Filter") {
    checkAnswer(
      sql(
        "select Country from filtertestTables where Country in ('china','france') group by Country"
      ),
      Seq(Row("china"), Row("france"))
    )
  }

  test("Logical condition") {
    checkAnswer(
      sql("select id,country from filtertestTables " + "where country='china' and name='aaa1'"),
      Seq(Row(1, "china"))
    )
  }

  override def afterAll {
    // sql("drop cube filtertestTable")
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "dd-MM-yyyy")
  }
}