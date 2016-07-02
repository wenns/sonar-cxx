/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


/**
 * Utility class holding various, well, utilities
 */
public final class CxxUtils {

  /**
   * Default logger.
   */
  public static final Logger LOG = Loggers.get("CxxPlugin");

  private CxxUtils() {
    // only static methods
  }

  /**
   * Normalize the given path to pass it to sonar. Return null if normalization
   * has failed.
   */
  public static String normalizePath(String filename) {
    try {
      return new File(filename).getCanonicalPath();
    } catch (java.io.IOException e) {
      LOG.error("path normalizing of '{}' failed: '{}'", filename, e.toString());
      return null;
    }
  }

  /**
   * @return returns case sensitive full path
   */
  public static String normalizePathFull(String filename, String baseDir) {
    File targetfile = new java.io.File(filename.trim());
    String filePath;
    if (targetfile.isAbsolute()) {
      filePath = normalizePath(filename);
    } else {
      // RATS, CppCheck and Vera++ provide names like './file.cpp' - add source folder for index check
      filePath = normalizePath(baseDir + File.separator + filename);
    }
    return filePath;
  }
  
  

  /**
   * <p>Gets the stack trace from a Throwable as a String.</p>
   * 
   * @param throwable  the <code>Throwable</code> to be examined
   * @return the stack trace as generated by the exception's printStackTrace(PrintWriter) method
   */
  public static String getStackTrace(final Throwable throwable) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }
}
