-- Unseen online messages
WITH ranked_data AS (
	SELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, 
		cvmes.Content, cv.IsGroup,
        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn
	FROM Conversation cv
	JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE '%Baobeo%' AND cvmem2.MemberId != 'Highman'
	JOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId
    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE '%kh%'
	WHERE cvmem.MemberId='Highman'
	GROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId
)
SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup
FROM ranked_data rd
WHERE rn = 1 AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND rd.Datetime >= all(
	SELECT ul.Datetime
    FROM UserLog ul
    WHERE ul.UserId = 'Highman' AND ul.LogType = 0
);

-- Unseen offline messages
WITH ranked_data AS (
	SELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, 
		cvmes.Content, cv.IsGroup,
        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn
	FROM Conversation cv
	JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE '%Baobeo%' AND cvmem2.MemberId != 'Highman'
	JOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId
    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE '%kh%'
	WHERE cvmem.MemberId='Highman'
	GROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId
)
SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup
FROM ranked_data rd
WHERE rn = 1 AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND rd.Datetime < all(
	SELECT ul.Datetime
    FROM UserLog ul
    WHERE ul.UserId = 'Highman' AND ul.LogType = 0
);

-- Seen messages
WITH ranked_data AS (
	SELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, 
		cvmes.Content, cv.IsGroup,
        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn
	FROM Conversation cv
	JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE '%Baobeo%' AND cvmem2.MemberId != 'Highman'
	JOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId
    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE '%kh%'
	WHERE cvmem.MemberId='Highman'
	GROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes2.Content
)
SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup
FROM ranked_data rd
WHERE rn = 1 AND EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
);

-- Other friends with no message
SELECT f.UserId, f.FriendId
FROM Friend f
WHERE (f.UserId = 'Highman' AND f.FriendId LIKE '%kizark%' OR f.UserId LIKE '%kizark%' AND f.FriendId = 'Highman') AND NOT EXISTS (
	SELECT *
    FROM Conversation cv
    JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
    JOIN ConversationMember cvmem2 ON cvmem.ConversationId = cvmem2.ConversationId
    WHERE cvmem.MemberId = f.UserId AND cvmem2.MemberId = f.FriendId AND cvmem.MemberId != cvmem2.MemberId AND cv.IsGroup = false
);

-- Get messages not seen by self
SELECT cvmes.MessageId
FROM ConversationMessage cvmes
WHERE cvmes.ConversationId='CV000001' AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.ConversationId = cvmes.ConversationId AND cvmes.MessageId = ms.MessageId AND ms.SeenId='Baobeo'
);