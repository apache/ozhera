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
package org.apache.ozhera.monitor.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/7/7 8:11 下午
 */
public class FreeMarkerUtil {


    /**
     * Get template files under the specified directory
     * @param name       The name of the template file
     * @param pathPrefix The directory of the template file
     */
    private static Template getTemplate(String name, String pathPrefix) throws IOException {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(FreeMarkerUtil.class, pathPrefix); //Set the directory of the template files.
        cfg.setDefaultEncoding("UTF-8");       //Set the default charset of the template files
        Template temp = cfg.getTemplate(name); //Search for the template file named "name" in the template file directory.

        return temp; //At this point, FreeMarker will look for a template file named "name" in the "pathPrefix" folder under the classpath.

    }


    /**
     * Output content to the console based on the template file.
     * @param name       The name of the template file.
     * @param pathPrefix The directory of the template file.
     * @param rootMap    The data model of the template.
     */
    public static String getContent(String pathPrefix, String name, Map<String, Object> rootMap) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        getTemplate(name, pathPrefix).process(rootMap, writer);
        String jsonStr = writer.toString();
        JsonObject returnData = new JsonParser().parse(jsonStr).getAsJsonObject();//Convert the template file to a JSON object and then to a JSON string
        return returnData.toString();
    }

    public static String getContentExceptJson(String pathPrefix, String name, Map<String, Object> rootMap) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        getTemplate(name, pathPrefix).process(rootMap, writer);
        String str = writer.toString();
        return str;
    }


    /**
     * Output content to a specified file based on a template file.
     * @param name       The name of the template file.
     * @param pathPrefix The directory of the template file.
     * @param rootMap    The data model of the template.
     * @param file       The output file for the content.
     */
    public static void printFile(String pathPrefix, String name, Map<String, Object> rootMap, File file) throws TemplateException, IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        getTemplate(name, pathPrefix).process(rootMap, out); //Output the content of the template file to the corresponding stream in UTF-8 encoding
        if (null != out) {
            out.close();
        }
    }

    public static String freemarkerProcess(Map input, String templateStr) {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        String template = "content";
        stringLoader.putTemplate(template, templateStr);
        Configuration cfg = new Configuration();
        cfg.setTemplateLoader(stringLoader);
        try {
            Template templateCon = cfg.getTemplate(template);
            StringWriter writer = new StringWriter();
            templateCon.process(input, writer);
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTemplateStr(String pathPrefix, String name) throws IOException {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(FreeMarkerUtil.class, pathPrefix); //Set the directory of the template file
        cfg.setDefaultEncoding("UTF-8");       //Set the default charset of the template files
        Template temp = cfg.getTemplate(name); //Look for the template file named "name" in the template file directory
        return temp.toString();
    }

}