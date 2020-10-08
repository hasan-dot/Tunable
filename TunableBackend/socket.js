const fs = require('fs');
const Recording = require('./models').Recording;

module.exports = (http) => {
    var io = require('socket.io')(http);
    io.on("connection", function(socket){
        socket.on('mobile', function (data) {
            console.log(data);
        });
        socket.on("disconnect", function(){
            console.log("One user disconnected")
        });
        socket.on("send", (time, user,msg)=>{
            let dir = __dirname+'/storage/mp3/'+user+'/';
            let file = dir+ time+'.mp3';
            if (!fs.existsSync(dir)){
                fs.mkdirSync(dir);
            }
            fs.open(file, 'a', function(err, fd) {  
                if (err) {
                    throw 'could not open file: ' + err;
                }
            
                // write the contents of the buffer, from position 0 to the end, to the file descriptor returned in opening our file
                fs.write(fd, new Uint8Array(msg.buffer_chunk),0, msg.bytes,null,function (err) {
                    if (err) throw 'error writing file: ' + err;
                    fs.close(fd, function() {
                        console.log('wrote the file successfully');
                    });
                });
            }); 
        });

        socket.on('finish',(file_info)=>{
            console.log()
            Recording.create({
                title: file_info.title,
                caption: file_info.caption,
                duration: file_info.duration,
                user_id: parseInt(file_info.user_id, 10),
                storage_directory: file_info.storage_directory 
            });
        });
        

    });
}