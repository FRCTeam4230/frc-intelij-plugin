# FRC IntelliJ IDEA Plugin

An IntelliJ IDEA plugin for FIRST Robotics Competition (FRC) robot development in Java. This plugin works with either the free open-sourced Community Edition or the licensed Ultimate Edition of IntelliJ IDEA.

:warning: As of v0.8 (Nov 2019), IntelliJ IDEA v2019.2 or later is required.

## Current State
The plugin is compatible with the 2019 changes make in the WPILib and the move to using Gradle. The plugin now contains a robust New Project Wizard to generate GradleRIO based projects. I will start looking at the 2020 beta releases to ensure compatibility as best as possible for the new build season.


### Known Issues

#### Documentation needs updating
The documentation at this project's wiki is outdated. I will do my best to get it updated. But for the most part, the plugin's use is fairly intuitive. 

#### RIOLog Net Console team number use
As you may recall, prior to 2019 the official WPI Eclipse plugin configured the FRC team on a system (i.e. local PC) wide basis. This plugin adhered to that. The team number is used by the RIOLog Net Console to determine the IP or URL to use to connect to the roboRIO. In 2019 a change was made to move the team number configuration into the project (specially to the `.wpi/wpilib_preferences.json` file.) I still need to update the RIOLog Net Console functionality to read the team number from that file, rather to an (IntelliJ IDEA) Application Wide setting, when determining the IP or URL to use. I have opened [issue #37](https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues/37) to track that work. This only affects teams that create projects using alternate team numbers. For those teams, the work around is to change the team number setting in. But this will affect all open FRC projects. This does present a limitation of not being able to have two or more projects open with different team numbers and being able to connect to the RIOLog in more than one project. That is however, I believe, a corner case. And it will be resolved soon. 


