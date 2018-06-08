# Shelfs
An api for JDA to build plugins and bots for Discord.

### Available plugins
There plugins are currently available for the bot.
Note that you don't have to install/use a single one of them.

- Music-Plugin
- Basic-Plugin
- Util-Plugin
- Update-Plugin (Kotlin)

### Plugin installation
Just drag an drop any plugin jars you like in the plugins folder.
Always be careful with plugins from unknown sources. They have the potential to
steal your discord bot token. <br>
**Plugins can steal your token!** <br>
**Plugins have the exact same permission as the bot itself!**

### Create a plugin

#### Plugin requirements:
- A class that extends ``ShelfsPlugin``
- A ``plugin.json`` on the very top of your plugin <br>
Everything else is optional <br>
#### plugin.json
The ``plugin.json`` contains the information Shelfs needs to load your plugin. **It is required!** <br>
Every ``plugin.json`` is checked against this [Json Schema](https://github.com/Cerberus-ik/shelfs/blob/master/shelfs-api/src/main/resources/schemas/pluginDescriptionSchema-1.0.json)
```json
{
  "author": "Your Name",
  "version": "1.0.0",
  "name": "Your plugin name",
  "main": "com.example.package.MyPlugin",
  "pluginDescription": "This is a test plugin"
}
```

##### Java
```java
public class MyPlugin extends ShelfsPlugin {
    @Override
    public void onEnable() {
        Shelfs.getJda().addEventListener(new EventListener());
        Shelfs.getCommandManager().registerCommand(this, "test", new TestCommand());
    }
}
```
##### Kotlin
```kotlin
class MyPlugin : ShelfsPlugin() {
    override fun onEnable(){ 
        Shelfs.getCommandManager().registerCommand(this, "test", TestCommand())
		Shelfs.getJda().addEventListener(EventListener())
	}    
}
```

##### Events
Since 0.4.0 Shelfs has it's own events. If you want to listen to them you need a class that extends the ``ShelfsListenerAdapter``. <br>
It not only gives you access to the Shelf events but although every JDA event. It is recommended to use this Adapter instead 
of the default one.


### Static vs Dynamic load
Shelfs supports two options of running your bot. ``Static load`` is the default option running shelfs
will generate a bot.jar that get's executed automatically. If you want to avoid writing to your disk at
every startup you can enable ``Dynamic load``. This option loads all plugins at startup without writing 
extra files to the disk. This option **can** cause issues if your plugin requires extra resource files.

### Dependencies
- kotlin runtime
- everit-org.json-schema
- net.dv8tion:JDA
- google-gson
- logback
- jcl-core ``Shelfs < 0.5``
- junit
- mysql-connector-java
- lava-player (Plugin dependency)
- jda-nas
 