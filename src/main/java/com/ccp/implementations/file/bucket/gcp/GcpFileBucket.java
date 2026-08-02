package com.ccp.implementations.file.bucket.gcp;

import java.io.FileInputStream;
import java.util.Base64;

import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.file.bucket.CcpFileBucket;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * Implementação de {@code CcpFileBucket} para o Google Cloud Storage. Autentica via
 * credenciais lidas da variável de ambiente {@code credentials_file} e expõe operações
 * de leitura ({@code get}), gravação ({@code save}) e exclusão ({@code delete}) de arquivos.
 */
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
			throw new CcpErrorGcpFileBucketOperation(e);
		}

	}

	
	public String delete(String tenant, String bucketName, String fileName) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv); 
			Storage service = StorageOptions.newBuilder().setProjectId(tenant)
					.setCredentials(GoogleCredentials.fromStream(fileInputStream))
					.build().getService();
			service.delete(BlobId.of(bucketName, fileName));
			return fileName;
			
		} catch (Exception e) {
			throw new CcpErrorGcpFileBucketOperation(e);
		}

	}


	public String save(String tenant, String bucketName, String fileName, String fileContent) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv);
			Storage service = StorageOptions.newBuilder().setProjectId(tenant)
					.setCredentials(GoogleCredentials.fromStream(fileInputStream))
					.build().getService();
			byte[] bytes = Base64.getDecoder().decode(fileContent);
			BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, fileName)).build();
			service.create(blobInfo, bytes);
			return fileName;
			
		} catch (Exception e) {
			throw new CcpErrorGcpFileBucketOperation(e);
		}

	}

	public String delete(String tenant, String bucketName) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv);
			Storage service = StorageOptions.newBuilder().setProjectId(tenant)
					.setCredentials(GoogleCredentials.fromStream(fileInputStream))
					.build().getService();
			for (com.google.cloud.storage.Blob blob : service.list(bucketName).iterateAll()) {
				blob.delete();
			}
			service.delete(bucketName);
			return bucketName;

		} catch (Exception e) {
			throw new CcpErrorGcpFileBucketOperation(e);
		}
	}

	@SuppressWarnings("serial")
	private static class CcpErrorGcpFileBucketOperation extends RuntimeException {
		private CcpErrorGcpFileBucketOperation(Throwable cause) {
			super(cause);
		}
	}
}
