package com.ccp.implementations.file.bucket.gcp;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.file.bucket.CcpFileBucket;

/**
 * DI provider that exposes {@code GcpFileBucket} as the {@code CcpFileBucket} implementation.
 */
public class CcpGcpFileBucket implements CcpInstanceProvider<CcpFileBucket> {
	/** The single instance: the class keeps no state. */
	public final static CcpGcpFileBucket INSTANCE = new CcpGcpFileBucket();

	private CcpGcpFileBucket() {}

	/**
	 * Builds the Google Cloud Storage implementation of {@code CcpFileBucket}.
	 * @return a new {@code GcpFileBucket}
	 */
	public CcpFileBucket getInstance() {
		GcpFileBucket gcpFileBucket = GcpFileBucket.INSTANCE;
		return gcpFileBucket;
	}

}
