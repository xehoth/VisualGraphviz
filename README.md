# VisualGraphviz
Preview and export graphs & Automatically draw a [weighted] [directed] graph, based on [graphviz](http://www.graphviz.org/).

Yan also see Chinese Version [here](/README_CN.md).

## Install
### Download
You can find release software on [release page](https://github.com/xehoth/VisualGraphviz/releases).

### Install Graphviz & Java
Before using VisualGraphviz, you should install [Graphviz](http://www.graphviz.org/) and [Java](https://www.java.com).

Then set them to your system environment.

## Usage
- Click `paint` or Press `Ctrl-p` to draw the graph.
- Click `export` to export the graph.
- Select `hasWeight` if the graph is weighted.
- Select `isDirected` if the graph is directed.
- Input the node of the graph like `u, v, w`.
- Enable / Disable autoPainting & saveLastData in `config`.

**The exported image is `export.xxx`**, the program will also create `tmp.png` and `tmp.dot`, you can just ignore them.

## Preview
![Preview](/preview.png)

## Todo
- [ ] Optimize the time while painting.
- [ ] Optimize UI.
- [ ] Preview more styles that can export.