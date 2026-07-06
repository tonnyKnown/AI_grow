import request from '../utils/request'

const normalizeMessage = (msg) => ({
    role: msg.role,
    content: msg.content,
    created_at: msg.created_at || msg.timestamp
})

export const chatApi = {
    sendMessage: (message, sessionId = 'chat_') => {
        return request({
            url: '/javachain/chat/simple/with-history',
            method: 'post',
            timeout: 30000,
            data: {
                sessionId,
                message
            }
        }).then(response => ({
            data: {
                reply: response.data,
                sessionId
            }
        }))
    },

    sendKnowledgeMessage: (data) => {
        const sessionId = data.sessionId || data.session_id
        return request({
            url: '/javachain/chat/simple/with-history',
            method: 'post',
            timeout: 30000,
            data: {
                sessionId,
                message: data.message
            }
        }).then(response => ({
            data: {
                reply: response.data,
                sessionId
            }
        }))
    },

    getChatHistory: (sessionId, limit) => {
        return request({
            url: `/javachain/chat/session/${sessionId}/history`,
            method: 'get'
        }).then(response => {
            const messages = Array.isArray(response.data) ? response.data : []
            const limitedMessages = limit ? messages.slice(-limit) : messages
            return {
                data: {
                    sessionId,
                    messages: limitedMessages.map(normalizeMessage)
                }
            }
        })
    },

    getUserSessions: () => {
        return Promise.resolve({
            data: []
        })
    },

    clearSession: (sessionId) => {
        return request({
            url: `/javachain/chat/session/${sessionId}/clear`,
            method: 'post'
        })
    },

    deleteSession: (sessionId) => {
        return request({
            url: `/javachain/chat/session/${sessionId}`,
            method: 'delete'
        })
    },

    getHistory: (sessionId) => {
        return request({
            url: `/javachain/chat/session/${sessionId}/history`,
            method: 'get'
        })
    },

    createSession: () => {
        return request({
            url: '/javachain/chat/session/create',
            method: 'post'
        })
    }
}
