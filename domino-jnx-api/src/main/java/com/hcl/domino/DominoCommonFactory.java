package com.hcl.domino;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.naming.Names;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

public interface DominoCommonFactory {
	static DominoCommonFactory INSTANCE = JNXServiceFinder.findRequiredService(DominoCommonFactory.class, DominoCommonFactory.class.getClassLoader());
	
	public static DominoCommonFactory getCommonFactory() {
		return INSTANCE;
	}
	
	DominoProcess createDominoProcess();
	DominoClientBuilder createDominoClientBuilder();
	MemoryStructureWrapperService createMemoryStructureWrapperService();
	Names createNames();
	NativeItemCoder createNativeItemCoder();
	FormulaCompiler createFormulaCompiler();
	NativeDesignSupport createNativeDesignSupport();
}
