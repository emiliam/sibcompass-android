package fi.yle.sibkompassi.extension;

import com.android.vending.expansion.zipfile.APEZProvider;

public class ZipFileContentProvider extends APEZProvider {

    @Override
    public String getAuthority() {
        return "fi.yle.sibkompassi.extension.ZipFileContentProvider";
    }
}