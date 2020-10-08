const User = require('../models').User;
var bcrypt = require('bcryptjs');
var jwt = require('jsonwebtoken');
const config = require('../config/secret');
var validator = require('validator');

module.exports = {
    create(req, res){
        if(!valid(req).status){
            return res.send({ auth: false, token: valid(req).message});
        }
        isUnique(req.body.email).then(exist =>{
            if(exist){
                return res.send({error: "User already registered, Please Sign in"});
            }else{
                return User
                    .create({
                        first_name: req.body.first_name,
                        last_name: req.body.last_name,
                        email: req.body.email,
                        password: bcrypt.hashSync(req.body.password, 8)
                    })
                    .then(user=>{
                        let token = jwt.sign({ id: user.id }, config.secret, {
                        expiresIn: 31536000 // expires in 1 year
                        });
                        res.status(200).send({ auth: true, token: token});
                    })
                    .catch(error => res.status(400).send({error: error}));
            }
        });
    },
    login(req, res){
        
        isUnique(req.body.email).then(user =>{
            if(!user){
                return res.send({error: "User not found, Please registered"});
            }
            passwordIsValid = bcrypt.compareSync(req.body.password, user.password);
            if(!passwordIsValid){
                return res.send({ auth: false, token: null });
            }
            var token = jwt.sign({ id: user.id }, config.secret, {
                expiresIn: 86400 
            });
            
            return res.send({ auth: true, token: token });
        });
    }, 
    
    me(req, res){
        let token = req.headers['x-access-token'];

        jwt.verify(token, config.secret, function(err, decoded) {
            if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
            findUser(decoded.id).then(user=>{
                console.log(user)
                return  res.send(user);
            })
           
        });
    }
};
let isUnique = (email) => {
    return User.findOne({ where: {email: email} })
                .then((user) => {
                    if (!user) 
                        return false;
                    return user;
                });
};
let findUser = (userId) =>{
    return User.findOne({ where: {id: userId} })
                .then((user) => {

                    return {
                        id: user.id,
                        first_name: user.first_name,
                        last_name: user.last_name,
                        email: user.email
                    };
                });
}
let valid = (req) => {
    if(!validator.isLength(req.body.first_name, 3)){
        return {
            status: false,
            message: "First Name should be 3 letters at least"
        };
    }
    if(!validator.isLength(req.body.last_name, 3)){
        return {
            status: false,
            message: "Last Name should be 3 letters at least"
        };
    }
    if(!validator.isEmail(req.body.email)){
        return {
            status: false,
            message: "Email not valid"
        };
    }
    if(!validator.isLength(req.body.password, 6)){
        return {
            status: false,
            message: "Password should be 6 charecters at least"
        };
    }
    if(!(req.body.password == req.body.c_password)){
        return {
            status: false,
            message: "Password Does not match"
        };
    }
    return {
        status: true,
        message: "Validated"
    }
}
