/*
 * Copyright 2015 - 2017 AZYVA INC. INC.
 *
 * This file is part of Dragom.
 *
 * Dragom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dragom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dragom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.azyva.dragom.test.integration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.azyva.dragom.tool.GenericRootModuleVersionJobInvokerTool;
import org.azyva.dragom.tool.RootManagerTool;


public class IntegrationTestSuiteSwitchToDynamicVersionToolPhase {
  /*********************************************************************************
   * Tests SwitchToDynamicVersionTool.
   * <p>
   * Phase tests.
   *********************************************************************************/
  public static void testSwitchToDynamicVersionToolPhase() {
    Path pathModel;
    InputStream inputStream;
    ZipInputStream zipInputStream;
    ZipEntry zipEntry;

    try {
      IntegrationTestSuite.printTestCategoryHeader("GenericRootModuleVersionJobInvokerTool org.azyva.dragom.job.SwitchToDynamicVersion SwitchToDynamicVersionToolHelp.txt | Phase tests");

      IntegrationTestSuite.resetTestWorkspace();

      try {
        pathModel = IntegrationTestSuite.pathTestWorkspace.resolve("basic-model.xml");
        inputStream = IntegrationTestSuite.class.getResourceAsStream("/basic-model.xml");
        Files.copy(inputStream, pathModel, StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();

        inputStream = IntegrationTestSuite.class.getResourceAsStream("/test-git-repos.zip");
        zipInputStream = new ZipInputStream(inputStream);

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
          Path path;

          path = IntegrationTestSuite.pathTestWorkspace.resolve(zipEntry.getName());

          if (zipEntry.isDirectory()) {
            path.toFile().mkdirs();
          } else {
            OutputStream outputStream;
            final int chunk = 1024;
            byte[] arrayByteBuffer;
            long size;
            int sizeRead;

            outputStream = new FileOutputStream(path.toFile());
            arrayByteBuffer = new byte[chunk];
            size = zipEntry.getSize();

            while (size > 0) {
              sizeRead = (int)Math.min(chunk,  size);
              sizeRead = zipInputStream.read(arrayByteBuffer, 0, sizeRead);
              outputStream.write(arrayByteBuffer, 0, sizeRead);
              size -= sizeRead;
            }

            outputStream.close();
          }
        }

        zipInputStream.close();
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }

      System.setProperty("org.azyva.dragom.init-property.GIT_REPOS_BASE_URL", "file:///" + IntegrationTestSuite.pathTestWorkspace.toAbsolutePath() + "/test-git-repos");
      System.setProperty("org.azyva.dragom.init-property.URL_MODEL" , pathModel.toUri().toString());
      System.setProperty("org.azyva.dragom.init-property.MODULE_EXISTENCE_CACHE_FILE" , IntegrationTestSuite.pathTestWorkspace.toAbsolutePath() + "/module-existence.properties");
      System.setProperty("org.azyva.dragom.init-property.IND_ECHO_INFO", "true");

      // ###############################################################################

      IntegrationTestSuite.printTestHeader("RootManagerTool --workspace=workspace add Domain2/app-b");
      try {
        RootManagerTool.main(new String[] {"--workspace=" + IntegrationTestSuite.pathTestWorkspace.resolve("workspace"), "add", "Domain2/app-b"});
      } catch (Exception e) {
        IntegrationTestSuite.validateExitException(e, 0);
      }
      IntegrationTestSuite.printTestFooter();

      // ###############################################################################

      IntegrationTestSuite.printTestHeader("RootManagerTool --workspace=workspace add Domain2/app-b");
      try {
        RootManagerTool.main(new String[] {"--workspace=" + IntegrationTestSuite.pathTestWorkspace.resolve("workspace"), "add", "Domain2/app-b"});
      } catch (Exception e) {
        IntegrationTestSuite.validateExitException(e, 0);
      }
      IntegrationTestSuite.printTestFooter();

      // ###############################################################################

      System.setProperty("org.azyva.dragom.init-property.SPECIFIC_PLUGIN_ID.org.azyva.dragom.model.plugin.SelectDynamicVersionPlugin", "uniform");

      // Response "D/develop-project1-sprint" to "to which version do you want to switch"
      IntegrationTestSuite.inputStreamDouble.write("D/develop-project1-sprint\n");

      // Response "Y" to "do you want to automatically reuse dynamic version"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Default response to "which base version" (D/master)
      IntegrationTestSuite.inputStreamDouble.write("\n");

      // Response "Y" to "do you want to automatically reuse base version"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "A" to "do you want to continue"
      IntegrationTestSuite.inputStreamDouble.write("A\n");

      // Response "sprint01" to "which phase"
      IntegrationTestSuite.inputStreamDouble.write("sprint01\n");

      // Response "Y" to "reuse phase"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "Y" to "process already dynamic version"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "A" to "do you want to continue (updating parent)"
      IntegrationTestSuite.inputStreamDouble.write("A\n");

      IntegrationTestSuite.printTestHeader("GenericRootModuleVersionJobInvokerTool org.azyva.dragom.job.SwitchToDynamicVersion SwitchToDynamicVersionToolHelp.txt --workspace=workspace --reference-path-matcher=/Domain2/app-b->/Framework/framework");
      try {
        GenericRootModuleVersionJobInvokerTool.main(new String[] {"org.azyva.dragom.job.SwitchToDynamicVersion", "SwitchToDynamicVersionToolHelp.txt", "--workspace=" + IntegrationTestSuite.pathTestWorkspace.resolve("workspace"), "--reference-path-matcher=/Domain2/app-b->/Framework/framework"});
      } catch (Exception e) {
        IntegrationTestSuite.validateExitException(e, 0);
      }
      IntegrationTestSuite.printTestFooter();

      // ###############################################################################

      System.setProperty("org.azyva.dragom.init-property.SPECIFIC_PLUGIN_ID.org.azyva.dragom.model.plugin.SelectDynamicVersionPlugin", "uniform");

      // Response "Y" to "process already dynamic version"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "D/develop-project1-sprint" to "to which version do you want to switch"
      IntegrationTestSuite.inputStreamDouble.write("D/develop-project1-sprint\n");

      // Response "Y" to "do you want to automatically reuse dynamic version"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "A" to "do you want to continue"
      IntegrationTestSuite.inputStreamDouble.write("A\n");

      // Response "sprint02" to "which phase"
      IntegrationTestSuite.inputStreamDouble.write("sprint02\n");

      // Default response to "reuse phase" (A)
      IntegrationTestSuite.inputStreamDouble.write("\n");

      // Default response to "which phase" (sprint02)
      IntegrationTestSuite.inputStreamDouble.write("\n");

      // Response "Y" to "reuse phase"
      IntegrationTestSuite.inputStreamDouble.write("Y\n");

      // Response "A" to "do you want to continue (updating parent)"
      IntegrationTestSuite.inputStreamDouble.write("A\n");

      IntegrationTestSuite.printTestHeader("GenericRootModuleVersionJobInvokerTool org.azyva.dragom.job.SwitchToDynamicVersion SwitchToDynamicVersionToolHelp.txt --workspace=workspace --reference-path-matcher=/Domain2/app-b->/Framework/framework");
      try {
        GenericRootModuleVersionJobInvokerTool.main(new String[] {"org.azyva.dragom.job.SwitchToDynamicVersion", "SwitchToDynamicVersionToolHelp.txt", "--workspace=" + IntegrationTestSuite.pathTestWorkspace.resolve("workspace"), "--reference-path-matcher=/Domain2/app-b->/Framework/framework"});
      } catch (Exception e) {
        IntegrationTestSuite.validateExitException(e, 0);
      }
      IntegrationTestSuite.printTestFooter();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
