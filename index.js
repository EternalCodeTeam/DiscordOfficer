const { Client, Collection, Partials } = require("discord.js");
const { config } = require("dotenv");
const discordModals = require("discord-modals");
// Init .env configuration
config();

const eternalClient = new Client({
    intents: 32767,
    partials: [Partials.Channel, Partials.Message, Partials.User, Partials.GuildMember, Partials.Reaction]
});

discordModals(eternalClient);
eternalClient.slashCommands = new Collection();

require("./handler/slashCommandHandler.js")(eternalClient, "commands");
require("./handler/eventsHandler.js")(eternalClient);
require("./events/music/initPlayer.js")(eternalClient);

eternalClient.login(process.env.ETERNAL_DISCORD_TOKEN);