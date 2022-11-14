const { Client, Collection } = require("discord.js");
const { config } = require("dotenv");
config();

const eternalClient = new Client({ intents: 32767 });

eternalClient.slashCommands = new Collection();

global.logger = require("./utils/Logger");

require("./handler/slashCommandHandler.js")(eternalClient, "commands");
require("./handler/eventsHandler.js")(eternalClient);
require("./events/music/initPlayer.js")(eternalClient);

eternalClient.login(process.env.ETERNAL_DISCORD_TOKEN).then(r => {
    logger.info("Logged in as " + eternalClient.user.tag);
});