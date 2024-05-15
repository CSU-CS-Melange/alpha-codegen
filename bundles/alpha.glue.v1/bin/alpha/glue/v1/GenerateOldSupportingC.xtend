package alpha.glue.v1

import alphaz.mde.Basic
import alphaz.mde.CodeGen

class GenerateOldSupportingC {
	
	def static void main(String[] args) {
		
		// Check args		
		(args.size < 2).thenQuitWithError('usage: alpha_v1_file out_dir')
		
		val alphaFile = args.get(0)
		val outDir = args.get(1)
		
		// Read input alpha program
		val prog = Basic.ReadAlphabets(alphaFile)
		(prog.systems.size > 1).thenQuitWithError('error: only single system alpha programs are supported by this tool')
		val system = prog.systems.get(0)
		
		// Generate supporting C files needed for compilation
		CodeGen.generateVerificationCode(prog, system.name, outDir)
		CodeGen.generateWrapper(prog, system.name, outDir)
		CodeGen.generateMakefile(prog, system.name, outDir, true)
	}
	
	def static thenQuitWithError(boolean conditionToQuit, String message) {
		if (conditionToQuit) {
			println(message)
			System.exit(1)
		}
		
	}
}