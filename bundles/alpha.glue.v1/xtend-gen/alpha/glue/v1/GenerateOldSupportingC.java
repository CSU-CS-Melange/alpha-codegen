package alpha.glue.v1;

import alphaz.mde.Basic;
import alphaz.mde.CodeGen;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.polymodel.polyhedralIR.AffineSystem;
import org.polymodel.polyhedralIR.Program;

@SuppressWarnings("all")
public class GenerateOldSupportingC {
  public static void main(final String[] args) {
    int _size = ((List<String>)Conversions.doWrapArray(args)).size();
    GenerateOldSupportingC.thenQuitWithError((_size < 2), "usage: alpha_v1_file out_dir");
    final String alphaFile = args[0];
    final String outDir = args[1];
    final Program prog = Basic.ReadAlphabets(alphaFile);
    int _size_1 = prog.getSystems().size();
    GenerateOldSupportingC.thenQuitWithError((_size_1 > 1), "error: only single system alpha programs are supported by this tool");
    final AffineSystem system = prog.getSystems().get(0);
    CodeGen.generateVerificationCode(prog, system.getName(), outDir);
    CodeGen.generateWrapper(prog, system.getName(), outDir);
    CodeGen.generateMakefile(prog, system.getName(), outDir, true);
  }

  public static void thenQuitWithError(final boolean conditionToQuit, final String message) {
    if (conditionToQuit) {
      InputOutput.<String>println(message);
      System.exit(1);
    }
  }
}
