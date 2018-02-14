package org.rebecalang.compiler.cakka;

import com.squareup.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;

public class Utils {
    public static void writeToFile(File source, JavaFile javaFile) {
        try {
            javaFile.writeTo(new File(source.getParent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String PACKAGE_NAME = "org.rebecalang.cakka";
}
