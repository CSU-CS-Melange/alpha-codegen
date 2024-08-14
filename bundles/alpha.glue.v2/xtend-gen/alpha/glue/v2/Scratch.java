package alpha.glue.v2;

import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.ComplexityCalculator;
import alpha.model.transformation.automation.OptimalSimplifyingReductions;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class Scratch {
  private static String outDir = "resources";

  public static void main(final String[] args) {
    try {
      final AlphaRoot root = AlphaLoader.loadAlpha("resources/simplification-inputs/star2d1r_abft_v1_5_25_25.alpha");
      final AlphaSystem system = root.getSystems().get(0);
      final OptimalSimplifyingReductions.State[] states = Scratch.optimize(system);
      final Function1<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>> _function = (OptimalSimplifyingReductions.State s) -> {
        int _indexOf = ((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)).indexOf(s);
        return Pair.<Integer, OptimalSimplifyingReductions.State>of(Integer.valueOf(_indexOf), s);
      };
      final Consumer<Pair<Integer, OptimalSimplifyingReductions.State>> _function_1 = (Pair<Integer, OptimalSimplifyingReductions.State> pair) -> {
        final OptimalSimplifyingReductions.State state = pair.getValue();
        final AlphaSystem stateSystem = state.root().getSystems().get(0);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(Scratch.outDir);
        _builder.append("/simplifications/v");
        Integer _key = pair.getKey();
        _builder.append(_key);
        final String simplificationOutDir = _builder.toString();
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append(simplificationOutDir);
        _builder_1.append("/");
        String _name = system.getName();
        _builder_1.append(_name);
        _builder_1.append(".alpha");
        AlphaModelSaver.writeToFile(_builder_1.toString(), state.show().toString());
      };
      ListExtensions.<OptimalSimplifyingReductions.State, Pair<Integer, OptimalSimplifyingReductions.State>>map(((List<OptimalSimplifyingReductions.State>)Conversions.doWrapArray(states)), _function).forEach(_function_1);
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
      int _complexity = ComplexityCalculator.complexity(system);
      final int targetComplexity = (_complexity - 1);
      final boolean trySplitting = false;
      final boolean verbose = true;
      final OptimalSimplifyingReductions osr = OptimalSimplifyingReductions.apply(system, 1, targetComplexity, trySplitting, verbose);
      _xblockexpression = osr.optimizations.get(Integer.valueOf(targetComplexity));
    }
    return ((OptimalSimplifyingReductions.State[])Conversions.unwrapArray(_xblockexpression, OptimalSimplifyingReductions.State.class));
  }
}
