const { ApplicationCommandType } = require("discord.js");
module.exports = {
    name: "music",
    description: "Music functions",
    options: [
        {
            "name": "volume",
            "description": "Set volume percent",
            "type": 1,
            "options": [
                {
                    "name": "level",
                    "description": "The user to get",
                    "type": 10,
                    "required": true,
                    "min_value": 1,
                    "max_value": 100
                }
            ]
        },
        {
            "name": "play",
            "description": "Play ur favorite music",
            "type": 1
        },
        {
            "name": "stop",
            "description": "Stop current queue",
            "type": 1
        },
        {
            "name": "back",
            "description": "Play last one song",
            "type": 1
        },
        {
            "name": "clear",
            "description": "Clear ur queue",
            "type": 1
        },
        {
            "name": "loop",
            "description": "Loop ur queue",
            "type": 1
        },
        {
            "name": "nowplaying",
            "description": "Now playing sound",
            "type": 1
        },
        {
            "name": "queue",
            "description": "List of songs added into queue",
            "type": 1
        },
        {
            "name": "resume",
            "description": "Resume song from queue",
            "type": 1
        },
        {
            "name": "search",
            "description": "Search a song",
            "type": 1
        },
        {
            "name": "skip",
            "description": "Go to the next song",
            "type": 1
        },
        {
            "name": "shuffle",
            "description": "Mix queue",
            "type": 1
        }
    ],
    type: ApplicationCommandType.ChatInput,
    run: async (client, interaction) => {
        switch (interaction.options._subcommand) {
            case 'volume':
                await require('./actions/volume.js')(interaction, client);
                return;
            case 'play':
                await require('./actions/play.js')(interaction, client);
                return;
            case 'stop':
                await require('./actions/stop.js')(interaction, client);
                return;
            case 'back':
                await require('./actions/back.js')(interaction, client);
                return;
            case 'clear':
                await require('./actions/clear.js')(interaction, client);
                return;
            case 'loop':
                await require('./actions/loop.js')(interaction, client);
                return;
            case 'nowplaying':
                await require('./actions/nowplaying.js')(interaction, client);
                return;
            case 'queue':
                await require('./actions/queue.js')(interaction, client);
                return;
            case 'resume':
                await require('./actions/resume.js')(interaction, client);
                return;
            case 'search':
                await require('./actions/search.js')(interaction, client);
                return;
            case 'skip':
                await require('./actions/skip.js')(interaction, client);
                return;
            case 'shuffle':
                await require('./actions/shuffle.js')(interaction, client);
                return;
            default:
                interaction.reply({ content: `Selected action was not found! Sorry it's not my problem `, ephemeral: true });
                return;
        }
    }
};