/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package run.mone.chaos.operator.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Bean;
import com.xiaomi.youpin.docean.anno.Configuration;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.Config;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

@Configuration
public class mongo {

    @Bean
    public Datastore createDatastore() {
        Config config = Ioc.ins().getBean(Config.class);
        String url = config.get("mongodb.url", "");
        String name = config.get("mongodb.name", "");

        Morphia morphia = new Morphia();
        morphia.mapPackage("run.mone.chaos.operator.dao.domain");
        MongoClient mongoClient = new MongoClient(new MongoClientURI(url));
        Datastore datastore = morphia.createDatastore(mongoClient, name);
        datastore.ensureIndexes();
        return datastore;
    }

}
