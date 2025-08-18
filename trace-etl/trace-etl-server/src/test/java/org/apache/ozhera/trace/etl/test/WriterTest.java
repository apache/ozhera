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

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author goodjava@qq.com
 * @date 2023/8/28 22:55
 */
public class WriterTest {

    private int num = 1000000;

    private String str = Strings.repeat("a", 10);


    @SneakyThrows
    @Test
    public void test0() {
        List<String> list = IntStream.range(0, num).mapToObj(i -> str).collect(Collectors.toList());
        StopWatch sw = new StopWatch();
        Enumeration<String> e = Collections.enumeration(list);
        sw.start();
        byte[] data = new FastWriter().getBytes(e);
        System.out.println(data.length);
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
    }


    @Test
    public void test1() {
        StopWatch sw = new StopWatch();
        sw.start();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(baos)) {
            IntStream.range(0, num).forEach(i -> {
                IntStream.range(0, 100).forEach(j -> {
                    try {
                        writer.write(str);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            writer.flush();
            byte[] bytes = baos.toByteArray();
            System.out.println(bytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
    }

    @Test
    public void test2() {
        StopWatch sw = new StopWatch();
        sw.start();
        Buffer buffer = new Buffer();
        IntStream.range(0, num).forEach(i -> {
            buffer.writeUtf8(str);
        });
        long size = buffer.size();
        System.out.println(size);
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
    }

    @SneakyThrows
    @Test
    public void test22() {
        StopWatch sw = new StopWatch();
        sw.start();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
        BufferedSink sink = Okio.buffer(Okio.sink(bos));
        IntStream.range(0, num).forEach(i -> {
            try {
                sink.write(str.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        sink.flush();
        ;
        long size = bos.size();
        System.out.println(size);
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
    }


    @Test
    public void test3() {
        Buffer buffer = new Buffer();
        buffer.write("abc".getBytes());
        buffer.write("abc".getBytes());
        System.out.println(buffer.readByteString().string(Charset.defaultCharset()));
    }

    @SneakyThrows
    @Test
    public void test4() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
        BufferedSink sink = Okio.buffer(Okio.sink(bos));
        sink.write("abc".getBytes());
        sink.write("abc".getBytes());
        sink.flush();
        System.out.println(bos.toByteArray().length);

    }


    @Test
    public void test5() {
        Vector<String> v = new Vector<>();
        v.add("a");
        v.add("b");
        v.add("c");
        ArrayList<String> list = Collections.list(v.elements());
        list.stream().parallel().forEach(i -> {
            System.out.println(i + " " + Thread.currentThread().getName());
        });
    }


    @Test
    public void test6() {
        Buffer buffer = new Buffer();
        buffer.write("abc".getBytes());
        System.out.println(buffer.readByteString().string(Charset.defaultCharset()));
        buffer.clear();
        System.out.println(buffer.readByteString().string(Charset.defaultCharset()));
    }


}