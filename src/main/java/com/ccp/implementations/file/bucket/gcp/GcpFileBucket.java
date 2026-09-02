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
import com.ccp.decorators.CcpTextDecorator;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;

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
			StorageOptions.Builder newBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder setProjectId = newBuilder.setProjectId(tenant);
			GoogleCredentials fromStream = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder setCredentials = setProjectId
					.setCredentials( 
							fromStream);
							StorageOptions build = setCredentials
							.build();
							Storage service = build.getService(); 
			com.google.cloud.storage.Blob blob = service.get(bucketName, fileName);
			byte[] content = blob.getContent();
			CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(content);
			CcpTextDecorator ccpStringDecoratorText = ccpStringDecorator.text();
			var asBase64 = ccpStringDecoratorText.asBase64();
			String encodeToString = asBase64.content;
			return encodeToString;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation;
		}

	}

	
	public String delete(String tenant, String bucketName, String fileName) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv); 
			StorageOptions.Builder newBuilder2 = StorageOptions.newBuilder();
			StorageOptions.Builder setProjectId2 = newBuilder2.setProjectId(tenant);
			GoogleCredentials fromStream2 = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder setCredentials2 = setProjectId2
					.setCredentials(fromStream2);
					StorageOptions build2 = setCredentials2
					.build();
					Storage service = build2.getService();
					BlobId blobIdOf = BlobId.of(bucketName, fileName);
					service.delete(blobIdOf);
			return fileName;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation2 = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation2;
		}

	}


	public String save(String tenant, String bucketName, String fileName, String fileContent) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv);
			StorageOptions.Builder newBuilder3 = StorageOptions.newBuilder();
			StorageOptions.Builder setProjectId3 = newBuilder3.setProjectId(tenant);
			GoogleCredentials fromStream3 = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder setCredentials3 = setProjectId3
					.setCredentials(fromStream3);
					StorageOptions build3 = setCredentials3
					.build();
					Storage service = build3.getService();
					Base64.Decoder decoder = Base64.getDecoder();
					byte[] bytes = decoder.decode(fileContent);
					BlobId blobIdOf2 = BlobId.of(bucketName, fileName);
					BlobInfo.Builder newBuilder4 = BlobInfo.newBuilder(blobIdOf2);
					BlobInfo blobInfo = newBuilder4.build();
			service.create(blobInfo, bytes);
			return fileName;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation3 = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation3;
		}

	}

	public String delete(String tenant, String bucketName) {
		try {
			String getenv = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(getenv);
			StorageOptions.Builder newBuilder5 = StorageOptions.newBuilder();
			StorageOptions.Builder setProjectId4 = newBuilder5.setProjectId(tenant);
			GoogleCredentials fromStream4 = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder setCredentials4 = setProjectId4
					.setCredentials(fromStream4);
					StorageOptions build4 = setCredentials4
					.build();
					Storage service = build4.getService();
					Page<Blob> serviceList = service.list(bucketName);
					var iterateAll = serviceList.iterateAll();
					for (com.google.cloud.storage.Blob blob : iterateAll) {
				blob.delete();
			}
			service.delete(bucketName);
			return bucketName;

		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation4 = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation4;
		}
	}

	@SuppressWarnings("serial")
	private static class CcpErrorGcpFileBucketOperation extends RuntimeException {
		private CcpErrorGcpFileBucketOperation(Throwable cause) {
			super(cause);
		}
	}
}
