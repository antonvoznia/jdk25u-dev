/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

/*
 * @test
 * @bug 8377512
 * @summary VM should not crash during AOT creation when a MethodHandle
 *          contains an old version class.
 * @requires vm.cds
 * @library /test/lib /test/hotspot/jtreg/runtime/cds/appcds
 * @compile OldClass.jasm
 * @compile MethodHandlerOldClass.java
 * @run driver TestOldClassMethodHandler
 */

import java.io.File;
import jdk.test.lib.cds.CDSOptions;
import jdk.test.lib.cds.CDSTestUtils;
import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;

public class TestOldClassMethodHandler {

    public static void main(String[] args) throws Exception {
        String mainClass = "MethodHandlerOldClass";
        
        JarBuilder.build("oldlib", "OldClass");
        String oldLibJar = TestCommon.getTestJar("oldlib.jar");

        JarBuilder.build("app", "MethodHandlerOldClass", "MethodHandlerOldClass$A");
        String appJar = TestCommon.getTestJar("app.jar");

        String classPath = oldLibJar + File.pathSeparator + appJar;
        String aotConfigFile = "app.aot.conf";
        String aotCacheFile = "app.aot";

        // 1. AOT Record
        ProcessBuilder pb = ProcessTools.createTestJavaProcessBuilder(
            "-XX:AOTMode=record",
            "-XX:AOTConfiguration=" + aotConfigFile,
            "-cp", classPath,
            mainClass
        );
        OutputAnalyzer output = CDSTestUtils.executeAndLog(pb, "record");
        output.shouldHaveExitValue(0);

        // 2. AOT Create
        pb = ProcessTools.createTestJavaProcessBuilder(
            "-XX:AOTMode=create",
            "-XX:AOTConfiguration=" + aotConfigFile,
            "-XX:AOTCache=" + aotCacheFile,
            "-Xlog:aot,class+path=info",
            "-cp", classPath,
            mainClass
        );
        output = CDSTestUtils.executeAndLog(pb, "create");
        output.shouldHaveExitValue(0);
    }
}
