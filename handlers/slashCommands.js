const fs = require("fs");
const { Routes } = require("discord-api-types/v9");
const { REST } = require("@discordjs/rest");

const TOKEN = process.env.ETERNAL_DISCORD_TOKEN;
const CLIENT_ID = process.env.ETERNAL_CLIENT_ID;

const rest = new REST({ version: "10" }).setToken(TOKEN);

module.exports = (client) => {
    const slashCommands = [];
    fs.readdirSync("./slashCommands/").forEach(async dir => {
        const commandFiles = fs.readdirSync(`./slashCommands/${dir}/`).filter(file => file.endsWith(".js"));

        for (const file of commandFiles) {
            const slashCommand = require(`../slashCommands/${dir}/${file}`);

            if ("data" in slashCommand && "execute" in slashCommand) {
                slashCommands.push(slashCommand.data.toJSON());
                client.commands.set(slashCommand.data.name, slashCommand);
            } else {
                logger.warn(`The command at ${file} is missing a required "data" or "execute" property.`);
            }

            delete require.cache[require.resolve(`../slashCommands/${dir}/${file}`)];
        }
    });

    (async () => {
        try {
            await rest.put(Routes.applicationGuildCommands(CLIENT_ID, process.env.ETERNAL_GUILD_ID), { body: slashCommands });
            logger.info("Successfully registered application commands.");
        } catch (error) {
            logger.error(error);
        }
    })();
};