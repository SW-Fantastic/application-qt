package org.swdc.qt.view;

import com.asual.lesscss.LessEngine;
import io.qt.core.QList;
import io.qt.gui.QFontDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    private static Map<String,Theme> themes = new ConcurrentHashMap<>();

    private String name;

    private File assetsRoot;

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
                            StringBuilder sb = new StringBuilder("family of file :").append(file.getName());
                            for (String family: families) {
                                sb.append(family).append(",");
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
    public void applyWithView(QtView view) {
        if (!this.ready) {
            this.prepare();
        }

        File skinAssets = assetsRoot.toPath()
                .resolve("skin")
                .resolve(this.name)
                .toFile();

        try {
            Path defaultStyle = skinAssets
                    .toPath()
                    .resolve("stage.css")
                    .toAbsolutePath();
            byte[] data = Files.readAllBytes(defaultStyle);
            view.setStyleSheet(new String(data, StandardCharsets.UTF_8));
        } catch (Exception e){
            throw new RuntimeException("渲染出现异常：",e);
        }
    }

    public static Theme getTheme(String name,File assets) {
        if (themes.containsKey(name)) {
            return themes.get(name);
        }
        Theme theme = new Theme(name,assets);
        themes.put(name,theme);
        return theme;
    }

}
