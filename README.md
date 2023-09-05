# Application Qt

基于我的自研依赖环境的Qt应用程序开发框架，可以目前支持Qt的核心功能，
UI需要使用QtDesigner设计开发。

由于作者买不起MacOS，因此虽然它`应该`能运行在Mac上，但是我不能肯定
会不会出现意想不到的问题。

## 系统需求：

Windows 10或者更高版本，
MacOS 12或更高版本，
Linux还没做，需要的时候再说吧。

就像其他的项目一样，这同样是作者自用的项目，因此不会有完备的测试，主要的平台是
Windows，其他系统在必要的时候或者作者经济宽松的时候也会进行支持，但我大部分时候都在用Windows。

## 项目说明

使用QtDesigner设计的界面文件，可以之间在项目中使用， 不需要经过uic编译为源代码。

目前只有QtCore、QtUITool、QtWidget、QtGui四个基础模块的支持。

引入此项目后需要从Release下载Qt的Native lib，它是一个7z文件，
使用本项目需要把此文件放在Application的Asset文件夹内。

