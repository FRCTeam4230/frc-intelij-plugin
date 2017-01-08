# FRC IntelliJ IDEA Plugin

An IntelliJ IDEA plugin for FIRST Robotics Competition (FRC) robot development in Java. This plugin works with either the free open-sourced Community Edition or the licensed Ultimate Edition of IntelliJ IDEA.

## Current State
**This plug-in is in beta and is not feature complete yet.** I started developing and using this plug-in for my own use during the 2016 FRC build season. I had hoped to have it more feature rich prior to the 2017 FRC season. But time did not allow for such. At this time, the primary feature is the the RiLog tool window which will show the output of the RioLog. There are also a few file templates for FRC Iterative Robots. I wil try to add features as the 2017 build season progresses. I have many planned, and some partially implemented. Thanks.

## Installation
The plugin can be installed from the IntelliJ IDEA plugin repository. In IntelliJ IDEA:


 1. Go to Settings > Plugins
 2. Click the "Browse Repositories..." button near the bottom
 3. Find or search for `FRC`
 4. Select the `FRC` plugin and click the Install button
 5. After the plugin downloads and installs, you will need to restart IntelliJ IDEA
 
 
  
## Use
You need to add an `FRC` Framework facet to the IDEA module. See [Adding Support for Frameworks and Technologies](https://www.jetbrains.com/help/idea/2016.3/adding-support-for-frameworks-and-technologies.html) for information on how to do such. Alternatively, when opening a project that use the WpiLib, the plugin will automatically detect such and offer to add the Framework Facet.