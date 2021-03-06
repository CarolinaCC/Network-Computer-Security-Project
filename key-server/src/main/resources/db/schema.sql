drop table if exists users;
drop table if exists files;
drop table if exists authorizations;


create table users(username varchar  primary key not null,
                    passwordhash bytea not null,
                    passwordsalt bytea not null);
				   

create table files(filename varchar  not null,
				   fileowner varchar not null, 
				   encryption_key bytea not null,
				   iv bytea not null,
				   fileId varchar not null,
				   unique(fileId),
				  primary key (filename, fileowner),
				  foreign key(fileowner) references users(username) on delete cascade);

create table authorizations(filename varchar not null,
						   fileowner varchar not null,
						   username varchar not null,
						   permission integer not null,
						   primary key(filename, fileowner, username),
						   foreign key(filename, fileowner) references files(filename, fileowner) on delete cascade,
						   foreign key (username) references users(username) on delete cascade);
