package com.empresa.service;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.empresa.util.NegocioException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Las imagenes de productos se guardan en Cloudflare R2 (API compatible con S3).
 * En la BD (producto.imagenId) solo se guarda la clave del objeto; el navegador descarga la imagen
 * directo desde la URL publica del bucket, sin pasar por este servidor.
 */
@Service
public class ImagenServiceImpl implements ImagenService {

	private static final String PREFIJO = "productos/";

	// Tipos permitidos -> extension. (SVG se excluye a proposito: puede contener scripts.)
	private static final Map<String, String> TIPOS = Map.of(
			"image/jpeg", ".jpg",
			"image/png", ".png",
			"image/webp", ".webp",
			"image/gif", ".gif");

	@Value("${r2.endpoint:}")
	private String endpoint;
	@Value("${r2.bucket:}")
	private String bucket;
	@Value("${r2.access-key:}")
	private String accessKey;
	@Value("${r2.secret-key:}")
	private String secretKey;
	@Value("${r2.public-url:}")
	private String publicUrl;

	private S3Client cliente; // se crea la primera vez que se usa

	private synchronized S3Client cliente() {
		if (cliente == null) {
			if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
				throw new NegocioException("El almacenamiento de imágenes (R2) no está configurado en el servidor");
			}
			cliente = S3Client.builder()
					.endpointOverride(URI.create(endpoint))
					.region(Region.of("auto"))
					.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
					// R2 no acepta los checksums adicionales que el SDK agrega por defecto
					.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
					.responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
					.build();
		}
		return cliente;
	}

	@Override
	public String guardaImagen(MultipartFile archivo) throws Exception {
		String tipo = archivo.getContentType();
		String extension = tipo == null ? null : TIPOS.get(tipo);
		if (extension == null) {
			throw new NegocioException("Formato no permitido. Usa JPG, PNG, WEBP o GIF");
		}
		String clave = PREFIJO + UUID.randomUUID() + extension;
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucket)
				.key(clave)
				.contentType(tipo)
				.cacheControl("public, max-age=31536000, immutable") // la clave cambia si cambia la imagen
				.build();
		cliente().putObject(request, RequestBody.fromBytes(archivo.getBytes()));
		return clave;
	}

	@Override
	public String urlPublica(String clave) {
		if (clave == null || publicUrl.isBlank()) {
			return null;
		}
		return publicUrl.endsWith("/") ? publicUrl + clave : publicUrl + "/" + clave;
	}

	@Override
	public void eliminaImagen(String clave) {
		// Solo claves generadas por este servicio
		if (clave == null || !clave.startsWith(PREFIJO) || clave.contains("..")) {
			throw new NegocioException("Clave de imagen no válida");
		}
		cliente().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
	}

}
