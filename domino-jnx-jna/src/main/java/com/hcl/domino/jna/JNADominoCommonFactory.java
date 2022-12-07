package com.hcl.domino.jna;


import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.commons.OSLoadStringProvider;
import com.hcl.domino.commons.richtext.structures.DefaultMemoryStructureWrapperService;
import com.hcl.domino.jna.data.JNANativeItemCoder;
import com.hcl.domino.jna.design.JNANativeDesignSupport;
import com.hcl.domino.jna.formula.JNAFormulaCompiler;
import com.hcl.domino.jna.naming.JNANames;
import com.hcl.domino.naming.Names;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoCommonFactory;

public class JNADominoCommonFactory implements DominoCommonFactory {
	public DominoProcess createDominoProcess() {
		return new JNADominoProcess();
	}
	
	public DominoClientBuilder createDominoClientBuilder() {
		return new JNADominoClientBuilder();
	}

	public MemoryStructureWrapperService createMemoryStructureWrapperService() {
		return new DefaultMemoryStructureWrapperService();
	}

	public Names createNames() {
		return new JNANames();
	}

	public NativeItemCoder createNativeItemCoder() {
		return new JNANativeItemCoder();
	}

	@Override
	public FormulaCompiler createFormulaCompiler() {
		return new JNAFormulaCompiler();
	}
	
	public OSLoadStringProvider createOSLoadStringProvier() {
		return new JNAOSLoadStringProvider();
	}
	
	public NativeDesignSupport createNativeDesignSupport() {
		return new JNANativeDesignSupport();
	}
}
