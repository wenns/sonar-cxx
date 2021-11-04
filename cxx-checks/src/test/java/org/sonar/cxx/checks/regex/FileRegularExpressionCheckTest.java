/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.checks.regex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

public class FileRegularExpressionCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void fileRegExWithoutFilePattern() throws UnsupportedEncodingException, IOException {
    var check = new FileRegularExpressionCheck();
    check.regularExpression = "stdafx\\.h";
    check.message = "Found 'stdafx.h' in file!";
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void fileRegExInvertWithoutFilePattern() throws UnsupportedEncodingException, IOException {
    var check = new FileRegularExpressionCheck();
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.UTF_8);
    check.regularExpression = "stdafx\\.h";
    check.invertRegularExpression = true;
    check.message = "Found no 'stdafx.h' in file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileRegExInvert.cc", ".");

    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void fileRegExCodingErrorActionReplace() throws UnsupportedEncodingException, IOException {
    var check = new FileRegularExpressionCheck();
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.US_ASCII);
    check.regularExpression = "stdafx\\.h";
    check.message = "Found 'stdafx.h' in file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileRegEx.cc", ".",
                                                      StandardCharsets.US_ASCII);

    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void fileRegExWithFilePattern() throws UnsupportedEncodingException, IOException {
    var check = new FileRegularExpressionCheck();
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.UTF_8);
    check.matchFilePattern = "/**/*.cc"; // all files with .cc file extension
    check.regularExpression = "#include\\s+\"stdafx\\.h\"";
    check.message = "Found '#include \"stdafx.h\"' in a .cc file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void fileRegExInvertWithFilePatternInvert() throws UnsupportedEncodingException, IOException {
    var check = new FileRegularExpressionCheck();
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.UTF_8);
    check.matchFilePattern = "/**/*.h"; // all files with not .h file extension
    check.invertFilePattern = true;
    check.regularExpression = "#include\\s+\"stdafx\\.h\"";
    check.invertRegularExpression = true;
    check.message = "Found no '#include \"stdafx.h\"' in a file with not '.h' file extension!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileRegExInvert.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(check.message)
      .noMore();
  }

}
