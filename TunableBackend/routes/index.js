const userController = require('../controllers').user;
const recController = require('../controllers').recording;
var VerifyToken = require('./VerifyToken');


module.exports = (app) => {

    app.get('/api', (req, res) => {
        res.send({
            message: 'Welcome you made it',
        });
    });

    app.post('/api/register', userController.create);

    app.post('/api/login', userController.login);
    app.get('/api/me', VerifyToken, userController.me);
    app.get('/api/getmusic', VerifyToken, recController.getRecording)
    app.post('/api/post/recording',VerifyToken,recController.createRecording)
   
    app.get('/api/play/:id/:key', recController.playRecording);
    
}