package alpha.glue.v2;

import alpha.codegen.Program;
import alpha.codegen.ProgramPrinter;
import alpha.codegen.writeC.SystemConverter;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.ComplexityCalculator;
import alpha.model.SystemBody;
import alpha.model.transformation.automation.OptimalSimplifyingReductions;
import alpha.model.util.ShowLegacyAlpha;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * This class is a wrapper around the new demand driven code generator.
 * It is intended to be used in conjunction with its sibling package
 * alpha.glue.v1 to generate a complete program that can be compiled.
 */
@SuppressWarnings("all")
public class GenerateNewWriteC {
  private static String alphaFile = System.getenv("ACC_ALPHA_FILE");

  private static String outDir = System.getenv("ACC_OUT_DIR");

  private static int choice = GenerateNewWriteC.parseInt(System.getenv("ACC_CHOICE"), 0);

  private static boolean runSimplification = (System.getenv("ACC_SIMPLIFY") != null);

  private static int numOptimizations = GenerateNewWriteC.parseInt(System.getenv("ACC_NUM_SIMPLIFICATIONS"), 1);

  private static int targetComplexity = GenerateNewWriteC.parseInt(System.getenv("ACC_TARGET_COMPLEXITY"), (-1));

  private static boolean trySplitting = (System.getenv("ACC_TRY_SPLITTING") != null);

  public static int parseInt(final String str, final int defaultValue) {
    int _xblockexpression = (int) 0;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return defaultValue;
      }
      _xblockexpression = Integer.parseInt(str);
    }
    return _xblockexpression;
  }

  public static void main(final String[] args) {
    try {
      GenerateNewWriteC.thenQuitWithError((GenerateNewWriteC.alphaFile == null), "no input alpha file specified via ACC_ALPHA_FILE");
      GenerateNewWriteC.thenQuitWithError((GenerateNewWriteC.outDir == null), "no output directory specified via ACC_OUT_DIR");
      final AlphaRoot root = AlphaLoader.loadAlpha(GenerateNewWriteC.alphaFile);
      int _size = root.getSystems().size();
      GenerateNewWriteC.thenQuitWithError((_size > 1), "error: only single system alpha programs are supported by this tool");
      final AlphaSystem system = root.getSystems().get(0);
      int _size_1 = system.getSystemBodies().size();
      GenerateNewWriteC.thenQuitWithError((_size_1 > 1), "error: only systems with a single body are supported by this tool");
      if (((GenerateNewWriteC.choice == 1) || (GenerateNewWriteC.choice == 0))) {
        GenerateNewWriteC.generateV1Alpha(system, GenerateNewWriteC.outDir);
      }
      if (((GenerateNewWriteC.choice == 2) || (GenerateNewWriteC.choice == 0))) {
        final OptimalSimplifyingReductions.State[] states = GenerateNewWriteC.optimize(system);
        int _size_2 = ((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)).size();
        boolean _equals = (_size_2 == 0);
        if (_equals) {
          System.exit(1);
        }
        final Function1<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>> _function = (OptimalSimplifyingReductions.State s) -> {
          int _indexOf = ((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)).indexOf(s);
          return Pair.<Integer, OptimalSimplifyingReductions.State>of(Integer.valueOf(_indexOf), s);
        };
        final Consumer<Pair<Integer, OptimalSimplifyingReductions.State>> _function_1 = (Pair<Integer, OptimalSimplifyingReductions.State> pair) -> {
          final OptimalSimplifyingReductions.State state = pair.getValue();
          final AlphaSystem stateSystem = state.root().getSystems().get(0);
          StringConcatenation _builder = new StringConcatenation();
          _builder.append(GenerateNewWriteC.outDir);
          _builder.append("/simplifications/v");
          Integer _key = pair.getKey();
          _builder.append(_key);
          final String simplificationOutDir = _builder.toString();
          GenerateNewWriteC.generateWriteC(stateSystem, simplificationOutDir);
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append(simplificationOutDir);
          _builder_1.append("/");
          String _name = system.getName();
          _builder_1.append(_name);
          _builder_1.append(".alpha");
          AlphaModelSaver.writeToFile(_builder_1.toString(), state.show().toString());
        };
        ListExtensions.<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>>map(((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)), _function).forEach(_function_1);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * Run the simplifying reductions algorithm
   */
  public static OptimalSimplifyingReductions.State[] optimize(final AlphaSystem system) {
    List<OptimalSimplifyingReductions.State> _xblockexpression = null;
    {
      if ((!GenerateNewWriteC.runSimplification)) {
        SystemBody _get = system.getSystemBodies().get(0);
        LinkedList<OptimalSimplifyingReductions.DynamicProgrammingStep> _newLinkedList = CollectionLiterals.<OptimalSimplifyingReductions.DynamicProgrammingStep>newLinkedList();
        OptimalSimplifyingReductions.State _state = new OptimalSimplifyingReductions.State(_get, _newLinkedList);
        return new OptimalSimplifyingReductions.State[] { _state };
      }
      if ((GenerateNewWriteC.targetComplexity == (-1))) {
        int _complexity = ComplexityCalculator.complexity(system);
        int _minus = (_complexity - 1);
        GenerateNewWriteC.targetComplexity = _minus;
      }
      final OptimalSimplifyingReductions osr = OptimalSimplifyingReductions.apply(system, GenerateNewWriteC.numOptimizations, GenerateNewWriteC.targetComplexity, GenerateNewWriteC.trySplitting);
      _xblockexpression = osr.optimizations.get(Integer.valueOf(GenerateNewWriteC.targetComplexity));
    }
    return ((OptimalSimplifyingReductions.State[])Conversions.unwrapArray(_xblockexpression, OptimalSimplifyingReductions.State.class));
  }

  /**
   * Generates the new (v2) demand driven code compatible with v1 for the given system
   */
  public static void generateWriteC(final AlphaSystem system, final String outDir) {
    try {
      new File(outDir).mkdirs();
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

  public static void thenQuitWithError(final boolean conditionToQuit, final String message) {
    if (conditionToQuit) {
      InputOutput.<String>println(("[alpha.glue.v2.GenerateNewWriteC]:" + message));
      System.exit(1);
    }
  }
}
