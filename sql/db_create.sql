CREATE DATABASE HooYah;

create table hooyah.User (
    Username varchar(255) primary key,
    FirstName nvarchar(255),
    LastName nvarchar(255),
    Password varchar(255),
    Address nvarchar(255),
    DoB timestamp,
    Gender bool,
    Email varchar(255),
    Status tinyint(1) default 0,
    StatusDetail nvarchar(255),
    CreationDate timestamp
);

create table hooyah.UserLog (
	UserId varchar(255),
    Datetime timestamp,
    LogType tinyint(1),
    LogDetail nvarchar(255),
    primary key (UserId, Datetime),
    foreign key (UserId) references User(Username)
);

create table hooyah.Conversation (
	ConversationId varchar(255) primary key,
	ConversationName nvarchar(255),
    IsGroup bool,
    IsE2EE bool default false
);

create table hooyah.ConversationMember (
	ConversationId varchar(255),
	MemberId varchar(255),
    IsAdmin bool,
    primary key (ConversationId, MemberId),
    foreign key (ConversationId) references Conversation(ConversationId),
    foreign key (MemberId) references User(Username)
);

create table hooyah.ConversationMessage (
	MessageId int,
	ConversationId varchar(255),
	SenderId varchar(255),
    Datetime timestamp,
    Content nvarchar(255),
    Encryption varchar(255) default null,
    primary key (MessageId, ConversationId),
    foreign key (ConversationId, SenderId) references ConversationMember(ConversationId, MemberId) 
);

create table hooyah.E2EE (
	ConversationId varchar(255),
    MemberId varchar(255),
    PublicKey varchar(255) default null,
    primary key (ConversationId, MemberId),
    foreign key (ConversationId, MemberId) references ConversationMember(ConversationId, MemberId)
);

create table hooyah.ConversationLog (
	ConversationId varchar(255),
    Datetime timestamp,
    LogType tinyint(1),
    MemberId varchar(255),
    LogDetail nvarchar(255),
    primary key (ConversationId, Datetime),
    foreign key (ConversationId) references ConversationMember(ConversationId) 
);

create table hooyah.MessageSeen (
	MessageId int,
    ConversationId varchar(255),
    SeenId varchar(255),
    primary key (MessageId, ConversationId, SeenId),
    foreign key (MessageId, ConversationId) references ConversationMessage(MessageId, ConversationId) 
);

create table hooyah.Friend (
	UserId varchar(255),
    FriendId varchar(255),
    Status tinyint(1) default 0,
    primary key (UserId, FriendId)
);

create table hooyah.FriendRequest (
	SenderId varchar(255),
    TargetId varchar(255),
    Datetime timestamp,
    Status tinyint(1) default 0,
    primary key (SenderId, TargetId, Datetime),
    foreign key (SenderId) references User(Username),
    foreign key (TargetId) references User(Username)
);

create table hooyah.SpamReport (
	ReporterId varchar(255),
    ReportedId varchar(255),
    Datetime timestamp,
    primary key (ReporterId, ReportedId, Datetime),
    foreign key (ReporterId) references User(Username),
    foreign key (ReportedId) references User(Username)
);

create table hooyah.Emptiness (
	placeholder int
);

DELIMITER //
create trigger hooyah.TRG_Request_Response
after update on FriendRequest
for each row
begin
	if old.Status=0 and new.Status=1 then
		insert into Friend (UserId, FriendId) values (old.SenderId, old.TargetId);
	end if;
end;
//
DELIMITER ;
-- drop trigger TRG_Request_Response;

insert into hooyah.User
values 
	('Highman', 'Nguyễn', 'Anh Khoa', '123456', '1234 gadgdf', '2003-01-01', true, 'dagdg@email', 0, '', '2023-02-12'),
	('Kizark', 'Nguyễn', 'Lâm Hải', 'abcdef', '35 fdsgfd', '2003-02-02', true, 'fasd@email', 0, '', '2023-02-12'),
	('Baobeo', 'Nguyễn Phú', 'Minh Bảo', 'abc123', '5435 ashhh', '2003-03-03', true, 'hhh@email', 0, '', '2023-02-12'),
	('adhd', 'agdsg', 'gadsghf', '123', '222 ttt', '1970-04-04', false, 'ytrhy@email', 0, '', '2023-02-12');
    
