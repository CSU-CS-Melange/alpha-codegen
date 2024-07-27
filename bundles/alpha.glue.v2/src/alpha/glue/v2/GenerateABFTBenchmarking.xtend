package alpha.glue.v2

import alpha.codegen.BaseDataType
import alpha.loader.AlphaLoader
import alpha.model.AlphaVisitable
import alpha.model.util.AShow
import java.util.List

import static java.lang.System.getenv

import static extension alpha.abft.ABFT.insertChecksumV1
import static extension alpha.abft.ABFT.insertChecksumV2
import static extension alpha.model.util.AlphaUtil.copyAE
import static extension java.lang.Integer.parseInt

class GenerateABFTBenchmarking {
	
	static String outDir = getenv('ACC_OUT_DIR')
	static boolean verbose = !(getenv('ACC_VERBOSE').isNullOrEmpty)
	static BaseDataType baseDataType = parseBaseDataType(getenv('ACC_BASE_DATATYPE'), BaseDataType.FLOAT)
	
	def static void main(String[] args) {
		// check (env variable) arguments
		(args.size < 3).thenQuitWithError('GenerateABFTBenchmarking received an invalid number of arguments')
		(outDir === null).thenQuitWithError('no output directory specified via ACC_OUT_DIR')
		
		val alphaFile = args.get(0)
		val tileSizes = (1..<args.size).map[i | args.get(i).parseInt]
				
		// Read input alpha program
		val root = AlphaLoader.loadAlpha(alphaFile)
		(root.systems.size > 1).thenQuitWithError('error: only single system alpha programs are supported by this tool')
		val system = root.systems.get(0)
		(system.systemBodies.size > 1).thenQuitWithError('error: only systems with a single body are supported by this tool')
		
		// Create copies for each of the ABFT version schemes
		val systemV1 = root.copyAE.systems.get(0)
		val systemV2 = root.copyAE.systems.get(0)
		
		system.pprint('// Input program')
		
		systemV1.insertChecksumV1(tileSizes)
		systemV2.insertChecksumV2(tileSizes)
		
		/*
		 * 
		 * TODO - do code generation here
		 * 
		 */
		
		systemV1.pprint('// ABFTv1')
		systemV2.pprint('// ABFTv2')
	}
	
	def static pprint(AlphaVisitable av, String msg) {
		if (verbose) {
			println(msg)
			println(AShow.print(av))
		}
	}
	
	def static parseInt(String str, int defaultValue) {
		if (str.isNullOrEmpty) return defaultValue
		Integer.parseInt(str)
	}
	
	def static parseBaseDataType(String str, BaseDataType defaultValue) {
		if (str.isNullOrEmpty) return defaultValue
		BaseDataType.valueOf(str.toUpperCase)
	}
	
	def static List<Integer> parseIntList(String str) {
		if (str.isNullOrEmpty) return #[]
		str.split(",").map[i | Integer.parseInt(i)]
	}
	
	def static thenQuitWithError(boolean conditionToQuit, String message) {
		if (conditionToQuit) {
			println('[alpha.glue.v2.GenerateNewWriteC]: ' + message)
			System.exit(1)
		}
	}
}