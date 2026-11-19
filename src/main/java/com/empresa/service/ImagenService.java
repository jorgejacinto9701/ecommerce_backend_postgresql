package com.empresa.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImagenService {

	/** Sube la imagen a R2 y devuelve su clave (ej: productos/uuid.jpg). */
	public abstract String guardaImagen(MultipartFile archivo) throws Exception;

	/** URL publica de una clave, o null si no hay URL publica configurada. */
	public abstract String urlPublica(String clave);

	public abstract void eliminaImagen(String clave);

}
