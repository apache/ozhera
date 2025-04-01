/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ozhera.trace.etl.test;

import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.IntStream;

/**
 * @author goodjava@qq.com
 * @date 2023/8/29 00:27
 */
public class FastWriter {


    public byte[] getBytes(Enumeration<String> mfs) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);

        ArrayList<Pair<ByteArrayOutputStream, OutputStreamWriter>> list = new ArrayList<>();

        int v =2;

        IntStream.range(0, v).forEach(i -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos);
            list.add(Pair.of(baos, writer));
        });


        Collections.list(mfs).stream().parallel().forEach(it -> {
            Pair<ByteArrayOutputStream, OutputStreamWriter> pair = list.get((it.hashCode()& Integer.MAX_VALUE) % v);
            OutputStreamWriter writer = pair.getValue();
            IntStream.range(0, 100).forEach(i -> {
                try {
                    writer.write(it);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        });
        if (true) {
            return new byte[]{};
        }

//        list.stream().map(p->{
//            try {
//                p.getValue().flush();
//                return p.getKey().toByteArray();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }).flatMap(Arrays::stream);
//        w.flush();
//        return b.toByteArray();
        return null;

    }

}