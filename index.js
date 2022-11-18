const { Client, GatewayIntentBits, Partials, Collection } = require("discord.js");
const fs = require("fs");
const { config } = require("dotenv");
config();

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.GuildPresences,
        GatewayIntentBits.GuildMessageReactions,
        GatewayIntentBits.DirectMessages,
        GatewayIntentBits.MessageContent
    ],
    partials: [Partials.Channel, Partials.Message, Partials.User, Partials.GuildMember, Partials.Reaction]
});

global.logger = require("./utils/Logger");
client.commands = new Collection();
client.prefix = config.prefix;

module.exports = client;

fs.readdirSync("./handlers").forEach((handler) => {
    require(`./handlers/${handler}`)(client);
});


client.login(process.env.ETERNAL_DISCORD_TOKEN);