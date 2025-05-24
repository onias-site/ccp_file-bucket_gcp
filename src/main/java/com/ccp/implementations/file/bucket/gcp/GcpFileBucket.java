package com.ccp.implementations.file.bucket.gcp;

import java.io.FileInputStream;

import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.file.bucket.CcpFileBucket;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

class GcpFileBucket implements CcpFileBucket {
	
	public String get(String tenant, String bucketName, String fileName) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv);
			Storage service = StorageOptions.newBuilder().setProjectId(tenant)
					.setCredentials(
							GoogleCredentials.fromStream(fileInputStream))
					.build().getService(); 
			com.google.cloud.storage.Blob blob = service.get(bucketName, fileName);
			byte[] content = blob.getContent();
			String encodeToString = new CcpStringDecorator(content).text().asBase64().content;
			return encodeToString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	
	public String delete(String tenant, String bucketName, String fileName) {
		// FIXME EXCLUIR BUCKET
		return "";
	}


	public String save(String tenant, String bucketName, String fileName, String fileContent) {
		// FIXME SALVAR BUCKET
		return "";
		
	}

	public String delete(String tenant, String bucketName) {
		// FIXME Auto-generated method stub
		return null;
	}
}
