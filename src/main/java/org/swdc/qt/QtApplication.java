package org.swdc.qt;

import io.qt.gui.QIcon;
import io.qt.gui.QImage;
import io.qt.gui.QPixmap;
import io.qt.widgets.QApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.config.AbstractConfig;
import org.swdc.dependency.AnnotationLoader;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.EnvironmentLoader;
import org.swdc.dependency.LoggerProvider;
import org.swdc.dependency.application.SWApplication;
import org.swdc.ours.common.PackageResources;
import org.swdc.ours.common.annotations.AnnotationDescription;
import org.swdc.ours.common.annotations.AnnotationDescriptions;
import org.swdc.ours.common.annotations.Annotations;
import org.swdc.qt.config.ApplicationConfigure;
import org.swdc.qt.utils.IOApplicationUtils;
import org.swdc.qt.utils.QtThreadPoolExecutor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class QtApplication implements SWApplication {


    private DependencyContext context = null;
    private QtResource resource = new QtResource();
    private boolean isNativeInitialized = false;

    private AbstractExecutorService executor;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void prepareApplication(AnnotationLoader loader) {
        try {
            InputStream bannerInputStream = this.getClass().getModule().getResourceAsStream("banner.txt");
            if (bannerInputStream == null) {
                bannerInputStream = QtApplication.class.getModule().getResourceAsStream("banner/banner.txt");
            }
            String banner = IOApplicationUtils.readStreamLines(bannerInputStream);
            System.out.println(banner);
        } catch (Exception e) {
            logger.error("failed to load banner.",e);
            this.stop(true);
            return;
        }

        logger.info("application is initializing...");

    }

    private void prepareNativeEnvironment(File baseDir) {
        String osName = System.getProperty("os.name").trim().toLowerCase();
        String nativePath =baseDir.getAbsolutePath() + File.separator +
                "platform" + File.separator + "Qt";
        if (osName.contains("mac")) {
            String url = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            String base = URLDecoder.decode(url, StandardCharsets.UTF_8);
            if (base.indexOf(".app") > 0) {
                // 位于MacOS的Bundle（.app软件包）内部，特殊处理以获取正确的路径。
                String location = base.substring(0,base.indexOf(".app")) + ".app/Contents/";
                Path target = new File(location).toPath();
                nativePath = target.resolve(nativePath).toFile().getAbsolutePath();
            }
        }

        System.setProperty("io.qt.library-path-override",nativePath);
        System.setProperty("io.qt.deploymentdir",nativePath);
        System.setProperty("io.qt.pluginpath",nativePath);

        File nativeFolder = new File(nativePath);
        File parentFolder = nativeFolder.getAbsoluteFile().getParentFile();
        if (!nativeFolder.exists()) {

            if(!nativeFolder.mkdirs()) {
                throw new RuntimeException("can not create platform folder, start failed.");
            }

            String resourceName = "";

            logger.info("no qt library found, will extract from resource.");
            if (osName.contains("windows")) {
                resourceName =  "windows";
            } else if (osName.contains("linux")) {
                resourceName = "linux";
            } else if (osName.contains("mac")) {
                resourceName =  "macos";
            } else {
                logger.error("failed to prepare Qt environment, your system is not supported.");
            }
            String osArch = System.getProperty("os.arch");
            List<String> arch64 = Arrays.asList("x64","amd64","x86_64");
            if (arch64.contains(osArch.toLowerCase())) {
                osArch = "x64";
            }
            File archived = new File(baseDir.getAbsolutePath() + File.separator + "qt-" + resourceName + "-" + osArch + ".7z");
            if (archived.exists()) {
                logger.info("extracting native libs....");
                PackageResources.extract7ZipFromFile(archived,nativeFolder);
            } else {
                logger.error("no native library found, please download from the release page of this project.");
                stop(true);
            }
        }

    }

    private void initializeResources(String[] args, AnnotationLoader loader) {

        AnnotationDescriptions annotations = Annotations.getAnnotations(this.getClass());
        AnnotationDescription appDesc = Annotations.findAnnotationIn(annotations,SWQtApplication.class);
        if (appDesc == null) {
            logger.error("Application must annotated with SWQtApplication annotation, and configured well, start failed.");
            this.stop(true);
            return;
        }


        Class[] configures = appDesc.getProperty(Class[].class,"configures");
        for (Class confClazz : configures) {
            if (ApplicationConfigure.class.isAssignableFrom(confClazz)) {
                resource.setConfigureClass(confClazz);
            }
            try {
                AbstractConfig config = (AbstractConfig) confClazz.getConstructor()
                        .newInstance();
                loader.withInstance(confClazz,config);
            } catch (Exception e) {
                logger.error("failed to load config about " + confClazz.getName(), e);
            }
        }

        if (resource.getConfigureClass() == null) {
            logger.error("failed to get the configure extends ApplicationConfigure in SWQtApplication annotation,\n adding a class extends it into configures list.");
            this.stop(true);
            return;
        }

        String osName = System.getProperty("os.name").trim().toLowerCase();
        logger.info(" starting at : " + osName);
        File file = null;

        String assetFolderPath = appDesc.getProperty(String.class,"assetsFolder");
        if (osName.contains("windows")) {
            file = new File(assetFolderPath);
        } else if (osName.contains("linux")) {
            file = new File(assetFolderPath);
        } else if (osName.contains("mac")) {
            String url = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            String base = URLDecoder.decode(url, StandardCharsets.UTF_8);
            if (base.indexOf(".app") > 0) {
                // 位于MacOS的Bundle（.app软件包）内部，特殊处理以获取正确的路径。
                String location = base.substring(0,base.indexOf(".app")) + ".app/Contents/";
                Path target = new File(location).toPath();
                target = target.resolve(assetFolderPath);
                file = target.toFile();
            } else {
                file = new File(assetFolderPath);
            }
        }

        this.prepareNativeEnvironment(file);

        logger.info("Qt environment is loading...");
        QApplication.initialize(args);
        isNativeInitialized = true;

        //this.executor =  new QtThreadPoolExecutor();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,4,10, TimeUnit.MINUTES,new LinkedBlockingQueue<>()
        );
        this.executor = executor;
        resource.setExecutor(this.executor);

        String[] icons = appDesc.getProperty(String[].class, "icons");
        QIcon theIcon = new QIcon();
        for (String icon : icons) {
            try {
                InputStream in = getClass().getModule().getResourceAsStream(icon);
                if (in != null) {
                    byte[] data = in.readAllBytes();
                    QImage image = QImage.fromData(data);
                    theIcon.addPixmap(QPixmap.fromImage(image));
                    in.close();
                }
            } catch (Exception e) {
                logger.error("failed to load this icon: " + icon);
            }
        }

        if (file == null || !file.exists()) {
            logger.error("failed to load asset folder at:" + assetFolderPath);
            logger.error("start failed, does the folder exist ?");
            this.stop(true);
            return;
        }

        logger.info("using asset folder: " + assetFolderPath);

        resource.setAssetFolder(file);
        resource.setAppIcon(theIcon);
        resource.setArgs(Arrays.asList(args));

        loader.withInstance(QtResource.class,resource);
        loader.withInstance(ExecutorService.class,executor);
        loader.withProvider(LoggerProvider.class);

        this.onConfig(loader);

        logger.info("resource has loaded.");
    }

    public void onConfig(EnvironmentLoader loader, QtResource resource) {

    }

    @Override
    public void onConfig(EnvironmentLoader loader) {
        this.onConfig(loader,resource);
    }

    @Override
    public void onStarted(DependencyContext context) {

    }

    @Override
    public void onShutdown(DependencyContext context) {

    }

    private void stop(boolean focus) {
        logger.info("application is closing...");
        try {
            this.onShutdown(context);
            if (context != null && AutoCloseable.class.isAssignableFrom(context.getClass())) {
                AutoCloseable closeable = (AutoCloseable) context;
                closeable.close();
            }
            logger.info("application context has closed.");
        } catch (Exception e) {
            logger.error("error on close application",e);
        }
        executor.shutdown();
        if (focus) {
            if (isNativeInitialized) {
                QApplication.shutdown();
                logger.info("Qt environment has closed.");
            }
            System.exit(0);
        }
    }

    public void applicationLaunch(String[] args) {

        AnnotationLoader loader = new AnnotationLoader();
        this.prepareApplication(loader);

        this.initializeResources(args,loader);
        context = loader.load();
        logger.info("application is started.");

        QApplication.setWindowIcon(resource.getAppIcon());
        QApplication.instance().aboutToQuit.connect(() -> this.stop(false));

        this.onStarted(context);
        QApplication.exec();

    }

}
