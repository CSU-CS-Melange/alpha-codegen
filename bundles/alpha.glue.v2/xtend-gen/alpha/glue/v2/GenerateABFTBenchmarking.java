package alpha.glue.v2;

import alpha.abft.ABFT;
import alpha.codegen.BaseDataType;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.AlphaVisitable;
import alpha.model.util.AShow;
import alpha.model.util.AlphaUtil;
import java.util.Collections;
import java.util.List;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class GenerateABFTBenchmarking {
  private static String outDir = System.getenv("ACC_OUT_DIR");

  private static boolean verbose = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_VERBOSE")));

  private static BaseDataType baseDataType = GenerateABFTBenchmarking.parseBaseDataType(System.getenv("ACC_BASE_DATATYPE"), BaseDataType.FLOAT);

  public static void main(final String[] args) {
    try {
      int _size = ((List<String>)Conversions.doWrapArray(args)).size();
      GenerateABFTBenchmarking.thenQuitWithError((_size < 3), "GenerateABFTBenchmarking received an invalid number of arguments");
      GenerateABFTBenchmarking.thenQuitWithError((GenerateABFTBenchmarking.outDir == null), "no output directory specified via ACC_OUT_DIR");
      final String alphaFile = args[0];
      int _size_1 = ((List<String>)Conversions.doWrapArray(args)).size();
      final Function1<Integer, Integer> _function = (Integer i) -> {
        return Integer.valueOf(Integer.parseInt(args[(i).intValue()]));
      };
      final Iterable<Integer> tileSizes = IterableExtensions.<Integer, Integer>map(new ExclusiveRange(1, _size_1, true), _function);
      final AlphaRoot root = AlphaLoader.loadAlpha(alphaFile);
      int _size_2 = root.getSystems().size();
      GenerateABFTBenchmarking.thenQuitWithError((_size_2 > 1), "error: only single system alpha programs are supported by this tool");
      final AlphaSystem system = root.getSystems().get(0);
      int _size_3 = system.getSystemBodies().size();
      GenerateABFTBenchmarking.thenQuitWithError((_size_3 > 1), "error: only systems with a single body are supported by this tool");
      final AlphaSystem systemV1 = AlphaUtil.<AlphaRoot>copyAE(root).getSystems().get(0);
      final AlphaSystem systemV2 = AlphaUtil.<AlphaRoot>copyAE(root).getSystems().get(0);
      GenerateABFTBenchmarking.pprint(system, "// Input program");
      ABFT.insertChecksumV1(systemV1, ((int[])Conversions.unwrapArray(tileSizes, int.class)));
      ABFT.insertChecksumV2(systemV2, ((int[])Conversions.unwrapArray(tileSizes, int.class)));
      GenerateABFTBenchmarking.pprint(systemV1, "// ABFTv1");
      GenerateABFTBenchmarking.pprint(systemV2, "// ABFTv2");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static String pprint(final AlphaVisitable av, final String msg) {
    String _xifexpression = null;
    if (GenerateABFTBenchmarking.verbose) {
      String _xblockexpression = null;
      {
        InputOutput.<String>println(msg);
        _xblockexpression = InputOutput.<String>println(AShow.print(av));
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }

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

  public static void thenQuitWithError(final boolean conditionToQuit, final String message) {
    if (conditionToQuit) {
      InputOutput.<String>println(("[alpha.glue.v2.GenerateNewWriteC]: " + message));
      System.exit(1);
    }
  }
}
