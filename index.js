const { Client, Collection, Partials } = require('discord.js');
const { config } = require('dotenv');
// Init .env configuration
config();

const eternalClient = new Client({
    intents: 32767,
    partials: [Partials.Channel, Partials.Message, Partials.User, Partials.GuildMember, Partials.Reaction]
});

eternalClient.slashCommands = new Collection();

require('./libs/slashCommandHandler.js')(eternalClient, "commands");
require('./libs/eventsHandler.js')(eternalClient);

eternalClient.login(process.env.ETERNAL_DISCORD_TOKEN)