insert into hooyah.UserLog
values
	('Highman', '2023-11-12', 0, '');

insert into hooyah.Conversation (ConversationId, ConversationName, IsGroup)
values
	('CV000001', 'Cuộc trò chuyện', false),
	('CV000002', 'Cuộc trò chuyện', true);

insert into hooyah.ConversationMember
values
	('CV000001', 'Highman', true),
    ('CV000002', 'Baobeo', false);
insert into hooyah.ConversationMember values ('CV000001', 'Baobeo', true);
insert into hooyah.ConversationMember values ('CV000002', 'Kizark', false);
insert into hooyah.ConversationMember values ('CV000002', 'Highman', true);
insert into hooyah.ConversationMember values ('CV000002', 'adhd', true);

insert into hooyah.Friend (UserId, FriendId)
values ('Highman', 'Baobeo');
insert into hooyah.Friend (UserId, FriendId) values ('Highman', 'adhd');

insert into hooyah.FriendRequest (SenderId, TargetId, Datetime) values ('Kizark', 'Highman', current_timestamp());
update hooyah.FriendRequest
	set Status=1
    where SenderId='Kizark' and TargetId='Highman'
    order by Datetime desc
    limit 1;
    
insert into hooyah.ConversationMessage (MessageId, ConversationId, SenderId, Datetime, Content)
values
	(1, 'CV000001', 'Highman', '2023-11-12 06:30:01', 'Hello'),
	(2, 'CV000001', 'Baobeo', '2023-11-12 06:30:06', 'Ayyy'),
	(3, 'CV000001', 'Highman', '2023-11-12 06:31:00', 'Bắn không'),
	(4, 'CV000001', 'Baobeo', '2023-11-12 06:31:15', 'kh'),
	(5, 'CV000001', 'Highman', '2023-11-12 06:31:19', 'Bruh'),
	(6, 'CV000001', 'Highman', '2023-11-12 06:31:21', 'oke'),
	(1, 'CV000002', 'Kizark', '2023-11-12 06:28:21', 'làm trận đi'),
	(2, 'CV000002', 'adhd', '2023-11-12 06:28:30', 'zo'),
	(3, 'CV000002', 'Highman', '2023-11-12 06:28:40', 'oke'),
	(4, 'CV000002', 'Kizark', '2023-11-12 06:29:30', 'Bao dau'),
	(5, 'CV000002', 'Highman', '2023-11-12 06:32:00', 'nó kh bắn'),
	(6, 'CV000002', 'Kizark', '2023-11-12 06:32:21', 'ax');
    
insert into hooyah.MessageSeen (MessageId, ConversationId, SeenId)
values
	(1, 'CV000001', 'Highman'),
	(1, 'CV000001', 'Baobeo'),
	(2, 'CV000001', 'Baobeo'),
	(2, 'CV000001', 'Highman'),
	(3, 'CV000001', 'Highman'),
	(3, 'CV000001', 'Baobeo'),
	(4, 'CV000001', 'Baobeo'),
	(4, 'CV000001', 'Highman'),
	(5, 'CV000001', 'Highman'),
	(5, 'CV000001', 'Baobeo'),
	(6, 'CV000001', 'Baobeo'),
	(6, 'CV000001', 'Highman'),
	(1, 'CV000002', 'Highman'),
	(1, 'CV000002', 'Kizark'),
	(1, 'CV000002', 'adhd'),
	(1, 'CV000002', 'Baobeo'),
	(2, 'CV000002', 'Highman'),
	(2, 'CV000002', 'Kizark'),
	(2, 'CV000002', 'adhd'),
	(2, 'CV000002', 'Baobeo'),
	(3, 'CV000002', 'Highman'),
	(3, 'CV000002', 'Kizark'),
	(3, 'CV000002', 'adhd'),
	(3, 'CV000002', 'Baobeo'),
	(4, 'CV000002', 'Highman'),
	(4, 'CV000002', 'Kizark'),
	(4, 'CV000002', 'adhd'),
	(4, 'CV000002', 'Baobeo'),
	(5, 'CV000002', 'Highman'),
	(5, 'CV000002', 'Kizark'),
	(5, 'CV000002', 'adhd'),
	(5, 'CV000002', 'Baobeo'),
	(6, 'CV000002', 'Kizark'),
	(6, 'CV000002', 'adhd'),
	(6, 'CV000002', 'Baobeo');
