# VisualGraphviz
基于 [graphviz](http://www.graphviz.org/)，自动生成 [有向 / 无向] [带权] 图，支持预览和导出的工具。

## 安装
### 下载
在 [release](https://github.com/xehoth/VisualGraphviz/releases) 页面下载。

### 安装 Graphviz & Java
在使用 VisualGraphviz 之前，请先安装 [Graphviz](http://www.graphviz.org/) 和 [Java](https://www.java.com).

然后把他们添加进系统环境变量。

## 使用方法
- 点击 `paint` 或按下 `ctrl-p` 生成预览图。
- 点击 `export` 导出图片。
- 如果为带权图，勾选 `hasWeight`。
- 如果为有向图，勾选 `isDirected`。
- 在右侧文本框输入这个图。
- 在 `config` 文件中配置是否启用自动绘制和数据保存。

**被导出的图片为 `export.xxx`**，程序在运行时还可能会创建 `tmp.png` 和 `tmp.dot`，你可以忽略它们。

## 预览图
![预览](/preview.png)

## Todo
- [ ] 优化性能。
- [ ] 美化界面。
- [ ] 增加其他支持导出样式的预览。