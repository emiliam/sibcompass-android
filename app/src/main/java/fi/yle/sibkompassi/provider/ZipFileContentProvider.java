package fi.yle.sibkompassi.provider;

import java.io.File;

import android.net.Uri;

import com.android.vending.expansion.zipfile.APEZProvider;

public class ZipFileContentProvider extends APEZProvider {
    private static final String AUTHORITY =  "fi.yle.sibkompassi.provider.ZipFileContentProvider";

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    public static Uri buildUri(String path) {
        StringBuilder contentPath = new StringBuilder("content://");
        contentPath.append(AUTHORITY);
        contentPath.append(File.separator);
        contentPath.append(path);
        String newPath = contentPath.toString();
        return Uri.parse(newPath);
    }
}