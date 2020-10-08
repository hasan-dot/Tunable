var app = require('express')();
var http = require('http').Server(app);
const bodyParser = require('body-parser');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}));

var models = require('./models');

models.sequelize.sync()
                .then(() => {
                    console.log('Database is greate !');
                })
                .catch((err) => {
                    console.log(err, 'Something went wrong in the Database');
                });

require('./routes')(app);
require('./socket.js')(http)
app.get('*', (req, res) => {
    res.send({
        message: 'Route requested not found, please make sure that the url is correct'
    });
});

http.listen(3000, function(){
    console.log('Listening on Port 3000');
});

module.exports = app;