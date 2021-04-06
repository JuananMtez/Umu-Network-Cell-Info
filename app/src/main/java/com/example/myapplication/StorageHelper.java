package com.example.myapplication;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StorageHelper {
    /**
     * Escribe (sobrescribiendo si el archivo ya existía) el contenido "content" en un fichero llamado "filename" en el alamacenamiento "externo" de la app. Este se podrá
     * encontrar a través de un explorador de archivos (PC o el del propio teléfono) en AlmacenamientoInternoCompartido>Android>Data>(nombre paquete de app)>.
     * Los ficheros así almacenados se borrarán si la aplicación se desinstala.
     * @param filename Nombre del fichero
     * @param content Contenido que se escribirá en el fichero
     * @param context Contexto (e.g. Activity) desde el que se llama al método
     * @throws IOException
     */
    public static void saveStringToFile(String filename,String content, Context context) throws IOException {
        File a = context.getExternalFilesDir(null);
        File file = new File(a, filename);
        FileWriter writer= new FileWriter(file,false);
        writer.write(content);
        writer.flush();
        writer.close();
    }

}
