package com.ccp.implementations.file.bucket.gcp;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.file.bucket.CcpFileBucket;

/**
 * Provedor de DI que expõe {@code GcpFileBucket} como implementação de {@code CcpFileBucket}.
 */
public class CcpGcpFileBucket implements CcpInstanceProvider<CcpFileBucket> {

	public CcpFileBucket getInstance() {
		return new GcpFileBucket();
	}

}
