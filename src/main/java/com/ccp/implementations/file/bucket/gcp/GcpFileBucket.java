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
 * {@code CcpFileBucket} implementation for Google Cloud Storage. Authenticates with
 * credentials read from the {@code credentials_file} environment variable and exposes file
 * read ({@code get}), write ({@code save}) and delete ({@code delete}) operations.
 */
class GcpFileBucket implements CcpFileBucket {
	
	/**
	 * Reads the file and returns its content in Base64. The bytes go through a platform-charset string before being
	 * encoded, so binary content may be altered.
	 * @param tenant the GCP project id
	 * @param bucketName the bucket
	 * @param fileName the file
	 * @return the content in Base64
	 * @throws CcpErrorGcpFileBucketOperation when the operation fails
	 */
	public String get(String tenant, String bucketName, String fileName) {
		try {
			String credentialsFilePath = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(credentialsFilePath);
			StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder builderWithProjectId = storageOptionsBuilder.setProjectId(tenant);
			GoogleCredentials credentials = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder builderWithCredentials = builderWithProjectId
					.setCredentials( 
							credentials);
							StorageOptions storageOptions = builderWithCredentials
							.build();
							Storage service = storageOptions.getService(); 
			com.google.cloud.storage.Blob blob = service.get(bucketName, fileName);
			byte[] content = blob.getContent();
			CcpStringDecorator contentDecorator = new CcpStringDecorator(content);
			CcpTextDecorator contentText = contentDecorator.text();
			var asBase64 = contentText.asBase64();
			String contentAsBase64 = asBase64.content;
			return contentAsBase64;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation;
		}

	}

	
	/**
	 * Deletes the file.
	 * @param tenant the GCP project id
	 * @param bucketName the bucket
	 * @param fileName the file
	 * @return the file name
	 * @throws CcpErrorGcpFileBucketOperation when the operation fails
	 */
	public String delete(String tenant, String bucketName, String fileName) {
		try {
			String credentialsFilePath = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(credentialsFilePath); 
			StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder builderWithProjectId = storageOptionsBuilder.setProjectId(tenant);
			GoogleCredentials credentials = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder builderWithCredentials = builderWithProjectId
					.setCredentials(credentials);
					StorageOptions storageOptions = builderWithCredentials
					.build();
					Storage service = storageOptions.getService();
					BlobId blobId = BlobId.of(bucketName, fileName);
					service.delete(blobId);
			return fileName;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation;
		}

	}


	/**
	 * Decodes the Base64 content and writes it as the file.
	 * @param tenant the GCP project id
	 * @param bucketName the bucket
	 * @param fileName the file
	 * @param fileContent the content in Base64
	 * @return the file name
	 * @throws CcpErrorGcpFileBucketOperation when the operation fails
	 */
	public String save(String tenant, String bucketName, String fileName, String fileContent) {
		try {
			String credentialsFilePath = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(credentialsFilePath);
			StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder builderWithProjectId = storageOptionsBuilder.setProjectId(tenant);
			GoogleCredentials credentials = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder builderWithCredentials = builderWithProjectId
					.setCredentials(credentials);
					StorageOptions storageOptions = builderWithCredentials
					.build();
					Storage service = storageOptions.getService();
					Base64.Decoder decoder = Base64.getDecoder();
					byte[] bytes = decoder.decode(fileContent);
					BlobId blobId = BlobId.of(bucketName, fileName);
					BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId);
					BlobInfo blobInfo = blobInfoBuilder.build();
			service.create(blobInfo, bytes);
			return fileName;
			
		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation;
		}

	}

	/**
	 * Deletes every file of the bucket and then the bucket itself.
	 * @param tenant the GCP project id
	 * @param bucketName the bucket
	 * @return the bucket name
	 * @throws CcpErrorGcpFileBucketOperation when the operation fails
	 */
	public String delete(String tenant, String bucketName) {
		try {
			String credentialsFilePath = System.getenv("credentials_file");
			FileInputStream fileInputStream = new FileInputStream(credentialsFilePath);
			StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder builderWithProjectId = storageOptionsBuilder.setProjectId(tenant);
			GoogleCredentials credentials = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder builderWithCredentials = builderWithProjectId
					.setCredentials(credentials);
					StorageOptions storageOptions = builderWithCredentials
					.build();
					Storage service = storageOptions.getService();
					Page<Blob> blobsPage = service.list(bucketName);
					var allBlobs = blobsPage.iterateAll();
					for (com.google.cloud.storage.Blob blob : allBlobs) {
				blob.delete();
			}
			service.delete(bucketName);
			return bucketName;

		} catch (Exception e) {
			CcpErrorGcpFileBucketOperation ccpErrorGcpFileBucketOperation = new CcpErrorGcpFileBucketOperation(e);
			throw ccpErrorGcpFileBucketOperation;
		}
	}

	/** Raised when a Cloud Storage operation fails. */
	@SuppressWarnings("serial")
	private static class CcpErrorGcpFileBucketOperation extends RuntimeException {
		/**
		 * Wraps the cause.
		 * @param cause the original failure
		 */
		private CcpErrorGcpFileBucketOperation(Throwable cause) {
			super(cause);
		}
	}
}
