package alpha.glue.v2;

import alpha.codegen.Program;
import alpha.codegen.ProgramPrinter;
import alpha.codegen.writeC.SystemConverter;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.util.ShowLegacyAlpha;
import java.io.FileWriter;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;

/**
 * This class is a wrapper around the new demand driven code generator.
 * It is intended to be used in conjunction with its sibling package
 * alpha.glue.v1 to generate a complete program that can be compiled.
 */
@SuppressWarnings("all")
public class GenerateNewWriteC {
  public static void main(final String[] args) {
    try {
      int _size = ((List<String>)Conversions.doWrapArray(args)).size();
      GenerateNewWriteC.thenQuitWithError((_size < 2), "usage: alpha_v2_file out_dir");
      final String alphaFile = args[0];
      final String outDir = args[1];
      final AlphaRoot root = AlphaLoader.loadAlpha(alphaFile);
      int _size_1 = root.getSystems().size();
      GenerateNewWriteC.thenQuitWithError((_size_1 > 1), "error: only single system alpha programs are supported by this tool");
      final AlphaSystem system = root.getSystems().get(0);
      GenerateNewWriteC.generateV1Alpha(system, outDir);
      GenerateNewWriteC.generateWriteC(system, outDir);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * Generates the old (v1) alphaz ab file for the given system
   */
  public static void generateV1Alpha(final AlphaSystem system, final String outDir) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(outDir);
    _builder.append("/");
    String _name = system.getName();
    _builder.append(_name);
    _builder.append(".ab");
    final String abFile = _builder.toString();
    AlphaModelSaver.writeToFile(abFile, ShowLegacyAlpha.print(system));
  }

  /**
   * Generates the new (v2) demand driven code compatible with v1 for the given system
   */
  public static void generateWriteC(final AlphaSystem system, final String outDir) {
    try {
      final Program program = SystemConverter.convert(system, true);
      final String code = ProgramPrinter.print(program).toString();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(outDir);
      _builder.append("/");
      String _name = system.getName();
      _builder.append(_name);
      _builder.append(".c");
      final FileWriter writer = new FileWriter(_builder.toString());
      writer.write(code);
      writer.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static void thenQuitWithError(final boolean conditionToQuit, final String message) {
    if (conditionToQuit) {
      InputOutput.<String>println(message);
      System.exit(1);
    }
  }
}
