package alpha.glue.v2

import alpha.abft.ABFTv3
import alpha.abft.codegen.Version
import alpha.codegen.BaseDataType
import alpha.loader.AlphaLoader
import alpha.model.AlphaModelSaver
import alpha.model.AlphaSystem
import alpha.model.AlphaVisitable
import alpha.model.transformation.Normalize
import alpha.model.transformation.reduction.NormalizeReduction
import alpha.model.util.AShow
import java.util.List

import static alpha.abft.codegen.Timer.*
import static java.lang.System.getenv

import static extension alpha.abft.ABFT.identify_convolution
import static extension alpha.abft.ABFT.insertChecksumV1
import static extension alpha.abft.ABFT.insertChecksumV2
import static extension alpha.abft.ABFT.insertChecksumV3
import static extension alpha.abft.codegen.BenchmarkInstance.baselineMemoryMap
import static extension alpha.abft.codegen.BenchmarkInstance.baselineSchedule
import static extension alpha.abft.codegen.BenchmarkInstance.v1MemoryMap
import static extension alpha.abft.codegen.BenchmarkInstance.v1Schedule
import static extension alpha.abft.codegen.BenchmarkInstance.v2MemoryMap
import static extension alpha.abft.codegen.BenchmarkInstance.v2Schedule
import static extension alpha.abft.codegen.BenchmarkInstance.v3MemoryMap
import static extension alpha.abft.codegen.BenchmarkInstance.v3Schedule
import static extension alpha.abft.codegen.Makefile.generateMakefile
import static extension alpha.abft.codegen.SystemCodeGen.generateSystemCode
import static extension alpha.abft.codegen.WrapperCodeGen.generateWrapper
import static extension alpha.model.util.AlphaUtil.copyAE
import static extension java.lang.Integer.parseInt

class GenerateABFTBenchmarking {
	
	static String outDir = getenv('ACC_OUT_DIR')
	static boolean verbose = !(getenv('ACC_VERBOSE').isNullOrEmpty)
	static BaseDataType baseDataType = parseBaseDataType(getenv('ACC_BASE_DATATYPE'), BaseDataType.FLOAT)
	static Version version = parseVersion(getenv('ACC_ABFT_VERSION'), null)
	
	def static void main(String[] args) {
		
		// check (env variable) arguments
		(args.size < 3).thenQuitWithError('GenerateABFTBenchmarking received an invalid number of arguments')
		(outDir === null).thenQuitWithError('no output directory specified via ACC_OUT_DIR')
		
		val alphaFile = args.get(0)
		val _tileSizes = (1..<args.size).map[i | args.get(i).parseInt].toList
		
		// Read input alpha program
		val root = AlphaLoader.loadAlpha(alphaFile)
		(root.systems.size > 1).thenQuitWithError('error: only single system alpha programs are supported by this tool')
		val system = root.systems.get(0)
		(system.systemBodies.size > 1).thenQuitWithError('error: only systems with a single body are supported by this tool')
		
		/* Set tilesizes for v1 and v2 */
		val TT = _tileSizes.get(0)
		val v1TileSizes = _tileSizes
		var v2TileSizes = null as int[]
		
		/* Code generation */
		val srcOutDir = outDir + '/src'
		
		
		system.generateSystemCode(system.baselineSchedule(), system.baselineMemoryMap, Version.BASELINE, v1TileSizes).save(srcOutDir, system.name + '.c')
		var systemV1 = null as AlphaSystem
		var systemV2 = null as AlphaSystem
		var systemV3 = null as AlphaSystem
		if (version === null || version == Version.ABFT_V1) {
			systemV1 = root.copyAE.systems.get(0)
			systemV1.insertChecksumV1(v1TileSizes).normalize
//			systemV1.generateSystemCode(systemV1.v1Schedule(v1TileSizes), systemV1.v1MemoryMap, Version.ABFT_V1, v1TileSizes).save(srcOutDir, systemV1.name + '.c')
		}
		if (version === null || version == Version.ABFT_V2) {
			val kernel = system.identify_convolution
			val radius = kernel.key
			val v2TXs = (1..<_tileSizes.size).map[i |
					_tileSizes.get(i) + 2 * TT * radius
			]
			v2TileSizes = #[TT] + v2TXs
			systemV2 = root.copyAE.systems.get(0)
			systemV2.insertChecksumV2(v2TileSizes).normalize
//			systemV2.generateSystemCode(systemV2.v2Schedule(TT), systemV2.v2MemoryMap, Version.ABFT_V2, v2TileSizes).save(srcOutDir, systemV2.name + '.c')
		}
		if (version === null || version == Version.ABFT_V3) {
			systemV3 = root.copyAE.systems.get(0)
			systemV3 = ABFTv3.insertChecksum(systemV3, _tileSizes).normalize
			systemV3.generateSystemCode(systemV3.v3Schedule(_tileSizes), systemV3.v3MemoryMap, Version.ABFT_V3, _tileSizes).save(srcOutDir, systemV3.name + '.c')
			systemV3.dbgASave(srcOutDir)
		}
		system.generateWrapper(systemV1, systemV2, systemV3, systemV3.v3MemoryMap, Version.WRAPPER, v1TileSizes, v2TileSizes).save(srcOutDir, system.name + '-wrapper.c')
		system.generateMakefile(systemV1, systemV2, systemV3, v1TileSizes).save(outDir, 'Makefile')
		generateTimer.save(srcOutDir, 'time.c')
	}
	
	def static body(AlphaSystem system) {
		system.systemBodies.get(0)
	}
	
	def static normalize(AlphaSystem system) {
		Normalize.apply(system)
		NormalizeReduction.apply(system)
		system
	}
	
	def static ASave(AlphaSystem system, String dir) {
		val fileName = dir + '/' + system.name + '.alpha'
		AlphaModelSaver.writeToFile(fileName, AShow.print(system))
		system
	}
	
	def static dbgASave(AlphaSystem system, String dir) {
		
		val copySys = system.copyAE
		
		val Y = copySys.outputs.get(0)
		val I = copySys.locals.findFirst[name == 'I']
		
		copySys.outputs.remove(Y)
		copySys.locals.add(Y)
		
		copySys.locals.remove(I)
		copySys.outputs.add(I)
		
		val fileName = dir + '/' + copySys.name + '.alpha'
		AlphaModelSaver.writeToFile(fileName, AShow.print(copySys))
		copySys
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
	
	def static parseVersion(String str, Version defaultValue) {
		if (str.isNullOrEmpty) return defaultValue
		switch (str.toUpperCase) {
			case 'V1' : Version.ABFT_V1
			case 'V2' : Version.ABFT_V2
			case 'V3' : Version.ABFT_V3
			default : defaultValue	
		}
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
	
	def static save(CharSequence code, String dir, String fileName) {
		val fullFileName = dir + '/' + fileName
		AlphaModelSaver.writeToFile(fullFileName, code.toString)
	}
	
}