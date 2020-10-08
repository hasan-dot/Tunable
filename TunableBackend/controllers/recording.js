const Recording = require('../models').Recording;
var jwt = require('jsonwebtoken');
const config = require('../config/secret');
var validator = require('validator');
const fs = require('fs');

module.exports = {
    createRecording(req, res){
        let token = req.headers['x-access-token'];
        jwt.verify(token, config.secret, function(err, decoded) {
            if (err) {
                return res
                        .send({ 
                            auth: false, message: 'Failed to authenticate token.' 
                        });
            }
            if(!valid(req.body).status){
                return res.send({error: valid(req.body).message});
            }
            insertRec({
                title: req.body.title,
                caption: req.body.caption,
                duration: req.body.duration,
                user_id: decoded.id,
                storage_directory: req.body.storage
            }).then(recording=>{
                res.status(200).send({status: "Recorded successfully"});
            })
            .catch(error => res.status(400).send({error: error}));
            
        });
    },
    getRecording(req, res){
        let token = req.headers['x-access-token'];
        jwt.verify(token, config.secret, function(err, decoded) {
            if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
            findRec(decoded.id).then(rec=>{
                console.log(rec)
                return  res.send(rec);
            })
           
        });
    },
    playRecording(req, res) {
        var key = req.params.key;
        var id = req.params.id
        var music = 'storage/mp3/' + id+'/'+key + '.mp3';
    
        var stat = fs.statSync(music);
        range = req.headers.range;
        var readStream;
    
        if (range !== undefined) {
            var parts = range.replace(/bytes=/, "").split("-");
    
            var partial_start = parts[0];
            var partial_end = parts[1];
    
            var start = parseInt(partial_start, 10);
            var end = partial_end ? parseInt(partial_end, 10) : stat.size - 1;
            var content_length = (end - start) + 1;
    
            res.status(206).header({
                'Content-Type': 'audio/mpeg',
                'Content-Length': content_length,
                'Content-Range': "bytes " + start + "-" + end + "/" + stat.size
            });
    
            readStream = fs.createReadStream(music, {start: start, end: end});
        } else {
            res.header({
                'Content-Type': 'audio/mpeg',
                'Content-Length': stat.size
            });
            readStream = fs.createReadStream(music);
        }
        readStream.pipe(res);
    }
};

let findRec = (userId) =>{
    return Recording.findAll({ 
        where: {
            user_id: userId 
        },
        order: [
            ['id', 'DESC']
        ], 
        attributes: [
            'id','title', 'duration', 'caption', 'storage_directory', 'user_id'
        ] 
    })
                .then((rec) => {
                    return {
                       Array: rec
                    };
                });

}

let insertRec = (recording) => {
    return Recording.create({
        title: recording.title,
        caption: recording.caption,
        duration: recording.duration,
        user_id: recording.user_id,
        storage_directory: recording.storage_directory
    })
    
}

let valid = (request) => {
    if(validator.isEmpty(request.title) || !validator.isLength(request.title, 3, 20)){
        console.log("hey")
        return {
            status: false,
            message: "Title should be between 3 and 20 letters"
        };
    }
    if(!validator.isLength(request.caption, 3, 25)){
        return {
            status: false,
            message: "Caption should be between 3 and 25 letters"
        };
    }
    if(validator.isEmpty(request.duration) || !validator.isLength(request.duration  , 5)){
        return {
            status: false,
            message: "Failed to Record, Please try again"
        };
    }
    return {
        status: true,
        message: "Validated"
    }
}
