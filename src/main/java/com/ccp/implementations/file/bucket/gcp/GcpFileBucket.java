package com.ccp.implementations.file.bucket.gcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import com.ccp.especifications.file.bucket.CcpFileBucket;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * {@code CcpFileBucket} implementation for Google Cloud Storage. Authenticates with
 * credentials read from the {@code credentials_file} environment variable and exposes file
 * read ({@code get}), write ({@code save}) and delete ({@code delete}) operations.
 */
class GcpFileBucket implements CcpFileBucket {

	/**
	 * Builds the Cloud Storage client of the project, with the credentials of the {@code credentials_file} environment
	 * variable. The credentials file is closed right after being read; until 2026-10-07 every operation left it open.
	 * @param tenant the GCP project id
	 * @return the client
	 * @throws IOException when the credentials file cannot be read
	 */
	private Storage getStorage(String tenant) throws IOException {
		String credentialsFilePath = System.getenv("credentials_file");
		try (FileInputStream fileInputStream = new FileInputStream(credentialsFilePath)) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(fileInputStream);
			StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
			StorageOptions.Builder builderWithProjectId = storageOptionsBuilder.setProjectId(tenant);
			StorageOptions.Builder builderWithCredentials = builderWithProjectId.setCredentials(credentials);
			StorageOptions storageOptions = builderWithCredentials.build();
			Storage service = storageOptions.getService();
			return service;
		}
	}

	/**
	 * Encodes the bytes of a file in Base64, as they are. Until 2026-10-07 the bytes went through a string in the
	 * platform charset before being encoded, so any byte sequence invalid in that charset (PDF, image, docx) came back
	 * replaced and the file was corrupted.
	 * @param content the bytes of the file
	 * @return the content in Base64
	 */
	static String toBase64(byte[] content) {
		Base64.Encoder encoder = Base64.getEncoder();
		String contentAsBase64 = encoder.encodeToString(content);
		return contentAsBase64;
	}

	/**
	 * Reads the file and returns its bytes in Base64 (see {@link #toBase64(byte[])}).
	 * @param tenant the GCP project id
	 * @param bucketName the bucket
	 * @param fileName the file
	 * @return the content in Base64
	 * @throws CcpErrorGcpFileBucketOperation when the operation fails
	 */
	public String get(String tenant, String bucketName, String fileName) {
		try {
			Storage service = this.getStorage(tenant);
			Blob blob = service.get(bucketName, fileName);
			byte[] content = blob.getContent();
			String contentAsBase64 = toBase64(content);
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
			Storage service = this.getStorage(tenant);
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
			Storage service = this.getStorage(tenant);
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
			Storage service = this.getStorage(tenant);
			Page<Blob> blobsPage = service.list(bucketName);
			var allBlobs = blobsPage.iterateAll();
			for (Blob blob : allBlobs) {
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
