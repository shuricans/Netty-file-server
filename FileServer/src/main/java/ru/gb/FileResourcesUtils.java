package ru.gb;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileResourcesUtils {
    private static final String FOLDER = "static";

    public List<String> getResourceFiles() {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filenames;
    }

    private InputStream getResourceAsStream() {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(FOLDER);

        return in == null ? getClass().getResourceAsStream(FOLDER) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public File getFileFromResource(String fileName) {
        fileName = FOLDER + System.getProperty("file.separator") + fileName;
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            return null;
        } else {

            // failed if files have whitespaces or special characters
            // return new File(resource.getFile());
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}