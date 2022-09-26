## Build Setup

The build is setup using Maven and Eclipse Tycho. The dependecies are handled by Eclipse rather than Maven, 
to enable compatability between the Eclipse development environment and Maven for automated builds.

To run the build, you can either use the script `scripts/build/ci-build.sh` or run `mvn clean verify` in the
`eclipse` directory.

The build will produce a jar that can be installed into Eclipse to enable the plugin.

The Eclipse plugin is configured to be compatible with Java 8 and beyond, and the Maven build will produce a
plugin that is compatible with the Eclipse version Photon and beyond. Note that building with Eclipse rather
than Maven may change these defaults.

## Installation Instructions

The plugin is installed into Eclipse using the `dropins` folder. To install the plugin, take the jar from the build
and place it in `$ECLIPSE_DIR/dropins/plugins`, where `ECLIPSE_DIR` refers to the directory where Eclipse is installed.

Start (or restart) Eclipse to use the plugin.

Note that to use the plugin you must also have xcal-commands in you xcalagent installation.

## Basic Usuage

To configure the plugin for use, first go to Window -> Preferences and find Xcalscan. In this preference pane, configure
the installation directory of xcalagent.

Now the tool is configured, you can configure a project for analysis. Add a project to Eclispe if you have not already, then
right click on the project to find it's preferences.

In the project preferences, find Xcalscal and configure the build path, build command and choose the project on the scan sever 
you wish to use. You can use the "Add New Project" option to add the project to the scan server if it is not already on the
server.

You can now use the Xcalscan menu to run the scanner. The results will apepar in the Problems view in Eclipse. The rulset is
included in the display message so that you can use the in-build Eclipse filtering. Right clicking on an Xcalscan finding in
the Problems window will give you the options to see the rule description and trace.

## Development Environment Setup

You will need to set up Eclipse to develop the plugin. If you wish to test a C project, it is recommended that you start by 
installing the C/C++ version of Eclipse (not the Java one). This will allow you to use both C and Java test projects.

Note that Eclipse from version 2020-09 only supports Java 11.

With Eclipse installed, navigate to Help -> Install New Software. Here, you want to find the Eclipse PDE Plug-in Developer
package and the Eclipse Project SDK and install them.

Import this repository into Eclipse (if not already done).

Finally, configure a Run Configuration to run the project as an Eclipse Application.

You can now run the plugin in a sandboxed Eclipse, and utilse the debugger.

## Getting Started Developing

There are a few points to start at to get familiar with how the plugin works:

 * `Activator.java` is the main entry point for the plugin. It is instantated first by Eclipse and acts as a mediator between the views and services
 * `plugin.xml` is the mainfest Eclipse uses to find views, actions and menu items
 * `XcalWrapper.java` is the wrapper around Xcalscan actions

User facing strings have been placed in `eclipse/src/com/xcal/eclipse/messages.properties`. This file can be used for translation.

## License

[Apache License v2.0](./LICENSE).