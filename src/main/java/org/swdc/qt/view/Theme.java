package org.swdc.qt.view;

import com.asual.lesscss.LessEngine;
import io.qt.core.QDir;
import io.qt.core.QList;
import io.qt.gui.QBrush;
import io.qt.gui.QColor;
import io.qt.gui.QFontDatabase;
import io.qt.gui.QPalette;
import io.qt.widgets.QApplication;
import io.qt.widgets.QWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 关于Qt的主题，包含两个部分，一个是样式表（stage.less），另一个是
 * ThemePalette，ThemePalette出现在AbstractQtView的子类中，提供一组
 * 和主题相关的颜色，用于绘制自定义的组件，如果没有也是可以的，这些颜色存储在主题的
 * 文件夹，放在stage.less旁边就行，命名为colors.properties，key是QPalette的
 * ColorRole的Name。
 *
 */
public class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    private static Map<String,Theme> themes = new ConcurrentHashMap<>();

    private String name;

    private File assetsRoot;

    private QPalette palette;

    private String styleTexts;

    private boolean ready;

    public Theme(String name, File assets) {
        this.name = name;
        this.assetsRoot = assets;
    }

    /**
     * 编译主题的less
     */
    private void prepare() {
        File themeFolder = assetsRoot.toPath()
                .resolve("skin")
                .resolve(this.name)
                .toFile();
        if (!themeFolder.exists()) {
            throw new RuntimeException("样式的文件夹不存在：" + themeFolder.getAbsolutePath());
        }
        QDir.addSearchPath("asset",themeFolder.getAbsolutePath());
        File[] files = themeFolder.listFiles();
        if (files == null) {
            throw new RuntimeException("样式文件夹是空的：" + themeFolder.getAbsolutePath());
        }
        try {
            for (File file: files) {
                if (file.isFile() && file.getName().endsWith("less")) {
                    String cssName = file.getName().replace("less", "css");
                    File css = new File(file.getParent() + File.separator + cssName);
                    if (css.exists()) {
                        css.delete();
                    }
                    LessEngine lessEngine = new LessEngine();
                    lessEngine.compile(file,css);
                } else if (file.isFile() && (
                        file.getName().toLowerCase().endsWith("ttf") ||
                                file.getName().toLowerCase().endsWith("otf")||
                                file.getName().toLowerCase().endsWith("ttc"))) {
                    try {
                        int loadedId = QFontDatabase.addApplicationFont(file.getAbsolutePath());
                        if (loadedId == -1) {
                            logger.error("can not load font: " + file.toPath());
                        } else {
                            QList<String> families = QFontDatabase.applicationFontFamilies(loadedId);
                            StringBuilder sb = new StringBuilder("family of file : ").append(file.getName());
                            for (String family: families) {
                                sb.append(" ").append(family).append(",");
                            }
                            sb.append(" has loaded.");
                            logger.info(" font :" + sb);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            this.ready = true;
        } catch (Exception e) {
            throw new RuntimeException("less编译失败：",e);
        }
    }

    /**
     * 给view添加样式
     * @param view
     */
    public void applyWithView(AbstractQtView view) {
        if (!this.ready) {
            this.prepare();
        }

        File skinAssets = assetsRoot.toPath()
                .resolve("skin")
                .resolve(this.name)
                .toFile();

        try {
            if (styleTexts == null) {
                Path defaultStyle = skinAssets
                        .toPath()
                        .resolve("stage.css")
                        .toAbsolutePath();
                byte[] data = Files.readAllBytes(defaultStyle);
                styleTexts = new String(data, StandardCharsets.UTF_8);
            }

            if (palette == null) {
                palette = new QPalette();
                Path colorSchema = skinAssets.toPath()
                        .resolve("colors.properties")
                        .toAbsolutePath();

                if (Files.exists(colorSchema)) {
                    Properties properties = new Properties();
                    InputStream in = Files.newInputStream(colorSchema);
                    properties.load(in);
                    in.close();

                    for (String key: properties.stringPropertyNames()) {
                        try {
                            QPalette.ColorRole role = QPalette.ColorRole.valueOf(key);
                            QColor color = fromString(properties.getProperty(key));
                            palette.setBrush(role, new QBrush(color));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }

            QWidget widget = (QWidget)view;
            widget.setStyleSheet(styleTexts);

        } catch (Exception e){
            throw new RuntimeException("渲染出现异常：",e);
        }
    }

    public static Theme getTheme(String name,File assets) {
        if (themes.containsKey(name)) {
            return themes.get(name);
        }
        Theme theme = new Theme(name,assets);
        theme.prepare();
        themes.put(name,theme);
        return theme;
    }

    public QPalette getPalette() {
        return palette;
    }

    public static QColor fromString(String colorStr) {
        colorStr = colorStr.toLowerCase();
        if (colorStr.startsWith("#")) {
            // hex string
            colorStr = colorStr.substring(1);
            if (colorStr.length() == 3) {
                // RGB
                return QColor.fromRgb(
                        Integer.parseInt(colorStr.substring(0,1).repeat(2),16),
                        Integer.parseInt(colorStr.substring(1,2).repeat(2),16),
                        Integer.parseInt(colorStr.substring(2).repeat(2),16)
                );
            } else if (colorStr.length() == 6) {
                // 两位RGB
                return QColor.fromRgb(
                        Integer.parseInt(colorStr.substring(0, 2), 16),
                        Integer.parseInt(colorStr.substring(2, 4), 16),
                        Integer.parseInt(colorStr.substring(4, 6), 16)
                );
            } else if (colorStr.length() == 8) {
                // 两位RGBA
                return QColor.fromRgb(
                        Integer.parseInt(colorStr.substring(0, 2), 16),
                        Integer.parseInt(colorStr.substring(2, 4), 16),
                        Integer.parseInt(colorStr.substring(4, 6), 16),
                        Integer.parseInt(colorStr.substring(6, 8), 16)
                );
            }
        } else if (colorStr.startsWith("rgb")) {
            if (colorStr.startsWith("rgb(")) {
                colorStr = colorStr.replace("rgb(","")
                        .replace(")","");
                String[] rgb = colorStr.split(",");
                return QColor.fromRgb(
                        Integer.parseInt(rgb[0]),
                        Integer.parseInt(rgb[1]),
                        Integer.parseInt(rgb[2])
                );
            } else if (colorStr.startsWith("rgba(")) {
                colorStr = colorStr.replace("rgba(","")
                        .replace(")","");
                String[] rgba = colorStr.split(",");
                String a = rgba[3];
                if (a.indexOf('.') > 0) {
                    double alpha = Double.parseDouble(a);
                    int intAlpha = (int)(alpha * 255);
                    a = "" + intAlpha;
                }
                return QColor.fromRgb(
                        Integer.parseInt(rgba[0]),
                        Integer.parseInt(rgba[1]),
                        Integer.parseInt(rgba[2]),
                        Integer.parseInt(a)
                );
            }
        }
        return null;
    }

}
