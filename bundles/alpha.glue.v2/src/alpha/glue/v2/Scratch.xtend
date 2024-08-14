package alpha.glue.v2

import alpha.loader.AlphaLoader
import alpha.model.AlphaModelSaver
import alpha.model.AlphaSystem
import alpha.model.transformation.automation.OptimalSimplifyingReductions
import alpha.model.transformation.automation.OptimalSimplifyingReductions.State

import static extension alpha.model.ComplexityCalculator.complexity

class Scratch {
	
	static String outDir = 'resources'
	
	def static void main(String[] args) {
		// Read input alpha program
		val root = AlphaLoader.loadAlpha('resources/simplification-inputs/star2d1r_abft_v1_5_25_25.alpha')
		val system = root.systems.get(0)
		
		// Hooks to expose optimization
		val states = system.optimize;
		
		states.map[s | states.indexOf(s) -> s].forEach[pair |
			val state = pair.value
			val stateSystem = state.root.systems.get(0)
			val simplificationOutDir = '''«outDir»/simplifications/v«pair.key»'''
//				stateSystem.generateWriteC(simplificationOutDir)
			AlphaModelSaver.writeToFile('''«simplificationOutDir»/«system.name».alpha''', state.show.toString)
		]
		
	}
	
	/** Run the simplifying reductions algorithm */
	def static State[] optimize(AlphaSystem system) {
		
//		OptimalSimplifyingReductions.DEBUG = debug
//		SimplifyingReductions.DEBUG = debug
		
		val targetComplexity = system.complexity - 1
		val trySplitting = false
		val verbose = true
		val osr = OptimalSimplifyingReductions.apply(system, 1, targetComplexity, trySplitting, verbose)
		
		// All optimized roots are guaranteed to have a single system
		osr.optimizations.get(targetComplexity)
	}
}