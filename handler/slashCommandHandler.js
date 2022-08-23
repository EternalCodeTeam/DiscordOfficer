const fs = require("fs");
const chalk = require("chalk");

const { PermissionsBitField } = require("discord.js");
const { Routes } = require("discord-api-types/v9");
const { REST } = require("@discordjs/rest");

const AsciiTable = require("ascii-table");
const table = new AsciiTable().setHeading("EternalCode -> Commands", "Loaded?").setBorder("|", "=", "-", "-");

const TOKEN = process.env.ETERNAL_DISCORD_TOKEN;
const CLIENT_ID = process.env.ETERNAL_CLIENT_ID;

const rest = new REST({ version: "10" }).setToken(TOKEN);

module.exports = (client, directory) => {
    const slashCommands = [];

    fs.readdirSync(`./${directory}`).forEach(async dir => {
        const files = fs.readdirSync(`./${directory}/${dir}/`).filter(file => file.endsWith(".js"));

        for (const file of files) {
            const slashCommand = require(`../${directory}/${dir}/${file}`);
            slashCommands.push({
                name: slashCommand.name,
                description: slashCommand.description,
                type: slashCommand.type,
                options: slashCommand.options ? slashCommand.options : null,
                default_permission: slashCommand.default_permission ? slashCommand.default_permission : null,
                default_member_permissions: slashCommand.default_member_permissions ? PermissionsBitField.resolve(slashCommand.default_member_permissions).toString() : null
            });

            if (slashCommand.name) {
                client.slashCommands.set(slashCommand.name, slashCommand);
                table.addRow(file.split(".js")[0], "✅");
            } else {
                table.addRow(file.split(".js")[0], "⛔");
            }
        }

    });

    console.log(chalk.blue(table.toString()));

    (async () => {
        try {
            await rest.put(
                Routes.applicationCommands(CLIENT_ID),
                { body: slashCommands }
            );

            console.log(`${chalk.green("[EternalCode.pl]")} ${chalk.yellow("Slash commands was registered")}`);
        } catch (error) {
            console.log(error);
        }
    })();
};