/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.checks;

import org.sonar.squidbridge.checks.CheckMessagesVerifier;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;

public class MissingNewLineAtEndOfFileCheckTest {

  private MissingNewLineAtEndOfFileCheck check = new MissingNewLineAtEndOfFileCheck();

  @Test
  public void test() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/MissingNewLineAtEndOfFile.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().withMessage("Add a new line at the end of this file.")
        .noMore();
  }

  @Test
  public void test2() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/EmptyFile.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().withMessage("Add a new line at the end of this file.")
        .noMore();
  }

  @Test
  public void test3() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/NonEmptyFile.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .noMore();
  }

}
