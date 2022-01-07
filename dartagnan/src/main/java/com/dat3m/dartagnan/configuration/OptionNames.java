package com.dat3m.dartagnan.configuration;

public class OptionNames {

	// Base Options
	public static final String PROPERTY = "property";
	public static final String METHOD = "method";
	public static final String SOLVER = "solver";
	public static final String TIMEOUT = "timeout";
	public static final String VALIDATE = "validate";
	
	// Encoding Options
	public static final String LOCALLY_CONSISTENT = "encoding.locallyConsistent";
	public static final String ALLOW_PARTIAL_EXECUTIONS = "encoding.allowPartialExecutions";
	public static final String MERGE_CF_VARS = "encoding.mergeCFVars";
	public static final String CO_ANTISYMMETRY = "encoding.co.antiSymm";
	public static final String ENCODE_FINAL_MEMVALUES = "encoding.encodeFinalMemoryValues";

	// Program Processing Options
	public static final String BOUND = "program.processing.loopBound";
	public static final String TARGET = "program.processing.compilationTarget";
	public static final String DETERMINISTIC_REORDERING = "program.processing.detReordering";
	public static final String REDUCE_SYMMETRY = "program.processing.reduceSymmetry";
	public static final String ATOMIC_BLOCKS_AS_LOCKS = "program.processing.atomicBlocksAsLocks";
	public static final String CONSTANT_PROPAGATION = "program.processing.constantPropagation";
	
	// Program Property Options
	public static final String ALWAYS_SPLIT_ON_JUMPS = "program.analysis.cf.alwaysSplitOnJump";
	public static final String MERGE_BRANCHES = "program.analysis.cf.mergeBranches";

	// Refinement Options
	public static final String ASSUME_LOCALLY_CONSISTENT_WMM = "refinement.assumeLocallyConsistentWMM";
	public static final String ASSUME_NO_OOTA = "refinement.assumeNoOOTA";
	
	// SMT solver Options
	public static final String PHANTOM_REFERENCES = "solver.z3.usePhantomReferences";

	// Witness Options
	public static final String WITNESS_ORIGINAL_PROGRAM_PATH = "witness.originalProgramFilePath";
	
	// SVCOMP Options
	public static final String PROPERTYPATH = "svcomp.property";
	public static final String UMIN = "svcomp.umin";
	public static final String UMAX = "svcomp.umax";
	public static final String STEP = "svcomp.step";
	public static final String SANITIZE = "svcomp.sanitize";
	public static final String OPTIMIZATION = "svcomp.optimization";
	public static final String INTEGER_ENCODING = "svcomp.integerEncoding";
}
