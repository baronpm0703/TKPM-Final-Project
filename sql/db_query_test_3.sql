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
WHERE rn = 1 AND IsGroup=false AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND rd.Datetime >= all(
	SELECT ul.Datetime
    FROM UserLog ul
    WHERE ul.UserId = 'Highman' AND ul.LogType = 0
) AND EXISTS (
	SELECT *
    FROM Friend f
    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)
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
WHERE rn = 1 AND IsGroup=false AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND rd.Datetime < all(
	SELECT ul.Datetime
    FROM UserLog ul
    WHERE ul.UserId = 'Highman' AND ul.LogType = 0
) AND EXISTS (
	SELECT *
    FROM Friend f
    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)
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
WHERE rn = 1 AND IsGroup=false AND EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND EXISTS (
	SELECT *
    FROM Friend f
    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)
);

-- Other friends with no message
SELECT cv.ConversationId, cvmem.MemberId
FROM Conversation cv
JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId 
WHERE cv.IsGroup=false AND cvmem.MemberId != 'Highman' AND cvmem.MemberId LIKE '%adhd%' AND EXISTS (
	SELECT *
    FROM Friend f
    WHERE (f.UserId = 'Highman' AND f.FriendId = cvmem.MemberId AND f.Status = 1) OR (f.UserId = cvmem.MemberId AND f.FriendId = 'Highman' AND f.Status = 2))
GROUP BY cv.ConversationId, cvmem.MemberId
HAVING COUNT(cvmes.MessageId) = 0;