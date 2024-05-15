package alpha.glue.v2

import alpha.codegen.ProgramPrinter
import alpha.codegen.writeC.SystemConverter
import alpha.loader.AlphaLoader
import alpha.model.AlphaModelSaver
import alpha.model.AlphaSystem
import alpha.model.transformation.automation.OptimalSimplifyingReductions
import alpha.model.util.ShowLegacyAlpha
import java.io.File
import java.io.FileWriter

import static java.lang.System.getenv

import static extension alpha.model.ComplexityCalculator.complexity
import static extension alpha.model.util.AlphaUtil.getContainerRoot
import alpha.model.transformation.automation.OptimalSimplifyingReductions.State

/**
 * This class is a wrapper around the new demand driven code generator.
 * It is intended to be used in conjunction with its sibling package
 * alpha.glue.v1 to generate a complete program that can be compiled.
 * 
 */

class GenerateNewWriteC {
	
	static String alphaFile = getenv('ACC_ALPHA_FILE')
	static String outDir = getenv('ACC_OUT_DIR')
	static int choice = parseInt(getenv('ACC_CHOICE'), 0)
	static boolean runSimplification = getenv('ACC_SIMPLIFY') !== null
	static int numOptimizations = parseInt(getenv('ACC_NUM_SIMPLIFICATIONS'), 1)
	static int targetComplexity = parseInt(getenv('ACC_TARGET_COMPLEXITY'), -1)
	static boolean trySplitting = getenv('ACC_TRY_SPLITTING') !== null

	def static parseInt(String str, int defaultValue) {
		if (str.isNullOrEmpty) return defaultValue
		Integer.parseInt(str)
	}
	
	def static void main(String[] args) {

		// check (env variable) arguments
		(alphaFile === null).thenQuitWithError('no input alpha file specified via ACC_ALPHA_FILE')
		(outDir === null).thenQuitWithError('no output directory specified via ACC_OUT_DIR')
		
		// Read input alpha program
		val root = AlphaLoader.loadAlpha(alphaFile)
		(root.systems.size > 1).thenQuitWithError('error: only single system alpha programs are supported by this tool')
		val system = root.systems.get(0)
		(system.systemBodies.size > 1).thenQuitWithError('error: only systems with a single body are supported by this tool')
		
		if (choice == 1 || choice == 0) {			
			// Generate the v1 alpha file from the input program along
			system.generateV1Alpha(outDir)
		}
		
		if (choice == 2 || choice == 0) {
			// Hooks to expose optimization
			
			val states = system.optimize
			if (states.size == 0) {
				System.exit(1)
			}
			states.map[s | states.indexOf(s) -> s].forEach[pair |
				val state = pair.value
				val stateSystem = state.root.systems.get(0)
				val simplificationOutDir = '''«outDir»/simplifications/v«pair.key»'''
				stateSystem.generateWriteC(simplificationOutDir)
				AlphaModelSaver.writeToFile('''«simplificationOutDir»/«system.name».alpha''', state.show.toString)
			]			
		}
	}
	
	/** Run the simplifying reductions algorithm */
	def static State[] optimize(AlphaSystem system) {
		if (! runSimplification) return #[new State(system.systemBodies.get(0), newLinkedList)]
		
//		OptimalSimplifyingReductions.DEBUG = debug
//		SimplifyingReductions.DEBUG = debug
		if (targetComplexity == -1)
			targetComplexity = system.complexity - 1
		val osr = OptimalSimplifyingReductions.apply(system, numOptimizations, targetComplexity, trySplitting)
		
		// All optimized roots are guaranteed to have a single system
		osr.optimizations.get(targetComplexity)
	}
	
	/** Generates the new (v2) demand driven code compatible with v1 for the given system */
	def static generateWriteC(AlphaSystem system, String outDir) {
		new File(outDir).mkdirs
		val program = SystemConverter.convert(system, true)
		val code = ProgramPrinter.print(program).toString
		val writer = new FileWriter('''«outDir»/«system.name».c''')
		writer.write(code)
		writer.close
	}
	
	/** Generates the old (v1) alphaz ab file for the given system */
	def static void generateV1Alpha(AlphaSystem system, String outDir) {
		val abFile = '''«outDir»/«system.name».ab'''
		AlphaModelSaver.writeToFile(abFile, ShowLegacyAlpha.print(system))		
	}
	
	def static thenQuitWithError(boolean conditionToQuit, String message) {
		if (conditionToQuit) {
			println('[alpha.glue.v2.GenerateNewWriteC]:' + message)
			System.exit(1)
		}
	}
}