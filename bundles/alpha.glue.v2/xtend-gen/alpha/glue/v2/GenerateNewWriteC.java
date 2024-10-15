package alpha.glue.v2;

import alpha.codegen.BaseDataType;
import alpha.codegen.Program;
import alpha.codegen.ProgramPrinter;
import alpha.codegen.demandDriven.WriteC;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.ComplexityCalculator;
import alpha.model.ReduceExpression;
import alpha.model.Variable;
import alpha.model.transformation.RemoveUnusedEquations;
import alpha.model.transformation.SubstituteByDef;
import alpha.model.transformation.automation.OptimalSimplifyingReductions;
import alpha.model.util.AShow;
import alpha.model.util.FaceLattice;
import alpha.model.util.ISLUtil;
import alpha.model.util.ShowLegacyAlpha;
import com.google.common.base.Objects;
import fr.irisa.cairn.jnimap.isl.ISLSet;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
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

  private static boolean runLegacySave = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_LEGACY_SAVE")));

  private static boolean runSimplification = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_SIMPLIFY")));

  private static int numOptimizations = GenerateNewWriteC.parseInt(System.getenv("ACC_NUM_SIMPLIFICATIONS"), 1);

  private static int targetComplexity = GenerateNewWriteC.parseInt(System.getenv("ACC_TARGET_COMPLEXITY"), (-1));

  private static boolean trySplitting = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_TRY_SPLITTING")));

  private static boolean verbose = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_VERBOSE")));

  private static BaseDataType baseDataType = GenerateNewWriteC.parseBaseDataType(System.getenv("ACC_BASE_DATATYPE"), BaseDataType.FLOAT);

  private static String faceLattice = GenerateNewWriteC.parseFaceLattice(System.getenv("ACC_FACE_LATTICE"), "");

  private static String faceLatticeStr = GenerateNewWriteC.parseFaceLattice(System.getenv("ACC_FACE_LATTICE_STR"), "");

  private static List<String> substituteNames = GenerateNewWriteC.parseList(System.getenv("ACC_SUBSTITUTE"));

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

  public static String parseFaceLattice(final String str, final String defaultValue) {
    String _xblockexpression = null;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return defaultValue;
      }
      _xblockexpression = str;
    }
    return _xblockexpression;
  }

  public static BaseDataType parseBaseDataType(final String str, final BaseDataType defaultValue) {
    BaseDataType _xblockexpression = null;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return defaultValue;
      }
      _xblockexpression = BaseDataType.valueOf(str.toUpperCase());
    }
    return _xblockexpression;
  }

  public static List<String> parseList(final String str) {
    String[] _xblockexpression = null;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
      }
      _xblockexpression = str.split(",");
    }
    return (List<String>)Conversions.doWrapArray(_xblockexpression);
  }

  public static List<Integer> parseIntList(final String str) {
    List<Integer> _xblockexpression = null;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList());
      }
      final Function1<String, Integer> _function = (String i) -> {
        return Integer.valueOf(Integer.parseInt(i));
      };
      _xblockexpression = ListExtensions.<String, Integer>map(((List<String>)Conversions.doWrapArray(str.split(","))), _function);
    }
    return _xblockexpression;
  }

  public static void main(final String[] args) {
    try {
      boolean _notEquals = (!Objects.equal(GenerateNewWriteC.faceLatticeStr, ""));
      if (_notEquals) {
        final ISLSet set = ISLUtil.toISLSet(GenerateNewWriteC.faceLatticeStr);
        final FaceLattice lattice = FaceLattice.create(set);
        InputOutput.<String>println(lattice.prettyPrint());
        return;
      }
      GenerateNewWriteC.thenQuitWithError((GenerateNewWriteC.alphaFile == null), "no input alpha file specified via ACC_ALPHA_FILE");
      GenerateNewWriteC.thenQuitWithError((GenerateNewWriteC.outDir == null), "no output directory specified via ACC_OUT_DIR");
      final AlphaRoot root = AlphaLoader.loadAlpha(GenerateNewWriteC.alphaFile);
      int _size = root.getSystems().size();
      GenerateNewWriteC.thenQuitWithError((_size > 1), "error: only single system alpha programs are supported by this tool");
      final AlphaSystem system = root.getSystems().get(0);
      int _size_1 = system.getSystemBodies().size();
      GenerateNewWriteC.thenQuitWithError((_size_1 > 1), "error: only systems with a single body are supported by this tool");
      boolean _notEquals_1 = (!Objects.equal(GenerateNewWriteC.faceLattice, ""));
      if (_notEquals_1) {
        final Function1<ReduceExpression, String> _function = (ReduceExpression re) -> {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Expression:");
          _builder.newLine();
          _builder.append("  ");
          String _print = AShow.print(re);
          _builder.append(_print, "  ");
          _builder.newLineIfNotEmpty();
          String _prettyPrint = re.getFacet().getLattice().prettyPrint();
          _builder.append(_prettyPrint);
          _builder.newLineIfNotEmpty();
          return _builder.toString();
        };
        final String ret = IterableExtensions.join(ListExtensions.<ReduceExpression, String>map(EcoreUtil2.<ReduceExpression>getAllContentsOfType(system, ReduceExpression.class), _function), "\n");
        InputOutput.<String>println(ret);
        return;
      }
      for (final String variableName : GenerateNewWriteC.substituteNames) {
        {
          final Function1<Variable, Boolean> _function_1 = (Variable it) -> {
            String _name = it.getName();
            return Boolean.valueOf((!Objects.equal(_name, variableName)));
          };
          GenerateNewWriteC.thenQuitWithError(IterableExtensions.<Variable>forall(system.getVariables(), _function_1), (("Cannot find the variable \"" + variableName) + "\" to substitute."));
          final Function1<Variable, Boolean> _function_2 = (Variable it) -> {
            String _name = it.getName();
            return Boolean.valueOf(Objects.equal(_name, variableName));
          };
          final Variable variable = IterableExtensions.<Variable>head(IterableExtensions.<Variable>filter(system.getVariables(), _function_2));
          SubstituteByDef.apply(system, variable);
          RemoveUnusedEquations.apply(system);
        }
      }
      if (GenerateNewWriteC.runLegacySave) {
        GenerateNewWriteC.generateV1Alpha(system, GenerateNewWriteC.outDir);
      }
      if (GenerateNewWriteC.runSimplification) {
        final OptimalSimplifyingReductions.State[] states = GenerateNewWriteC.optimize(system);
        GenerateNewWriteC.thenQuitWithError((states == null), "No simplifications found, exiting");
        final Function1<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>> _function_1 = (OptimalSimplifyingReductions.State s) -> {
          int _indexOf = ((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)).indexOf(s);
          return Pair.<Integer, OptimalSimplifyingReductions.State>of(Integer.valueOf(_indexOf), s);
        };
        final Consumer<Pair<Integer, OptimalSimplifyingReductions.State>> _function_2 = (Pair<Integer, OptimalSimplifyingReductions.State> pair) -> {
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
        ListExtensions.<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>>map(((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)), _function_1).forEach(_function_2);
      } else {
        GenerateNewWriteC.generateWriteC(system, GenerateNewWriteC.outDir);
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
        return null;
      }
      if ((GenerateNewWriteC.targetComplexity == (-1))) {
        int _complexity = ComplexityCalculator.complexity(system);
        int _minus = (_complexity - 1);
        GenerateNewWriteC.targetComplexity = _minus;
      }
      final OptimalSimplifyingReductions osr = OptimalSimplifyingReductions.apply(system, GenerateNewWriteC.numOptimizations, GenerateNewWriteC.targetComplexity, GenerateNewWriteC.trySplitting, GenerateNewWriteC.verbose);
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
      final Program program = WriteC.convert(system, GenerateNewWriteC.baseDataType, true);
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
    AlphaModelSaver.writeToFile(abFile, ShowLegacyAlpha.print(system, GenerateNewWriteC.baseDataType.toString().toLowerCase()));
  }

  public static void thenQuitWithError(final boolean conditionToQuit, final String message) {
    if (conditionToQuit) {
      InputOutput.<String>println(("[alpha.glue.v2.GenerateNewWriteC]:" + message));
      System.exit(1);
    }
  }
}
