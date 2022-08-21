const { Client } = require('discord.js');
const { config } = require('dotenv');
const { Handler } = require('discord-slash-command-handler');
// Init .env configuration
config();

const eternalClient = new Client({ intents: ["GUILDS", "GUILD_MESSAGES"] });

eternalClient.on('ready', () => {
   const slashHandler = new Handler(eternalClient, {
       commandFolder: "/commands",
       commandType: "file" || "folder",
       allSlash: true,
       autoDefer: true,
       permissionReply: "Nie posiadasz odpowiednich permisji do korzystania z tego polecenia",
       errorReply: "Wystąpił błąd wewnętrzny, skontaktuj się z autorem oprogramowania",
       notOwnerReply: "Tylko twórcy tego oprogramowania mają możliwość z korzystania z tego polecenia."
   })
});