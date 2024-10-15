package popl.artifact;

import alpha.codegen.BaseDataType;
import alpha.codegen.Program;
import alpha.codegen.ProgramPrinter;
import alpha.codegen.demandDriven.WriteC;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.AlphaVisitable;
import alpha.model.transformation.automation.OptimalSimplifyingReductions;
import alpha.model.util.Show;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class Korra {
  private static String outDir = "resources/generated";

  public static void main(final String[] args) {
    Korra.working_example();
  }

  public static void working_example() {
    try {
      final AlphaRoot root = AlphaLoader.loadAlpha("artifact/inputs/prefix_max.alpha");
      final AlphaSystem system = root.getSystems().get(0);
      Korra.pprint(system, "// input");
      Korra.runOSR(system);
      Korra.generateWriteC(system, "resources/generated");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * Helper functions
   */
  public static void runOSR(final AlphaSystem system) {
    final int limit = 1;
    final int targetComplexity = 1;
    final boolean debug = true;
    final boolean trySplit = true;
    final Map<Integer, List<OptimalSimplifyingReductions.State>> opts = OptimalSimplifyingReductions.apply(system, limit, targetComplexity, trySplit, debug).optimizations;
    final List<OptimalSimplifyingReductions.State> states = opts.get(Integer.valueOf(targetComplexity));
    InputOutput.<String>println("\n\n\nSimplifications:\n");
    final Function1<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>> _function = (OptimalSimplifyingReductions.State s) -> {
      int _indexOf = states.indexOf(s);
      return Pair.<Integer, OptimalSimplifyingReductions.State>of(Integer.valueOf(_indexOf), s);
    };
    final Consumer<Pair<Integer, OptimalSimplifyingReductions.State>> _function_1 = (Pair<Integer, OptimalSimplifyingReductions.State> pair) -> {
      final OptimalSimplifyingReductions.State state = pair.getValue();
      final AlphaSystem stateSystem = state.root().getSystems().get(0);
      InputOutput.println();
      InputOutput.<CharSequence>println(state.show());
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(Korra.outDir);
      _builder.append("/");
      String _name = system.getName();
      _builder.append(_name);
      _builder.append("/simplifications/v");
      Integer _key = pair.getKey();
      _builder.append(_key);
      final String simplificationOutDir = _builder.toString();
      Korra.generateWriteC(stateSystem, simplificationOutDir);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append(simplificationOutDir);
      _builder_1.append("/");
      String _name_1 = system.getName();
      _builder_1.append(_name_1);
      _builder_1.append(".alpha");
      AlphaModelSaver.writeToFile(_builder_1.toString(), state.show().toString());
    };
    ListExtensions.<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>>map(states, _function).forEach(_function_1);
  }

  /**
   * Generates the new (v2) demand driven code compatible with v1 for the given system
   */
  public static void generateWriteC(final AlphaSystem system, final String outDir) {
    try {
      new File(outDir).mkdirs();
      final Program program = WriteC.convert(system, BaseDataType.FLOAT, true);
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

  public static String pprint(final AlphaVisitable av, final String msg) {
    String _xblockexpression = null;
    {
      InputOutput.<String>println(msg);
      _xblockexpression = InputOutput.<String>println(Show.<AlphaVisitable>print(av));
    }
    return _xblockexpression;
  }
}
