WITH ranked_data AS (
	SELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmes.Datetime, cvmes.Content,
		ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn
	FROM Conversation cv
	JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
	JOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId
	WHERE cvmem.MemberId='Highman'
)
SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, SenderId, Datetime, Content
FROM ranked_data rd
JOIN MessageSeen ms ON ms.ConversationId = rd.ConversationId AND rd.MessageId = ms.MessageId
WHERE rn = 1;

WITH ranked_data AS (
	SELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmes.Datetime, cvmes.Content,
		ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn
	FROM Conversation cv
	JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId
	JOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId
	WHERE cvmem.MemberId='Highman'
)
SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, SenderId, rd.Datetime, Content
FROM ranked_data rd
WHERE rn = 1 AND NOT EXISTS (
	SELECT *
    FROM MessageSeen ms
    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = 'Highman'
) AND rd.Datetime > all(
	SELECT ul.Datetime
    FROM UserLog ul
    WHERE ul.UserId = 'Highman' AND ul.LogType = 0
);