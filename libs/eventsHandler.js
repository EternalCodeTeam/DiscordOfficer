const fs = require('fs');
const chalk = require('chalk')
const AsciiTable = require('ascii-table')
const table = new AsciiTable()
table.setHeading('EternalCode.pl -> Events', 'Loaded?').setBorder('|', '=', "-", "-")

module.exports = (client) => {
    fs.readdirSync('./events/').filter((file) => file.endsWith('.js')).forEach((event) => {
        require(`../events/${event}`)(client);
        table.addRow(event.split('.js')[0], '✅')
    })
    console.log(chalk.blue(table.toString()))
    console.log(`${chalk.green("[EternalCode.pl]")} ${chalk.yellow("Events was registered")}`)
};