'use strict';
module.exports = (sequelize, DataTypes) => {
  const Recording = sequelize.define('Recording', {
    title: DataTypes.STRING,
    caption: DataTypes.STRING,
    duration: DataTypes.STRING,
    storage_directory: DataTypes.STRING
  });
  Recording.associate = function(models) {
    Recording.belongsTo(models.User,{
      foreignKey: 'user_id',
    })
  };
  return Recording;
};