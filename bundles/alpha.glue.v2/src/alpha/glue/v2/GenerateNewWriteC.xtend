package alpha.glue.v2

import alpha.codegen.ProgramPrinter
import alpha.codegen.writeC.SystemConverter
import alpha.loader.AlphaLoader
import alpha.model.AlphaModelSaver
import alpha.model.AlphaSystem
import alpha.model.util.ShowLegacyAlpha
import java.io.FileWriter

/**
 * This class is a wrapper around the new demand driven code generator.
 * It is intended to be used in conjunction with its sibling package
 * alpha.glue.v1 to generate a complete program that can be compiled.
 * 
 */

class GenerateNewWriteC {
	
	def static void main(String[] args) {

		// Check args
		(args.size < 2).thenQuitWithError('usage: alpha_v2_file out_dir [choice]')
		
		val alphaFile = args.get(0)
		val outDir = args.get(1)
		val choice = if (args.size > 2) Integer.parseInt(args.get(2)) else 0
		
		// Read input alpha program
		val root = AlphaLoader.loadAlpha(alphaFile)
		(root.systems.size > 1).thenQuitWithError('error: only single system alpha programs are supported by this tool')
		val system = root.systems.get(0)
		
		if (choice == 1 || choice == 0) {			
			// Generate the v1 alpha before any transformations
			system.generateV1Alpha(outDir)
		}
		
		if (choice == 2 || choice == 0) {
			
			//
			// Placeholder for processing any transformations like simplifying reductions
			//
			
			// Generate the v2 demand driven code
			system.generateWriteC(outDir)			
		}
	}
	
	/** Generates the old (v1) alphaz ab file for the given system */
	def static void generateV1Alpha(AlphaSystem system, String outDir) {
		val abFile = '''«outDir»/«system.name».ab'''
		AlphaModelSaver.writeToFile(abFile, ShowLegacyAlpha.print(system))		
	}
	
	/** Generates the new (v2) demand driven code compatible with v1 for the given system */
	def static generateWriteC(AlphaSystem system, String outDir) {
		val program = SystemConverter.convert(system, true)
		val code = ProgramPrinter.print(program).toString
		val writer = new FileWriter('''«outDir»/«system.name».c''')
		writer.write(code)
		writer.close
	}
	
	def static thenQuitWithError(boolean conditionToQuit, String message) {
		if (conditionToQuit) {
			println(message)
			System.exit(1)
		}
	}
}
