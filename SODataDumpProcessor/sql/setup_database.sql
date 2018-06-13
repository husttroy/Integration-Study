 create database stackoverflow;
 use database stackoverflow;
 create table questions(Id INT NOT NULL PRIMARY KEY, AcceptedAnswerId INT, Tags VARCHAR(256), ViewCount INT);
 create table answers(Id INT NOT NULL PRIMARY KEY, ParentId INT, Body text, Score INT, IsAccepted BOOLEAN, Tags VARCHAR(256), ViewCount INT);
 
 /* show table schema*/
 describe questions;
 describe answers;