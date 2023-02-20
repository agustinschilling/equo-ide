# <image align="left" src=".github/equo_logo.svg"> Equo IDE: IDE as a build artifact

[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/dev.equo.ide?color=blue&label=gradle%20plugin)](plugin-gradle)
[![Maven Plugin](https://img.shields.io/maven-central/v/dev.equo.ide/equo-ide-maven-plugin?color=blue&label=maven%20plugin)](plugin-maven)
[![Solstice OSGi](https://img.shields.io/maven-central/v/dev.equo.ide/solstice?color=blue&label=solstice%20OSGi)](solstice)

**PUBLIC BETA! Try it out, but it's not production ready yet. [Join our mailing list](https://equo.dev/ide) to be notified when it's ready.** The gradle plugin works a lot better than the maven plugin for now.

- a build plugin for [Gradle](plugin-gradle) and [Maven](plugin-maven)
- downloads, configures, and launches an instance of the Eclipse IDE
- ensures that all of your devs have a zero-effort and perfectly repeatable IDE setup process

Use it like this in Gradle with `gradlew equoIde` ([more info](plugin-gradle))

```gradle
plugins {
  id 'dev.equo.ide' version '{{ latest version at top of page }}'
}
equoIde { // launch with gradlew equoIde
  jdt()
}
```

or like this in Maven with `mvn equo-ide:launch` ([more info](plugin-maven))

```xml
<plugin><!-- add this to pom.xml/<project><build><plugins> -->
  <groupId>dev.equo.ide</groupId>
  <artifactId>equo-ide-maven-plugin</artifactId>
  <version>{{ latest version at top of page }}</version>
  <configuration>
     <jdt/>
  </configuration>
</plugin>
  ...
```

You can see all the plugins we support in [`CATALOG.md`](CATALOG.md), and we also support any arbitrary maven-based or p2-based plugins. See [P2_MULTITOOL.md](P2_MULTITOOL.md) for info on browsing and working with p2 repositories.

## How it works

Much of the complexity of downloading, running, and modifying the Eclipse IDE is caused OSGi and p2. Equo IDE cuts out this complexity by replacing p2 with plain-old maven, and replacing OSGi with a simple shim called [Solstice](https://github.com/equodev/equo-ide/tree/main/solstice). This makes it much easier to build, debug, and run Eclipse-based applications.
