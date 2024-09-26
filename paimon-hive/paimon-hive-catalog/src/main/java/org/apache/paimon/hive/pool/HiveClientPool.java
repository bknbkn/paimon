/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.hive.pool;

import org.apache.paimon.client.ClientPool;
import org.apache.paimon.hive.RetryingMetaStoreClientFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.thrift.TException;

import java.util.function.Supplier;

/**
 * Pool of Hive Metastore clients.
 *
 * <p>Mostly copied from iceberg.
 */
public class HiveClientPool extends ClientPool.ClientPoolImpl<IMetaStoreClient, TException> {

    public HiveClientPool(int poolSize, Configuration conf, String clientClassName) {
        super(poolSize, clientSupplier(conf, clientClassName));
    }

    private static Supplier<IMetaStoreClient> clientSupplier(
            Configuration conf, String clientClassName) {
        HiveConf hiveConf = new HiveConf(conf, HiveClientPool.class);
        hiveConf.addResource(conf);
        return () -> new RetryingMetaStoreClientFactory().createClient(hiveConf, clientClassName);
    }

    @Override
    protected void close(IMetaStoreClient client) {
        client.close();
    }
}
