const fs = require("fs");

const { PermissionsBitField } = require("discord.js");
const { Routes } = require("discord-api-types/v9");
const { REST } = require("@discordjs/rest");

const TOKEN = process.env.ETERNAL_DISCORD_TOKEN;
const CLIENT_ID = process.env.ETERNAL_CLIENT_ID;

const rest = new REST({ version: "10" }).setToken(TOKEN);

module.exports = (client) => {
    const slashCommands = [];

    fs.readdirSync("./slashCommands/").forEach(async dir => {
        const files = fs.readdirSync(`./slashCommands/${dir}/`).filter(file => file.endsWith(".js"));

        for (const file of files) {
            const slashCommand = require(`../slashCommands/${dir}/${file}`);
            slashCommands.push({
                name: slashCommand.name,
                description: slashCommand.description,
                type: slashCommand.type,
                options: slashCommand.options ? slashCommand.options : null,
                default_permission: slashCommand.default_permission ? slashCommand.default_permission : null,
                default_member_permissions: slashCommand.default_member_permissions ? PermissionsBitField.resolve(slashCommand.default_member_permissions).toString() : null
            });
        }

    });

    (async () => {
        try {
            await rest.put(
                process.env.ETERNAL_GUILD_ID ?
                    Routes.applicationGuildCommands(CLIENT_ID, process.env.ETERNAL_GUILD_ID) :
                    Routes.applicationCommands(CLIENT_ID),
                { body: slashCommands }
            );
            logger.info("Successfully registered application commands.");
        } catch (error) {
            logger.error(error);
        }
    })();
};