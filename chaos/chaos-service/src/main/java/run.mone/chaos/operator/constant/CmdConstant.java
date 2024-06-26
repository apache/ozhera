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
package run.mone.chaos.operator.constant;

/**
 * @author zhangping17
 */
public class CmdConstant {

    public static final String BLANK_SPACE = " ";

    public static final String CPU = "--cpu" + BLANK_SPACE;

    public static final String CPU_LOAD = "-l";//"--cpu-load";

    public static final String MEM_STRESS_SIZE = "--size" + BLANK_SPACE;

    public static final String MEM_STRESS_TIME = "--time" + BLANK_SPACE;

    public static final String MEM_STRESS_WORKER = "--workers 1";

    public static final String VM = "--vm" + BLANK_SPACE;

    public static final String VM_BYTES = "--vm-bytes" + BLANK_SPACE;

    public static final String VM_HANG = "--vm-hang";

    public static final String IO_NUM = "-i";//"--io";

    public static final String HDD = "-d";//"--hdd";

    public static final String HDD_BYTES = "--hdd-bytes";

    public static final String TIMEOUT = "--timeout" + BLANK_SPACE;

    public static final String DIVISION = ",";

    public static final String TC_DELAY = "tc,qdisc,add,dev,%s,root,netem,delay,%dms";

    public static final String TC_LOSS = "tc,qdisc,add,dev,%s,root,netem,loss,%d%%";

    public static final String TC_CORRUPTION = "tc,qdisc,add,dev,%s,root,netem,corrupt,%d%%";

    public static final String TC_DUPLICATES = "tc,qdisc,add,dev,%s,root,netem,duplicate,%d%%";

    public static final String TC_DELETE = "tc,qdisc,del,dev,%s,root";

    public static final String JVM_TEMPLATE = "RULE %s\n" +
            "CLASS %s\n" +
            "METHOD %s\n" +
            "AT ENTRY\n" +
            "IF true\n" +
            "DO %s\n" +
            "ENDRULE\n";

    public static final String JVM_TEMPLATE_DELAY = "java.lang.Thread.sleep(%dL)";

    public static final String JVM_TEMPLATE_GC = "System.gc()";

    public static final String JVM_TEMPLATE_EXCEPTION = "";

    public static final String JVM_TEMPLATE_RETURN = "return \"%s\"";

    public static final String JVM_FILE = "/tmp";

    public static final String JVM_BTM = ".btm";

    //内部PAUSE容器镜像
    public static final String POD_FAILURE_PAUSE_IMAGE = "micr.cloud.mioffice.cn/mixiao/mione-chaos-pause:v0.0.1";

    //mione-chaos-containerName-podName-init/normal
    public static final String POD_FAILURE_ANNOTATION = "mione-chaos-%s-%s-%s";

    public static final String HTTP_TPROXY_CMD = "/usr/local/bin/tproxy -i -vv";


}
