package com.empresa.controller;

import java.util.HashMap;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.empresa.service.ImagenService;
import com.empresa.util.NegocioException;

import lombok.RequiredArgsConstructor;

/**
 * Imagenes de productos en Cloudflare R2 (solo administracion, requiere JWT).
 * - subir    : POST   /url/imagen/subir            (multipart "archivo") -> { idImagen = clave en R2, url }
 * - eliminar : DELETE /url/imagen/elimina?clave=productos/xxxx.jpg
 * Para mostrar la imagen no hay endpoint: el frontend usa la URL publica del bucket + la clave.
 */
@RestController
@RequestMapping("/url/imagen")
@RequiredArgsConstructor
public class ImagenController {

	private final ImagenService imagenService;

	@ResponseBody
	@PostMapping("/subir")
	public HashMap<String, Object> subirImagen(@RequestParam MultipartFile archivo) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			if (archivo.isEmpty()) {
				mapSalida.put("mensaje", "El archivo está vacío");
				return mapSalida;
			}
			String clave = imagenService.guardaImagen(archivo);
			mapSalida.put("mensaje", "Imagen subida");
			mapSalida.put("idImagen", clave);
			mapSalida.put("url", imagenService.urlPublica(clave));
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		} catch (Exception e) {
			mapSalida.put("mensaje", "Error al subir la imagen");
		}
		return mapSalida;
	}

	@ResponseBody
	@DeleteMapping("/elimina")
	public HashMap<String, Object> eliminaImagen(@RequestParam String clave) {
		HashMap<String, Object> mapSalida = new HashMap<String, Object>();
		try {
			imagenService.eliminaImagen(clave);
			mapSalida.put("mensaje", "Se eliminó la imagen");
		} catch (NegocioException e) {
			mapSalida.put("mensaje", e.getMessage());
		} catch (Exception e) {
			mapSalida.put("mensaje", "Error al eliminar la imagen");
		}
		return mapSalida;
	}

}
