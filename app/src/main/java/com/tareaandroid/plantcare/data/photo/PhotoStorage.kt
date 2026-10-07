package com.tareaandroid.plantcare.data.photo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Archivo de foto: [path] se guarda en Room y [uri] se entrega a la app de cámara. */
data class PhotoFile(val path: String, val uri: Uri)

/**
 * Guarda las fotos de las plantas en el almacenamiento privado de la app
 * (`files/photos/`), así no hace falta ningún permiso de almacenamiento.
 */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { mkdirs() }

    /** Crea un archivo vacío para que la cámara escriba en él la foto. */
    fun createPhotoFile(): PhotoFile {
        val file = File(photosDir, "plant_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return PhotoFile(file.absolutePath, uri)
    }

    /** Copia una imagen elegida de la galería a la carpeta de la app. @return su ruta o `null`. */
    suspend fun importFromGallery(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(photosDir, "plant_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)!!.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }.getOrNull()
    }

    /** Borra una foto; solo actúa sobre archivos de la carpeta de la app. */
    fun delete(path: String?) {
        val file = path?.let(::File) ?: return
        if (file.parentFile == photosDir) file.delete()
    }
}
