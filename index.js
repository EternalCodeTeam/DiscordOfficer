const { Client, Collection, IntentsBitField } = require('discord.js');
const { config } = require('dotenv');
const discordModals = require('discord-modals');
// Init .env configuration
config();

const eternalClient = new Client({
    intents: [
        IntentsBitField.Flags.Guilds,
        IntentsBitField.Flags.DirectMessages,
        IntentsBitField.Flags.DirectMessageReactions,
        IntentsBitField.Flags.MessageContent,
        IntentsBitField.Flags.GuildMessages,
        IntentsBitField.Flags.GuildMembers,
        IntentsBitField.Flags.GuildMessageReactions,
        IntentsBitField.Flags.GuildBans,
        IntentsBitField.Flags.GuildVoiceStates,
    ],
});
discordModals(eternalClient);
eternalClient.slashCommands = new Collection();

require('./libs/slashCommandHandler.js')(eternalClient, "commands");
require('./libs/eventsHandler.js')(eternalClient);
require('./events/music/initPlayer.js')(eternalClient);

eternalClient.login(process.env.ETERNAL_DISCORD_TOKEN)