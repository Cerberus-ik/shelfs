# Shelfs
An api for JDA to build plugins and bots for Discord.

### Available plugins
There plugins are currently available for the bot.
Note that you don't have to install/use a single one of them.

- Music-Plugin
- Basic-Plugin
- Util-Plugin
- Update-Plugin (Kotlin)

### Create a plugin
##### Java
```java
public class Main extends ShelfsPlugin {
    @Override
    public void onEnable() {
        Shelfs.getJda().addEventListener(new EventListener());
        Shelfs.getCommandManager().registerCommand(this, "test", new TestCommand());
    }
}
```
##### Kotlin
```kotlin
class Main : ShelfsPlugin() {
    override fun onEnable(){ 
        Shelfs.getCommandManager().registerCommand(this, "test", TestCommand())
		Shelfs.getJda().addEventListener(EventListener())
	}    
}
```



### Road map
- Add an authentication check to the database
- Custom plugin var support
- Custom permissions

### Dependencies
- everit-org.json-schema
- net.dv8tion:JDA
- google-gson
- logback
- jcl-core
- junit
- mysql-connector-java
- lava-player (Plugin dependency)
- jda-nas
 