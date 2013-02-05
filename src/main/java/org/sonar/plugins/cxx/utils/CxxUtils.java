/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.utils;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.WildcardPattern;

/**
 * Utility class holding various, well, utilities
 */
public final class CxxUtils {

  private CxxUtils() {
    // only static methods
  }
  /**
   * Default logger.
   */
  public static final Logger LOG = LoggerFactory.getLogger("CxxPlugin");

  /**
   * @param file
   * @return Returns file path of provided file, or "null" if file == null
   */
  public static String fileToAbsolutePath(File file) {
    if (file == null) {
      return "null";
    }
    return file.getAbsolutePath();
  }

  /**
   * @param file
   * @return Returns file path of provided file, or "null" if file == null
   */
  public static boolean isATestFile(String data, String file) {
    boolean isTest = false;
    String[] patterns = CxxUtils.createStringArray(data, "");
    for (int i = 0; i < patterns.length; i++) {
      if (WildcardPattern.create(patterns[i]).match(file.replace("\\", "/"))) {
        isTest = true;
        break;
      }
    }
    return isTest;
  }

  /**
   * @param values
   * @param defaultValues
   * @return creates a array of strings, from string delimited by semi-colon
   */
  public static String[] createStringArray(String values, String defaultValues) {
    String[] data = new String[0];
    if (values != null && !values.equals("")) {
      data = StringUtils.split(values, ",");
    }
    return data;
  }
}
