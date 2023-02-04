package org.swdc.qt.font;

import io.qt.core.QByteArray;
import io.qt.core.QList;
import io.qt.gui.QFont;
import io.qt.gui.QFontDatabase;
import org.swdc.qt.QtApplication;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QtIconFontLoader {

    private static Map<String,String> locationFontFamilyMap = new HashMap<>();

    public static void loadFromModal(String resource, Map<FontSize, QFont> resourceMap) {
        if (locationFontFamilyMap.containsKey(resource)) {
            return;
        }
        try {
            Module module = QtApplication.class.getModule();
            InputStream fontIn = module.getResourceAsStream(resource);
            int id = QFontDatabase.addApplicationFontFromData(new QByteArray(fontIn.readAllBytes()));
            QList<String> fontFamilies = QFontDatabase.applicationFontFamilies(id);
            String family = fontFamilies.get(0);
            resourceMap.put(FontSize.LARGE,new QFont(family,64));
            resourceMap.put(FontSize.MIDDLE_LARGE,new QFont(family,32));
            resourceMap.put(FontSize.MIDDLE,new QFont(family,24));
            resourceMap.put(FontSize.MIDDLE_SMALL,new QFont(family,18));
            resourceMap.put(FontSize.SMALL,new QFont(family,16));
            resourceMap.put(FontSize.VERY_SMALL,new QFont(family,14));
            resourceMap.put(FontSize.SMALLEST,new QFont(family,12));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
