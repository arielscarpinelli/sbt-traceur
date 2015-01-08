sbt-traceur
===========

[Traceur ES6 -> ES5 compiler](https://github.com/google/traceur-compiler) plugin for sbt-web

To use this plugin use the addSbtPlugin command within your project's `plugins.sbt` file:

    addSbtPlugin("com.typesafe.sbt" % "sbt-traceur" % "1.0.0")

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

    lazy val root = (project in file(".")).enablePlugins(SbtWeb)

By default the plugin will try to run traceur against the asset file `javascripts/main.js` and produce a `main.js` output file which includes all the imported modules from main.js along with the traceur runtime. You only need then to include this output file from your app with

```html
<script src="@routes.Assets.versioned("main.js")"></script>
```

Options
-------

Option                                      | Description
--------------------------------------------|------------
sourceFileNames in traceur in Assets        | Files to compile. Should just be the 'root' modules, traceur will pull the rest. So for example if A.js requires B.js requires C.js, only list A.js here. Default `javascripts/main.js`
sourceFileNames in traceur in TestAssets    | Files to compile for tests `javascript-tests/main.js`
outputFileName in traceur in Assets         | Name of the output file. Default main.js
outputFileName in traceur in TestAssets     | Name of the output file for tests. Default main-test.js
experimental                                | Turns on all experimental features. Default false
sourceMaps                                  | Enable source maps generation. Default true
includeRuntime                              | If traceur-runtime.js code should be included in the output file. Default true
extraOptions                                | Extra options to pass to traceur command line. Refer to [traceur docs](https://github.com/google/traceur-compiler/wiki/Options-for-Compiling) for all available options 

