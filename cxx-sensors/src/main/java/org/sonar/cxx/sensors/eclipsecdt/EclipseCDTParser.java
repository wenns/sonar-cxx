/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.eclipsecdt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class EclipseCDTParser {

  private static final Logger LOG = Loggers.get(EclipseCDTParser.class);

  private static final IParserLogService LOG_ADAPTER = new IParserLogService() {
    @Override
    public boolean isTracing() {
      return LOG.isDebugEnabled();
    }

    @Override
    public void traceLog(String msg) {
      LOG.debug(msg);
    }
  };

  private final IASTTranslationUnit translationUnit;
  private final LinebasedOffsetTranslator offsetTranslator;

  public EclipseCDTParser(String path) throws EclipseCDTException {
    try {
      offsetTranslator = new LinebasedOffsetTranslator(path);
    } catch (IOException e) {
      throw new EclipseCDTException("Unable to read file " + path, e);
    }

    final IIndex ignoreIndex = null;
    final String[] ignoreIncludePaths = null;
    final Map<String, String> ignoreDefinedMacros = null;
    final int noSpecialParseOptions = 0;
    IScannerInfo ignoreScannerInfo = new ScannerInfo(ignoreDefinedMacros, ignoreIncludePaths);
    IncludeFileContentProvider emptyFilesProvider = IncludeFileContentProvider.getEmptyFilesProvider();

    FileContent fileContent = FileContent.createForExternalFileLocation(path);

    try {
      translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, ignoreScannerInfo,
          emptyFilesProvider, ignoreIndex, noSpecialParseOptions, LOG_ADAPTER);
    } catch (CoreException e) {
      throw new EclipseCDTException("Unable to parse file " + path, e);
    }

    logPreprocessorProblems();
  }

  private void logPreprocessorProblems() {
    if (LOG.isDebugEnabled()) {
      for (IASTProblem problem : translationUnit.getPreprocessorProblems()) {
        LOG.debug(problem.getMessageWithLocation());
      }
    }
  }

  /**
   * Traverse the given top-level declaration and find all IASTName nodes, which
   * describe this or arbitrary nested declaration
   */
  private Set<IASTName> getDeclarationNameNodes(IASTDeclaration declaration) {
    if (!declaration.isPartOfTranslationUnitFile()) {
      return Collections.emptySet();
    }

    final Set<IASTName> declarationNameNodes = new HashSet<>();
    declaration.accept(new ASTGenericVisitor(true) {
      {
        includeInactiveNodes = true;
      }

      @Override
      public int visit(IASTName name) {
        if (name.isDeclaration() && name.isPartOfTranslationUnitFile() && name.getFileLocation() != null) {
          declarationNameNodes.add(name);
        }
        return PROCESS_CONTINUE;
      }

    });

    return declarationNameNodes;
  }

  private LinebasedTextRange newRange(IASTFileLocation location) throws EclipseCDTException {
    int globalOffset = location.getNodeOffset();
    int length = location.getNodeLength();
    LinebasedTextPointer start = offsetTranslator.newPointer(globalOffset);
    LinebasedTextPointer end = offsetTranslator.newPointer(globalOffset + length);
    return new LinebasedTextRange(start, end);
  }

  public Map<LinebasedTextRange, Set<LinebasedTextRange>> generateSymbolTable() throws EclipseCDTException {
    // collect all declarations from te translation unit
    IASTDeclaration[] declarations = translationUnit.getDeclarations(true);
    final Set<IASTName> declarationNames = new HashSet<>();
    for (IASTDeclaration declaration : declarations) {
      declarationNames.addAll(getDeclarationNameNodes(declaration));
    }

    // collect all references to the declarations
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = new HashMap<>();
    for (IASTName declarationName : declarationNames) {
      IBinding binding = declarationName.resolveBinding();
      if (binding == null) {
        continue;
      }

      IASTFileLocation declarationLocation = declarationName.getFileLocation();
      LinebasedTextRange declarationTextRange = newRange(declarationLocation);

      Set<LinebasedTextRange> references = new HashSet<>();
      for (IASTName referenceName : translationUnit.getReferences(binding)) {
        if (referenceName != null && referenceName.isPartOfTranslationUnitFile()
            && referenceName.getFileLocation() != null) {
          IASTFileLocation referenceRange = referenceName.getFileLocation();
          references.add(newRange(referenceRange));
        }
      }

      table.put(declarationTextRange, references);
    }
    return table;
  }
}
