package com.ccp.implementations.file.bucket.gcp;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.file.bucket.CcpFileBucket;

/**
 * DI provider that exposes {@code GcpFileBucket} as the {@code CcpFileBucket} implementation.
 */
public class CcpGcpFileBucket implements CcpInstanceProvider<CcpFileBucket> {

	/**
	 * Builds the Google Cloud Storage implementation of {@code CcpFileBucket}.
	 * @return a new {@code GcpFileBucket}
	 */
	public CcpFileBucket getInstance() {
		GcpFileBucket gcpFileBucket = new GcpFileBucket();
		return gcpFileBucket;
	}

}
