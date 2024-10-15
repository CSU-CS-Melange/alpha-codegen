package popl.artifact

import alpha.codegen.BaseDataType
import alpha.codegen.ProgramPrinter
import alpha.codegen.demandDriven.WriteC
import alpha.loader.AlphaLoader
import alpha.model.AlphaModelSaver
import alpha.model.AlphaSystem
import alpha.model.AlphaVisitable
import alpha.model.transformation.automation.OptimalSimplifyingReductions
import alpha.model.util.Show
import java.io.File
import java.io.FileWriter
import alpha.model.util.FaceLattice
import alpha.model.ReduceExpression

class Korra {
	
	static String outDir = 'resources/generated'
	
	def static void main(String[] args) {
		working_example
	}
	
	def static void working_example() {
		val root = AlphaLoader.loadAlpha('artifact/inputs/prefix_max.alpha')
		val system = root.systems.get(0)
		
		system.pprint('// input')
		
		system.runOSR
		
		system.generateWriteC('resources/generated')
	}
	
	
	
	
	
	/*
	 * Helper functions
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	static def void runOSR(AlphaSystem system) {
		val limit = 1
		val targetComplexity = 1
		val debug = true
		val trySplit = true
		val opts = OptimalSimplifyingReductions.apply(system, limit, targetComplexity, trySplit, debug).optimizations
		val states = opts.get(targetComplexity)
		
		println('\n\n\nSimplifications:\n')
		
		states.map[s | states.indexOf(s) -> s].forEach[pair |
			val state = pair.value
			val stateSystem = state.root.systems.get(0)
			println()
			println(state.show)
			val simplificationOutDir = '''«outDir»/«system.name»/simplifications/v«pair.key»'''
			stateSystem.generateWriteC(simplificationOutDir)
			AlphaModelSaver.writeToFile('''«simplificationOutDir»/«system.name».alpha''', state.show.toString)
		]
	}
	
	/** Generates the new (v2) demand driven code compatible with v1 for the given system */
	def static generateWriteC(AlphaSystem system, String outDir) {
		new File(outDir).mkdirs
		val program = WriteC.convert(system, BaseDataType.FLOAT, true)
		val code = ProgramPrinter.print(program).toString
		val writer = new FileWriter('''«outDir»/«system.name».c''')
		writer.write(code)
		writer.close
	}
	
	static def pprint(AlphaVisitable av, String msg) {
		println(msg)
		println(Show.print(av))
	}
}