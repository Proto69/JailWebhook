# Minecraft Jail Discord Webhook (Bukkit)

Simple minecraft plugin, sending discord embed on jail command

>Feel free to alter the code of this plugin to your needs

## Features
* Customizable messages
* Every message can be disabled/enabled
* Start command customization (instead of `jail` it could be anything)
* Message on jail command with format:
> /jail `nick` `duration` `cell` r:`reason handling spaces`


## Installation
1. Download the latest release on the right side of the page.
2. Drop the `.jar` file in your `plugins` folder.
3. Start the server and a folder `JailWebhook` will be generated with a `config.yml` file.
4. Add the Webhook URL to the top of the file, if you are unsure how to get one reffer to [this page](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks)
5. Configure the plugins using `config.yml` to your needs
6. Reload the plugin using `/jwreload`


## Credits
* [alexpado's Plugin starter project](https://github.com/alexpado/papermc-plugin-starter)
* [k3kdude's Discord Webhook API](https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb)
* [sravan1946 Plugin Template](https://github.com/sravan1946/Minecraft-Server-Discord-Webhook)
