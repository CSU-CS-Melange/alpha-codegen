package alpha.glue.v2;

import alpha.abft.ABFT;
import alpha.abft.ABFTv3;
import alpha.abft.codegen.BenchmarkInstance;
import alpha.abft.codegen.Makefile;
import alpha.abft.codegen.SystemCodeGen;
import alpha.abft.codegen.Timer;
import alpha.abft.codegen.Version;
import alpha.abft.codegen.WrapperCodeGen;
import alpha.codegen.BaseDataType;
import alpha.loader.AlphaLoader;
import alpha.model.AlphaModelSaver;
import alpha.model.AlphaRoot;
import alpha.model.AlphaSystem;
import alpha.model.AlphaVisitable;
import alpha.model.SystemBody;
import alpha.model.Variable;
import alpha.model.transformation.Normalize;
import alpha.model.transformation.reduction.NormalizeReduction;
import alpha.model.util.AShow;
import alpha.model.util.AlphaUtil;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class GenerateABFTBenchmarking {
  private static String outDir = System.getenv("ACC_OUT_DIR");

  private static boolean verbose = (!StringExtensions.isNullOrEmpty(System.getenv("ACC_VERBOSE")));

  private static BaseDataType baseDataType = GenerateABFTBenchmarking.parseBaseDataType(System.getenv("ACC_BASE_DATATYPE"), BaseDataType.FLOAT);

  private static Version version = GenerateABFTBenchmarking.parseVersion(System.getenv("ACC_ABFT_VERSION"), null);

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
      final List<Integer> _tileSizes = IterableExtensions.<Integer>toList(IterableExtensions.<Integer, Integer>map(new ExclusiveRange(1, _size_1, true), _function));
      final AlphaRoot root = AlphaLoader.loadAlpha(alphaFile);
      int _size_2 = root.getSystems().size();
      GenerateABFTBenchmarking.thenQuitWithError((_size_2 > 1), "error: only single system alpha programs are supported by this tool");
      final AlphaSystem system = root.getSystems().get(0);
      int _size_3 = system.getSystemBodies().size();
      GenerateABFTBenchmarking.thenQuitWithError((_size_3 > 1), "error: only systems with a single body are supported by this tool");
      final Integer TT = _tileSizes.get(0);
      final List<Integer> v1TileSizes = _tileSizes;
      int[] v2TileSizes = ((int[]) null);
      final String srcOutDir = (GenerateABFTBenchmarking.outDir + "/src");
      String _generateSystemCode = SystemCodeGen.generateSystemCode(system, BenchmarkInstance.baselineSchedule(system), BenchmarkInstance.baselineMemoryMap(system), Version.BASELINE, ((int[])Conversions.unwrapArray(v1TileSizes, int.class)));
      String _name = system.getName();
      String _plus = (_name + ".c");
      GenerateABFTBenchmarking.save(_generateSystemCode, srcOutDir, _plus);
      AlphaSystem systemV1 = ((AlphaSystem) null);
      AlphaSystem systemV2 = ((AlphaSystem) null);
      AlphaSystem systemV3 = ((AlphaSystem) null);
      if (((GenerateABFTBenchmarking.version == null) || Objects.equal(GenerateABFTBenchmarking.version, Version.ABFT_V1))) {
        systemV1 = AlphaUtil.<AlphaRoot>copyAE(root).getSystems().get(0);
        GenerateABFTBenchmarking.normalize(ABFT.insertChecksumV1(systemV1, ((int[])Conversions.unwrapArray(v1TileSizes, int.class))));
      }
      if (((GenerateABFTBenchmarking.version == null) || Objects.equal(GenerateABFTBenchmarking.version, Version.ABFT_V2))) {
        final Pair<Integer, Map<List<Integer>, Double>> kernel = ABFT.identify_convolution(system);
        final Integer radius = kernel.getKey();
        int _size_4 = _tileSizes.size();
        final Function1<Integer, Integer> _function_1 = (Integer i) -> {
          Integer _get = _tileSizes.get((i).intValue());
          return Integer.valueOf(((_get).intValue() + ((2 * (TT).intValue()) * (radius).intValue())));
        };
        final Iterable<Integer> v2TXs = IterableExtensions.<Integer, Integer>map(new ExclusiveRange(1, _size_4, true), _function_1);
        Iterable<Integer> _plus_1 = Iterables.<Integer>concat(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(TT)), v2TXs);
        v2TileSizes = ((int[])Conversions.unwrapArray(_plus_1, int.class));
        systemV2 = AlphaUtil.<AlphaRoot>copyAE(root).getSystems().get(0);
        GenerateABFTBenchmarking.normalize(ABFT.insertChecksumV2(systemV2, v2TileSizes));
      }
      if (((GenerateABFTBenchmarking.version == null) || Objects.equal(GenerateABFTBenchmarking.version, Version.ABFT_V3))) {
        systemV3 = AlphaUtil.<AlphaRoot>copyAE(root).getSystems().get(0);
        systemV3 = GenerateABFTBenchmarking.normalize(ABFTv3.insertChecksum(systemV3, ((int[])Conversions.unwrapArray(_tileSizes, int.class))));
        String _generateSystemCode_1 = SystemCodeGen.generateSystemCode(systemV3, BenchmarkInstance.v3Schedule(systemV3, ((int[])Conversions.unwrapArray(_tileSizes, int.class))), BenchmarkInstance.v3MemoryMap(systemV3), Version.ABFT_V3, ((int[])Conversions.unwrapArray(_tileSizes, int.class)));
        String _name_1 = systemV3.getName();
        String _plus_2 = (_name_1 + ".c");
        GenerateABFTBenchmarking.save(_generateSystemCode_1, srcOutDir, _plus_2);
        GenerateABFTBenchmarking.dbgASave(systemV3, srcOutDir);
      }
      String _generateWrapper = WrapperCodeGen.generateWrapper(system, systemV1, systemV2, systemV3, BenchmarkInstance.v3MemoryMap(systemV3), Version.WRAPPER, ((int[])Conversions.unwrapArray(v1TileSizes, int.class)), v2TileSizes);
      String _name_2 = system.getName();
      String _plus_3 = (_name_2 + "-wrapper.c");
      GenerateABFTBenchmarking.save(_generateWrapper, srcOutDir, _plus_3);
      GenerateABFTBenchmarking.save(Makefile.generateMakefile(system, systemV1, systemV2, systemV3, ((int[])Conversions.unwrapArray(v1TileSizes, int.class))), GenerateABFTBenchmarking.outDir, "Makefile");
      GenerateABFTBenchmarking.save(Timer.generateTimer(), srcOutDir, "time.c");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static SystemBody body(final AlphaSystem system) {
    return system.getSystemBodies().get(0);
  }

  public static AlphaSystem normalize(final AlphaSystem system) {
    AlphaSystem _xblockexpression = null;
    {
      Normalize.apply(system);
      NormalizeReduction.apply(system);
      _xblockexpression = system;
    }
    return _xblockexpression;
  }

  public static AlphaSystem ASave(final AlphaSystem system, final String dir) {
    AlphaSystem _xblockexpression = null;
    {
      String _name = system.getName();
      String _plus = ((dir + "/") + _name);
      final String fileName = (_plus + ".alpha");
      AlphaModelSaver.writeToFile(fileName, AShow.print(system));
      _xblockexpression = system;
    }
    return _xblockexpression;
  }

  public static AlphaSystem dbgASave(final AlphaSystem system, final String dir) {
    AlphaSystem _xblockexpression = null;
    {
      final AlphaSystem copySys = AlphaUtil.<AlphaSystem>copyAE(system);
      final Variable Y = copySys.getOutputs().get(0);
      final Function1<Variable, Boolean> _function = (Variable it) -> {
        String _name = it.getName();
        return Boolean.valueOf(Objects.equal(_name, "I"));
      };
      final Variable I = IterableExtensions.<Variable>findFirst(copySys.getLocals(), _function);
      copySys.getOutputs().remove(Y);
      copySys.getLocals().add(Y);
      copySys.getLocals().remove(I);
      copySys.getOutputs().add(I);
      String _name = copySys.getName();
      String _plus = ((dir + "/") + _name);
      final String fileName = (_plus + ".alpha");
      AlphaModelSaver.writeToFile(fileName, AShow.print(copySys));
      _xblockexpression = copySys;
    }
    return _xblockexpression;
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

  public static Version parseVersion(final String str, final Version defaultValue) {
    Version _xblockexpression = null;
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(str);
      if (_isNullOrEmpty) {
        return defaultValue;
      }
      Version _switchResult = null;
      String _upperCase = str.toUpperCase();
      if (_upperCase != null) {
        switch (_upperCase) {
          case "V1":
            _switchResult = Version.ABFT_V1;
            break;
          case "V2":
            _switchResult = Version.ABFT_V2;
            break;
          case "V3":
            _switchResult = Version.ABFT_V3;
            break;
          default:
            _switchResult = defaultValue;
            break;
        }
      } else {
        _switchResult = defaultValue;
      }
      _xblockexpression = _switchResult;
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

  public static void save(final CharSequence code, final String dir, final String fileName) {
    final String fullFileName = ((dir + "/") + fileName);
    AlphaModelSaver.writeToFile(fullFileName, code.toString());
  }
}
