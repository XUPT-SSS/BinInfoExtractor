
package utils.ida;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UnknownFormatConversionException;

import utils.zylib.io.SystemHelpers;

/**
 * Contains a few simple methods that are useful for dealing with IDA and command line handling
 */
public final class IDAHelpers {
  public static final String IDA32_EXECUTABLE;
  public static final String IDA64_EXECUTABLE;

  static {
    if (SystemHelpers.isRunningWindows()) {
      IDA32_EXECUTABLE = "idaq.exe";
      IDA64_EXECUTABLE = "idaq64.exe";
    } else {
      IDA32_EXECUTABLE = "idaq";
      IDA64_EXECUTABLE = "idaq64";
    }
  }

  /**
   * Exports an IDB using BinExport to a .BinExport file. This is a modified version of the code in
   * BinNavi, currently only used from BinDiff. It should be refactored so it can be used from both.
   */
  public static Process createIdaProcess(final String idaExe, final File idcPath,
      final String idbFileName, final String outputDirectory) throws Exception {
    final String idcFileString = idcPath.getAbsolutePath();

    final String sArgument = getSArgument(idcFileString, SystemHelpers.isRunningWindows());

    // Setup the invocation of the IDA to SQL exporter
    final ProcessBuilder processBuilder =
        new ProcessBuilder(idaExe, "-A", "-OExporterModule:" + outputDirectory, sArgument,
            idbFileName);

    // ESCA-JAVA0166:
    // Now launch the exporter to export the IDB to the database
    try {
      Process processInfo = null;

      processBuilder.redirectErrorStream(true);
      processInfo = processBuilder.start();

      // Java manages the streams internally - if they are full, the
      // process blocks, i.e. IDA hangs, so we need to consume them.
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(processInfo.getInputStream()))) {
          reader.lines().forEach(System.out::println);
      } catch (final IOException exception) {
          //Ignore
      }
      return processInfo;
      
    } catch (final Exception exception) {
      try {
        // TODO: What can we do here ? Do we have a ZyLib-wide logger ?
        // CUtilityFunctions.logException(exception);
      } catch (final UnknownFormatConversionException e) {
        // Some Windows error messages contain %1 characters.
      }

      throw new Exception("Failed attempting to launch the importer with IDA: " + exception,
          exception);
    }
  }

  /**
   * Builds the -S argument for IDA Pro.
   * 
   * @param idcFile The IDC file to execute.
   * @param isRunningWindows True, if the user is exporting on Windows. False, otherwise.
   * 
   * @return The created -S argument.
   */
  public static String getSArgument(final String idcFile, final boolean isRunningWindows) {
    return isRunningWindows ? "-S\\\"" + idcFile + "\\\"" : "-S\"" + idcFile + "\"";
  }
}
